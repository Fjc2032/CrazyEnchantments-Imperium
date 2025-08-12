package com.badbones69.crazyenchantments.paper.enchantments;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.Starter;
import com.badbones69.crazyenchantments.paper.api.enums.CEnchantments;
import com.badbones69.crazyenchantments.paper.api.managers.WingsManager;
import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import com.badbones69.crazyenchantments.paper.api.utils.EnchantUtils;
import com.badbones69.crazyenchantments.paper.api.utils.WingsUtils;
import com.badbones69.crazyenchantments.paper.controllers.AttributeController;
import com.badbones69.crazyenchantments.paper.controllers.settings.EnchantmentBookSettings;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class BootEnchantments implements Listener {

    @NotNull
    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    @NotNull
    private final Starter starter = this.plugin.getStarter();

    // Plugin Managers.
    @NotNull
    private final WingsManager wingsManager = this.starter.getWingsManager();

    @NotNull
    private final EnchantmentBookSettings enchantmentBookSettings = this.starter.getEnchantmentBookSettings();

    //Controllers
    @NotNull
    private final AttributeController attributeController = new AttributeController();

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerEquip(PlayerArmorChangeEvent event) {
        Player player = event.getPlayer();
        ItemStack boots = player.getInventory().getBoots();
        if (boots == null) return;

        //Enchants
        CEnchantment gears = CEnchantments.GEARS.getEnchantment();
        double power = this.enchantmentBookSettings.getLevel(boots, gears) * 0.75;

        //Attributes
        AttributeModifier gearsModifier = new AttributeModifier(new NamespacedKey(this.plugin, "gears"), power, AttributeModifier.Operation.ADD_NUMBER);

        //Metadata
        ItemMeta meta = boots.getItemMeta();

        if (this.wingsManager.isWingsEnabled()) {
            // Check the new armor piece.
            WingsUtils.checkArmor(event.getNewItem(), true, null, player);

            // Check the old armor piece.
            WingsUtils.checkArmor(null, false, event.getOldItem(), player);
        }
        if (this.enchantmentBookSettings.hasEnchantment(boots.getItemMeta(), gears)) {
            meta.addAttributeModifier(Attribute.MOVEMENT_SPEED, gearsModifier);
            boots.setItemMeta(meta);
        }

        this.attributeController.updateAttributes(player, Attribute.MOVEMENT_SPEED, gearsModifier, gears, boots, EquipmentSlot.FEET);

    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerFly(PlayerToggleFlightEvent event) {
        if (!this.wingsManager.isWingsEnabled()) return;

        Player player = event.getPlayer();

        if (player.getEquipment().getBoots() == null) return;
        if (!this.enchantmentBookSettings.getEnchantments(player.getEquipment().getBoots()).containsKey(CEnchantments.WINGS.getEnchantment())) return;

        if (WingsUtils.checkRegion(player) || WingsUtils.isEnemiesNearby(player)) return;

        if (event.isFlying()) {
            if (player.getAllowFlight()) {
                event.setCancelled(true);
                player.setFlying(true);
                this.wingsManager.addFlyingPlayer(player);
            }
        } else {
            this.wingsManager.removeFlyingPlayer(player);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom() == event.getTo()) return;

        Player player = event.getPlayer();
        boolean isFlying = player.isFlying(); // TODO implement single method for all enchantment checks. #EnchantUtils

        if (this.wingsManager.isWingsEnabled() && this.enchantmentBookSettings.getEnchantments(player.getEquipment().getBoots()).containsKey(CEnchantments.WINGS.getEnchantment())) {
            if (WingsUtils.checkRegion(player)) {
                if (!WingsUtils.isEnemiesNearby(player)) {
                    player.setAllowFlight(true);
                } else {
                    if (isFlying && WingsUtils.checkGameMode(player)) {
                        player.setFlying(false);
                        player.setAllowFlight(false);
                        this.wingsManager.removeFlyingPlayer(player);
                    }
                }
            } else {
                if (isFlying && WingsUtils.checkGameMode(player)) {
                    player.setFlying(false);
                    player.setAllowFlight(false);
                    this.wingsManager.removeFlyingPlayer(player);
                }
            }

            if (isFlying) this.wingsManager.addFlyingPlayer(player);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!this.wingsManager.isWingsEnabled()) return;

        if (!this.enchantmentBookSettings.getEnchantments(player.getEquipment().getBoots()).containsKey(CEnchantments.WINGS.getEnchantment())) return;

        if (WingsUtils.checkRegion(player) || WingsUtils.isEnemiesNearby(player)) return;

        player.setAllowFlight(true);
        this.wingsManager.addFlyingPlayer(player);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (!this.wingsManager.isWingsEnabled() || !this.wingsManager.isFlyingPlayer(player)) return;

        player.setFlying(false);
        player.setAllowFlight(false);
        this.wingsManager.removeFlyingPlayer(player);
    }
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onFallInterceptByEnchant(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!this.enchantmentBookSettings.getEnchantments(player.getEquipment().getBoots()).containsKey(CEnchantments.JELLYLEGS.getEnchantment())) return;
        if (!EntityDamageEvent.DamageCause.FALL.equals(event.getCause())) return;

        event.setCancelled(true);
    }
    @EventHandler()
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player target)) return;
        if (!EnchantUtils.isArmorEventActive(target, CEnchantments.METAPHYSICAL, target.getInventory().getBoots())) return;
        if (target.hasPotionEffect(PotionEffectType.SLOWNESS)) target.removePotionEffect(PotionEffectType.SLOWNESS);
        if (CEnchantments.METAPHYSICAL.getChance() >= 80) {
            if (target.hasPotionEffect(PotionEffectType.SLOWNESS)) target.removePotionEffect(PotionEffectType.SLOWNESS);
        }
    }
    @EventHandler()
    public void onAttack1(EntityDamageByEntityEvent event) {
        CEnchantment quiverEnchant = CEnchantments.QUIVER.getEnchantment();
        if (!(event.getEntity() instanceof Player player)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!this.enchantmentBookSettings.getEnchantments(player.getEquipment().getBoots()).containsKey(quiverEnchant)) return;

        Vector vector = player.getLocation().getDirection();
        vector.setY(player.getY() + ((double) CEnchantments.QUIVER.getChance() / 100));
        vector.multiply(3).normalize();

        player.setVelocity(vector);
    }


}