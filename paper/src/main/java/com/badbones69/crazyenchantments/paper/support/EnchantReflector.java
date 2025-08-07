package com.badbones69.crazyenchantments.paper.support;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.Starter;
import com.badbones69.crazyenchantments.paper.api.enums.CEnchantments;
import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import com.badbones69.crazyenchantments.paper.controllers.settings.EnchantmentBookSettings;
import io.papermc.paper.enchantments.EnchantmentRarity;
import io.papermc.paper.registry.set.RegistryKeySet;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityCategory;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

@SuppressWarnings({"removal"})
public class EnchantReflector extends Enchantment {

    public EnchantReflector(@NotNull CEnchantments data) {
        this.enchantment = data.getEnchantment();
        this.data = data;
    }

    @NotNull
    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    @NotNull
    private final Starter starter = this.plugin.getStarter();

    @NotNull
    private final EnchantmentBookSettings settings = this.starter.getEnchantmentBookSettings();

    @NotNull
    private final CEnchantment enchantment;

    @NotNull
    private final CEnchantments data;

    @Override
    public @NotNull String getName() {
        return this.enchantment.getName();
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return new NamespacedKey(this.plugin, this.enchantment.getCustomName());
    }

    @Override
    public int getMaxLevel() {
        return this.enchantment.getMaxLevel();
    }

    @Override
    public int getStartLevel() {
        return 0;
    }

    @Deprecated
    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return this.settings.getTarget(this.data);
    }

    public @NotNull EnchantmentTarget getItemTarget(@NotNull CEnchantments data) {
        return this.settings.getTarget(data);
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public boolean conflictsWith(@NotNull Enchantment other) {
        return false;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack item) {
        return false;
    }

    @Override
    public @NotNull Component displayName(int level) {
        return null;
    }

    @Override
    public boolean isTradeable() {
        return false;
    }

    @Override
    public boolean isDiscoverable() {
        return false;
    }

    @Override
    public int getMinModifiedCost(int level) {
        return 0;
    }

    @Override
    public int getMaxModifiedCost(int level) {
        return 0;
    }

    @Override
    public int getAnvilCost() {
        return 0;
    }

    @Override
    public @NotNull EnchantmentRarity getRarity() {
        return null;
    }

    @Override
    public float getDamageIncrease(int level, @NotNull EntityCategory entityCategory) {
        return 0;
    }

    @Override
    public float getDamageIncrease(int level, @NotNull EntityType entityType) {
        return 0;
    }

    @Override
    public @NotNull Set<EquipmentSlotGroup> getActiveSlotGroups() {
        return Set.of();
    }

    @Override
    public @NotNull Component description() {
        return Component.text(this.enchantment.getInfoDescription().stream().toString().concat(""));
    }

    @Override
    public @NotNull RegistryKeySet<ItemType> getSupportedItems() {
        return null;
    }

    @Override
    public @Nullable RegistryKeySet<ItemType> getPrimaryItems() {
        return null;
    }

    @Override
    public int getWeight() {
        return 0;
    }

    @Override
    public @NotNull RegistryKeySet<Enchantment> getExclusiveWith() {
        return null;
    }

    @Override
    public @NotNull String translationKey() {
        return "";
    }

    public @NotNull CEnchantment getEnchantment() {
        return this.enchantment;
    }

    public @NotNull CEnchantments getData() {
        return data;
    }

    @Override
    public @NotNull String getTranslationKey() {
        return "";
    }
}
