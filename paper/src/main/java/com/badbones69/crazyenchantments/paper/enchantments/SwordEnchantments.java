package com.badbones69.crazyenchantments.paper.enchantments;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.Methods;
import com.badbones69.crazyenchantments.paper.Starter;
import com.badbones69.crazyenchantments.paper.api.CrazyManager;
import com.badbones69.crazyenchantments.paper.api.economy.Currency;
import com.badbones69.crazyenchantments.paper.api.economy.CurrencyAPI;
import com.badbones69.crazyenchantments.paper.api.enums.CEnchantments;
import com.badbones69.crazyenchantments.paper.api.enums.Messages;
import com.badbones69.crazyenchantments.paper.api.events.RageBreakEvent;
import com.badbones69.crazyenchantments.paper.api.objects.CEPlayer;
import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import com.badbones69.crazyenchantments.paper.api.builders.ItemBuilder;
import com.badbones69.crazyenchantments.paper.api.utils.EnchantUtils;
import com.badbones69.crazyenchantments.paper.api.utils.EntityUtils;
import com.badbones69.crazyenchantments.paper.api.utils.EventUtils;
import com.badbones69.crazyenchantments.paper.controllers.BossBarController;
import com.badbones69.crazyenchantments.paper.controllers.settings.EnchantmentBookSettings;
import com.badbones69.crazyenchantments.paper.scheduler.FoliaRunnable;
import com.badbones69.crazyenchantments.paper.support.PluginSupport;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.*;
import java.util.stream.Stream;

public class SwordEnchantments implements Listener {

    @NotNull
    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    @NotNull
    private final Starter starter = this.plugin.getStarter();

    @NotNull
    private final CrazyManager crazyManager = this.starter.getCrazyManager();

    @NotNull
    private final EnchantmentBookSettings enchantmentBookSettings = this.starter.getEnchantmentBookSettings();

    @NotNull
    private final Methods methods = this.starter.getMethods();

    @NotNull
    private final BukkitScheduler scheduler = Bukkit.getScheduler();

    // Plugin Support.
    @NotNull
    private final PluginSupport pluginSupport = this.starter.getPluginSupport();

    @NotNull
    private final BossBarController bossBarController = this.plugin.getBossBarController();

    // Economy Management.
    @NotNull
    private final CurrencyAPI currencyAPI = this.starter.getCurrencyAPI();

    //Other

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (EventUtils.isIgnoredEvent(event) || EventUtils.isIgnoredUUID(event.getDamager().getUniqueId())) return;

        if (this.pluginSupport.isFriendly(event.getDamager(), event.getEntity())) return;

        if (this.crazyManager.isBreakRageOnDamageOn() && event.getEntity() instanceof Player player) {
            CEPlayer cePlayer = this.crazyManager.getCEPlayer(player);

            if (cePlayer != null) {
                RageBreakEvent rageBreakEvent = new RageBreakEvent(player, event.getDamager(), this.methods.getItemInHand(player));
                this.plugin.getServer().getPluginManager().callEvent(rageBreakEvent);

                if (!rageBreakEvent.isCancelled() && cePlayer.hasRage()) {
                    cePlayer.getRageTask().cancel();
                    cePlayer.setRageMultiplier(0.0);
                    cePlayer.setRageLevel(0);
                    cePlayer.setRage(false);

                    rageInformPlayer(player, Messages.RAGE_DAMAGED, 0f);
                }
            }
        }

        if (!(event.getEntity() instanceof LivingEntity en)) return;
        if (!(event.getDamager() instanceof final Player damager)) return;

        CEPlayer cePlayer = this.crazyManager.getCEPlayer(damager);
        ItemStack item = this.methods.getItemInHand(damager);

        @NotNull final Double maxhealth = damager.getAttribute(Attribute.MAX_HEALTH).getValue();

        if (event.getEntity().isDead()) return;

        Map<CEnchantment, Integer> enchantments = this.enchantmentBookSettings.getEnchantments(item);
        boolean isEntityPlayer = event.getEntity() instanceof Player;

