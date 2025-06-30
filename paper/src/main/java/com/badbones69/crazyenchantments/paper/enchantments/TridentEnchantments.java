package com.badbones69.crazyenchantments.paper.enchantments;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.Methods;
import com.badbones69.crazyenchantments.paper.Starter;
import com.badbones69.crazyenchantments.paper.api.enums.CEnchantments;
import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import com.badbones69.crazyenchantments.paper.api.utils.EnchantUtils;
import com.badbones69.crazyenchantments.paper.controllers.settings.EnchantmentBookSettings;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        if (!(item.equals(ItemStack.of(Material.TRIDENT)))) return;

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
            double twingeStack = (event.getDamage() / (4.37 - this.enchantmentBookSettings.getLevel(item, twingeEnchant)));

            List<BukkitTask> twingeTasks = new ArrayList<>();

            twingeTasks.add(this.scheduler.runTaskTimer(plugin, () -> target.damage(twingeStack), 40L, 20L));
            twingeTasks.add(this.scheduler.runTaskTimer(plugin, () -> target.sendMessage("You are bleeding! (Twinge)"), 40L, 20L));
            twingeTasks.add(this.scheduler.runTaskTimer(plugin, () -> attacker.sendMessage("** TWINGE **"), 40L, 20L));

            this.scheduler.runTaskLater(plugin, () -> {
                for (BukkitTask task : twingeTasks) task.cancel();
            }, 80L);
        }
    }
}
