package com.badbones69.crazyenchantments.paper.enchantments;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.Methods;
import com.badbones69.crazyenchantments.paper.Starter;
import com.badbones69.crazyenchantments.paper.api.CrazyManager;
import com.badbones69.crazyenchantments.paper.api.FileManager;
import com.badbones69.crazyenchantments.paper.api.enums.CEnchantments;
import com.badbones69.crazyenchantments.paper.api.events.PreBookApplyEvent;
import com.badbones69.crazyenchantments.paper.api.objects.CEBook;
import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import com.badbones69.crazyenchantments.paper.api.builders.ItemBuilder;
import com.badbones69.crazyenchantments.paper.api.utils.EnchantUtils;
import com.badbones69.crazyenchantments.paper.api.utils.EntityUtils;
import com.badbones69.crazyenchantments.paper.api.utils.EventUtils;
import com.badbones69.crazyenchantments.paper.controllers.settings.EnchantmentBookSettings;
import com.badbones69.crazyenchantments.paper.support.PluginSupport;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.DoubleStream;

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

    @NotNull
    private final FileManager fileManager = this.starter.getFileManager();

    @NotNull
    private final FileManager.Files config = FileManager.Files.CONFIG;

    // Plugin Support.
    @NotNull
    private final PluginSupport pluginSupport = this.starter.getPluginSupport();

    //Local management
    @NotNull
    private HashMap<Player, HashMap<Block, BlockFace>> blocks = new HashMap<>();

    @NotNull
    private final Set<UUID> blacksmith = new HashSet<>();


    //todo() Change this to local variable to prevent overwriting
    private double bleedStack;

    private double bleedCap;

    private boolean allowDebug = this.fileManager.getFile(config).getBoolean("Settings.debug", false);

    @NotNull
    private final Set<Material> axes = Set.of(
            Material.WOODEN_AXE,
            Material.STONE_AXE,
            Material.IRON_AXE,
            Material.GOLDEN_AXE,
            Material.DIAMOND_AXE,
            Material.NETHERITE_AXE
    );

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
     * @param data The enum data value of the enchantment
     * @param bleed The bleed damage amount
     * @param cap The soft-maximum that the bleed damage can reach.
     * @return The new bleed damage amount based on data provided.
     */
    private double handleBleedCap(@NotNull CEnchantments data, double bleed, double cap) {
        this.bleedStack = bleed;
        this.bleedCap = cap;
        if (bleed > cap) bleed = Math.min(cap, cap + data.getChance() * 0.5);
        if (Double.isNaN(bleed)) bleed = cap;
        this.plugin.getLogger().warning("[DEBUG] Bleed cap exceeded! Implementing soft cap...");
        this.plugin.getLogger().warning("New bleed stack: " + bleed);
        return this.bleedStack = bleed;
    }

    /**
     * This function uses harsher calculations to define the bleed cap.
     * @param data The enum data value of the enchantment
     * @param item The item with the enchantment
     * @param bleed The bleed damage amount
     * @param cap The maximum damage that can be reached
     * @return The new bleed damage, as a double.
     */
    private double handleBleedCap(@NotNull CEnchantments data, @NotNull ItemStack item, double bleed, double cap) {
        this.bleedStack = bleed;
        this.bleedCap = cap;
        if (bleed > cap) bleed = cap + this.enchantmentBookSettings.getLevel(item, data.getEnchantment()) * 0.5;
        if (Double.isNaN(bleed)) bleed = cap;
        this.plugin.getLogger().warning("[DEBUG] Bleed cap exceeded! Implementing soft cap...");
        this.plugin.getLogger().warning("New bleed stack: " + bleed);
        return this.bleedStack = bleed;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (EventUtils.isIgnoredEvent(event)) return;
        if (this.pluginSupport.isFriendly(event.getDamager(), event.getEntity())) return;

        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (!(event.getDamager() instanceof Player damager)) return;

        ItemStack item = this.methods.getItemInHand(damager);

        Map<CEnchantment, Integer> enchantments = this.enchantmentBookSettings.getEnchantments(item);

        if (EnchantUtils.isEventActive(CEnchantments.BERSERK, damager, item, enchantments)) {
            int berserkLevel = enchantmentBookSettings.getLevel(item, CEnchantments.BERSERK.getEnchantment());
            int amplifier  = (berserkLevel >= 4) ? 1 : 0;

            if (CEnchantments.BERSERK.isOffCooldown(damager.getUniqueId(), berserkLevel, true)) {
                damager.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 20 + (enchantments.get(CEnchantments.BERSERK.getEnchantment())) * 20, amplifier));
                damager.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20 + (enchantments.get(CEnchantments.BERSERK.getEnchantment())) * 20, amplifier));
            }
        }

        if (EnchantUtils.isEventActive(CEnchantments.BLESSED, damager, item, enchantments)) removeBadPotions(damager);

        if (EnchantUtils.isEventActive(CEnchantments.FAMISHED, damager, item, enchantments)&& damager.getFoodLevel() < 20) {
            int FamishedLevel = enchantmentBookSettings.getLevel(item, CEnchantments.FAMISHED.getEnchantment());
            int food = 2 * enchantments.get(CEnchantments.FAMISHED.getEnchantment());

            if (CEnchantments.FAMISHED.isOffCooldown(damager.getUniqueId(), FamishedLevel, true)) {
                if (damager.getFoodLevel() + food < 20) damager.setFoodLevel((int) (damager.getSaturation() + food));

                if (damager.getFoodLevel() + food > 20) damager.setFoodLevel(20);
            }
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
            double damageAmount = event.getDamage() + (double) damager.getExpToLevel() / 2500;
            double cap = Math.min(event.getDamage(), reaperEnchant.getChance());
            if (damageAmount > cap) damageAmount = cap;
            if (damageAmount == 0) return;
            entity.damage(event.getDamage() + damageAmount);

            if (allowDebug) {
                if (damageAmount == cap) damager.sendMessage("Reaper capped: " + cap);
                damager.sendMessage("Reaper damage: " + (damageAmount));
            }
        }
        if (EnchantUtils.isEventActive(CEnchantments.PUMMEL, damager, item, enchantments)) {
            if (!(event.getDamager() instanceof LivingEntity target)) return;
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 6, 1));
        }
        if (EnchantUtils.isEventActive(CEnchantments.CLEAVE, damager, item, enchantments)) {
            CEnchantment cleaveEnchant = CEnchantments.CLEAVE.getEnchantment();
            //Get the world the player is in.
            World world = damager.getWorld();

            int cleaveLvl = this.enchantmentBookSettings.getLevel(item, cleaveEnchant);

            //Build a new BoundingBox and then create an array containing all the entities in that box.
            BoundingBox region = damager.getBoundingBox();
            region = region.expand(3 + cleaveLvl);
            Collection<Entity> targets = world.getNearbyEntities(region);

            //Use a stream to target entities in the array based on set conditions
            targets.stream()
                    .filter(target -> target instanceof LivingEntity)
                    .filter(target -> !target.equals(damager))
                    .filter(target -> !target.equals(entity))
                    .limit(4 + cleaveLvl)
                    .map(target -> (LivingEntity) target)
                    .forEach(target -> {
                        double damage = (event.getDamage() - ((double) cleaveEnchant.getMaxLevel() / cleaveLvl));
                        if (damage <= 0) damage = 5;
                        target.damage(damage);
                        if (allowDebug) {
                            this.plugin.getLogger().info("Cleave activated! Damage output: " + damage);
                            damager.sendMessage("Cleave damage: " + damage);
                        }
                    });
        }
        if (EnchantUtils.isEventActive(CEnchantments.INSANITY, damager, item, enchantments)) {
            if (!CEnchantments.INSANITY.isOffCooldown(damager.getUniqueId(), (enchantments.get(CEnchantments.INSANITY.getEnchantment())), true)) return;
            CEnchantment insanityEnchant = CEnchantments.INSANITY.getEnchantment();
            if (!(event.getEntity() instanceof Player target)) return;
            ItemStack axe = this.methods.getItemInHand(target);

            if (this.axes.contains(axe.getType())) {
                event.setDamage(event.getDamage() * (1 + 0.2 * this.enchantmentBookSettings.getLevel(item, insanityEnchant)));
                damager.sendMessage("* INSANITY *");
            }
        }
        if (EnchantUtils.isEventActive(CEnchantments.BARBARIAN, damager, item, enchantments)) {
            if (!CEnchantments.BARBARIAN.isOffCooldown(damager.getUniqueId(), (enchantments.get(CEnchantments.BARBARIAN.getEnchantment())), true)) return;
            CEnchantment barbarianEnchant = CEnchantments.BARBARIAN.getEnchantment();
            int level = this.enchantmentBookSettings.getLevel(item, barbarianEnchant);
            int barbarianDamage = 5;
            event.setDamage(event.getDamage() + ThreadLocalRandom.current().nextInt((level * barbarianDamage) + 1));
        }
        if (EnchantUtils.isEventActive(CEnchantments.BLACKSMITH, damager, item, enchantments)) {
            if (!CEnchantments.BLACKSMITH.isOffCooldown(damager.getUniqueId(), (enchantments.get(CEnchantments.BLACKSMITH.getEnchantment())), true)) return;
            CEnchantment blacksmithEnchant = CEnchantments.BLACKSMITH.getEnchantment();
            ItemStack[] equipment = damager.getEquipment().getArmorContents();
            Sound sound = Sound.BLOCK_CALCITE_BREAK;
            for (ItemStack armor : equipment) {
                if (armor == null) continue;
                ItemMeta meta = armor.getItemMeta();
                Damageable damageable = (Damageable) meta;
                int modifier = damageable.getDamage() - (2 + this.enchantmentBookSettings.getLevel(item, blacksmithEnchant));
                if (modifier < 0) return;
                damageable.setDamage(modifier);
                armor.setItemMeta(meta);
                damager.playSound(damager.getLocation(), sound, 1.0F, 2.0F);
                damager.sendMessage("** BLACKSMITH **");

                blacksmith.add(damager.getUniqueId());
                if (blacksmith.contains(damager.getUniqueId())) {
                    event.setDamage(event.getDamage() / 2.0);
                    blacksmith.remove(damager.getUniqueId());
                }
            }
        }
        if (EnchantUtils.isEventActive(CEnchantments.HEX, damager, item, enchantments)) {
            CEnchantment hexEnchant = CEnchantments.HEX.getEnchantment();

            int level = this.enchantmentBookSettings.getLevel(item, hexEnchant);
            final int[] interval = {0};
            final int cap = (level) * 20;

            DoubleStream random = new Random().doubles(3, 7);
            double num = random.findAny().getAsDouble();
            if (Double.isNaN(num)) num = 3;
            final double value = num;

            final ScheduledTask[] task = new ScheduledTask[1];
            task[0] = entity.getScheduler().runAtFixedRate(plugin, object -> {
                damager.sendMessage("** HEX **");
                entity.sendMessage("Enemy Hex active");
                interval[0]++;
                entity.setLastDamage(entity.getLastDamage() - value);
                entity.damage(value);

                if (interval[0] >= cap) {
                    entity.sendMessage("Enemy's Hex has worn off.");
                    task[0].cancel();
                    interval[0] = 0;
                }
            }, () ->  damager.sendMessage("Target is dead. Retiring task..."), 0L, level);

        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void bleedHandler(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        ItemStack item = this.methods.getItemInHand(damager);
        final Map<CEnchantment, Integer> enchantments = this.enchantmentBookSettings.getEnchantments(item);

        if (EnchantUtils.isEventActive(CEnchantments.DEVOUR, damager, item, enchantments)) {
            if (!CEnchantments.DEVOUR.isOffCooldown(damager.getUniqueId(), (enchantments.get(CEnchantments.DEVOUR.getEnchantment())), true)) return;
            CEnchantment devourEnchant = CEnchantments.DEVOUR.getEnchantment();
            int level = this.enchantmentBookSettings.getLevel(item, devourEnchant);
            int amplifier = 1;
            int duration = 40;

            if (!EnchantUtils.isEventActive(CEnchantments.BLEED, damager, item, enchantments)) return;
            damager.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration, amplifier));
            damager.sendMessage("§cDevour activated! You feel a surge of power.");
        }
        if (EnchantUtils.isEventActive(CEnchantments.BLEED, damager, item, enchantments)) {
            CEnchantment bleedEnchant = CEnchantments.BLEED.getEnchantment();
            double level = this.enchantmentBookSettings.getLevel(item, bleedEnchant);

            //Create a bleed stack
            double stack = (event.getDamage() / (bleedEnchant.getMaxLevel() - level));
            double cap = bleedEnchant.getChanceIncrease() + level;
            this.bleedStack = handleBleedCap(CEnchantments.BLEED, stack, cap);

            //Particle builder
            Particle.DustOptions dustOptions = new Particle.DustOptions(Color.RED, 2.0F);
            World world = target.getWorld();
            final Location playerPos = target.getLocation();
            final Location offset = target.getLocation().offset(0, 2, 0).toLocation(world);
            final Location offset1 = target.getLocation().offset(0, 1, 0).toLocation(world);

            //Array that will store all tasks related to Bleed
            List<BukkitTask> bleedTasks = new ArrayList<>();

            //These tasks are stored and run
            bleedTasks.add(this.scheduler.runTaskTimer(plugin, () -> world.spawnParticle(Particle.DUST, playerPos, 12, dustOptions), 40L, 20L));
            bleedTasks.add(this.scheduler.runTaskTimer(plugin, () -> world.spawnParticle(Particle.DUST, offset, 12, dustOptions), 40L, 20L));
            bleedTasks.add(this.scheduler.runTaskTimer(plugin, () -> world.spawnParticle(Particle.DUST, offset1, 12, dustOptions), 40L, 20L));
            bleedTasks.add(this.scheduler.runTaskTimer(plugin, () -> target.damage(this.bleedStack), 40L, 20L));
            bleedTasks.add(this.scheduler.runTaskTimer(plugin, () -> target.sendMessage("You are bleeding!"), 40L, 20L));
            bleedTasks.add(this.scheduler.runTaskTimer(plugin, () -> damager.sendMessage("** BLEED **"), 40L, 20L));

            //Cancel the runnable if the target is dead
            bleedTasks.add(this.scheduler.runTaskTimer(plugin, () -> {
                for (BukkitTask task : bleedTasks) {
                    if (target.isDead()) task.cancel();
                }
            }, 1L, 20L));

            //Removes the tasks from the plugin after 80 ticks to avoid a memory leak
            this.scheduler.runTaskLater(plugin, () -> {
                for (BukkitTask task : bleedTasks) {
                    task.cancel();
                }
            }, 80L);
        }
        if (EnchantUtils.isEventActive(CEnchantments.DEEPBLEED, damager, item, enchantments)) {
            CEnchantment deepbleedEnchant = CEnchantments.DEEPBLEED.getEnchantment();
            double level = this.enchantmentBookSettings.getLevel(item, deepbleedEnchant);

            double stack = (event.getDamage() / (level));
            int cap = deepbleedEnchant.getChance() / deepbleedEnchant.getMaxLevel();
            this.bleedStack = handleBleedCap(CEnchantments.DEEPBLEED, item, stack, cap);

            Particle.DustOptions dustOptions = new Particle.DustOptions(Color.BLACK, 2.0F);
            World world = target.getWorld();
            final Location location = target.getLocation();
            final Location offset0 = location.offset(0, 2, 0).toLocation(world);
            final Location offset1 = location.offset(0, 1, 0).toLocation(world);

            ScheduledTask[] tasks = new ScheduledTask[6];
            tasks[0] = target.getScheduler().runAtFixedRate(plugin, obj -> world.spawnParticle(Particle.DUST, offset0, 15, dustOptions), null, 40L, 20L);
            tasks[1] = target.getScheduler().runAtFixedRate(plugin, obj -> world.spawnParticle(Particle.DUST, offset1, 15, dustOptions), null, 40L, 20L);
            tasks[2] = target.getScheduler().runAtFixedRate(plugin, obj -> world.spawnParticle(Particle.DUST, location, 15, dustOptions), null, 40L, 20L);
            tasks[3] = target.getScheduler().runAtFixedRate(plugin, obj -> target.damage(this.bleedStack + 1), null, 40L, 20L);
            tasks[4] = target.getScheduler().runAtFixedRate(plugin, obj -> {
                target.sendMessage("You are bleeding!");
                damager.sendMessage("*** DEEP BLEED ***");
            }, null, 40L, 20L);

            tasks[5] = target.getScheduler().runDelayed(plugin, cancelable -> {
                for (ScheduledTask task : tasks) task.cancel();
            }, null, 100L);
        }
        if (EnchantUtils.isEventActive(CEnchantments.CORRUPT, damager, item, enchantments)) {
            CEnchantment corruptEnchant = CEnchantments.CORRUPT.getEnchantment();
            double level = this.enchantmentBookSettings.getLevel(item, corruptEnchant);
            double cap = corruptEnchant.getChanceIncrease() + level;
            double damageAmt = Math.min(cap, (event.getDamage() / (4 - level)));
            List<BukkitTask> runnables = new ArrayList<>();

            runnables.add(this.scheduler.runTaskTimer(plugin, () -> target.getWorld().spawnParticle(Particle.SMOKE, target.getLocation(), 10, 0, 2, 0), 0L, 5L));
            runnables.add(this.scheduler.runTaskTimer(plugin, () -> target.getWorld().spawnParticle(Particle.SMOKE, target.getLocation(), 10, 0, 1, 0), 0L, 5L));
            runnables.add(this.scheduler.runTaskTimer(plugin, () -> target.getWorld().spawnParticle(Particle.SMOKE, target.getLocation(), 10, 1, 0, 1), 0L, 5L));
            runnables.add(this.scheduler.runTaskTimer(plugin, () -> damager.sendMessage("** CORRUPT **"), 20L, 20L));
            runnables.add(this.scheduler.runTaskTimer(plugin, () -> target.damage(damageAmt), 20L, 20L));

            damager.sendMessage("Corrupt damage: " + damageAmt);

            runnables.add(this.scheduler.runTaskTimer(plugin, () -> {
                for (BukkitTask task : runnables) {
                    if (target.isDead()) task.cancel();
                }
            }, 1L, 20L));
            this.scheduler.runTaskLater(plugin, () -> {
                for (BukkitTask task : runnables) task.cancel();
            }, 100L);
        }
        //if (allowDebug) damager.sendMessage("Base damage: " + event.getDamage());
    }

    @EventHandler()
    public void durabilityHandler(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        ItemStack item = this.methods.getItemInHand(damager);
        final Map<CEnchantment, Integer> enchantments = this.enchantmentBookSettings.getEnchantments(item);

        if (EnchantUtils.isEventActive(CEnchantments.BLACKSMITH, damager, item, enchantments)) {
            CEnchantment blacksmithEnchant = CEnchantments.BLACKSMITH.getEnchantment();
            ItemStack[] equipment = damager.getEquipment().getArmorContents();
            Sound sound = Sound.BLOCK_CALCITE_BREAK;
            for (ItemStack armor : equipment) {
                if (armor == null) return;
                ItemMeta meta = armor.getItemMeta();
                Damageable damageable = (Damageable) meta;
                int modifier = damageable.getDamage() - (2 + this.enchantmentBookSettings.getLevel(item, blacksmithEnchant));
                if (modifier < 0) return;
                damageable.setDamage(modifier);
                armor.setItemMeta(meta);
                damager.playSound(damager.getLocation(), sound, 1.0F, 2.0F);
            }
        }
        if (EnchantUtils.isEventActive(CEnchantments.DEMONFORGED, damager, item, enchantments) && target instanceof Player player) {

            ItemStack armorItem = switch (this.methods.percentPick(4, 0)) {
                case 1 -> player.getEquipment().getHelmet();
                case 2 -> player.getEquipment().getChestplate();
                case 3 -> player.getEquipment().getLeggings();
                default -> player.getEquipment().getBoots();
            };

            this.methods.removeDurability(armorItem, player);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onArrowBreakTrigger(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player target)) return;
        if (!(event.getDamager() instanceof Arrow proj)) return;

        ItemStack item = this.methods.getItemInHand(target);

        final Map<CEnchantment, Integer> enchantments = this.enchantmentBookSettings.getEnchantments(item);
        Sound sound = Sound.ITEM_SHIELD_BLOCK;

        if (EnchantUtils.isEventActive(CEnchantments.ARROWBREAK, target, item, enchantments)) {
            event.setCancelled(true);
            Vector power = proj.getVelocity().normalize().multiply(-1.5).setY(0.4);
            Location tp = proj.getLocation().add(proj.getVelocity().normalize().multiply(0.5));

            boolean success = proj.teleport(tp);
            if (!success) target.sendMessage("Something went wrong while trying to handle the projectile!");
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
    }

    @ApiStatus.Experimental()
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        final ItemStack axe = player.getInventory().getItemInMainHand();
        CEnchantment timberEnchant = CEnchantments.TIMBER.getEnchantment();

        final Map<CEnchantment, Integer> enchants = this.enchantmentBookSettings.getEnchantments(axe);

        if (!EnchantUtils.isEventActive(CEnchantments.TIMBER, player, axe, enchants)) return;

        if (!this.enchantmentBookSettings.hasEnchantment(axe.getItemMeta(), timberEnchant)) return;
        if (!this.axes.contains(axe)) return;

        final Block initialBlock = event.getBlock();
        final Location position = initialBlock.getLocation();
        final HashSet<Block> targets = getSurroundingBlocks(position);
        final Set<Block> allblocks = reachMoreBlocks(
                position,
                this.blocks.get(player).get(initialBlock),
                this.enchantmentBookSettings.getLevel(axe, timberEnchant)
        );

        //Using the Blast boolean cause im lazy AF and cant be bothered to make another one
        boolean damage = FileManager.Files.CONFIG.getFile().getBoolean(
                "Settings.EnchantmentOptions.Blast-Full-Durability"
        );

        Collections.addAll(Arrays.asList(allblocks.toArray()), targets);
        this.blocks.remove(player);

        final Sound sound = Sound.BLOCK_WOOD_BREAK;

        for (Block block: allblocks) {
            if (block.isEmpty()) continue;
            block.breakNaturally(axe, true, true);
            player.playSound(player, sound, 1.0F, 2.0F);
            if (damage) this.methods.removeDurability(axe, player);
        }
        if (!damage) this.methods.removeDurability(axe, player);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBookApply(PreBookApplyEvent event) {
        CEBook book = event.getCEBook();

        if (!event.getSuccessful()) return;

        if (book.getEnchantment().equals(CEnchantments.DEEPBLEED.getEnchantment())) {
            this.enchantmentBookSettings.swapToHeroicEnchant(CEnchantments.DEEPBLEED, event.getEnchantedItem(), event.getPlayer());
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
    @NotNull private HashSet<Block> getSurroundingBlocks(Location area) {
        HashSet<Block> adj = new HashSet<>();
        adj.add(area.clone().add(0, 1, 0).getBlock());
        adj.add(area.clone().add(0, -1, 0).getBlock());
        adj.add(area.clone().add(1, 0, 1).getBlock());
        adj.add(area.clone().add(-1, 0, 0).getBlock());
        adj.add(area.clone().add(0, 0, 1).getBlock());
        adj.add(area.clone().add(0, 0, -1).getBlock());

        return adj;
    }

    @NotNull private HashSet<Block> reachMoreBlocks(Location area, BlockFace region, Integer depth) {
        Location secondary = area.clone();

        switch (region) {
            case UP -> {
                area.add(-1, -depth, -1);
                secondary.add(-1, 0, -1);
            }
            case DOWN -> {
                area.add(1, depth, 1);
                secondary.add(-1, 0, -1);
            }
            case EAST -> {
                area.add(-depth, 1, 1);
                secondary.add(0, -1, -1);
            }
            case WEST -> {
                area.add(depth, 1, -1);
                secondary.add(0, -1, 1);
            }
            case NORTH -> {
                area.add(1, 1, depth);
                secondary.add(-1, -1, 0);
            }
            case SOUTH -> {
                area.add(-1, 1, -depth);
                secondary.add(0, -1, 1);
            }
            case SELF -> {
                area.add(-depth, 0, depth);
                secondary.add(0, 0 ,0);
            }
            case NORTH_EAST -> {

            }
            case NORTH_WEST -> {

            }
            case SOUTH_EAST -> {

            }
            case SOUTH_WEST -> {

            }
            case null, default -> {}
        }

        return this.methods.getEnchantBlocks(area, secondary);
    }
}