        if (isEntityPlayer && EnchantUtils.isEventActive(CEnchantments.DISARMER, damager, item, enchantments)) {
            Player player = (Player) event.getEntity();

            EquipmentSlot equipmentSlot = getSlot(this.methods.percentPick(4, 0));

            ItemStack armor = switch (equipmentSlot) {
                case HEAD -> player.getEquipment().getHelmet();
                case CHEST -> player.getEquipment().getChestplate();
                case LEGS -> player.getEquipment().getLeggings();
                case FEET -> player.getEquipment().getBoots();
                default -> null;
            };

            if (armor != null) {
                switch (equipmentSlot) {
                    case HEAD -> player.getEquipment().setHelmet(null);
                    case CHEST -> player.getEquipment().setChestplate(null);
                    case LEGS -> player.getEquipment().setLeggings(null);
                    case FEET -> player.getEquipment().setBoots(null);
                }

                this.methods.addItemToInventory(player, armor);
            }
        }

        if (isEntityPlayer && EnchantUtils.isEventActive(CEnchantments.DISORDER, damager, item, enchantments)) {

            Player player = (Player) event.getEntity();
            Inventory inventory = player.getInventory();
            List<ItemStack> items = new ArrayList<>();
            List<Integer> slots = new ArrayList<>();

            for (int i = 0; i < 9; i++) {
                ItemStack inventoryItem = inventory.getItem(i);

                if (inventoryItem != null) {
                    items.add(inventoryItem);
                    inventory.setItem(i, null);
                }

                slots.add(i);
            }

            Collections.shuffle(items);
            Collections.shuffle(slots);

            for (int i = 0; i < items.size(); i++) {
                inventory.setItem(slots.get(i), items.get(i));
            }

            if (!Messages.DISORDERED_ENEMY_HOT_BAR.getMessageNoPrefix().isEmpty())
                damager.sendMessage(Messages.DISORDERED_ENEMY_HOT_BAR.getMessage());
        }

        // Check if CEPlayer is null as plugins like citizen use Player objects.
        if (cePlayer != null && EnchantUtils.isEventActive(CEnchantments.RAGE, damager, item, enchantments)) {

            if (cePlayer.hasRage()) {
                cePlayer.getRageTask().cancel();

                if (cePlayer.getRageMultiplier() <= this.crazyManager.getRageMaxLevel())
                    cePlayer.setRageMultiplier(cePlayer.getRageMultiplier() + (enchantments.get(CEnchantments.RAGE.getEnchantment()) * crazyManager.getRageIncrement()));

                int rageUp = cePlayer.getRageLevel() + 1;

                if (cePlayer.getRageMultiplier().intValue() >= rageUp) {
                    rageInformPlayer(damager, Messages.RAGE_RAGE_UP, Map.of("%Level%", String.valueOf(rageUp)), ((float) rageUp / (float) (this.crazyManager.getRageMaxLevel() + 1)));
                    cePlayer.setRageLevel(rageUp);
                }

                event.setDamage(event.getDamage() * cePlayer.getRageMultiplier());
            } else {
                cePlayer.setRageMultiplier(1.0);
                cePlayer.setRage(true);
                cePlayer.setRageLevel(1);

                rageInformPlayer(damager, Messages.RAGE_BUILDING, ((float) cePlayer.getRageLevel() / (float) this.crazyManager.getRageMaxLevel()));
            }

            cePlayer.setRageTask(new FoliaRunnable(cePlayer.getPlayer().getScheduler(), null) {
                @Override
                public void run() {
                    cePlayer.setRageMultiplier(0.0);
                    cePlayer.setRage(false);
                    cePlayer.setRageLevel(0);

                    rageInformPlayer(damager, Messages.RAGE_COOLED_DOWN, 0f);
                }
            }.runDelayed(plugin, 80));
        }

