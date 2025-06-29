package com.badbones69.crazyenchantments.paper.listeners.misc;

import com.badbones69.crazyenchantments.paper.api.enums.CEnchantments;
import com.badbones69.crazyenchantments.paper.api.events.PreBookApplyEvent;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MiscEquipmentListener implements Listener {


    private final Set<Material> leggings = Set.of(Material.LEATHER_LEGGINGS, Material.GOLDEN_LEGGINGS, Material.IRON_LEGGINGS, Material.CHAINMAIL_LEGGINGS,
            Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS);
    private final Set<Material> chestplates = Set.of(Material.LEATHER_CHESTPLATE, Material.GOLDEN_CHESTPLATE, Material.IRON_CHESTPLATE,
            Material.CHAINMAIL_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE);

    private final List<CEnchantments> targets = Arrays.asList(CEnchantments.values());

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onLeggingsBookApply(PreBookApplyEvent event) {
        for (CEnchantments dataset : targets) {
            if (!leggingsCheck(dataset)) return;
            if (!leggings.contains(event.getEnchantedItem().getType())) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("This enchantment can only be applied to leggings.");
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChestplateBookApply(PreBookApplyEvent event) {
        for (CEnchantments dataset : targets) {
            if (!chestplateCheck(dataset)) return;
            if (!chestplates.contains(event.getEnchantedItem().getType())) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("This enchantment can only be applied to chestplates.");
            }
        }
    }
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onTridentApply(PreBookApplyEvent event) {
        for (CEnchantments dataset : targets) {

        }
    }

    private boolean leggingsCheck(CEnchantments data) {
        String name = data.getMiscTypeName();
        return (Objects.equals(name, "Leggings"));
    }
    private boolean chestplateCheck(CEnchantments data) {
        String name = data.getMiscTypeName();
        return (Objects.equals(name, "Chestplate"));
    }
    private boolean tridentCheck(CEnchantments data) {
        String type = data.getTypeName();
        return (Objects.equals(type, "Trident"));
    }
}
