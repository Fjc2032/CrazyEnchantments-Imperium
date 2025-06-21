package com.badbones69.crazyenchantments.paper.enchantments;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.Methods;
import com.badbones69.crazyenchantments.paper.Starter;
import com.badbones69.crazyenchantments.paper.api.CrazyManager;
import com.badbones69.crazyenchantments.paper.api.enums.CEnchantments;
import com.badbones69.crazyenchantments.paper.api.enums.pdc.Enchant;
import com.badbones69.crazyenchantments.paper.api.events.BookApplyEvent;
import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import com.badbones69.crazyenchantments.paper.api.builders.ItemBuilder;
import com.badbones69.crazyenchantments.paper.api.utils.EnchantUtils;
import com.badbones69.crazyenchantments.paper.api.utils.EntityUtils;
import com.badbones69.crazyenchantments.paper.api.utils.EventUtils;
import com.badbones69.crazyenchantments.paper.controllers.settings.EnchantmentBookSettings;
import com.badbones69.crazyenchantments.paper.support.PluginSupport;
import net.Indyuce.mmoitems.particle.api.ParticleType;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AxeEnchantments implements Listener {

    @NotNull
    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    @NotNull
    private final Starter starter = this.plugin.getStarter();

    @NotNull
    private final Methods methods = this.starter.getMethods();

    @NotNull
    private final EnchantmentBookSettings enchantmentBookSettings = this.starter.getEnchantmentBookSettings();

    @NotNull
    private final CrazyManager crazyManager = this.starter.getCrazyManager();

    private Enchant enchant;

    // Plugin Support.
    @NotNull
    private final PluginSupport pluginSupport = this.starter.getPluginSupport();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (EventUtils.isIgnoredEvent(event)) return;
        if (this.pluginSupport.isFriendly(event.getDamager(), event.getEntity())) return;

        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (!(event.getDamager() instanceof Player damager)) return;

        ItemStack item = this.methods.getItemInHand(damager);

        if (entity.isDead()) return;

        Map<CEnchantment, Integer> enchantments = this.enchantmentBookSettings.getEnchantments(item);

        if (EnchantUtils.isEventActive(CEnchantments.BERSERK, damager, item, enchantments)) {
                damager.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, (enchantments.get(CEnchantments.BERSERK.getEnchantment()) + 5) * 20, 1));
                damager.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, (enchantments.get(CEnchantments.BERSERK.getEnchantment()) + 5) * 20, 0));
        }

        if (EnchantUtils.isEventActive(CEnchantments.BLESSED, damager, item, enchantments)) removeBadPotions(damager);

        if (EnchantUtils.isEventActive(CEnchantments.FEEDME, damager, item, enchantments)&& damager.getFoodLevel() < 20) {
            int food = 2 * enchantments.get(CEnchantments.FEEDME.getEnchantment());

            if (damager.getFoodLevel() + food < 20) damager.setFoodLevel((int) (damager.getSaturation() + food));

            if (damager.getFoodLevel() + food > 20) damager.setFoodLevel(20);
        }

        if (EnchantUtils.isEventActive(CEnchantments.REKT, damager, item, enchantments)) event.setDamage(event.getDamage() * 2);

        if (EnchantUtils.isEventActive(CEnchantments.CURSED, damager, item, enchantments))
            entity.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, (enchantments.get(CEnchantments.CURSED.getEnchantment()) + 9) * 20, 1));

        if (EnchantUtils.isEventActive(CEnchantments.DIZZY, damager, item, enchantments))
            entity.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, (enchantments.get(CEnchantments.DIZZY.getEnchantment()) + 9) * 20, 0));

        if (EnchantUtils.isEventActive(CEnchantments.BATTLECRY, damager, item, enchantments)) {
            for (Entity nearbyEntity : damager.getNearbyEntities(3, 3, 3)) {
                entity.getScheduler().run(plugin, task -> {
                    if (!this.pluginSupport.isFriendly(damager, nearbyEntity)) {
                        Vector vector = damager.getLocation().toVector().normalize().setY(.5);
                        Vector vector1 = nearbyEntity.getLocation().toVector().subtract(vector);
                        nearbyEntity.setVelocity(vector1);
                    }
                }, null);
            }
        }

        if (EnchantUtils.isEventActive(CEnchantments.DEMONFORGED, damager, item, enchantments) && entity instanceof Player player) {

            ItemStack armorItem = switch (this.methods.percentPick(4, 0)) {
                case 1 -> player.getEquipment().getHelmet();
                case 2 -> player.getEquipment().getChestplate();
                case 3 -> player.getEquipment().getLeggings();
                default -> player.getEquipment().getBoots();
            };

            this.methods.removeDurability(armorItem, player);
        }
        //Imperium
        if (EnchantUtils.isEventActive(CEnchantments.REAPER, damager, item, enchantments)) {
            CEnchantment reaperEnchant = CEnchantments.REAPER.getEnchantment();
            int damageAmount = damager.getExpToLevel();
            int cap = this.enchantmentBookSettings.getLevel(item, reaperEnchant) * 10;
            if (damageAmount > cap) damageAmount = cap;
            event.setDamage(event.getDamage() * (1 + (double) damageAmount / 1500));
        }
        if (EnchantUtils.isEventActive(CEnchantments.PUMMEL, damager, item, enchantments)) {
            if (!(event.getDamager() instanceof LivingEntity target)) return;
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 6, 1));
        }
        if (EnchantUtils.isEventActive(CEnchantments.CLEAVE, damager, item, enchantments)) {
            World world = event.getDamager().getWorld();
            if (!(event.getEntity() instanceof LivingEntity victim)) return;
            BoundingBox region = new BoundingBox(damager.getX(), damager.getY(), damager.getZ(), victim.getX() + 3, victim.getY(), victim.getZ() + 3);
            Collection<Entity> targets = world.getNearbyEntities(region);

            for (Entity target : targets) {
                if (!(target instanceof LivingEntity)) return;
                ((LivingEntity) target).damage(event.getDamage() * ((double) CEnchantments.CLEAVE.getChance() / 20));
            }
        }
        if (EnchantUtils.isEventActive(CEnchantments.CORRUPT, damager, item, enchantments)) {
            damager.sendMessage("** CORRUPT **");
            if (!(event.getEntity() instanceof LivingEntity target)) return;
            target.damage(event.getDamage());
            Bukkit.getScheduler().runTaskLater(plugin, () -> target.damage(event.getDamage()), 40L);
            Bukkit.getScheduler().runTaskLater(plugin, () -> target.damage(event.getDamage()), 60L);
            Bukkit.getScheduler().runTaskLater(plugin, () -> target.damage(event.getDamage()), 80L);
        }
        if (EnchantUtils.isEventActive(CEnchantments.INSANITY, damager, item, enchantments)) {
            if (!(event.getEntity() instanceof Player target)) return;
            ItemStack axe = target.getActiveItem();

            Collection<ItemStack> axes = new ArrayList<>();
            axes.add(ItemStack.of(Material.WOODEN_AXE));
            axes.add(ItemStack.of(Material.STONE_AXE));
            axes.add(ItemStack.of(Material.GOLDEN_AXE));
            axes.add(ItemStack.of(Material.IRON_AXE));
            axes.add(ItemStack.of(Material.DIAMOND_AXE));
            axes.add(ItemStack.of(Material.NETHERITE_AXE));

            for (ItemStack selectedItem : axes) {
                if (selectedItem.equals(axe)) {
                    event.setDamage(event.getDamage() + (1 + (double) CEnchantments.INSANITY.getChance() / 100));
                }
            }
        }
        if (EnchantUtils.isEventActive(CEnchantments.BARBARIAN, damager, item, enchantments)) {
            event.setDamage(event.getDamage() * (1 + ((double) CEnchantments.BARBARIAN.getChance() / 100)));
        }
        if (EnchantUtils.isEventActive(CEnchantments.BLEED, damager, item, enchantments)) {
            if (!(event.getEntity() instanceof Player player)) return;

            enchantmentBookSettings.createCooldown(CEnchantments.BLEED.getEnchantment(), item, damager.getUniqueId(), 2000L, 2L);

            Particle.DustOptions dustOptions = new Particle.DustOptions(Color.RED, 5.0F);

            List<BukkitTask> bleedTasks = new ArrayList<>();

            bleedTasks.add(Bukkit.getScheduler().runTaskTimer(plugin, () -> player.spawnParticle(Particle.DUST, player.getLocation(), 12, dustOptions), 40L, 20L));
            bleedTasks.add(Bukkit.getScheduler().runTaskTimer(plugin, () -> player.damage(event.getDamage() / (enchantmentBookSettings.getLevel(item, CEnchantments.BLEED.getEnchantment()) * 1.05)), 40L, 20L));
            bleedTasks.add(Bukkit.getScheduler().runTaskTimer(plugin, () -> player.sendMessage("You are bleeding!"), 40L, 20L));
            bleedTasks.add(Bukkit.getScheduler().runTaskTimer(plugin, () -> damager.sendMessage("** BLEED **"), 40L, 20L));

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (BukkitTask task : bleedTasks) {
                    task.cancel();
                }
            }, 80L);
        }
        if (EnchantUtils.isEventActive(CEnchantments.DEVOUR, damager, item, enchantments)) {
            while (EnchantUtils.isEventActive(CEnchantments.BLEED, damager, item, enchantments)) {
                if (!(event.getEntity() instanceof Player player)) return;
                player.damage(event.getDamage() * (1 + ((double) CEnchantments.DEVOUR.getChance() / 10)));
                damager.sendMessage("** Devour - BLEED STACK **");
            }
        }
        if (EnchantUtils.isEventActive(CEnchantments.BLACKSMITH, damager, item, enchantments)) {
            ItemStack[] equipment = damager.getEquipment().getArmorContents();
            for (ItemStack armor : equipment) {
                if (armor == null) return;
                ItemMeta meta = armor.getItemMeta();
                Damageable damageable = (Damageable) meta;
                int modifier = damageable.getDamage() - (2 + enchant.getLevel("Blacksmith"));
                if (modifier < 0) return;
                damageable.setDamage(modifier);
                armor.setItemMeta(meta);
                damager.playSound((net.kyori.adventure.sound.Sound) Sound.BLOCK_CALCITE_BREAK);
            }
        }
        if (EnchantUtils.isEventActive(CEnchantments.ARROWBREAK, damager, item, enchantments)) {
            if (event.getDamageSource().getDamageType().equals(DamageType.ARROW)) {
                event.setCancelled(true);
                damager.playSound((net.kyori.adventure.sound.Sound) Sound.BLOCK_ANVIL_DESTROY);
            }
        }
        if (EnchantUtils.isEventActive(CEnchantments.DEEPBLEED, damager, item, enchantments)) {
            enchantmentBookSettings.createCooldown(CEnchantments.DEEPBLEED.getEnchantment(), item, damager.getUniqueId(), 500L, 1L);

            if (!(event.getEntity() instanceof Player player)) return;

            Particle.DustOptions dustOptions = new Particle.DustOptions(Color.RED, 5.0F);

            List<BukkitTask> bleedTasks = new ArrayList<>();

            bleedTasks.add(Bukkit.getScheduler().runTaskTimer(plugin, () -> player.spawnParticle(Particle.DUST, player.getLocation(), 12, dustOptions), 40L, 20L));
            bleedTasks.add(Bukkit.getScheduler().runTaskTimer(plugin, () -> player.damage(event.getDamage() / (enchantmentBookSettings.getLevel(item, CEnchantments.DEEPBLEED.getEnchantment()) * 1.75)), 40L, 20L));
            bleedTasks.add(Bukkit.getScheduler().runTaskTimer(plugin, () -> player.sendMessage("You are bleeding!"), 40L, 20L));
            bleedTasks.add(Bukkit.getScheduler().runTaskTimer(plugin, () -> damager.sendMessage("** BLEED **"), 40L, 20L));

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (BukkitTask task : bleedTasks) {
                    task.cancel();
                }
            }, 80L);
        }
        //Imperium
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (player.getKiller() == null) return;

        if (!this.pluginSupport.allowCombat(player.getLocation())) return;

        Player damager = player.getKiller();
        ItemStack item = this.methods.getItemInHand(damager);

        if (EnchantUtils.isEventActive(CEnchantments.DECAPITATION, damager, item, this.enchantmentBookSettings.getEnchantments(item))) {
            event.getDrops().add(new ItemBuilder().setMaterial(Material.PLAYER_HEAD).setPlayerName(player.getName()).build());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();

        if (killer == null) return;

        ItemStack item = this.methods.getItemInHand(killer);
        Map<CEnchantment, Integer> enchantments = this.enchantmentBookSettings.getEnchantments(item);
        Material headMat = EntityUtils.getHeadMaterial(event.getEntity());

        if (headMat != null && !EventUtils.containsDrop(event, headMat)) {
            double multiplier = this.crazyManager.getDecapitationHeadMap().getOrDefault(headMat, 0.0);

            if (multiplier != 0.0 && EnchantUtils.isEventActive(CEnchantments.DECAPITATION, killer, item, enchantments, multiplier)) {
                ItemStack head = new ItemBuilder().setMaterial(headMat).build();
                event.getDrops().add(head);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBookApply(BookApplyEvent event) {
        CEnchantment deepbleedEnchant = CEnchantments.DEEPBLEED.getEnchantment();
        enchantmentBookSettings.swapToHeroicEnchant(deepbleedEnchant, deepbleedEnchant.getOldEnchant(), event.getEnchantedItem());
    }

    private void removeBadPotions(Player player) {
        List<PotionEffectType> bad = new ArrayList<>() {{
            add(PotionEffectType.BLINDNESS);
            add(PotionEffectType.NAUSEA);
            add(PotionEffectType.HUNGER);
            add(PotionEffectType.POISON);
            add(PotionEffectType.SLOWNESS);
            add(PotionEffectType.MINING_FATIGUE);
            add(PotionEffectType.WEAKNESS);
            add(PotionEffectType.WITHER);
        }};

        bad.forEach(player::removePotionEffect);
    }

    public Enchant getEnchant() {
        return enchant;
    }

    public void setEnchant(Enchant enchant) {
        this.enchant = enchant;
    }
}