        if (en instanceof Player player && EnchantUtils.isEventActive(CEnchantments.SKILLSWIPE, damager, item, enchantments)) {
            int SkillSwipeLevel = enchantments.get(CEnchantments.SKILLSWIPE.getEnchantment());
            int min = 25;
            int max = 100 + (25 * SkillSwipeLevel);
            int amount = new Random().nextInt(max - min + 1) + min;

            if (CEnchantments.SKILLSWIPE.isOffCooldown(damager.getUniqueId(), SkillSwipeLevel, true)) {

                if (player.getTotalExperience() > 0) {

                    if (this.currencyAPI.getCurrency(player, Currency.XP_TOTAL) >= amount) {
                        this.currencyAPI.takeCurrency(player, Currency.XP_TOTAL, amount);
                    } else {
                        player.setTotalExperience(0);
                    }

                    this.currencyAPI.giveCurrency(damager, Currency.XP_TOTAL, amount);
                }
            }
        }

        if (damager.getHealth() > 0 && EnchantUtils.isEventActive(CEnchantments.LIFESTEAL, damager, item, enchantments)) {
            int steal = enchantments.get(CEnchantments.LIFESTEAL.getEnchantment());
            if (!CEnchantments.LIFESTEAL.isOffCooldown(damager.getUniqueId(), steal, true)) return;
            // Uses getValue as if the player has health boost it is modifying the base so the value after the modifier is needed.

            if (damager.getHealth() + steal < maxhealth) damager.setHealth(damager.getHealth() + steal);

            if (damager.getHealth() + steal >= maxhealth) damager.setHealth(maxhealth);
        }

        if (EnchantUtils.isEventActive(CEnchantments.NUTRITION, damager, item, enchantments)) {
            if (CEnchantments.NUTRITION.isOffCooldown(damager.getUniqueId(), enchantments.get(CEnchantments.NUTRITION.getEnchantment()), true)) {
                if (damager.getSaturation() + (2 * enchantments.get(CEnchantments.NUTRITION.getEnchantment())) <= 20)
                    damager.setSaturation(damager.getSaturation() + (2 * enchantments.get(CEnchantments.NUTRITION.getEnchantment())));

                if (damager.getSaturation() + (2 * enchantments.get(CEnchantments.NUTRITION.getEnchantment())) >= 20)
                    damager.setSaturation(20);
            }
        }

        if (damager.getHealth() > 0 && EnchantUtils.isEventActive(CEnchantments.VAMPIRE, damager, item, enchantments)) {
            // Uses getValue as if the player has health boost it is modifying the base so the value after the modifier is needed.

            if (damager.getHealth() + event.getDamage() / 2 < maxhealth)
                damager.setHealth(damager.getHealth() + event.getDamage() / 2);

            if (damager.getHealth() + event.getDamage() / 2 >= maxhealth) damager.setHealth(maxhealth);
        }

