package com.badbones69.crazyenchantments.paper.controllers;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ArmorTypeController {

    private final Player player;

    private final ItemStack[] armorContents;

    private boolean isArmorControllerEnabled = false;

    private boolean isEffectApplied;

    /**
     * Local constructor for ArmorTypeController.
     * @param player The player that will be used in this class
     */
    public ArmorTypeController(@NotNull Player player) {
        this.player = player;
        this.armorContents = this.player.getInventory().getArmorContents();
    }

    @NotNull
    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    @NotNull
    private final Set<Material> goldArmor =  Set.of(
            Material.GOLDEN_HELMET,
            Material.GOLDEN_CHESTPLATE,
            Material.GOLDEN_LEGGINGS,
            Material.GOLDEN_BOOTS
    );

    @NotNull
    private final Set<Material> ironArmor = Set.of(
            Material.IRON_HELMET,
            Material.IRON_CHESTPLATE,
            Material.IRON_LEGGINGS,
            Material.IRON_BOOTS
    );


    //Gold

    public boolean isArmorGold() {
        boolean check = false;
        for (ItemStack armor : this.armorContents) {
            if (armor == null) continue;


            check = this.goldArmor.contains(armor.getType());
        }
        return this.isArmorControllerEnabled && check;
    }

    public int getAmountOfGoldArmor() {
        int amt = 0;
        for (ItemStack ignored : this.armorContents) {
            if (isArmorGold()) amt++;
        }
        return amt;
    }

    //Iron

    public boolean isArmorIron() {
        boolean check = false;
        for (ItemStack armor : this.armorContents) {
            if (armor == null) continue;

            check = this.ironArmor.contains(armor.getType());
        }
        return this.isArmorControllerEnabled && check;
    }

    public @Nullable Material getArmorType(EquipmentSlot slot) {
        return this.player.getEquipment().getItem(slot).getType();
    }

    public @Nullable String getArmorType(String name) {
        if (name.toLowerCase().startsWith("golden_")) return "gold";
        else if (name.toLowerCase().startsWith("leather_")) return "leather";
        else if (name.toLowerCase().startsWith("diamond_")) return "diamond";
        else if (name.toLowerCase().startsWith("iron_")) return "iron";
        else if (name.toLowerCase().startsWith("chainmail_")) return "chainmail";

        return null;
    }

    public @Nullable List<Material> getArmorTypes() {
        List<Material> types = new ArrayList<>();
        for (ItemStack armor : this.armorContents) {
            if (armor == null) continue;
            types.add(armor.getType());
        }
        return types;
    }

    public boolean isArmorControllerEnabled() {
        return this.isArmorControllerEnabled;
    }

    public void applyEffects() {

    }

    public boolean isEffectApplied() {
        return this.isEffectApplied;
    }
}
