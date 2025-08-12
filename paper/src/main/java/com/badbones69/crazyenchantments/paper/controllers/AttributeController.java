package com.badbones69.crazyenchantments.paper.controllers;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import com.badbones69.crazyenchantments.paper.controllers.settings.EnchantmentBookSettings;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import jdk.jfr.Experimental;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Experimental
public class AttributeController {

    @NotNull
    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    @NotNull
    private final EnchantmentBookSettings enchantmentBookSettings = new EnchantmentBookSettings();

    @NotNull
    public static Map<UUID, AttributeController> dataset = new ConcurrentHashMap<>();

    @NotNull
    public final Map<UUID, AttributeController> data = dataset;

    @NotNull
    public Collection<AttributeModifier> modifiers = new ConcurrentLinkedDeque<>();

    public Attribute attribute;

    public ScheduledTask[] task;

    public boolean isAttributeHandlingEnabled = true;

    public static @NotNull Map<UUID, AttributeController> getDataset() {
        return dataset;
    }

    /**
     * Starts a task calculation that will remove the attribute modifier when the item is no longer valid.
     * @param player The player this task will target
     * @param attribute The attribute being affected
     * @param modifier The modifier that will affect the attribute
     * @param enchantment The enchantment this attribute combination is tied to
     */
    public void updateAttributes(Player player, @Nullable Attribute attribute, @Nullable AttributeModifier modifier, CEnchantment enchantment) {
        if (player == null || attribute == null) return;

        this.task[0] = player.getScheduler().runAtFixedRate(plugin, check -> {
            for (ItemStack armor : player.getInventory().getArmorContents()) {
                if (armor == null) continue;
                if (modifier == null) continue;
                if (!this.enchantmentBookSettings.hasEnchantment(armor.getItemMeta(), enchantment)) {
                    player.getAttribute(attribute).removeModifier(modifier);
                    check.cancel();
                }

                if (armor.isEmpty()) check.cancel();
            }
        }, null, 10L, 5L);
    }

    /**
     * Runs a check that will remove the attribute from the item once the requirements are no longer met.
     * Specifically targets held items.
     * @param player The player this task will target
     * @param attribute The attribute being affected
     * @param modifier The modifier that will affect the attribute
     * @param enchantment The enchantment this attribute combination is tied to
     * @param tool The ItemStack this is tied to. Can be any item, as long as the player is holding it.
     */
    public void updateAttributes(Player player, @Nullable Attribute attribute, @Nullable AttributeModifier modifier, CEnchantment enchantment, @NotNull ItemStack tool) {
        if (player == null || attribute == null || modifier == null || enchantment == null) {
            this.plugin.getLogger().warning("One or more parts of updateAttributes is null. Exiting...");
            return;
        }
        if (player.getInventory().getItemInMainHand().isSimilar(tool)) return;
        if (!this.enchantmentBookSettings.hasEnchantment(tool.getItemMeta(), enchantment)) {
            ItemMeta meta = tool.getItemMeta();
            for (Attribute selection : meta.getAttributeModifiers().keySet()) {
                meta.removeAttributeModifier(selection);
            }
            tool.setItemMeta(meta);
        }
    }

    /**
     * Runs a check that will remove the attribute from the armor piece once conditions are met.
     * @param player The player this will target
     * @param attribute The attribute being affected
     * @param modifier The modifier that will affect the attribute
     * @param enchantment The enchantment this is tied to
     * @param armor The ItemStack this will be tied to (should be armor)
     * @param slot The EquipmentSlot this armor piece should occupy.
     */
    public void updateAttributes(Player player, @Nullable Attribute attribute, @Nullable AttributeModifier modifier, CEnchantment enchantment, @NotNull ItemStack armor, @NotNull EquipmentSlot slot) {
        if (player == null || attribute == null || modifier == null || enchantment == null) return;

        if (player.getEquipment().getItem(slot).equals(armor)) return;
        if (!this.enchantmentBookSettings.hasEnchantment(armor.getItemMeta(), enchantment)) {
            ItemMeta meta = armor.getItemMeta();
            for (Attribute selection : meta.getAttributeModifiers().keySet()) {
                meta.removeAttributeModifier(selection);
            }
            armor.setItemMeta(meta);
        }
    }

    /**
     * Clears all attributes that were added to the player.
     * @param player The player to clear the attributes from.
     */
    public void clearAllAttributes(Player player) {
        for (Attribute attribute : Registry.ATTRIBUTE) for (AttributeModifier modifier : this.modifiers) {
            player.getAttribute(attribute).removeModifier(modifier);
            this.modifiers.clear();
        }
    }

    public void add(AttributeModifier element) {
        this.modifiers.add(element);
    }
}
