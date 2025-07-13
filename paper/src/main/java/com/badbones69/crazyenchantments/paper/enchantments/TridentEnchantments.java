package com.badbones69.crazyenchantments.paper.enchantments;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.Methods;
import com.badbones69.crazyenchantments.paper.Starter;
import com.badbones69.crazyenchantments.paper.api.enums.CEnchantments;
import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import com.badbones69.crazyenchantments.paper.api.utils.EnchantUtils;
import com.badbones69.crazyenchantments.paper.controllers.settings.EnchantmentBookSettings;
import io.papermc.paper.event.player.PlayerArmSwingEvent;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TridentEnchantments implements Listener {

    @NotNull
    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    @NotNull
    private final Starter starter = this.plugin.getStarter();

    @NotNull
    private final Methods methods = this.starter.getMethods();

    @NotNull
    private final BukkitScheduler scheduler = Bukkit.getScheduler();

    // Settings.
    @NotNull
    private final EnchantmentBookSettings enchantmentBookSettings = this.starter.getEnchantmentBookSettings();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTridentThrow(ProjectileLaunchEvent event) {
        //Shooter is the player shooting the trident.
        //Trident is, well, the trident.
        if (!(event.getEntity().getShooter() instanceof Player shooter)) return;
        if (!(event.getEntity() instanceof Trident trident)) return;

        ItemStack item = this.methods.getItemInHand(shooter);
        if (!item.equals(ItemStack.of(Material.TRIDENT))) return;

        Map<CEnchantment, Integer> enchants = this.enchantmentBookSettings.getEnchantments(item);

        World world = shooter.getWorld();

        if (EnchantUtils.isEventActive(CEnchantments.AURA, shooter, item, enchants)) {
            CEnchantment auraEnchant = CEnchantments.AURA.getEnchantment();
            List<BukkitTask> runnables = new ArrayList<>();
            AtomicInteger time = new AtomicInteger();
            int newtime = time.get();
            double level = this.enchantmentBookSettings.getLevel(item, auraEnchant);

            runnables.add(this.scheduler.runTaskTimer(plugin, () -> {
                @NotNull Collection<LivingEntity> targets = world.getNearbyLivingEntities(
                        trident.getLocation(),
                        4 + level
                );
                for (LivingEntity entity : targets) {
                    if (entity.equals(shooter)) continue;
                    entity.damage(level);
                }
                Material material = Material.BREEZE_ROD;
                world.spawnParticle(
                        Particle.SMOKE,
                        trident.getLocation(),
                        20,
                        material.createBlockData()
                );
            }, 0L, 20L));

            //todo tf am i doing with this?
            runnables.add(this.scheduler.runTaskTimer(plugin, () -> time.getAndIncrement(), 0L, 20L));
            runnables.add(this.scheduler.runTaskTimer(plugin, () -> shooter.sendMessage("AURA active for " + newtime / 20), 1L, 20L));

            this.scheduler.runTaskTimer(plugin, () -> {
                for (BukkitTask task : runnables) {
                    if (!trident.isValid() || trident.isInBlock() || trident.hasDealtDamage()) task.cancel();
                }
            }, 0L, 20L);

            //Stop the aura if it takes too long to cancel by itself
            this.scheduler.runTaskLater(plugin, () -> {
                for (BukkitTask task : runnables) task.cancel();
            }, 300L);
        }

    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTridentHit(EntityDamageByEntityEvent event) {
        //Shooter is the player shooting the trident.
        //Target is the LivingEntity being targeted.
        //Trident is, well, the trident.
        if (!(event.getDamager() instanceof Trident trident)) return;
        if (!(trident.getShooter() instanceof Player shooter)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        ItemStack item = this.methods.getItemInHand(shooter);

        Map<CEnchantment, Integer> enchants = this.enchantmentBookSettings.getEnchantments(item);

        if (EnchantUtils.isEventActive(CEnchantments.IMPACT, shooter, item, enchants)) {
            event.setDamage(event.getDamage() * 2);
        }
    }
    @EventHandler(ignoreCancelled = true)
    public void onTridentMeleeAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        ItemStack item = this.methods.getItemInHand(attacker);

        Map<CEnchantment, Integer> enchants = this.enchantmentBookSettings.getEnchantments(item);

        if (EnchantUtils.isEventActive(CEnchantments.TWINGE, attacker, item, enchants)) {
            CEnchantment twingeEnchant = CEnchantments.TWINGE.getEnchantment();
            double twingeStack = (event.getDamage() / (twingeEnchant.getMaxLevel() - this.enchantmentBookSettings.getLevel(item, twingeEnchant)));

            List<BukkitTask> twingeTasks = new ArrayList<>();

            twingeTasks.add(this.scheduler.runTaskTimer(plugin, () -> target.damage(twingeStack), 40L, 20L));
            twingeTasks.add(this.scheduler.runTaskTimer(plugin, () -> target.sendMessage("You are bleeding! (Twinge)"), 40L, 20L));
            twingeTasks.add(this.scheduler.runTaskTimer(plugin, () -> attacker.sendMessage("** TWINGE **"), 40L, 20L));

            this.scheduler.runTaskLater(plugin, () -> {
                for (BukkitTask task : twingeTasks) task.cancel();
            }, 80L);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTridentSwing(PlayerArmSwingEvent event) {
        Player player = event.getPlayer();
        Location target = player.getEyeLocation();
        Location location = player.getLocation();

        double distance = target.distance(location);

    }
}
