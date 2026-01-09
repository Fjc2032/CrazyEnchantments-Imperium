package com.badbones69.crazyenchantments.paper.listeners.misc;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import com.badbones69.crazyenchantments.paper.api.utils.ColorUtils;
import com.badbones69.crazyenchantments.paper.api.utils.NumberUtils;
import com.badbones69.crazyenchantments.paper.controllers.settings.EnchantmentBookSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LoreUpdater implements Listener {

    private final CrazyEnchantments plugin = CrazyEnchantments.getInstance();

    private final EnchantmentBookSettings settings = plugin.getStarter().getEnchantmentBookSettings();

    @EventHandler
    public void alternate(PlayerItemHeldEvent event) {
        int slot = event.getNewSlot();
        ItemStack item = event.getPlayer().getInventory().getItem(slot);
        if (item == null) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        Map<CEnchantment, Integer> enchants = settings.getEnchantments(meta);

        List<Component> currentLore = meta.lore();
        if (currentLore == null) currentLore = new ArrayList<>();

        List<Component> nonEnchantLore = new ArrayList<>();
        for (Component line : currentLore) {
            boolean isEnchantment = false;
            for (CEnchantment enchant : enchants.keySet()) {
                String name = enchant.getCustomName();
                String serializedLore = PlainTextComponentSerializer.plainText().serializeOrNull(line);
                if (serializedLore != null && serializedLore.contains(name)) {
                    isEnchantment = true;
                    break;
                }
            }
            if (!isEnchantment) nonEnchantLore.add(line);
        }

        List<Component> enchantLore = new ArrayList<>();
        for (Map.Entry<CEnchantment, Integer> entry : enchants.entrySet()) {
            String line = entry.getKey().getCustomName() + " " + NumberUtils.convertLevelString(entry.getValue());
            enchantLore.add(ColorUtils.legacyTranslateColourCodes(line));
        }

        List<Component> finalLore = new ArrayList<>();
        finalLore.addAll(nonEnchantLore);
        finalLore.addAll(enchantLore);

        // The idea is to wipe away all the enchants before finally setting them back into the lore.
        // In theory, it should stop duplicates
        stripLoreOfEnchants(currentLore, enchants);

        if (!currentLore.equals(finalLore)) {
            meta.lore(finalLore);
            item.setItemMeta(meta);
        }
    }

    /**
     * Theoretically strips an item of its lore.
     * @param lore The lore to target
     * @param enchants The enchantments that should be removed
     */
    private static void stripLoreOfEnchants(List<Component> lore, Map<CEnchantment, Integer> enchants) {
        for (Map.Entry<CEnchantment, Integer> entry : enchants.entrySet()) {
            String path = entry.getKey().getCustomName() + " " + entry.getValue();
            path = ColorUtils.removeColor(path);
            for (Component component : lore) {
                String line = PlainTextComponentSerializer.plainText().serialize(component);
                line = ColorUtils.removeColor(line);

                if (line.equals(path)) lore.remove(component);
            }
        }
    }
}