        //Imperium Enchants: Insomnia
        if (EnchantUtils.isEventActive(CEnchantments.INSOMNIA, damager, item, enchantments)) {
            UUID playerUUID = damager.getUniqueId();
            // Check if the enchantment is on cooldown
            int level = enchantmentBookSettings.getLevel(item, CEnchantments.INSOMNIA.getEnchantment());

            if (CEnchantments.INSOMNIA.isOffCooldown(playerUUID, level, true)) {
                if (event.getEntity() instanceof LivingEntity target) {
                    // Amplifier level 7 = amplifier 3: l5-3=a2  l2-1=a1
                    int amplifier = (level >= 7) ? 2 : (level >= 3 ? 1 : 0);
                    // Durations: (Level_7 = 4_seconds): (Level_6 = 9_seconds): (Level_5 = 7_seconds): (Level_4 = 6_seconds):
                    // (Level_3 = 5_seconds): (Level_2 = 4_seconds): (Level_1 = 2_seconds):
                    int duration = (level >= 7) ? 80 : (level == 6) ? 180 : (level == 5) ? 140 : (level == 4) ? 120 : (level == 3) ? 100 : (level == 2 ? 80 : 40);
                    target.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, duration, amplifier));
                    target.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, duration, amplifier));
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration, amplifier));
                }
                return;
            }
        }
        //IMPERIUM: Insomnia

        if (EnchantUtils.isEventActive(CEnchantments.BLINDNESS, damager, item, enchantments)) {
            en.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 3 * 20, enchantments.get(CEnchantments.BLINDNESS.getEnchantment()) - 1));
        }

        if (EnchantUtils.isEventActive(CEnchantments.CONFUSION, damager, item, enchantments)) {
            int level = enchantmentBookSettings.getLevel(item, CEnchantments.CONFUSION.getEnchantment());

            if (CEnchantments.CONFUSION.isOffCooldown(damager.getUniqueId(), level, true)) {
                int duration = (level >= 3) ? 120 : (level == 2 ? 80 : 40);
                en.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, duration, 0));
            }
        }

        if (EnchantUtils.isEventActive(CEnchantments.DOUBLESTRIKE, damager, item, enchantments)) {
            event.setDamage((event.getDamage() * 2));
        }

        if (EnchantUtils.isEventActive(CEnchantments.EXECUTE, damager, item, enchantments)) {
            damager.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 3 + (getLevel(item, CEnchantments.EXECUTE)) * 20, 3));
        }

        if (EnchantUtils.isEventActive(CEnchantments.FASTTURN, damager, item, enchantments)) {
            event.setDamage(event.getDamage() + (event.getDamage() / 3));
        }

        if (EnchantUtils.isEventActive(CEnchantments.FEATHERWEIGHT, damager, item, enchantments)) {
            int FeatherWeightlevel = enchantmentBookSettings.getLevel(item, CEnchantments.FEATHERWEIGHT.getEnchantment());
            if (CEnchantments.FEATHERWEIGHT.isOffCooldown(damager.getUniqueId(), FeatherWeightlevel, true)) {
                damager.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, FeatherWeightlevel * 2 * 20, FeatherWeightlevel - 1));
            }
        }

        if (EnchantUtils.isEventActive(CEnchantments.OBLITERATE, damager, item, enchantments)) {
            CEnchantment obliterate = CEnchantments.OBLITERATE.getEnchantment();
            int level = enchantmentBookSettings.getLevel(item, obliterate);

            if (CEnchantments.OBLITERATE.isOffCooldown(damager.getUniqueId(), level, true)) {
                //Scale knockback: 1.0 base + 0.5 per level
                double strengthX = 1.0 + (0.5 * level);
                double strengthY = 0.25 + (0.25 * level);

                Vector direction = damager.getLocation().getDirection().multiply(strengthX).setY(strengthY);
                event.getEntity().setVelocity(direction);
            }// else {
            //    damager.sendMessage("Obliterate on cooldown!");
            //}
        }

        if (EnchantUtils.isEventActive(CEnchantments.PARALYZE, damager, item, enchantments)) {

            for (LivingEntity entity : this.methods.getNearbyLivingEntities(2D, damager)) {
                EntityDamageEvent damageByEntityEvent = new EntityDamageEvent(entity, EntityDamageEvent.DamageCause.MAGIC, DamageSource.builder(DamageType.INDIRECT_MAGIC).withDirectEntity(damager).build(), 5D);
                this.methods.entityEvent(damager, entity, damageByEntityEvent);
            }

            en.getWorld().strikeLightningEffect(en.getLocation());
            en.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 3 * 20, 2));
            en.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 3 * 20, 2));
        }

        if (EnchantUtils.isEventActive(CEnchantments.SLOWMO, damager, item, enchantments)) {
            en.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 3 * 20, enchantments.get(CEnchantments.SLOWMO.getEnchantment())));
        }

        if (EnchantUtils.isEventActive(CEnchantments.SNARE, damager, item, enchantments)) {
            en.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 3 * 20, 0));
            en.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 3 * 20, 0));
        }

        if (EnchantUtils.isEventActive(CEnchantments.TRAP, damager, item, enchantments)) {
            int level = getLevel(item, CEnchantments.TRAP);
            en.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 3 * 20, 2 + level));
        }

        if (EnchantUtils.isEventActive(CEnchantments.POISON, damager, item, enchantments)) {
            en.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 5 * 20, enchantments.get(CEnchantments.POISON.getEnchantment())));
        }

        if (EnchantUtils.isEventActive(CEnchantments.WITHER, damager, item, enchantments)) {
            en.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 2 * 20, 2));
        }

        if (EnchantUtils.isEventActive(CEnchantments.FAMINE, damager, item, enchantments)) {
            int famineLevel = enchantmentBookSettings.getLevel(item, CEnchantments.FAMINE.getEnchantment());
            en.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, famineLevel * 2 * 20, famineLevel - 1));
        }
        //IMPERIUM
        if (EnchantUtils.isEventActive(CEnchantments.ENDERSLAYER, damager, item, enchantments)) {
            @NotNull final Set<Class<? extends LivingEntity>> endmobs = Set.of(
                    Enderman.class,
                    Endermite.class,
                    Shulker.class,
                    EnderDragon.class
            );
            CEnchantment enderslayerEnchant = CEnchantments.ENDERSLAYER.getEnchantment();
            double damage = event.getDamage() * this.enchantmentBookSettings.getLevel(item, enderslayerEnchant);
            if (endmobs.contains(en.getClass())) en.damage(damage);
            event.setDamage(damage);
            damager.sendMessage("* ENDER SLAYER * (" + en.getClass() + ")");
        }
        //todo lol does this even work?
        if (EnchantUtils.isEventActive(CEnchantments.NETHERSLAYER, damager, item, enchantments)) {
            @NotNull final Set<Class<? extends LivingEntity>> nethermobs = Set.of(
                    Blaze.class,
                    Piglin.class,
                    PiglinBrute.class,
                    MagmaCube.class,
                    PigZombie.class,
                    WitherSkeleton.class,
                    Zoglin.class
            );
            CEnchantment netherslayerEnchant = CEnchantments.NETHERSLAYER.getEnchantment();
            double damage = event.getDamage() * this.enchantmentBookSettings.getLevel(item, netherslayerEnchant);
            if (nethermobs.contains(en.getClass())) en.damage(damage);
            event.setDamage(damage);
            damager.sendMessage("* NETHER SLAYER * (" + en.getClass() + ")");
        }
        if (EnchantUtils.isEventActive(CEnchantments.SHACKLE, damager, item, enchantments)) {
            CEnchantment shackleEnchant = CEnchantments.SHACKLE.getEnchantment();
            Location playerPos = damager.getLocation();
            Vector vector = playerPos.toVector().subtract(en.getLocation().toVector());
            vector = vector.normalize().multiply(2 + this.enchantmentBookSettings.getLevel(item, shackleEnchant));
            en.setVelocity(vector);
        }
        if (EnchantUtils.isEventActive(CEnchantments.GREATSWORD, damager, item, enchantments)) {
            if (!(event.getEntity() instanceof Player target)) return;
            if (target.getInventory().getItemInMainHand().getType().equals(Material.BOW)) {
                event.setDamage(event.getDamage() * (damager.getVelocity().normalize().length() / 2));
            }
        }
        if (EnchantUtils.isEventActive(CEnchantments.DOMINATE, damager, item, enchantments)) {
            if (!(event.getEntity() instanceof LivingEntity target)) return;
            target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, CEnchantments.DOMINATE.getChance(),
                    this.enchantmentBookSettings.getLevel(item, CEnchantments.DOMINATE.getEnchantment())));
        }
        if (EnchantUtils.isEventActive(CEnchantments.BLOCK, damager, item, enchantments)) {
            event.setCancelled(true);
            if (!(event.getEntity() instanceof LivingEntity target)) return;
            target.damage(4 + ((double) CEnchantments.BLOCK.getChance() / 11));
        }
        if (EnchantUtils.isEventActive(CEnchantments.DEMONIC, damager, item, enchantments)) {
            if (!(event.getEntity() instanceof LivingEntity target)) return;
            if (target.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE))
                target.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
        }
        if (EnchantUtils.isEventActive(CEnchantments.DISTANCE, damager, item, enchantments)) {
            damager.setVelocity(damager.getLocation().getDirection().multiply(-2).normalize());
            damager.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, CEnchantments.DISTANCE.getChance() * 4,
                    this.enchantmentBookSettings.getLevel(item, CEnchantments.DISTANCE.getEnchantment())));
        }
        if (EnchantUtils.isEventActive(CEnchantments.INVERSION, damager, item, enchantments)) {
            CEnchantment inversionEnchant = CEnchantments.INVERSION.getEnchantment();
            int level = this.enchantmentBookSettings.getLevel(item, inversionEnchant);
            if (!CEnchantments.INVERSION.isOffCooldown(damager.getUniqueId(), level, true)) return;
            event.setCancelled(true);
            double heal = (damager.getHealth() + level);
            if (heal >= maxhealth) heal = maxhealth;
            damager.setHealth(heal);
            damager.sendMessage("Inversion healed you for " + level);
        }
        if (EnchantUtils.isEventActive(CEnchantments.SILENCE, damager, item, enchantments)) {
            if (!(en instanceof Player target)) return;
            int level = getLevel(item, CEnchantments.SILENCE);

            List<ItemStack> inv = new ArrayList<>();
            Collections.addAll(inv, target.getInventory().getArmorContents());
            inv.add(target.getInventory().getItemInOffHand());
            inv.add(target.getInventory().getItemInMainHand());

            for (ItemStack targetItem : inv) {
                if (targetItem == null || targetItem.getType().isAir()) continue;
                @NotNull Map<CEnchantment, Integer> enchantmentMap = this.enchantmentBookSettings.getEnchantments(targetItem);
                Random random = new Random();
                int number = random.nextInt(level + 1);
                if (number == 0) number = 1;

                enchantmentMap.keySet().stream()
                        .limit(number)
                        .filter(CEnchantment::isActivated)
                        .forEach(enchantment -> {
                            enchantment.setActivated(false);
                            this.plugin.getLogger().info("[DEBUG] [Silence] Silence task added!");
                            this.scheduler.runTaskLater(plugin, () -> {
                                enchantment.setActivated(true);
                                this.plugin.getLogger().info("[DEBUG] [Silence] Runnable for silence stopped!");
                            }, 60L);
                        });
            }
        }
        if (EnchantUtils.isEventActive(CEnchantments.STUN, damager, item, enchantments)) {
            CEnchantment stunEnchant = CEnchantments.STUN.getEnchantment();
            if (!CEnchantments.STUN.isOffCooldown(damager.getUniqueId(), (enchantments.get(CEnchantments.STUN.getEnchantment())), true)) return;
            if (!en.hasPotionEffect(PotionEffectType.SLOWNESS))
                en.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, CEnchantments.STUN.getChance(), this.enchantmentBookSettings.getLevel(item, stunEnchant)));
            if (!en.hasPotionEffect(PotionEffectType.WEAKNESS))
                en.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, CEnchantments.STUN.getChance(), this.enchantmentBookSettings.getLevel(item, stunEnchant)));
            if (damager.hasPotionEffect(PotionEffectType.SLOWNESS))
                damager.removePotionEffect(PotionEffectType.SLOWNESS);
        }
        if (EnchantUtils.isEventActive(CEnchantments.SWARM, damager, item, enchantments)) {
            //The more entities there are in an area, the higher the buff to damage.
            //Radius considered is raised by each level.
            //Maximum level should be 5.\
            World world = damager.getWorld();
            double radius = 5 + getLevel(item, CEnchantments.SWARM);
            Collection<LivingEntity> total = world.getNearbyLivingEntities(damager.getLocation(), radius);
            event.setDamage(event.getDamage() * ((double) total.size() / 2));
        }
        //IMPERIUM
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;

        Player damager = event.getEntity().getKiller();
        Player player = event.getEntity();
        ItemStack item = this.methods.getItemInHand(damager);
        Map<CEnchantment, Integer> enchantments = this.enchantmentBookSettings.getEnchantments(item);

        if (EnchantUtils.isEventActive(CEnchantments.HEADLESS, damager, item, enchantments)) {
            ItemStack head = new ItemBuilder().setMaterial("PLAYER_HEAD").setPlayerName(player.getName()).build();
            event.getDrops().add(head);
        }

        if (EnchantUtils.isEventActive(CEnchantments.LIFEBLOOM, damager, item, enchantments)) {
            if (CEnchantments.LIFEBLOOM.isOffCooldown(damager.getUniqueId(), enchantmentBookSettings.getLevel(item, CEnchantments.LIFEBLOOM.getEnchantment()), true)) {
                for (Entity entity : player.getNearbyEntities(10, 10, 10)) {
                    if (!pluginSupport.isFriendly(entity, player)) continue;
                    Player ally = (Player) entity;

                    ally.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 5));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            Player damager = event.getEntity().getKiller();
            ItemStack item = this.methods.getItemInHand(damager);
            Map<CEnchantment, Integer> enchantments = this.enchantmentBookSettings.getEnchantments(item);

            if (EnchantUtils.isEventActive(CEnchantments.INQUISITIVE, damager, item, enchantments)) {
                if (!CEnchantments.INQUISITIVE.isOffCooldown(damager.getUniqueId(), (enchantments.get(CEnchantments.INQUISITIVE.getEnchantment())), true)) return;
                event.setDroppedExp((int) Math.round(event.getDroppedExp() * (1.0 + 0.25 * enchantments.get(CEnchantments.INQUISITIVE.getEnchantment()))));
            }

            Material headMat = EntityUtils.getHeadMaterial(event.getEntity());
            if (headMat != null && !EventUtils.containsDrop(event, headMat)) {
                double multiplier = this.crazyManager.getDecapitationHeadMap().getOrDefault(headMat, 0.0);
                if (multiplier != 0.0 && EnchantUtils.isEventActive(CEnchantments.HEADLESS, damager, item, enchantments, multiplier)) {
                    ItemStack head = new ItemBuilder().setMaterial(headMat).build();
                    event.getDrops().add(head);
                }
            }

            // The entity that is killed is a player.
            if (event.getEntity() instanceof Player && EnchantUtils.isEventActive(CEnchantments.CHARGE, damager, item, enchantments)) {
                int radius = 4 + enchantments.get(CEnchantments.CHARGE.getEnchantment());
                damager.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10 * 20, 1));

                damager.getNearbyEntities(radius, radius, radius).stream().filter(entity ->
                        this.pluginSupport.isFriendly(entity, damager)).forEach(entity ->
                        ((Player) entity).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10 * 20, 1)));
            }
            if (EnchantUtils.isEventActive(CEnchantments.KILLAURA, damager, item, enchantments)) {
                if (!CEnchantments.KILLAURA.isOffCooldown(damager.getUniqueId(), (enchantments.get(CEnchantments.KILLAURA.getEnchantment())), true)) return;
                World world = event.getEntity().getWorld();
                Collection<LivingEntity> entities = world.getNearbyLivingEntities(event.getEntity().getLocation(), 1);
                for (LivingEntity entity : entities) {
                    if (entity instanceof Player) return;
                    entity.setHealth(0);
                }
            }
        }
    }

    private EquipmentSlot getSlot(int slot) {
        return switch (slot) {
            case 1 -> EquipmentSlot.CHEST;
            case 2 -> EquipmentSlot.LEGS;
            case 3 -> EquipmentSlot.FEET;
            default -> EquipmentSlot.HEAD;
        };
    }

    private void rageInformPlayer(Player player, Messages message, Map<String, String> placeholders, float progress) {
        if (message.getMessageNoPrefix().isBlank()) return;

        if (this.crazyManager.useRageBossBar()) {
            this.bossBarController.updateBossBar(player, message.getMessageNoPrefix(placeholders), progress);
        } else {
            player.sendMessage(message.getMessage(placeholders));
        }
    }

    private void rageInformPlayer(Player player, Messages message, float progress) {
        if (message.getMessageNoPrefix().isBlank()) return;

        if (this.crazyManager.useRageBossBar()) {
            this.bossBarController.updateBossBar(player, message.getMessageNoPrefix(), progress);
        } else {
            player.sendMessage(message.getMessage());
        }
    }

    /**
     * Local function to get the level because I'm tired of typing it every time
     * @param itemStack Item you're getting the level from
     * @param data The enchantment you're checking
     * @return The level of the enchantment, as an integer.
     */
    private int getLevel(ItemStack itemStack, @NotNull CEnchantments data) {
        return this.enchantmentBookSettings.getLevel(itemStack, data.getEnchantment());
    }
}
