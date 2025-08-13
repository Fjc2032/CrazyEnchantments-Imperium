package com.badbones69.crazyenchantments.paper.enchantments;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.Methods;
import com.badbones69.crazyenchantments.paper.Starter;
import com.badbones69.crazyenchantments.paper.api.enums.CEnchantments;
import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import com.badbones69.crazyenchantments.paper.api.utils.EnchantUtils;
import com.badbones69.crazyenchantments.paper.controllers.AttributeController;
import com.badbones69.crazyenchantments.paper.controllers.settings.EnchantmentBookSettings;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.checkerframework.checker.units.qual.N;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class ToolEnchantments implements Listener {

    @NotNull
    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    @NotNull
    private final Starter starter = this.plugin.getStarter();

    @NotNull
    private final Methods methods = this.starter.getMethods();

    // Controllers
    @NotNull
    private final AttributeController attributeController = new AttributeController();

    // Settings.
    @NotNull
    private final EnchantmentBookSettings enchantmentBookSettings = this.starter.getEnchantmentBookSettings();


    @ApiStatus.Experimental
    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack item = this.methods.getItemInHand(player);

        //Enchants
        CEnchantment oxygenate = CEnchantments.OXYGENATE.getEnchantment();
        CEnchantment haste = CEnchantments.HASTE.getEnchantment();

        //Metadata
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        try {
            if (this.enchantmentBookSettings.hasEnchantment(meta, oxygenate)) {
                if (!player.isInWaterOrBubbleColumn()) return;
                int power = this.enchantmentBookSettings.getLevel(item, oxygenate);
                AttributeModifier oxygenateModifier = new AttributeModifier(new NamespacedKey(this.plugin, "oxygenate"), power * 7, AttributeModifier.Operation.ADD_NUMBER);
                player.getAttribute(Attribute.OXYGEN_BONUS).addModifier(oxygenateModifier);

                this.attributeController.updateAttributes(player, Attribute.OXYGEN_BONUS, oxygenateModifier, oxygenate);
                this.attributeController.add(Attribute.OXYGEN_BONUS, oxygenateModifier);
            }
            if (this.enchantmentBookSettings.hasEnchantment(meta, haste)) {
                int power = this.enchantmentBookSettings.getLevel(item, haste);
                AttributeModifier hasteModifier = new AttributeModifier(new NamespacedKey(this.plugin, "haste"), power * 10, AttributeModifier.Operation.ADD_NUMBER);
                meta.addAttributeModifier(Attribute.MINING_EFFICIENCY, hasteModifier);
                item.setItemMeta(meta);

                this.attributeController.updateAttributes(player, Attribute.MINING_EFFICIENCY, hasteModifier, haste, item);
                this.attributeController.add(Attribute.MINING_EFFICIENCY, hasteModifier);
            }
        } catch (IllegalArgumentException ignored) {
        }
    }

    @EventHandler()
    public void onBlockTap(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        ItemStack tool = event.getItem();

        if (block == null) return;
        if (!block.isSolid()) return;

        if (EnchantUtils.isEventActive(CEnchantments.REFORGED, player, tool, this.enchantmentBookSettings.getEnchantments(tool))) reforgedTrigger(player, tool);
        if (EnchantUtils.isEventActive(CEnchantments.OBBYDESTROYER, player, tool, this.enchantmentBookSettings.getEnchantments(tool))) {
            int level = this.enchantmentBookSettings.getLevel(Objects.requireNonNull(tool), CEnchantments.OXYGENATE.getEnchantment());

            if (CEnchantments.OBBYDESTROYER.isOffCooldown(player.getUniqueId(), level, true)) {
                if (!block.getType().equals(Material.OBSIDIAN)) return;
                player.playSound(player, Sound.BLOCK_CALCITE_BREAK, 1.0F, 2.0F);
                block.setType(Material.AIR);
            }
        }
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

        if (EnchantUtils.isEventActive(CEnchantments.ABIDING, player, item, enchantments)) {
            if (item.getItemMeta().isUnbreakable()) return;
            keepDurability(item);
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
    private void keepDurability(@Nullable ItemStack tool) {
        if (tool == null) {
            this.plugin.getLogger().warning("[DEBUG] [Abiding] Something is wrong with the tool you are using.");
            this.plugin.getLogger().warning("[DEBUG] [Abiding] Null.");
            return;
        } else this.plugin.getLogger().info("[DEBUG] [Abiding] Got the tool. Continuing...");
        ItemMeta meta = tool.getItemMeta();
        meta.setUnbreakable(true);
        tool.setItemMeta(meta);
    }
}
