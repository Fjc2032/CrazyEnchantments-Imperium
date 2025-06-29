package com.badbones69.crazyenchantments.paper.enchantments;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.Methods;
import com.badbones69.crazyenchantments.paper.Starter;
import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import com.badbones69.crazyenchantments.paper.controllers.settings.EnchantmentBookSettings;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class TridentEnchantments implements Listener {

    @NotNull
    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    @NotNull
    private final Starter starter = this.plugin.getStarter();

    @NotNull
    private final Methods methods = this.starter.getMethods();

    // Settings.
    @NotNull
    private final EnchantmentBookSettings enchantmentBookSettings = this.starter.getEnchantmentBookSettings();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTridentThrow(ProjectileLaunchEvent event) {

    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTridentHit(ProjectileHitEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player shooter)) return;
        if (!(event.getEntity() instanceof Trident trident)) return;

        ItemStack item = shooter.getActiveItem();
        if (!(item.equals(ItemStack.of(Material.TRIDENT)))) return;

        Map<CEnchantment, Integer> enchants = this.enchantmentBookSettings.getEnchantments(item);
    }
}
