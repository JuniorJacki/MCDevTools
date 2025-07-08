// Copyright JuniorJacki 2025
// Paper 1.21.4-R0.1-SNAPSHOT
// Needed additional Libraries: 
// - org.json -> https://mvnrepository.com/artifact/org.json/json

package de.juniorjacki.mcdevtools.inventory;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicReference;

public class JSONSerializer {

    public static JSONObject serialize(Inventory inventory) {
        JSONObject root = new JSONObject();
        root.put("size", inventory.getSize());
        JSONObject items = new JSONObject();
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null) {
                JSONObject itemData = new JSONObject();
                itemData.put("id", item.getType().name());
                itemData.put("amount", item.getAmount());
                if (item.getItemMeta() instanceof Damageable damageable) {
                    itemData.put("damage",damageable.getMaxDamage()-damageable.getDamage());
                    itemData.put("maxDamage", damageable.getMaxDamage());
                }
                JSONObject enchantments = new JSONObject();
                AtomicReference<Short> enchantSize = new AtomicReference<>((short) 0);
                item.getEnchantments().forEach((key, value) -> {
                    enchantSize.set((short) (enchantSize.get() + 1));
                    JSONObject enchantmentData = new JSONObject();
                    enchantmentData.put("key", key.getKey());
                    enchantmentData.put("level", value);
                    enchantments.put(String.valueOf(enchantSize.get()), enchantmentData);
                });
                enchantments.put("size", enchantSize.get());
                itemData.put("enchantments", enchantments);
                items.put(String.valueOf(i), itemData);
            }
        }
        root.put("items", items);
        return root;
    }

    public static Inventory deserialize(String inventoryJsonData) {
        JSONObject root = new JSONObject(inventoryJsonData);
        if (!root.has("items") || !root.has("size")) return null;
        int size = root.getInt("size");
        Inventory inventory = Bukkit.createInventory(null,size);
        JSONObject items = root.getJSONObject("items");
        for (int i = 0; i < size; i++) {
            if (items.has(String.valueOf(i))) {
                JSONObject itemData = items.getJSONObject(String.valueOf(i));
                ItemStack stack = new ItemStack(Material.valueOf(itemData.getString("id")), itemData.getInt("amount"));
                if (itemData.has("damage")) {
                    ItemMeta meta = stack.getItemMeta();
                    Damageable damageable = (Damageable) meta;
                    damageable.setDamage(itemData.getInt("damage"));
                    stack.setItemMeta(meta);
                }
                if (itemData.has("enchantments")) {
                    JSONObject enchantments = itemData.getJSONObject("enchantments");
                    if (enchantments.has("size")) {
                        for (int a = 0; a < enchantments.getInt("size"); a++) {
                            if (enchantments.has(String.valueOf(a))) {
                                JSONObject enchantmentData = enchantments.getJSONObject(String.valueOf(a));
                                Enchantment enchantment = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get((NamespacedKey) enchantmentData.get("key"));
                                if (enchantment != null) {
                                    stack.addEnchantment(enchantment, enchantmentData.getInt("level"));
                                }
                            }
                        }
                    }
                }
                inventory.setItem(i, stack);
            }

        }
        return inventory;
    }



}
