package com.badbones69.crazyenchantments.paper.enchantments;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.Methods;
import com.badbones69.crazyenchantments.paper.Starter;
import com.badbones69.crazyenchantments.paper.api.CrazyManager;
import com.badbones69.crazyenchantments.paper.api.enums.CEnchantments;
import com.badbones69.crazyenchantments.paper.api.enums.pdc.DataKeys;
import com.badbones69.crazyenchantments.paper.api.events.AuraActiveEvent;
import com.badbones69.crazyenchantments.paper.api.events.PreBookApplyEvent;
import com.badbones69.crazyenchantments.paper.api.managers.ArmorEnchantmentManager;
import com.badbones69.crazyenchantments.paper.api.objects.ArmorEnchantment;
import com.badbones69.crazyenchantments.paper.api.objects.CEBook;
import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import com.badbones69.crazyenchantments.paper.api.objects.PotionEffects;
import com.badbones69.crazyenchantments.paper.api.utils.EnchantUtils;
import com.badbones69.crazyenchantments.paper.api.utils.EventUtils;
import com.badbones69.crazyenchantments.paper.controllers.settings.EnchantmentBookSettings;
import com.badbones69.crazyenchantments.paper.controllers.settings.ProtectionCrystalSettings;
import com.badbones69.crazyenchantments.paper.support.PluginSupport;
import com.badbones69.crazyenchantments.paper.tasks.processors.ArmorProcessor;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class ArmorEnchantments implements Listener {

    @NotNull
    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    @NotNull
    private final Starter starter = this.plugin.getStarter();

    @NotNull
    private final Methods methods = this.starter.getMethods();

    @NotNull
    private final CrazyManager crazyManager = this.starter.getCrazyManager();

    @NotNull
    private final BukkitScheduler scheduler = Bukkit.getScheduler();

    // Settings.
    @NotNull
    private final ProtectionCrystalSettings protectionCrystalSettings = this.starter.getProtectionCrystalSettings();

    @NotNull
    private final EnchantmentBookSettings enchantmentBookSettings = this.starter.getEnchantmentBookSettings();

    // Plugin Support.
    @NotNull
    private final PluginSupport pluginSupport = this.starter.getPluginSupport();

    // Plugin Managers.
    @NotNull
    private final ArmorEnchantmentManager armorEnchantmentManager = this.starter.getArmorEnchantmentManager();

    private final ArmorProcessor armorProcessor = new ArmorProcessor();

    private final List<UUID> fallenPlayers = new ArrayList<>();

    public ArmorEnchantments() {
        armorProcessor.start();
    }

    public void stop() {
        armorProcessor.stop();
    }

    @EventHandler
    public void onDeath(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack air = new ItemStack(Material.AIR);

        player.getScheduler().runDelayed(this.plugin, playerTask -> newUpdateEffects(player, air, air), null, 10);
    }

    @EventHandler
    public void onEquip(PlayerArmorChangeEvent event) {
        NamespacedKey key = DataKeys.enchantments.getNamespacedKey();
        Player player = event.getPlayer();
        ItemStack newItem = event.getNewItem();
        ItemStack oldItem = event.getOldItem();
        boolean oldHasMeta = oldItem.hasItemMeta();
        boolean newHasMeta = newItem.hasItemMeta();

        // Return if no enchants would effect the player with the change.
        if ((!newHasMeta || !newItem.getItemMeta().getPersistentDataContainer().has(key))
             && (!oldHasMeta || !oldItem.getItemMeta().getPersistentDataContainer().has(key))) return;

        // Added to prevent armor change event being called on damage.
        if (newHasMeta && oldHasMeta
            && Objects.equals(newItem.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING),
                              oldItem.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING))) return;

        newUpdateEffects(player, newItem, oldItem);
    }

    /**
     * Checks the players current armor and updates any needed effects that are created by CrazyEnchantments.
     * Removes all effects that should no longer be on the player and adds the highest level for the others
     * based on their armor.
     * @param player The player for whom to update effects.
     * @param newItem The new item equipped.
     * @param oldItem The item that had previously been equipped.
     */
    private void newUpdateEffects(@NotNull Player player, @NotNull ItemStack newItem, @NotNull ItemStack oldItem) {
        Map<CEnchantment, Integer> topEnchants = currentEnchantsOnPlayerAdded(player, newItem);

        // Remove all effects that they no longer should have from the armor.
        if (!oldItem.isEmpty()) {
            getTopPotionEffects(this.enchantmentBookSettings.getEnchantments(oldItem)
                    .entrySet().stream()
                    .filter(enchant -> !topEnchants.containsKey(enchant.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b)))
                    .keySet()
                    .forEach(player::removePotionEffect);
        }

        // Add all new effects that said player should now have.
        for (Map.Entry<PotionEffectType, Integer> effect : getTopPotionEffects(topEnchants).entrySet()) {
            for (PotionEffect currentEffect : player.getActivePotionEffects()) {
                if (!currentEffect.getType().equals(effect.getKey())) continue;
                if (currentEffect.getAmplifier() >= effect.getValue() - 1) break;

                player.removePotionEffect(effect.getKey());
                break;
            }
            player.addPotionEffect(new PotionEffect(effect.getKey(), -1, effect.getValue() - 1));
        }
    }

    /**
     * Pulls the data off of all of the enchantments provided and filters out the worst ones.
     * @param topEnchants A list of {@link CEnchantment}'s to filter.
     * @return Returns a list of top potion effects from the provided list of enchantments.
     */
    @NotNull
    private Map<PotionEffectType, Integer> getTopPotionEffects(@NotNull Map<CEnchantment, Integer> topEnchants) {
        Map<CEnchantments, HashMap<PotionEffectType, Integer>> enchantmentPotions = this.crazyManager.getEnchantmentPotions();
        HashMap<PotionEffectType, Integer> topPotions = new HashMap<>();

        topEnchants.forEach((key, value) -> enchantmentPotions.entrySet()
                .stream().filter(enchantedPotion -> enchantedPotion.getKey().getEnchantment().equals(key))
                .forEach(enchantedPotion -> enchantedPotion.getValue().entrySet().stream()
                        .filter(pot -> !topPotions.containsKey(pot.getKey()) || (topPotions.get(pot.getKey()) != -1 && topPotions.get(pot.getKey()) <= pot.getValue()))
                        .forEach(pot -> topPotions.put(pot.getKey(), value))));

        return topPotions;
    }

    /**
     *
     * @param player The player to check.
     * @param newItem The equipped item.
     * @return Returns a map of all current active enchants on the specified player.
     */
    @NotNull
    private HashMap<CEnchantment, Integer> currentEnchantsOnPlayerAdded(@NotNull Player player, @NotNull ItemStack newItem) {
        HashMap<CEnchantment, Integer> toAdd = getTopEnchantsOnPlayer(player);

        if (!newItem.isEmpty()) {
            this.enchantmentBookSettings.getEnchantments(newItem).entrySet().stream()
                    .filter(ench -> !toAdd.containsKey(ench.getKey()) || toAdd.get(ench.getKey()) <= ench.getValue())
                    .filter(ench -> EnchantUtils.isArmorEventActive(player, CEnchantments.valueOf(ench.getKey().getName().toUpperCase()), newItem))
                    .forEach(ench -> toAdd.put(ench.getKey(), ench.getValue()));
        }

        return toAdd;
    }

    /**
     *
     * @param player The player to check for {@link CEnchantments}.
     * @return A list of {@link CEnchantments}'s on the player.
     */
    @NotNull
    private HashMap<CEnchantment, Integer> getTopEnchantsOnPlayer(@NotNull Player player) {
        HashMap<CEnchantment, Integer> topEnchants = new HashMap<>();

        Arrays.stream(player.getEquipment().getArmorContents())
                .map(this.enchantmentBookSettings::getEnchantments)
                .forEach(enchantments -> enchantments.entrySet().stream()
                        .filter(ench -> !topEnchants.containsKey(ench.getKey()) || topEnchants.get(ench.getKey()) <= ench.getValue())
                        .forEach(ench -> topEnchants.put(ench.getKey(), ench.getValue())));

        return topEnchants;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void potionHandler(EntityDamageByEntityEvent event) {
        if (EventUtils.isIgnoredEvent(event) || EventUtils.isIgnoredUUID(event.getDamager().getUniqueId())) return;
        if (this.pluginSupport.isFriendly(event.getDamager(), event.getEntity())) return;

        if (!(event.getDamager() instanceof LivingEntity damager) || !(event.getEntity() instanceof Player player)) return;

        for (ItemStack armor : player.getEquipment().getArmorContents()) {
            Map<CEnchantment, Integer> enchants = this.enchantmentBookSettings.getEnchantments(armor);
            if (enchants.isEmpty()) continue;

            for (ArmorEnchantment armorEnchantment : this.armorEnchantmentManager.getArmorEnchantments()) {
                CEnchantments enchantment = armorEnchantment.getEnchantment();

                if (EnchantUtils.isEventActive(enchantment, player, armor, enchants)) {

                    if (armorEnchantment.isPotionEnchantment()) {
                        for (PotionEffects effect : armorEnchantment.getPotionEffects()) {
                            damager.addPotionEffect(new PotionEffect(effect.potionEffect(), effect.duration(), (armorEnchantment.isLevelAddedToAmplifier() ? enchants.get(enchantment.getEnchantment()) : 0) + effect.amplifier()));
                        }
                    } else {
                        event.setDamage(event.getDamage() * ((armorEnchantment.isLevelAddedToAmplifier() ? enchants.get(enchantment.getEnchantment()) : 0) + armorEnchantment.getDamageAmplifier()));
                    }
                }
            }

            if (player.getHealth() <= 4 && EnchantUtils.isEventActive(CEnchantments.ADRENALINE, player, armor, enchants)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 3 + (enchants.get(CEnchantments.ADRENALINE.getEnchantment())) * 20, 10));
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 3 + (enchants.get(CEnchantments.ADRENALINE.getEnchantment())) * 20, 3));
            }
            if (EnchantUtils.isEventActive(CEnchantments.POISONED, player, armor, enchants)) {
                if (!(damager instanceof Player target)) return;
                int duration = CEnchantments.POISONED.getChance() / 8;
                PotionEffect poison = new PotionEffect(PotionEffectType.POISON, duration, 2, true, false, true);
                target.addPotionEffect(poison);
            }
            if (EnchantUtils.isEventActive(CEnchantments.JUDGEMENT, player, armor, enchants)) {
                CEnchantment judgementEnchant = CEnchantments.JUDGEMENT.getEnchantment();
                if (!damager.hasPotionEffect(PotionEffectType.POISON)) damager.addPotionEffect(new PotionEffect(PotionEffectType.POISON, CEnchantments.JUDGEMENT.getChance(), this.enchantmentBookSettings.getLevel(armor, judgementEnchant)));
                if (!player.hasPotionEffect(PotionEffectType.REGENERATION)) player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, CEnchantments.JUDGEMENT.getChance(), this.enchantmentBookSettings.getLevel(armor, judgementEnchant)));
            }
            if (EnchantUtils.isEventActive(CEnchantments.CURSE, player, armor, enchants)) {
                CEnchantment curseEnchant = CEnchantments.CURSE.getEnchantment();
                int level = this.enchantmentBookSettings.getLevel(armor, curseEnchant);
                if (player.getHealth() < 6) {
                    if (!player.hasPotionEffect(PotionEffectType.STRENGTH)) player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, CEnchantments.CURSE.getChance(), level - 1));
                    if (!player.hasPotionEffect(PotionEffectType.RESISTANCE)) player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, CEnchantments.CURSE.getChance(), level - 1));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, level));
                }
            }
            if (EnchantUtils.isEventActive(CEnchantments.CLARITY, player, armor, enchants)) {
                if (player.hasPotionEffect(PotionEffectType.BLINDNESS)) player.removePotionEffect(PotionEffectType.BLINDNESS);
            }
        }

    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void healthHandler(EntityDamageByEntityEvent event) {
        if (EventUtils.isIgnoredEvent(event) || EventUtils.isIgnoredUUID(event.getDamager().getUniqueId())) return;
        if (this.pluginSupport.isFriendly(event.getDamager(), event.getEntity())) return;

        if (!(event.getDamager() instanceof LivingEntity damager) || !(event.getEntity() instanceof Player player)) return;

        final @Nullable AttributeInstance maxhealth = player.getAttribute(Attribute.MAX_HEALTH);
        double maxhealthdouble = maxhealth.getValue();

        for (ItemStack armor : player.getEquipment().getArmorContents()) {
            Map<CEnchantment, Integer> enchants = this.enchantmentBookSettings.getEnchantments(armor);
            if (enchants.isEmpty()) continue;
            if (player.isDead()) continue;

            for (ArmorEnchantment armorEnchantment : this.armorEnchantmentManager.getArmorEnchantments()) {
                CEnchantments enchantment = armorEnchantment.getEnchantment();

                if (EnchantUtils.isEventActive(enchantment, player, armor, enchants)) {

                    if (armorEnchantment.isPotionEnchantment()) {
                        for (PotionEffects effect : armorEnchantment.getPotionEffects()) {
                            damager.addPotionEffect(new PotionEffect(effect.potionEffect(), effect.duration(), (armorEnchantment.isLevelAddedToAmplifier() ? enchants.get(enchantment.getEnchantment()) : 0) + effect.amplifier()));
                        }
                    } else {
                        event.setDamage(event.getDamage() * ((armorEnchantment.isLevelAddedToAmplifier() ? enchants.get(enchantment.getEnchantment()) : 0) + armorEnchantment.getDamageAmplifier()));
                    }
                }
            }
            if (player.getHealth() <= event.getFinalDamage() && EnchantUtils.isEventActive(CEnchantments.SYSTEMREBOOT, player, armor, enchants)) {
                player.setHealth(maxhealth.getValue());
                event.setCancelled(true);

                return;
            }
            if (player.getHealth() > 0 && EnchantUtils.isEventActive(CEnchantments.ENLIGHTENED, player, armor, enchants)) {
                double heal = enchants.get(CEnchantments.ENLIGHTENED.getEnchantment());
                // Uses getValue as if the player has health boost it is modifying the base so the value after the modifier is needed.

                if (player.getHealth() + heal < maxhealth.getValue()) player.setHealth(player.getHealth() + heal);

                if (player.getHealth() + heal >= maxhealth.getValue()) player.setHealth(maxhealth.getValue());
            }
            if (EnchantUtils.isEventActive(CEnchantments.WARD, player, armor, enchants)) {
                CEnchantment wardEnchant = CEnchantments.WARD.getEnchantment();
                int level = this.enchantmentBookSettings.getLevel(armor, wardEnchant);

                if (!CEnchantments.WARD.isOffCooldown(player.getUniqueId(), level, true)) return;
                if (player.isDead()) return;

                double amount = player.getHealth() + ((double) this.enchantmentBookSettings.getLevel(armor, wardEnchant) / 4);
                double playerHealth = player.getHealth() + amount;
                event.setCancelled(true);

                //Will this stop the propelling issue? Who knows
                this.scheduler.runTaskLater(plugin, () -> player.setVelocity(new Vector(0, 0, 0)), 2L);
                if (playerHealth >= maxhealthdouble) playerHealth = maxhealthdouble;
                player.setHealth(playerHealth);
                player.sendMessage("* WARD * (Healed you for: " + playerHealth + ")");
            }
            if (EnchantUtils.isEventActive(CEnchantments.ANGELIC, player, armor, enchants)) {
                CEnchantment angelicEnchant = CEnchantments.ANGELIC.getEnchantment();
                if (!CEnchantments.ANGELIC.isOffCooldown(
                        player.getUniqueId(),
                        this.enchantmentBookSettings.getLevel(armor, angelicEnchant),
                        true
                )) return;
                double modifier = player.getHealth() + ((double) this.enchantmentBookSettings.getLevel(armor, angelicEnchant) / 4);
                if (player.isDead()) return;
                if (modifier >= maxhealthdouble) modifier = maxhealthdouble;
                player.setHealth(modifier);
                player.sendMessage("** ANGELIC ** (Healed you for: " + modifier + ")");
            }
            if (EnchantUtils.isEventActive(CEnchantments.CREEPERARMOR, player, armor, enchants)) {
                CEnchantment targetEnchant = CEnchantments.CREEPERARMOR.getEnchantment();
                if (player.getLastDamageCause().getCause().equals(DamageCause.BLOCK_EXPLOSION) || player.getLastDamageCause().getCause().equals(DamageCause.ENTITY_EXPLOSION)) {
                    event.setCancelled(true);
                    this.scheduler.runTaskLater(plugin, () -> player.setVelocity(new Vector(0, 0, 0)), 2L);
                }
                if (CEnchantments.CREEPERARMOR.getChance() >= 15) {
                    double value = player.getHealth() + this.enchantmentBookSettings.getLevel(armor, targetEnchant);
                    if (value >= maxhealthdouble) value = maxhealthdouble;
                    player.setHealth(value);
                    double rep = player.getHealth() - this.enchantmentBookSettings.getLevel(armor, targetEnchant);
                    player.sendMessage("** CREEPER ARMOR **\nHealed for " + rep);
                }
            }
            if (EnchantUtils.isEventActive(CEnchantments.DEATHGOD, player, armor, enchants)) {
                CEnchantment deathgodEnchant = CEnchantments.DEATHGOD.getEnchantment();
                double modifier = player.getHealth() / 2;
                if (modifier >= maxhealthdouble) modifier = maxhealthdouble;
                if (player.getHealth() < modifier) {
                    player.setHealth(player.getHealth() + 5 + (double) (this.enchantmentBookSettings.getLevel(armor, deathgodEnchant)) / 4);
                    player.sendMessage("** DEATH GOD ** (Healed you for: " + modifier + ")");
                }
            }
            if (EnchantUtils.isEventActive(CEnchantments.ENDERWALKER, player, armor, enchants)) {
                CEnchantment endwalkerEnchant = CEnchantments.ENDERWALKER.getEnchantment();
                if (!(event.getEntity() instanceof Player victim)) return;
                if (victim.hasPotionEffect(PotionEffectType.POISON)) victim.removePotionEffect(PotionEffectType.POISON);
                if (victim.hasPotionEffect(PotionEffectType.WITHER)) victim.removePotionEffect(PotionEffectType.WITHER);
                double modifier = player.getHealth() + this.enchantmentBookSettings.getLevel(armor, endwalkerEnchant);
                if (modifier >= maxhealthdouble) modifier = maxhealthdouble;
                victim.setHealth(modifier);
                player.sendMessage("* ENDER WALKER * (Healed you for: " + modifier + ")");
            }
            if (EnchantUtils.isEventActive(CEnchantments.SPIRITS, player, armor, enchants)) {
                //Declare a new empty collection of blazes as an ArrayList
                Collection<Blaze> blazes = new ArrayList<>();
                List<BukkitTask> spiritTasks = new ArrayList<>();

                //Get the world, position, and region the player is in.
                CEnchantment targetEnchant = CEnchantments.SPIRITS.getEnchantment();
                World world = player.getWorld();
                Location playerPos = player.getLocation();
                BoundingBox box = player.getBoundingBox();

                //Get the level of the enchantment as a variable
                int level = this.enchantmentBookSettings.getLevel(armor, targetEnchant);

                //Iterate through the enchantment levels. For every level, another blaze can be spawned.
                for (int i = 0; i < level; i++) {
                    world.spawn(playerPos, Blaze.class);
                }
                //Builds a new runnable that checks the amount of blazes in player region, then adds them to the
                //new Collection.
                spiritTasks.add(this.scheduler.runTaskLater(plugin, () -> {
                    Collection<Entity> nearbyEntities = world.getNearbyEntities(box.expand(8, 8, 8));
                    for (Entity entity : nearbyEntities) {
                        if (!(entity instanceof Blaze blaze)) continue;
                        blaze.setTarget(damager);
                        blazes.add(blaze);
                    }

                }, 40L));
                //Heal the player based on the amount of blazes in the Collection, as well as the enchantment level.
                spiritTasks.add(this.scheduler.runTaskTimer(plugin, () -> {
                    if (!blazes.isEmpty()) {
                        double modifier = player.getHealth() + (blazes.size() + level);
                        double newPlayerHealth = Math.min(modifier, maxhealthdouble);
                        player.setHealth(newPlayerHealth);
                    }
                }, 0L, 20L));

                //Builds a new runnable that removes the blazes after a period of time.
                spiritTasks.add(this.scheduler.runTaskLater(plugin, () -> {
                    for (Blaze blaze : blazes) {
                        if (!blaze.isDead()) blaze.remove();
                    }
                }, 200L));

                this.scheduler.runTaskLater(plugin, () -> {
                    for (BukkitTask task : spiritTasks) {
                        if (blazes.isEmpty()) task.cancel();
                    }
                }, 400L);

                this.scheduler.runTaskLater(plugin, () -> {
                    for (BukkitTask task : spiritTasks) task.cancel();
                }, 500L);
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (EventUtils.isIgnoredEvent(event) || EventUtils.isIgnoredUUID(event.getDamager().getUniqueId())) return;
        if (this.pluginSupport.isFriendly(event.getDamager(), event.getEntity())) return;

        if (!(event.getDamager() instanceof LivingEntity damager) || !(event.getEntity() instanceof Player player)) return;

        for (ItemStack armor : player.getEquipment().getArmorContents()) {
            Map<CEnchantment, Integer> enchants = this.enchantmentBookSettings.getEnchantments(armor);
            if (enchants.isEmpty()) continue;

            for (ArmorEnchantment armorEnchantment : this.armorEnchantmentManager.getArmorEnchantments()) {
                CEnchantments enchantment = armorEnchantment.getEnchantment();

                if (EnchantUtils.isEventActive(enchantment, player, armor, enchants)) {

                    if (armorEnchantment.isPotionEnchantment()) {
                        for (PotionEffects effect : armorEnchantment.getPotionEffects()) {
                            damager.addPotionEffect(new PotionEffect(effect.potionEffect(), effect.duration(), (armorEnchantment.isLevelAddedToAmplifier() ? enchants.get(enchantment.getEnchantment()) : 0) + effect.amplifier()));
                        }
                    } else {
                        event.setDamage(event.getDamage() * ((armorEnchantment.isLevelAddedToAmplifier() ? enchants.get(enchantment.getEnchantment()) : 0) + armorEnchantment.getDamageAmplifier()));
                    }
                }
            }

            if (EnchantUtils.isEventActive(CEnchantments.MANEUVER, player, armor, enchants)) {
                event.setCancelled(true);
                return;
            }

            if (player.isSneaking() && EnchantUtils.isEventActive(CEnchantments.CROUCH, player, armor, enchants)) {
                double percentageReduced = (CEnchantments.CROUCH.getChance() + (CEnchantments.CROUCH.getChanceIncrease() * enchants.get(CEnchantments.CROUCH.getEnchantment()))) / 100.0;
                double newDamage = event.getFinalDamage() * (1 - percentageReduced);

                if (newDamage < 0) newDamage = 0;

                event.setDamage(newDamage);
            }

            if (EnchantUtils.isEventActive(CEnchantments.SHOCKWAVE, player, armor, enchants)) {
                damager.setVelocity(player.getLocation().getDirection().multiply(2).setY(1.25));
            }

            if (player.getHealth() <= 8 && EnchantUtils.isEventActive(CEnchantments.ROCKET, player, armor, enchants)) {
                // Anti cheat support here with AAC or any others.
                player.getScheduler().runDelayed(this.plugin, playerTask -> player.setVelocity(player.getLocation().toVector().subtract(damager.getLocation().toVector()).normalize().setY(1)), null, 1);
                this.fallenPlayers.add(player.getUniqueId());

                //todo() is this EXPLOSION_HUGE?
                player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation(), 1);

                player.getScheduler().runDelayed(this.plugin, playerTask -> fallenPlayers.remove(player.getUniqueId()), null, 8 * 20);
            }
            //depricated INSOMNIA function 
            //if (EnchantUtils.isEventActive(CEnchantments.INSOMNIA, player, armor, enchants)) damager.damage(event.getDamage() + enchants.get(CEnchantments.INSOMNIA.getEnchantment()));

            if (EnchantUtils.isEventActive(CEnchantments.MOLTEN, player, armor, enchants)) damager.setFireTicks((enchants.get(CEnchantments.MOLTEN.getEnchantment()) * 2) * 20);

            if (EnchantUtils.isEventActive(CEnchantments.SAVIOR, player, armor, enchants)) event.setDamage(event.getDamage() / 2);

            if (EnchantUtils.isEventActive(CEnchantments.CACTUS, player, armor, enchants)) damager.damage(enchants.get(CEnchantments.CACTUS.getEnchantment()));

            if (EnchantUtils.isEventActive(CEnchantments.STORMCALLER, player, armor, enchants)) {
                Entity lightning = this.methods.lightning(damager);

                for (LivingEntity en : this.methods.getNearbyLivingEntities(2D, player)) {
                    EntityDamageEvent damageByEntityEvent = new EntityDamageEvent(en, DamageCause.LIGHTNING, DamageSource.builder(DamageType.LIGHTNING_BOLT).withCausingEntity(player).withDirectEntity(lightning).build(), 5D);
                    this.methods.entityEvent(player, en, damageByEntityEvent);
                }

                damager.damage(5D);
            }
            if (damager instanceof Player target) {
                if (EnchantUtils.isEventActive(CEnchantments.SHUFFLE, player, armor, enchants)) {
                    CEnchantment shuffleEnchant = CEnchantments.SHUFFLE.getEnchantment();
                    int level = enchantmentBookSettings.getLevel(armor, shuffleEnchant);

                    // Check cooldown before running
                    if (CEnchantments.SHUFFLE.isOffCooldown(target.getUniqueId(), level, true)) {

                        // Gets items in the hotbar as an array
                        ItemStack[] hotbar = new ItemStack[9];
                        for (int i = 0; i < 9; i++) {
                            hotbar[i] = target.getInventory().getItem(i);
                        }

                        // Convert array to a modifiable list and shuffle it
                        List<ItemStack> items = new ArrayList<>(Arrays.asList(hotbar));
                        Collections.shuffle(items);

                        // Set the shuffled items back to the hotbar
                        for (int i = 0; i < 9; i++) {
                            target.getInventory().setItem(i, items.get(i));
                        }
                    } else {
                        target.sendMessage("Shuffle on cooldown!");
                    }
                }
            }
            if (EnchantUtils.isEventActive(CEnchantments.HARDENED, player, armor, enchants)) {
                CEnchantment hardenedEnchant = CEnchantments.HARDENED.getEnchantment();
                @Nullable ItemStack @NotNull [] playerArmor = player.getInventory().getArmorContents();
                for (ItemStack equipment : playerArmor) {
                    if (equipment == null) return;
                    ItemMeta meta = equipment.getItemMeta();
                    Damageable damage = (Damageable) meta;
                    damage.setDamage(damage.getDamage() - this.enchantmentBookSettings.getLevel(armor, hardenedEnchant));
                    if (damage.getDamage() <= 0) return;
                    equipment.setItemMeta(meta);
                }
            }
            if (EnchantUtils.isEventActive(CEnchantments.TRICKSTER, player, armor, enchants)) {
                Location playerPos = player.getLocation();
                Location targetPos = event.getEntity().getLocation();

                Vector direction = playerPos.toVector().normalize().subtract(targetPos.toVector());
                direction = direction.normalize().multiply(2);

                try {
                    player.setVelocity(direction);
                } catch (IllegalArgumentException error) {
                    this.plugin.getLogger().severe("Something has gone horribly wrong while attempting this action.");
                    this.plugin.getLogger().severe("Here is some information:");
                    this.plugin.getLogger().severe("Directional velocity: " + direction);
                    this.plugin.getLogger().severe("Error: " + error.getCause());
                }
            }

            if (EnchantUtils.isEventActive(CEnchantments.TANK, player, armor, enchants)) {
                CEnchantment tankEnchant = CEnchantments.TANK.getEnchantment();
                if (!(event.getDamager() instanceof Player attacker)) return;
                @NotNull ItemStack weapon = this.methods.getItemInHand(attacker);
                @NotNull final Set<Material> axes = Set.of(
                        Material.WOODEN_AXE,
                        Material.STONE_AXE,
                        Material.IRON_AXE,
                        Material.GOLDEN_AXE,
                        Material.DIAMOND_AXE,
                        Material.NETHERITE_AXE
                );
                if (axes.contains(null)) return;
                if (axes.contains(weapon.getType())) {
                    double damage = event.getDamage() - this.enchantmentBookSettings.getLevel(weapon, tankEnchant);
                    event.setDamage(damage);
                    player.sendMessage("* TANK *");
                }
            }
            if (EnchantUtils.isEventActive(CEnchantments.DESTRUCTION, player, armor, enchants)) {
                CEnchantment destructionEnchant = CEnchantments.DESTRUCTION.getEnchantment();
                //Get the world and region the player is in.
                World world = player.getWorld();
                BoundingBox region = player.getBoundingBox();

                //Expand the region to account for nearby entities.
                region = region.expand(8);

                //Dump all entities in the BoundingBox into a new Collection
                Collection<Entity> nearbyEntities = world.getNearbyEntities(region);
                int limit = 2 + this.enchantmentBookSettings.getLevel(armor, destructionEnchant);

                //Build a stream to damage entities based on the following conditions
                nearbyEntities.stream()
                        .filter(entity -> entity instanceof LivingEntity)
                        .map(entity -> (LivingEntity) entity)
                        .limit(limit)
                        .forEach(entity -> {
                            entity.damage(event.getDamage() / 2);
                            entity.sendMessage("** DESTRUCTION **");
                        });
            }
            if (EnchantUtils.isEventActive(CEnchantments.DIMINISH, player, armor, enchants)) {
                double lastAttack = player.getLastDamage();
                event.setDamage(lastAttack / 2);
            }
            if (EnchantUtils.isEventActive(CEnchantments.ARMORED, player, armor, enchants)) {
                CEnchantment armoredEnchant = CEnchantments.ARMORED.getEnchantment();
                if (!(event.getDamager() instanceof Player attacker)) return;
                @NotNull final Set<Material> swords = Set.of(
                        Material.WOODEN_SWORD,
                        Material.STONE_SWORD,
                        Material.IRON_SWORD,
                        Material.GOLDEN_SWORD,
                        Material.DIAMOND_SWORD,
                        Material.NETHERITE_SWORD
                );
                @NotNull ItemStack item = this.methods.getItemInHand(attacker);
                if (swords.contains(item.getType())) {
                    event.setDamage(event.getDamage() - this.enchantmentBookSettings.getLevel(armor, armoredEnchant));
                    player.sendMessage("* ARMORED *");
                }
            }
            if (EnchantUtils.isEventActive(CEnchantments.RAGDOLL, player, armor, enchants)) {
                Vector direction = player.getLocation().getDirection();
                direction.normalize().multiply(2 + enchantmentBookSettings.getLevel(armor, CEnchantments.RAGDOLL.getEnchantment()));
                player.setVelocity(direction);
            }
            if (EnchantUtils.isEventActive(CEnchantments.ARROWDEFLECT, player, armor, enchants)) {
                if (event.getDamager() instanceof Arrow) event.setCancelled(true);
            }
            if (EnchantUtils.isEventActive(CEnchantments.MIGHTYCACTUS, player, armor, enchants)) {
                CEnchantment targetEnchant = CEnchantments.MIGHTYCACTUS.getEnchantment();
                event.setCancelled(true);
                damager.damage(1 + enchantmentBookSettings.getLevel(armor, targetEnchant));
                this.scheduler.runTaskLater(plugin, () -> damager.setVelocity(new Vector(0, 0, 0)), 2L);
            }
            if (EnchantUtils.isEventActive(CEnchantments.HEAVY, player, armor, enchants)) {
                CEnchantment targetEnchant = CEnchantments.HEAVY.getEnchantment();

                if (!(event.getDamager() instanceof Arrow)) return;
                double newDamage = event.getDamage() - (event.getDamage() * (0.02 + enchantmentBookSettings.getLevel(armor, targetEnchant)));
                event.setDamage(newDamage);
            }
            if (EnchantUtils.isEventActive(CEnchantments.REINFORCED, player, armor, enchants)) {
                CEnchantment reinforcedEnchant = CEnchantments.REINFORCED.getEnchantment();
                double level = this.enchantmentBookSettings.getLevel(armor, reinforcedEnchant);

                BoundingBox box = player.getBoundingBox().expand(3 + level);
                World world = player.getWorld();
                Collection<Entity> aggressors = world.getNearbyEntities(box);
                double radius = box.getWidthX() + box.getWidthZ();


                for (Entity entity : aggressors) {
                    if (!(entity instanceof LivingEntity livingEntity)) return;
                    if (isInRadius(box.getCenter().toLocation(world), livingEntity, world, radius)) {
                        event.setDamage(event.getDamage() / (1.5 * level));
                        player.sendMessage("** REINFORCED **");
                    }
                }
            }
        }

        if (!(damager instanceof Player)) return;

        for (ItemStack armor : Objects.requireNonNull(damager.getEquipment()).getArmorContents()) {
            Map<CEnchantment, Integer> enchants = this.enchantmentBookSettings.getEnchantments(armor);
            if (!enchants.containsKey(CEnchantments.LEADERSHIP.getEnchantment())) continue;

            int radius = 4 + enchants.get(CEnchantments.LEADERSHIP.getEnchantment());
            int players = (int) damager.getNearbyEntities(radius, radius, radius).stream().filter(entity -> entity instanceof Player && this.pluginSupport.isFriendly(damager, entity)).count();

            if (players > 0 && EnchantUtils.isEventActive(CEnchantments.LEADERSHIP, player, armor, enchants)) {
                event.setDamage(event.getDamage() + (players / 2d));
            }
        }
    }

    @EventHandler()
    private void absorptionHandler(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player victim)) return;

        for (ItemStack armor : victim.getEquipment().getArmorContents()) {
            @NotNull final Map<CEnchantment, Integer> enchants = this.enchantmentBookSettings.getEnchantments(armor);

            if (EnchantUtils.isEventActive(CEnchantments.FAT, victim, armor, enchants)) {
                CEnchantment fatEnchant = CEnchantments.FAT.getEnchantment();
                int level = this.enchantmentBookSettings.getLevel(armor, fatEnchant);
                event.setDamage(Math.max(event.getDamage() - level, 0));
                if (level >= 3) {
                    AttributeModifier modifier = new AttributeModifier(new NamespacedKey(plugin, "fat"), level * 8, AttributeModifier.Operation.ADD_NUMBER);
                    victim.getAttribute(Attribute.MAX_ABSORPTION).addModifier(modifier);
                    victim.setAbsorptionAmount(level * 2);

                    this.scheduler.runTaskLater(plugin, () -> victim.getAttribute(Attribute.MAX_ABSORPTION).removeModifier(modifier), 120L);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        for (ItemStack armor : attacker.getEquipment().getArmorContents()) {
            @NotNull final Map<CEnchantment, Integer> enchants = this.enchantmentBookSettings.getEnchantments(armor);
            if (EnchantUtils.isEventActive(CEnchantments.DEATHBRINGER, attacker, armor, enchants)) {
                victim.damage(event.getDamage() * 2);
                attacker.sendMessage("* DEATHBRINGER *");
                attacker.sendMessage("Deathbringer damage: " + event.getDamage() * 2);
            }
        }

    }

    @EventHandler()
    public void onArrowHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player player)) return;

        for (ItemStack armor : player.getEquipment().getArmorContents()) {
            if (armor == null || !armor.hasItemMeta()) continue;
            Map<CEnchantment, Integer> enchants = this.enchantmentBookSettings.getEnchantments(armor);

            if (this.enchantmentBookSettings.hasEnchantment(armor.getItemMeta(), CEnchantments.MARKSMAN.getEnchantment())) {
                event.setDamage(event.getDamage() * this.enchantmentBookSettings.getLevel(armor, CEnchantments.MARKSMAN.getEnchantment()));
            }
        }

    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onAura(AuraActiveEvent event) {
        Player player = event.getPlayer();
        Player other = event.getOther();

        if (!player.canSee(other) || !other.canSee(player)) return;
        if (this.pluginSupport.isVanished(player) || this.pluginSupport.isVanished(other)) return;

        CEnchantments enchant = event.getEnchantment();
        int level = event.getLevel();

        if (!this.pluginSupport.allowCombat(other.getLocation()) || this.pluginSupport.isFriendly(player, other) || this.methods.hasPermission(other, "bypass.aura", false)) return;

        Map<CEnchantment, Integer> enchantments = Map.of(enchant.getEnchantment(), level);

        switch (enchant) {
            case BLIZZARD -> {
                if (EnchantUtils.isAuraActive(player, enchant, enchantments)) other.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 5 * 20, level - 1));
            }

            case INTIMIDATE -> {
                if (EnchantUtils.isAuraActive(player, enchant, enchantments)) other.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 3 * 20, level - 1));
            }

            case ACIDRAIN -> {
                if (EnchantUtils.isAuraActive(player, enchant, enchantments)) other.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 4 * 20, 1));
            }

            case SANDSTORM -> {
                if (EnchantUtils.isAuraActive(player, enchant, enchantments)) other.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 10 * 20, 0));
            }

            case RADIANT -> {
                if (EnchantUtils.isAuraActive(player, enchant, enchantments)) other.setFireTicks(5 * 20);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerMovement(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ()) return;

        armorProcessor.add(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (player.getKiller() == null) return;

        Player killer = player.getKiller();

        if (!this.pluginSupport.allowCombat(player.getLocation())) return;

        for (ItemStack item : player.getEquipment().getArmorContents()) {
            Map<CEnchantment, Integer> enchantments = this.enchantmentBookSettings.getEnchantments(item);

            if (EnchantUtils.isEventActive(CEnchantments.SELFDESTRUCT, player, item, enchantments)) {
                if (player.getHealth() <= 2) {
                    this.methods.explode(player);
                    World world = player.getWorld();
                    if (Boolean.TRUE.equals(world.getGameRuleValue(GameRule.KEEP_INVENTORY))) return;
                    List<ItemStack> items = event.getDrops().stream().filter(drop ->
                            ProtectionCrystalSettings.isProtected(drop) && this.protectionCrystalSettings.isProtectionSuccessful(player)).toList();

                    event.getDrops().clear();
                    event.getDrops().addAll(items);
                }
            }

            if (EnchantUtils.isEventActive(CEnchantments.RECOVER, player, item, enchantments)) {
                killer.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 8 * 20, 2));
                killer.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 5 * 20, 1));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerFallDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (!this.fallenPlayers.contains(player.getUniqueId())) return;

        if (!DamageCause.FALL.equals(event.getCause())) return;

        event.setCancelled(true);

    }

    @EventHandler()
    public void onBookApply(PreBookApplyEvent event) {
        CEBook book = event.getCEBook();

        if (!event.getSuccessful()) return;

        if (book.getEnchantment().equals(CEnchantments.MIGHTYCACTUS.getEnchantment())) {
            this.enchantmentBookSettings.swapToHeroicEnchant(CEnchantments.MIGHTYCACTUS, event.getEnchantedItem(), event.getPlayer());
        }
    }

    private boolean isInRadius(@NotNull Location location, @NotNull LivingEntity entity, World world, double radius) {
        return world.getNearbyLivingEntities(location, radius).contains(entity);
    }
}
