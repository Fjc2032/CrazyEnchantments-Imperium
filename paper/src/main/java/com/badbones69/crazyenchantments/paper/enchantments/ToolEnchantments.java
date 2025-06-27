package com.badbones69.crazyenchantments.paper.enchantments;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.Methods;
import com.badbones69.crazyenchantments.paper.Starter;
import com.badbones69.crazyenchantments.paper.api.enums.CEnchantments;
import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import com.badbones69.crazyenchantments.paper.api.utils.EnchantUtils;
import com.badbones69.crazyenchantments.paper.controllers.settings.EnchantmentBookSettings;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

public class ToolEnchantments implements Listener {

    @NotNull
    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    @NotNull
    private final Starter starter = this.plugin.getStarter();

    @NotNull
    private final Methods methods = this.starter.getMethods();

    // Settings.
    @NotNull
    private final EnchantmentBookSettings enchantmentBookSettings = this.starter.getEnchantmentBookSettings();

    @EventHandler()
    public void onPlayerClick(PlayerInteractEvent event) {
        // Check what hand is being used as the event fires for each hand.
        if (Objects.equals(event.getHand(), EquipmentSlot.HAND)) updateEffects(event.getPlayer());
    }
    @EventHandler()
    public void onBlockTap(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        ItemStack tool = event.getItem();

        if (block == null) return;
        if (!block.isSolid()) return;

        if (EnchantUtils.isEventActive(CEnchantments.REFORGED, player, tool, this.enchantmentBookSettings.getEnchantments(tool))) reforgedTrigger(player, tool);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTelepathy(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = this.methods.getItemInHand(player);

        if (EnchantUtils.isEventActive(CEnchantments.TELEPATHY, player, tool, this.enchantmentBookSettings.getEnchantments(tool))) {
            event.setCancelled(true);
            this.methods.addItemToInventory(player, event.getItems());
        }
    }
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = this.methods.getItemInHand(player);
        Map<CEnchantment, Integer> enchantments = this.enchantmentBookSettings.getEnchantments(item);
        
        int potionTime = 20;

        if (EnchantUtils.isEventActive(CEnchantments.OXYGENATE, player, item, enchantments)) {
            player.removePotionEffect(PotionEffectType.WATER_BREATHING);
            player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, potionTime, 5));
        }

    }

    private void updateEffects(Player player) {
        ItemStack item = this.methods.getItemInHand(player);
        Map<CEnchantment, Integer> enchantments = this.enchantmentBookSettings.getEnchantments(item);

        int potionTime = 5 * 20;

        if (EnchantUtils.isEventActive(CEnchantments.HASTE, player, item, enchantments)) {
            int power = enchantments.get(CEnchantments.HASTE.getEnchantment());
            player.removePotionEffect(PotionEffectType.HASTE);
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, potionTime, power - 1));
        }
    }
    private void reforgedTrigger(Player player, ItemStack tool) {
        if (tool == null) return;
        ItemMeta meta = tool.getItemMeta();
        Damageable damageable = (Damageable) meta;
        int damage = damageable.getDamage();
        int modifier = CEnchantments.REFORGED.getChance() * player.getExpToLevel();
        int newDurability = damage - modifier;
        if (newDurability < 0) return;
        damageable.setDamage(newDurability);
        if (damageable.getDamage() < 0) return;
        tool.setItemMeta(meta);
        player.sendMessage("Your tool has been reforged!");
        player.sendMessage("New durability: " + newDurability);
    }
    private void keepDurability(@NotNull Player player, @Nullable ItemStack tool) {
        if (tool == null) return;
        ItemMeta meta = tool.getItemMeta();
        Damageable damageable = (Damageable) meta;
        damageable.setUnbreakable(true);
        tool.setItemMeta(meta);
    }
}
