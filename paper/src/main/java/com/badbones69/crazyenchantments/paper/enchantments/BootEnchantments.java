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
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
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

import java.util.Map;
import java.util.Random;

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

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerEquip(PlayerArmorChangeEvent event) {
        Player player = event.getPlayer();

        if (this.wingsManager.isWingsEnabled()) {
            // Check the new armor piece.
            WingsUtils.checkArmor(event.getNewItem(), true, null, player);

            // Check the old armor piece.
            WingsUtils.checkArmor(null, false, event.getOldItem(), player);
        }
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
        // Only act on full block movement
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        if (!this.enchantmentBookSettings.getEnchantments(player.getEquipment().getBoots())
                .containsKey(CEnchantments.LAVAWALKER.getEnchantment())) return;

        if (player.getLocation().subtract(0, 0.1, 0).getBlock().isPassable()) return;

        Location baseLoc = player.getLocation();
        boolean convertedAnyLava = false;
        Vector direction = baseLoc.getDirection().setY(0).normalize();
        Location center = baseLoc.clone().add(direction);

        int radius = 3;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z > radius * radius) continue;

                Location checkLoc = center.clone().add(x, -1, z);
                Block block = checkLoc.getBlock();

                if (block.getType() == Material.LAVA) {
                    if (block.getBlockData() instanceof Levelled lavaData) {
                        if (lavaData.getLevel() != 0) continue;
                    }
                    Block aboveBlock = block.getRelative(0, 1, 0);
                    if (aboveBlock.getType() != Material.LAVA) {
                        block.setType(Material.OBSIDIAN);
                        convertedAnyLava = true;

                        int delay = 20 * (3 + new Random().nextInt(4)); // 3–6 seconds
                        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                            if (!block.getChunk().isLoaded()) return;
                            if (block.getType() == Material.OBSIDIAN) {
                                block.setType(Material.MAGMA_BLOCK);

                                Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                                    if (!block.getChunk().isLoaded()) return;
                                    if (block.getType() == Material.MAGMA_BLOCK) {
                                        block.setType(Material.LAVA);

                                        if (new Random().nextInt(3) == 0) {
                                            for (Player p : Bukkit.getOnlinePlayers()) {
                                                if (p.getWorld().equals(block.getWorld()) &&
                                                        p.getLocation().distanceSquared(block.getLocation()) < 100) {
                                                    p.playSound(block.getLocation(), Sound.ITEM_FIRECHARGE_USE, 0.6f, 0.01f);
                                                    p.playSound(block.getLocation(), Sound.BLOCK_FIRE_AMBIENT, 0.5f, 1f);
                                                }
                                            }
                                        }
                                    }
                                }, 20 * 2);
                            }
                        }, delay);
                    }
                }
            }
        }
        if (convertedAnyLava) {
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 0.7f, 1f);
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
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!this.enchantmentBookSettings.getEnchantments(victim.getEquipment().getBoots()).containsKey(quiverEnchant)) return;
        Map<CEnchantment, Integer> enchants = enchantmentBookSettings.getEnchantments(victim.getEquipment().getBoots());

        if (!enchants.containsKey(quiverEnchant)) return;
        int level = enchants.get(CEnchantments.QUIVER.getEnchantment());
        if (!CEnchantments.QUIVER.isOffCooldown(victim.getUniqueId(), level, true)) return;

        if (!attacker.isOnGround()) return;
        Vector direction = attacker.getLocation().getDirection().normalize();
        double verticalBoost = 0.3 + (0.1 * level);

        direction.setY(verticalBoost);
        direction.multiply(1.5);
        attacker.setVelocity(direction);

    }
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamagedByEntity(EntityDamageByEntityEvent event) {
        CEnchantment torrentEnchant = CEnchantments.TORRENT.getEnchantment();

        if (!(event.getEntity() instanceof LivingEntity target)) return;

        // Determine attacker
        LivingEntity attacker = null;

        if (event.getDamager() instanceof LivingEntity direct) {
            attacker = direct;
        } else if (event.getDamager() instanceof org.bukkit.entity.Projectile projectile) {
            if (projectile.getShooter() instanceof LivingEntity shooter) {
                attacker = shooter;
            }
        }

        if (attacker == null) return;
        if (!this.enchantmentBookSettings.getEnchantments(attacker.getEquipment().getBoots()).containsKey(torrentEnchant)) return;

        // Check if the attacker is wearing boots with the Torrent enchant
        int level = enchantmentBookSettings.getLevel(attacker.getEquipment().getBoots(), CEnchantments.TORRENT.getEnchantment());

        // Water check
        Material blockType = attacker.getEyeLocation().getBlock().getType();
        boolean inWater =
                blockType == Material.WATER ||
                        blockType == Material.BUBBLE_COLUMN ||
                        blockType == Material.KELP;

        // Rain check
        boolean inRain = attacker.getWorld().hasStorm() &&
                attacker.getWorld().getBlockAt(attacker.getLocation()).getLightFromSky() == 15;

        if (!(inWater || inRain)) return;

        // Boost the damage
        double originalDamage = event.getDamage();
        double boostedDamage = originalDamage * (1.0 + 0.25 * level); // Damage boost
        event.setDamage(boostedDamage);
    }


}