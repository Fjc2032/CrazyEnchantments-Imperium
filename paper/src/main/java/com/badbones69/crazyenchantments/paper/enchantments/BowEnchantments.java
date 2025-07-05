package com.badbones69.crazyenchantments.paper.enchantments;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.Methods;
import com.badbones69.crazyenchantments.paper.Starter;
import com.badbones69.crazyenchantments.paper.api.FileManager.Files;
import com.badbones69.crazyenchantments.paper.api.enums.CEnchantments;
import com.badbones69.crazyenchantments.paper.api.events.BookApplyEvent;
import com.badbones69.crazyenchantments.paper.api.managers.BowEnchantmentManager;
import com.badbones69.crazyenchantments.paper.api.objects.BowEnchantment;
import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import com.badbones69.crazyenchantments.paper.api.objects.EnchantedArrow;
import com.badbones69.crazyenchantments.paper.api.utils.BowUtils;
import com.badbones69.crazyenchantments.paper.api.utils.EnchantUtils;
import com.badbones69.crazyenchantments.paper.api.utils.EventUtils;
import com.badbones69.crazyenchantments.paper.controllers.settings.EnchantmentBookSettings;
import com.badbones69.crazyenchantments.paper.support.PluginSupport;
import net.minecraft.world.entity.projectile.Fireball;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BowEnchantments implements Listener {

    @NotNull
    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    @NotNull
    private final Starter starter = this.plugin.getStarter();

    @NotNull
    private final Methods methods = this.starter.getMethods();

    @NotNull
    private final EnchantmentBookSettings enchantmentBookSettings = this.starter.getEnchantmentBookSettings();

    // Plugin Support.
    @NotNull
    private final PluginSupport pluginSupport = this.starter.getPluginSupport();

    // Plugin Managers.
    @NotNull
    private final BowEnchantmentManager bowEnchantmentManager = this.starter.getBowEnchantmentManager();

    @NotNull
    private final BowUtils bowUtils = this.starter.getBowUtils();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBowShoot(final EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (EventUtils.isIgnoredEvent(event) || EventUtils.isIgnoredUUID(event.getEntity().getUniqueId())) return;
        if (!(event.getProjectile() instanceof Arrow arrow)) return;

        ItemStack bow = event.getBow();

        if (!this.bowUtils.allowsCombat(player)) return;

        Map<CEnchantment, Integer> enchants = this.enchantmentBookSettings.getEnchantments(bow);
        if (enchants.isEmpty()) return;

        // Add the arrow to the list.
        this.bowUtils.addArrow(arrow, bow, enchants);

        // MultiArrow only code below.
        if (EnchantUtils.isEventActive(CEnchantments.MULTIARROW, player, bow, enchants)) {
            int power = enchants.get(CEnchantments.MULTIARROW.getEnchantment());

            for (int i = 1; i <= power; i++) this.bowUtils.spawnArrows(player, arrow, bow);
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLand(ProjectileHitEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player shooter)) return;
        if (!(event.getEntity() instanceof Arrow entityArrow)) return;
        if (!this.bowUtils.allowsCombat(event.getEntity())) return;

        EnchantedArrow enchantedArrow = this.bowUtils.getEnchantedArrow(entityArrow);

        if (enchantedArrow == null) return;

        // Spawn webs related to STICKY_SHOT.
        this.bowUtils.spawnWebs(event.getHitEntity(), enchantedArrow);

        if (EnchantUtils.isEventActive(CEnchantments.BOOM, shooter, enchantedArrow.bow(), enchantedArrow.enchantments())) {
            this.methods.explode(enchantedArrow.getShooter(), enchantedArrow.arrow());
            enchantedArrow.arrow().remove();
        }

        if (EnchantUtils.isEventActive(CEnchantments.LIGHTNING, shooter, enchantedArrow.bow(), enchantedArrow.enchantments())) {
            int level = enchantmentBookSettings.getLevel(enchantedArrow.bow(), CEnchantments.LIGHTNING.getEnchantment());
            if (CEnchantments.LIGHTNING.isOffCooldown(shooter.getUniqueId(), level, true)) {
                Location location = enchantedArrow.arrow().getLocation();

                Entity lightning = location.getWorld().strikeLightningEffect(location);

                int lightningSoundRange = Files.CONFIG.getFile().getInt("Settings.EnchantmentOptions.Lightning-Sound-Range", 160);

                try {
                    location.getWorld().playSound(location, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, (float) lightningSoundRange / 16f, 1);
                } catch (Exception ignore) {
                }

                for (LivingEntity entity : this.methods.getNearbyLivingEntities(2D, enchantedArrow.arrow())) {
                    EntityDamageEvent damageByEntityEvent = new EntityDamageEvent(entity, DamageCause.LIGHTNING, DamageSource.builder(DamageType.LIGHTNING_BOLT).withCausingEntity(shooter).withDirectEntity(lightning).build(), 5D);

                    EventUtils.addIgnoredEvent(damageByEntityEvent);
                    EventUtils.addIgnoredUUID(shooter.getUniqueId());
                    shooter.getServer().getPluginManager().callEvent(damageByEntityEvent);

                    if (!damageByEntityEvent.isCancelled() && !this.pluginSupport.isFriendly(enchantedArrow.getShooter(), entity) && !enchantedArrow.getShooter().getUniqueId().equals(entity.getUniqueId()))
                        entity.damage(5D);

                    EventUtils.removeIgnoredEvent(damageByEntityEvent);
                    EventUtils.removeIgnoredUUID(shooter.getUniqueId());
                }
            } else {
                shooter.sendMessage("Lightning on cooldown!");
            }

        }

        // Removes the arrow from the list after 5 ticks. This is done because the onArrowDamage event needs the arrow in the list, so it can check.
        entityArrow.getScheduler().runDelayed(this.plugin, (arrowTask) -> this.bowUtils.removeArrow(enchantedArrow),  null, 5);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onArrowDamage(EntityDamageByEntityEvent event) {
        if (EventUtils.isIgnoredEvent(event)) return;
        if (!(event.getDamager() instanceof Arrow entityArrow)) return;
        if (!(event.getEntity() instanceof LivingEntity entity)) return;

        EnchantedArrow enchantedArrow = this.bowUtils.getEnchantedArrow(entityArrow);
        if (enchantedArrow == null) return;

        if (!this.pluginSupport.allowCombat(enchantedArrow.arrow().getLocation())) return;
        // Damaged player is friendly.

        if (EnchantUtils.isEventActive(CEnchantments.DOCTOR, enchantedArrow.getShooter(), enchantedArrow.bow(), enchantedArrow.enchantments()) && this.pluginSupport.isFriendly(enchantedArrow.getShooter(), event.getEntity())) {
            int heal = 1 + enchantedArrow.getLevel(CEnchantments.DOCTOR);
            // Uses getValue as if the player has health boost it is modifying the base so the value after the modifier is needed.
            double maxHealth = entity.getAttribute(Attribute.MAX_HEALTH).getValue();

            if (entity.getHealth() < maxHealth) {
                if (entity.getHealth() + heal < maxHealth) entity.setHealth(entity.getHealth() + heal);
                if (entity.getHealth() + heal >= maxHealth) entity.setHealth(maxHealth);
            }
        }

        // Damaged player is an enemy.
        if (this.pluginSupport.isFriendly(enchantedArrow.getShooter(), entity)) return;

        this.bowUtils.spawnWebs(event.getEntity(), enchantedArrow);

        if (EnchantUtils.isEventActive(CEnchantments.PULL, enchantedArrow.getShooter(), enchantedArrow.bow(), enchantedArrow.enchantments())) {
            Vector v = enchantedArrow.getShooter().getLocation().toVector().subtract(entity.getLocation().toVector()).normalize().multiply(3);
            entity.setVelocity(v);
        }

        for (BowEnchantment bowEnchantment : this.bowEnchantmentManager.getBowEnchantments()) {
            CEnchantments enchantment = bowEnchantment.getEnchantment();

            if (!EnchantUtils.isEventActive(enchantment, enchantedArrow.getShooter(), enchantedArrow.bow(), enchantedArrow.enchantments()))
                continue;

            if (bowEnchantment.isPotionEnchantment()) {
                bowEnchantment.getPotionEffects().forEach(effect -> entity.addPotionEffect(new PotionEffect(effect.potionEffect(), effect.duration(),
                        (bowEnchantment.isLevelAddedToAmplifier() ? enchantedArrow.getLevel(enchantment) : 0) + effect.amplifier())));
            } else {
                event.setDamage(event.getDamage() * ((bowEnchantment.isLevelAddedToAmplifier() ? enchantedArrow.getLevel(enchantment) : 0) + bowEnchantment.getDamageAmplifier()));
            }
        }
        //Imperium
        if (EnchantUtils.isEventActive(CEnchantments.LONGBOW, enchantedArrow.getShooter(), enchantedArrow.bow(), enchantedArrow.enchantments())) {
            if (entity.getActiveItem().equals(ItemStack.of(Material.BOW))) {
                event.setDamage(event.getDamage() * (this.enchantmentBookSettings.getLevel(enchantedArrow.bow(), CEnchantments.LONGBOW.getEnchantment())));
            }
        }
        if (EnchantUtils.isEventActive(CEnchantments.UNFOCUS, enchantedArrow.getShooter(), enchantedArrow.bow(), enchantedArrow.enchantments())) {
            entity.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, CEnchantments.UNFOCUS.getChance(), 1, true, true, true));
            event.setDamage(event.getDamage() * 1.5);
        }
        if (EnchantUtils.isEventActive(CEnchantments.VIRUS, enchantedArrow.getShooter(), enchantedArrow.bow(), enchantedArrow.enchantments())) {
            CEnchantment virusEnchant = CEnchantments.VIRUS.getEnchantment();
            if (entity.hasPotionEffect(PotionEffectType.POISON) || entity.hasPotionEffect(PotionEffectType.WITHER)) {
                Collection<PotionEffect> effects = new ArrayList<>();
                effects.add(new PotionEffect(PotionEffectType.POISON, virusEnchant.getChance(), this.enchantmentBookSettings.getLevel(enchantedArrow.bow(), virusEnchant)));
                effects.add(new PotionEffect(PotionEffectType.WITHER, virusEnchant.getChance(), this.enchantmentBookSettings.getLevel(enchantedArrow.bow(), virusEnchant)));
                entity.addPotionEffects(effects);
            }
        }
        if (EnchantUtils.isEventActive(CEnchantments.INFERNAL, enchantedArrow.getShooter(), enchantedArrow.bow(), enchantedArrow.enchantments())) {
            event.getEntity().setFireTicks(CEnchantments.INFERNAL.getChance());
        }
        if (EnchantUtils.isEventActive(CEnchantments.SNIPER, enchantedArrow.getShooter(), enchantedArrow.bow(), enchantedArrow.enchantments())) {
            CEnchantment sniperEnchantment = CEnchantments.SNIPER.getEnchantment();
            Location arrowPos = entityArrow.getLocation();
            Location targetPos = entity.getLocation();

            double targetHeight = entity.getHeight();
            double headshotThreshold = targetPos.getY() + (targetHeight * 0.80);
            double arrowPosY = arrowPos.getY();

            if (arrowPosY >= headshotThreshold) {
                event.setDamage(event.getDamage() * (2.5 + (this.enchantmentBookSettings.getLevel(enchantedArrow.bow(), sniperEnchantment))));
                enchantedArrow.getShooter().sendMessage("**HEADSHOT**");
                if (entity instanceof Player player) player.sendMessage("Headshot from SNIPER hit you for " + event.getDamage());
                if (enchantedArrow.getShooter() instanceof LivingEntity damager) damager.sendMessage("You hit your target for " + event.getDamage());
            }

        }
        if (EnchantUtils.isEventActive(CEnchantments.FARCAST, enchantedArrow.getShooter(), enchantedArrow.bow(), enchantedArrow.enchantments())) {
            if (!(event.getEntity() instanceof Player target)) return;
            Vector direction = (enchantedArrow.getShooter().getLocation().toVector().subtract(target.getLocation().toVector().subtract(new Vector(10, 1, 10))));
            direction.normalize().multiply(1 + (CEnchantments.FARCAST.getChance() / 20));
            target.setVelocity(direction);
        }
        if (EnchantUtils.isEventActive(CEnchantments.ARROWLIFESTEAL, enchantedArrow.getShooter(), enchantedArrow.bow(), enchantedArrow.enchantments())) {
            if (!(enchantedArrow.getShooter() instanceof Player shooter)) return;
            double shooterHealth = shooter.getHealth();
            double modifier = event.getDamage();

            shooter.setHealth(shooterHealth + modifier);
        }
        if (EnchantUtils.isEventActive(CEnchantments.BIDIRECTIONAL, enchantedArrow.getShooter(), enchantedArrow.bow(), enchantedArrow.enchantments())) {
            CEnchantment targetEnchant = CEnchantments.BIDIRECTIONAL.getEnchantment();

            List<Block> arrowAttached = enchantedArrow.getArrow().getAttachedBlocks();
            Block block = arrowAttached.getFirst();
            Location arrowPos = block.getLocation();

            entity.setVelocity(arrowPos.getDirection().normalize().multiply(1.05 + enchantmentBookSettings.getLevel(enchantedArrow.bow(), targetEnchant)));
        }
        if (EnchantUtils.isEventActive(CEnchantments.HELLFIRE, enchantedArrow.getShooter(), enchantedArrow.bow(), enchantedArrow.enchantments())) {
            CEnchantment hellfireEnchant = CEnchantments.HELLFIRE.getEnchantment();

            World world = entity.getWorld();
            Vector direction = entity.getLocation().getDirection();
            float yield = (float) 1 + (enchantmentBookSettings.getLevel(enchantedArrow.bow(), hellfireEnchant));

            Location arrowPos = entityArrow.getLocation();
            Entity ball = world.spawnEntity(arrowPos, EntityType.FIREBALL);
            if (!(ball instanceof Fireball fireball)) return;
            fireball.bukkitYield = yield;

            ball.setVelocity(direction.normalize());
        }
    }

        //Imperium

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onWebBreak(BlockBreakEvent event) {
        if (!EventUtils.isIgnoredEvent(event) && this.bowUtils.getWebBlocks().contains(event.getBlock())) event.setCancelled(true);
    }
}
