package com.badbones69.crazyenchantments.paper.enchantments;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.Methods;
import com.badbones69.crazyenchantments.paper.Starter;
import com.badbones69.crazyenchantments.paper.api.CrazyManager;
import com.badbones69.crazyenchantments.paper.api.enums.CEnchantments;
import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import com.badbones69.crazyenchantments.paper.api.builders.ItemBuilder;
import com.badbones69.crazyenchantments.paper.api.utils.EnchantUtils;
import com.badbones69.crazyenchantments.paper.api.utils.EntityUtils;
import com.badbones69.crazyenchantments.paper.api.utils.EventUtils;
import com.badbones69.crazyenchantments.paper.controllers.settings.EnchantmentBookSettings;
import com.badbones69.crazyenchantments.paper.support.PluginSupport;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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

    @NotNull
    private final BukkitScheduler scheduler = Bukkit.getScheduler();

    //Private fields that handles bleed damage amounts.
    private double bleedStack = 1;
    private double bleedCap;

    // Plugin Support.
    @NotNull
    private final PluginSupport pluginSupport = this.starter.getPluginSupport();

    private double getCurrentBleedStack() {
        return this.bleedStack;
    }

    private double getBleedCap() {
        return this.bleedCap;
    }

    private void setBleedStack(double value) {
        this.bleedStack = value;
    }

    /**
     *
     * @param data Uses this enum value to do some stuff
     * @param bleed The bleed damage amount
     * @param cap The soft-maximum that the bleed damage can reach.
     * @return The new bleed damage amount based on data provided.
     */
    private double handleBleedCap(@NotNull CEnchantments data, double bleed, double cap) {
        this.bleedStack = bleed;
        this.bleedCap = cap;
        try {
            if (bleed > cap) bleed = (cap * (1 + data.getChance()));
            this.starter.getLogger().warning("[DEBUG] Bleed cap exceeded! Implementing soft cap...");
            this.starter.getLogger().warning("New bleed stack: " + bleed);
            return this.bleedStack = bleed;
        } catch (NullPointerException exception) {
            plugin.getLogger().warning("Something has gone horribly wrong while setting the bleed cap!");
            plugin.getLogger().warning("Stacktrace: " + exception);
        }
        return this.bleedStack = bleed;
    }

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

        if (EnchantUtils.isEventActive(CEnchantments.DIZZY, damager, item, enchantments)) {
            int level = enchantmentBookSettings.getLevel(item, CEnchantments.DIZZY.getEnchantment());

            if (CEnchantments.DIZZY.isOffCooldown(damager.getUniqueId(), level, true)) {
                int duration = (level >= 3) ? 120 : (level == 2 ? 80 : 40);
                entity.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, duration, 0));
            }
        }

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
            double damageAmount = 1 + (double) damager.getExpToLevel() / 1500;
            double cap = (event.getDamage() / (reaperEnchant.getMaxLevel() - this.enchantmentBookSettings.getLevel(item, reaperEnchant)));
            if (damageAmount > cap) damageAmount = cap;
            event.setDamage(damageAmount);
        }
        if (EnchantUtils.isEventActive(CEnchantments.PUMMEL, damager, item, enchantments)) {
            if (!(event.getDamager() instanceof LivingEntity target)) return;
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 6, 1));
        }
        if (EnchantUtils.isEventActive(CEnchantments.CLEAVE, damager, item, enchantments)) {
            CEnchantment cleaveEnchant = CEnchantments.CLEAVE.getEnchantment();
            //Get the world the player is in.
            World world = damager.getWorld();

            if (this.pluginSupport.inClaim(damager)) return;

            int cleaveLvl = this.enchantmentBookSettings.getLevel(item, cleaveEnchant);

            //Build a new BoundingBox and then create an array containing all the entities in that box.
            BoundingBox region = damager.getBoundingBox();
            region = region.expand(3 + cleaveLvl);
            Collection<Entity> targets = world.getNearbyEntities(region);

            //Use a stream to target entities in the array based on set conditions
            targets.stream()
                    .filter(target -> target instanceof LivingEntity)
                    .filter(target -> !target.equals(damager))
                    .sorted((obj1, obj2) -> {
                        boolean isObj1Player = obj1 instanceof Player;
                        boolean isObj2Player = obj2 instanceof Player;
                        return Boolean.compare(!isObj1Player, !isObj2Player);
                    })
                    .limit(4 + cleaveLvl)
                    .map(target -> (LivingEntity) target)
                    .forEach(target -> {
                        double damage = (event.getDamage() - ((double) cleaveEnchant.getMaxLevel() / cleaveLvl));
                        if (damage <= 0) damage = 5;
                        target.damage(damage);
                    });
        }
        if (EnchantUtils.isEventActive(CEnchantments.CORRUPT, damager, item, enchantments)) {
            CEnchantment corruptEnchant = CEnchantments.CORRUPT.getEnchantment();
            damager.sendMessage("** CORRUPT **");
            if (!(event.getEntity() instanceof LivingEntity target)) return;
            double damageAmt = (event.getDamage() / (corruptEnchant.getMaxLevel() - this.enchantmentBookSettings.getLevel(item, corruptEnchant)));
            List<BukkitTask> runnables = new ArrayList<>();

            runnables.add(this.scheduler.runTaskTimer(plugin, () -> target.getWorld().spawnParticle(Particle.SMOKE, target.getLocation(), 10, 0, 2, 0), 0L, 5L));
            runnables.add(this.scheduler.runTaskTimer(plugin, () -> target.getWorld().spawnParticle(Particle.SMOKE, target.getLocation(), 10, 0, 1, 0), 0L, 5L));
            runnables.add(this.scheduler.runTaskTimer(plugin, () -> target.getWorld().spawnParticle(Particle.SMOKE, target.getLocation(), 10, 1, 0, 1), 0L, 5L));
            this.scheduler.runTaskLater(plugin, () -> target.damage(damageAmt), 20L);
            this.scheduler.runTaskLater(plugin, () -> target.damage(damageAmt), 40L);
            this.scheduler.runTaskLater(plugin, () -> target.damage(damageAmt), 60L);
            this.scheduler.runTaskLater(plugin, () -> target.damage(damageAmt), 80L);

            for (BukkitTask task : runnables) {
                if (target.isDead()) task.cancel();
            }
            this.scheduler.runTaskLater(plugin, () -> {
                for (BukkitTask task : runnables) task.cancel();
            }, 100L);
        }
        if (EnchantUtils.isEventActive(CEnchantments.INSANITY, damager, item, enchantments)) {
            if (!(event.getEntity() instanceof LivingEntity target)) return;
            ItemStack axe = target.getActiveItem();

            @NotNull Set<ItemStack> axes = Set.of(
                    ItemStack.of(Material.WOODEN_AXE),
                    ItemStack.of(Material.STONE_AXE),
                    ItemStack.of(Material.IRON_AXE),
                    ItemStack.of(Material.GOLDEN_AXE),
                    ItemStack.of(Material.DIAMOND_AXE),
                    ItemStack.of(Material.NETHERITE_AXE)
            );

            for (ItemStack selectedItem : axes) {
                if (selectedItem.equals(axe)) {
                    event.setDamage(event.getDamage() + (1 + (double) CEnchantments.INSANITY.getChance() / 100));
                }
            }
        }
        if (EnchantUtils.isEventActive(CEnchantments.BARBARIAN, damager, item, enchantments)) {
            CEnchantment barbarianEnchant = CEnchantments.BARBARIAN.getEnchantment();
            event.setDamage(event.getDamage() * (this.enchantmentBookSettings.getLevel(item, barbarianEnchant)));
        }
        if (EnchantUtils.isEventActive(CEnchantments.BLEED, damager, item, enchantments)) {
            CEnchantment bleedEnchant = CEnchantments.BLEED.getEnchantment();
            //Check if the target is a LivingEntity
            if (!(event.getEntity() instanceof LivingEntity player)) return;

            //Create a bleed stack
            double stack = (event.getDamage() / (bleedEnchant.getMaxLevel() - this.enchantmentBookSettings.getLevel(item, bleedEnchant)));
            double cap = bleedEnchant.getChance();
            this.bleedStack = handleBleedCap(CEnchantments.BLEED, stack, cap);

            //Particle builder
            Particle.DustOptions dustOptions = new Particle.DustOptions(Color.RED, 2.0F);
            World world = player.getWorld();
            final Location playerPos = player.getLocation();
            final Location offset = player.getLocation().offset(0, 2, 0).toLocation(world);
            final Location offset1 = player.getLocation().offset(0, 1, 0).toLocation(world);

            //Array that will store all tasks related to Bleed
            List<BukkitTask> bleedTasks = new ArrayList<>();

            //These tasks are stored and run
            bleedTasks.add(this.scheduler.runTaskTimer(plugin, () -> world.spawnParticle(Particle.DUST, playerPos, 12, dustOptions), 40L, 20L));
            bleedTasks.add(this.scheduler.runTaskTimer(plugin, () -> world.spawnParticle(Particle.DUST, offset, 12, dustOptions), 40L, 20L));
            bleedTasks.add(this.scheduler.runTaskTimer(plugin, () -> world.spawnParticle(Particle.DUST, offset1, 12, dustOptions), 40L, 20L));
            bleedTasks.add(this.scheduler.runTaskTimer(plugin, () -> player.damage(this.bleedStack), 40L, 20L));
            bleedTasks.add(this.scheduler.runTaskTimer(plugin, () -> player.sendMessage("You are bleeding!"), 40L, 20L));
            bleedTasks.add(this.scheduler.runTaskTimer(plugin, () -> damager.sendMessage("** BLEED **"), 40L, 20L));

            //Cancel the runnable if the player is dead
            this.scheduler.runTask(plugin, () -> {
                for (BukkitTask task : bleedTasks) {
                    if (player.isDead()) task.cancel();
                }
            });

            //Removes the tasks from the plugin after 80 ticks to avoid a memory leak
            this.scheduler.runTaskLater(plugin, () -> {
                for (BukkitTask task : bleedTasks) {
                    task.cancel();
                }
            }, 80L);
        }
        if (EnchantUtils.isEventActive(CEnchantments.DEVOUR, damager, item, enchantments)) {
            CEnchantment devourEnchant = CEnchantments.DEVOUR.getEnchantment();
            List<BukkitTask> devourTasks = new ArrayList<>();

            if (EnchantUtils.isEventActive(CEnchantments.BLEED, damager, item, enchantments)) {
                if (!(event.getEntity() instanceof LivingEntity player)) return;

                double devourStack = (event.getDamage() / (devourEnchant.getMaxLevel() - this.enchantmentBookSettings.getLevel(item, devourEnchant)));
                double cap = devourEnchant.getChance();
                this.bleedStack = handleBleedCap(CEnchantments.DEVOUR, devourStack, cap);

                devourTasks.add(this.scheduler.runTaskTimer(plugin, () -> player.damage(this.bleedStack), 40L, 20L));
                devourTasks.add(this.scheduler.runTaskTimer(plugin, () -> damager.sendMessage("** DEVOUR **"), 40L, 20L));

                event.setDamage(event.getDamage());

                for (BukkitTask task : devourTasks) {
                    if (player.isDead()) task.cancel();
                }

                this.scheduler.runTaskLater(plugin, () -> {
                    for (BukkitTask task : devourTasks) {
                        task.cancel();
                    }
                }, 80L);
            }
        }
        if (EnchantUtils.isEventActive(CEnchantments.BLACKSMITH, damager, item, enchantments)) {
            CEnchantment blacksmithEnchant = CEnchantments.BLACKSMITH.getEnchantment();
            ItemStack[] equipment = damager.getEquipment().getArmorContents();
            for (ItemStack armor : equipment) {
                if (armor == null) return;
                ItemMeta meta = armor.getItemMeta();
                Damageable damageable = (Damageable) meta;
                int modifier = damageable.getDamage() - (2 + this.enchantmentBookSettings.getLevel(item, blacksmithEnchant));
                if (modifier < 0) return;
                damageable.setDamage(modifier);
                armor.setItemMeta(meta);
                damager.playSound((net.kyori.adventure.sound.Sound) Sound.BLOCK_CALCITE_BREAK);
            }
        }
        //todo() this needs to match Bleed but I'm tired rn so lol
        if (EnchantUtils.isEventActive(CEnchantments.DEEPBLEED, damager, item, enchantments)) {
            //Literally the same thing as bleed but more damage
            CEnchantment deepbleedEnchant = CEnchantments.DEEPBLEED.getEnchantment();
            double stack = (event.getDamage() / (deepbleedEnchant.getMaxLevel() - this.enchantmentBookSettings.getLevel(item, deepbleedEnchant)));
            double cap = deepbleedEnchant.getChance();
            this.bleedStack = handleBleedCap(CEnchantments.DEEPBLEED, stack, cap);

            if (!(event.getEntity() instanceof LivingEntity player)) return;
            if (player.isDead()) return;
            Particle.DustOptions dustOptions = new Particle.DustOptions(Color.RED, 5.0F);
            List<BukkitTask> bleedTasks = new ArrayList<>();

            bleedTasks.add(this.scheduler.runTaskTimer(plugin, () -> player.getWorld().spawnParticle(Particle.DUST, player.getLocation(), 12, dustOptions), 40L, 20L));
            bleedTasks.add(this.scheduler.runTaskTimer(plugin, () -> player.damage(this.bleedStack), 40L, 20L));
            bleedTasks.add(this.scheduler.runTaskTimer(plugin, () -> player.sendMessage("You are bleeding!"), 40L, 20L));
            bleedTasks.add(this.scheduler.runTaskTimer(plugin, () -> damager.sendMessage("** BLEED **"), 40L, 20L));

            this.scheduler.runTaskLater(plugin, () -> {
                for (BukkitTask task : bleedTasks) {
                    task.cancel();
                }
            }, 80L);
        }
        //Imperium
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onArrowBreakTrigger(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player target)) return;
        if (!(event.getDamager() instanceof Arrow proj)) return;

        ItemStack item = this.methods.getItemInHand(target);

        final Map<CEnchantment, Integer> enchantments = this.enchantmentBookSettings.getEnchantments(item);
        Sound sound = Sound.BLOCK_ANVIL_DESTROY;

        if (EnchantUtils.isEventActive(CEnchantments.ARROWBREAK, target, item, enchantments)) {
            Vector power = proj.getVelocity().multiply(-1.5);
            Location tp = proj.getLocation().add(proj.getVelocity().normalize().multiply(0.1));
            power = power.setY(0.4);

            proj.teleport(tp);
            proj.setVelocity(power);
            proj.setFireTicks(0);
            proj.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);

            target.sendMessage("*** ARROW BREAK ***");
            target.playSound(target.getLocation(), sound, 1.0F, 2.0F);
        }
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
        if (EnchantUtils.isEventActive(CEnchantments.BLEED, killer, item, enchantments)) {
            CEnchantment bleedEnchant = CEnchantments.BLEED.getEnchantment();
            bleedEnchant.setActivated(false);
        }
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
}
