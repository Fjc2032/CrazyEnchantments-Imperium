package com.badbones69.crazyenchantments.paper.api.enums;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.Methods;
import com.badbones69.crazyenchantments.paper.Starter;
import com.badbones69.crazyenchantments.paper.api.CrazyManager;
import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import com.badbones69.crazyenchantments.paper.api.objects.enchants.EnchantmentType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public enum CEnchantments {

    //	----------------Boots----------------  \\
    GEARS("Gears", "Boots"),
    WINGS("Wings", "Boots"),
    ROCKET("Rocket", "Boots", 15, 5),
    SPRINGS("Springs", "Boots"),
    ANTIGRAVITY("AntiGravity", "Boots"),
    //	----------------Bows----------------  \\
    PULL("Pull", "Bow", 25, 10),
    VENOM("Venom", "Bow", 10, 5),
    DOCTOR("Doctor", "Bow"),
    PIERCING("Piercing", "Bow", 5, 5),
    ICEFREEZE("IceFreeze", "Bow", 25, 10),
    MULTIARROW("MultiArrow", "Bow", 25, 10),
    STICKY_SHOT("Sticky-Shot", "Bow", 10, 10),
    SNIPER("Sniper", "Bow", 25, 5),
    //	----------------Helmets----------------  \\
    IMPLANTS("Implants", "Helmet", 5, 5),
    //	----------------Swords----------------  \\
    TRAP("Trap", "Sword", 10, 5),
    RAGE("Rage", "Sword"),
    VIPER("Viper", "Sword", 10, 5),
    SNARE("Snare", "Sword", 10, 3),
    SLOWMO("SlowMo", "Sword", 5, 5),
    WITHER("Wither", "Sword", 10, 3),
    VAMPIRE("Vampire", "Sword", 5, 5),
    EXECUTE("Execute", "Sword"),
    FASTTURN("FastTurn", "Sword", 5, 5),
    DISARMER("Disarmer", "Sword", 5, 1),
    PARALYZE("Paralyze", "Sword", 15, 5),
    BLINDNESS("Blindness", "Sword", 5, 1),
    LIFESTEAL("LifeSteal", "Sword", 15, 5),
    INQUISITIVE("Inquisitive", "Sword", 50, 25),
    DOUBLEDAMAGE("DoubleDamage", "Sword", 5, 1),
    DISORDER("Disorder", "Sword", 1, 0),
    CHARGE("Charge", "Sword"),
    //	----------------Armor----------------  \\
    VALOR("Valor", "Armor"),
    DRUNK("Drunk", "Helmet"),
    NINJA("Ninja", "Armor"),
    ANGEL("Angel", "Armor"),
    TAMER("Tamer", "Armor"),
    GUARDS("Guards", "Armor"),
    VOODOO("Voodoo", "Armor", 15, 5),
    SAVIOR("Savior", "Armor", 15, 5),
    CACTUS("Cactus", "Armor", 25, 25),
    FREEZE("Freeze", "Armor", 10, 5),
    RECOVER("Recover", "Armor"),
    NURSERY("Nursery", "Armor", 5, 5),
    FORTIFY("Fortify", "Armor", 10, 5),
    OVERLOAD("OverLoad", "Armor"),
    BLIZZARD("Blizzard", "Armor"),
    ACIDRAIN("AcidRain", "Armor", 5, 5),
    SANDSTORM("SandStorm", "Armor", 5, 5),
    SMOKEBOMB("SmokeBomb", "Helmet", 5, 5),
    PAINGIVER("PainGiver", "Armor", 10, 5),
    INTIMIDATE("Intimidate", "Armor"),
    BURNSHIELD("BurnShield", "Armor"),
    LEADERSHIP("Leadership", "Armor", 10, 5),
    INFESTATION("Infestation", "Armor"),
    NECROMANCER("Necromancer", "Armor"),
    STORMCALLER("StormCaller", "Armor", 10, 5),
    ENLIGHTENED("Enlightened", "Armor", 10, 5),
    CYBORG("Cyborg", "Armor"),
    BEEKEEPER("BeeKeeper", "Armor"),
    MANEUVER("Maneuver", "Armor", 10, 5),
    CROUCH("Crouch", "Armor", 10, 5),
    SHOCKWAVE("Shockwave", "Armor", 10, 5, "Chestplate"),
    SYSTEMREBOOT("SystemReboot", "Armor", 10, 5),
    //	----------------Axes----------------  \\
    REKT("Rekt", "Axe", 5, 1),
    CURSED("Cursed", "Axe", 10, 5),
    BLESSED("Blessed", "Axe", 10, 5),
    BATTLECRY("BattleCry", "Axe", 10, 5),
    DEMONFORGED("DemonForged", "Axe", 10, 5),
    //	----------------PickAxes----------------  \\
    VEINMINER("VeinMiner", "Pickaxe"),
    BLAST("Blast", "Pickaxe"),
    //	----------------Tools----------------  \\
    //	----------------Hoes----------------  \\
    GREENTHUMB("GreenThumb", "Hoe", 10, 10),
    HARVESTER("Harvester", "Hoe"),
    TILLER("Tiller", "Hoe"),
    PLANTER("Planter", "Hoe"),
    //	----------------All----------------  \\
    HELLFORGED("HellForged", "Damaged-Items", 5, 5),
    //IMPERIUM: Simple Enchantments
    //Armour
    AQUATIC("Aquatic", "Helmet"),
    GLOWING("Glowing", "Helmet"),
    SHUFFLE("Shuffle", "Armor", 4, 2, 400L, 40L),
    //weapons
    HEADLESS("Headless", "Sword", 20, 20),
    OBLITERATE("Obliterate", "Sword", 10, 5, 400L, 40L),
    CONFUSION("Confusion", "Sword", 15, 5, 80L, -20L),
    INSOMNIA("Insomnia", "Swords", 10, 2, 200L, 10L),
    DECAPITATION("Decapitation", "Axe", 20, 20),
    DIZZY("Dizzy", "Axe", 15, 5, 80L, -20L),
    LIGHTNING("Lightning", "Bow", 24, 8, 60L),//should lightning only strike if entity is hit?
    //tools
    AUTOSMELT("AutoSmelt", "Pickaxe", 30, 35),
    EXPERIENCE("Experience", "Pickaxe", 15, 15, 20L),
    FURNACE("Furnace", "Pickaxe", 100L),
    OXYGENATE("Oxygenate", "Tool", 60L),
    HASTE("Haste", "Tool"),
    //missing
    //EPICNESS kinda poinless
    //THUNDERINGBLOW thunder i think it was just strike lighting more as an effect enchant
    //ETHERAL haste upon killing mobs kinda pointless
    //Strike strikes lighting on impact with the ground

    //IMPERIUM: Unique Enchantments
    //ARMOUR
    LIFEBLOOM("Lifebloom", "Sword", 20, 10, 200L, -25L),
    FAMISHED("Famished", "Sword", 12, 4, 8L), //famine
    OBBYDESTROYER("ObbyDestroyer", "Pickaxe", 20, 15, 160L, 20L),//obsidian destoryer
    BERSERK("Berserk", "Axe", 4, 4, 160L, 20L),
    WARD("Ward", "Armor", 3, 4, 100L),
    CURSE("Curse", "Armor", 10, 2, "Chestplate"), //10, 2, 140L)
    HULK("Hulk", "Armor"),//curse??
    ADRENALINE("Adrenaline", "Boots", 15, 15, 120L, -40L),//endershift
    BOOM("Boom", "Bow", 20, 10, 8L),//explosive
    LIGHTWEIGHT("LightWeight", "Sword", 35, 20, 40L),//featherweight
    MOLTEN("Molten", "Armor", 30, 10, 40L),//molten
    RADIANT("Radiant", "Armor"),//molten??
    FEEDME("FeedMe", "Axe", 14, 4, 40L),//ravenous
    NUTRITION("Nutrition", "Sword", 14, 4, 40L),//ravenous but for swords
    COMMANDER("Commander", "Helmet", 9, 3, 120L),
    SELFDESTRUCT("SelfDestruct", "Armor", 24, 10, 80L),
    TELEPATHY("Telepathy", "Tool", 40, 20),
    //sustain missing SUSTAIN("Sustain", "Armor", 12, 2, 160L)
    SKILLSWIPE("SkillSwipe", "Sword", 15, 2, 100L),
    //plague carrier PLAGUECARRIER("PlagueCarrier", "Armor", 20, 10, 60L)
    VIRUS("Virus", "Bow", 16, 4),

    //NEW - Imperium
    POISONED("Poisoned", "Armor", 10, 5),
    HARDENED("Hardened", "Armor", 30, 10),
    LONGBOW("Longbow", "Bow", 30, 10),
    UNFOCUS("Unfocus", "Bow", 10, 5),
    ENDERSLAYER("Enderslayer", "Sword", 50, 10),
    REAPER("Reaper", "Axe", 10, 3),
    //Modified: Reaper now scales damage based on XP, and will no longer apply wither effects.
    //This is final.

    NETHERSLAYER("Netherslayer", "Sword", 50, 10),
    SHACKLE("Shackle", "Sword", 70, 10),
    REFORGED("Reforged", "Tool", 10, 10),
    GREATSWORD("Greatsword", "Sword", 40, 10),
    TRICKSTER("Trickster", "Armor", 25, 6),
    PUMMEL("Pummel", "Axe", 30, 5),
    FARCAST("Farcast", "Bow", 10, 5),
    MARKSMAN("Marksman", "Armor", 60, 10),
    ANGELIC("Angelic", "Armor", 20, 5),
    JELLYLEGS("Jellylegs", "Boots", 10, 3),
    CLEAVE("Cleave", "Axe", 15, 5),
    DOMINATE("Dominate", "Swords", 10, 3),
    ENDERWALKER("Enderwalker", "Boots", 8, 3),
    INFERNAL("Infernal", "Bow", 7, 4),
    TANK("Tank", "Armor", 16, 4),
    BLOCK("Block", "Sword", 11, 11),
    CORRUPT("Corrupt", "Axe", 20, 7),
    CREEPERARMOR("CreeperArmor", "Armor", 10, 3),
    DEMONIC("Demonic", "Sword", 10, 3),
    METAPHYSICAL("Metaphysical", "Boots", 50, 10),
    DISTANCE("Distance", "Sword", 14, 3),
    //SNIPER(Exists, but was redone. Putting comment here for tracking purposes.)
    FAT("Fat", "Armor", 12, 2, "Chestplate"),
    DEATHBRINGER("Deathbringer", "Armor", 14, 7),
    DESTRUCTION("Destruction", "Helmet", 10, 5),
    DEATHGOD("DeathGod", "Helmet", 8, 3),
    INSANITY("Insanity", "Axe", 50, 8),
    DIMINISH("Diminish", "Armor", 20, 6, "Chestplate"),
    BARBARIAN("Barbarian", "Axe", 20, 4),
    ABIDING("Abiding", "Tool"),
    QUIVER("Quiver", "Boots", 10, 7),
    INVERSION("Inversion", "Sword", 20, 4),
    BLEED("Bleed", "Axe", 28, 4),
    DEVOUR("Devour", "Axe", 10, 5),
    ARMORED("Armored", "Armor", 60, 10),
    CLARITY("Clarity", "Armor"),
    SILENCE("Silence", "Sword", 30, 10),
    KILLAURA("Killaura", "Sword", 18, 5),
    STUN("Stun", "Sword", 10, 10),
    JUDGEMENT("Judgement", "Armor", 10, 10),
    BLACKSMITH("Blacksmith", "Axe", 30, 10),
    RAGDOLL("Ragdoll", "Armor", 30, 5),
    ARROWBREAK("Arrowbreak", "Axe", 10, 10),
    ARROWDEFLECT("Arrowdeflect", "Armor", 10, 10),
    ARROWLIFESTEAL("ArrowLifesteal", "Bow", 10, 10),
    HELLFIRE("Hellfire", "Bow", 15, 10, 60L),
    HEAVY("Heavy", "Armor", 10, 5, 20L),
    REINFORCED("Reinforced", "Armor", 20, 10),
    TIMBER("Timber", "Axe", 10, 5),
    SPIRITS("Spirits", "Armor", 10, 10, 600L),
    //SUGGESTED - Imperium
    SWARM("Swarm", "Sword"),

    //HEROIC
    MIGHTYCACTUS("MightyCactus", "Armor", 10, 10, true, CEnchantments.CACTUS),
    DEEPBLEED("DeepBleed", "Axe", 10, 10, true, CEnchantments.BLEED),
    BIDIRECTIONAL("BidirectionalTeleportation", "Bow", 15, 10, true, null),

    // Trident //
    IMPACT("Impact", "Trident", 15, 10),
    TWINGE("Twinge", "Trident", 40, 8),
    AURA("Aura", "Trident")

    ;


    @NotNull
    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    @NotNull
    private final Starter starter = this.plugin.getStarter();

    @NotNull
    private final CrazyManager crazyManager = this.starter.getCrazyManager();

    @NotNull
    private final Methods methods = this.starter.getMethods();

    private final String name;
    private final String typeName;
    private final String miscTypeName;
    private final boolean hasChanceSystem;
    private final int chance;
    private final int chanceIncrease;
    private final CEnchantments oldEnchant;
    private final boolean isHeroic;
    private final long cooldown;
    private final long cooldownDecrease;

    private CEnchantment cachedEnchantment = null;

    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public static void invalidateCachedEnchants() {
        for (CEnchantments value : values()) {
            value.cachedEnchantment = null;
        }
    }

    /**
     * @param name Name of the enchantment.
     * @param typeName Type of items it goes on.
     */
    CEnchantments(String name, String typeName) {
        this.name = name;
        this.typeName = typeName;
        this.chance = 0;
        this.chanceIncrease = 0;
        this.hasChanceSystem = false;
        this.isHeroic = false;
        this.oldEnchant = null;
        this.cooldown = 0;
        this.cooldownDecrease = 0;
        this.miscTypeName = null;
    }

    CEnchantments(String name, String typeName, String miscTypeName) {
        this.name = name;
        this.typeName = typeName;
        this.miscTypeName = miscTypeName;
        this.chance = 0;
        this.chanceIncrease = 0;
        this.hasChanceSystem = false;
        this.isHeroic = false;
        this.oldEnchant = null;
        this.cooldown = 0;
        this.cooldownDecrease = 0;
    }

    /**
     * @param name Name of the enchantment.
     * @param typeName Type of items it goes on.
     * @param chance The chance the enchantment has to activate.
     * @param chanceIncrease The amount the chance increases per level.
     */
    CEnchantments(String name, String typeName, int chance, int chanceIncrease) {
        this.name = name;
        this.typeName = typeName;
        this.chance = chance;
        this.chanceIncrease = chanceIncrease;
        this.hasChanceSystem = true;
        this.isHeroic = false;
        this.oldEnchant = null;
        this.cooldown = 0;
        this.cooldownDecrease = 0;
        this.miscTypeName = null;
    }

    CEnchantments(String name, String typeName, int chance, int chanceIncrease, String miscTypeName) {
        this.name = name;
        this.typeName = typeName;
        this.chance = chance;
        this.chanceIncrease = chanceIncrease;
        this.miscTypeName = miscTypeName;
        this.hasChanceSystem = true;
        this.isHeroic = false;
        this.oldEnchant = null;
        this.cooldown = 0;
        this.cooldownDecrease = 0;
    }

    /**
     *
     * @param name Name of the enchantment.
     * @param typeName Type of items it goes on.
     * @param chance The chance the enchantment has to activate.
     * @param chanceIncrease The amount the chance increases per level.
     * @param cooldown The amount of time, in ticks, that must elapse before the enchant can proc again. Must be a long.
     */
    CEnchantments(String name, String typeName, int chance, int chanceIncrease, long cooldown) {
        this.name = name;
        this.typeName = typeName;
        this.chance = chance;
        this.chanceIncrease = chanceIncrease;
        this.hasChanceSystem = true;
        this.isHeroic = false;
        this.oldEnchant = null;
        this.cooldown = cooldown;
        this.cooldownDecrease = 0;
        this.miscTypeName = null;
    }

    CEnchantments(String name, String typeName, int chance, int chanceIncrease, long cooldown, String miscTypeName) {
        this.name = name;
        this.typeName = typeName;
        this.chance = chance;
        this.chanceIncrease = chanceIncrease;
        this.cooldown = cooldown;
        this.miscTypeName = miscTypeName;
        this.hasChanceSystem = true;
        this.isHeroic = false;
        this.cooldownDecrease = 0;
        this.oldEnchant = null;
    }
    /**
     *
     * @param name Name of the enchantment.
     * @param typeName Type of items it goes on.
     * @param chance The chance the enchantment has to activate.
     * @param chanceIncrease The amount the chance increases per level.
     * @param cooldown The amount of time, in ticks, that must elapse before the enchant can proc again. Must be a long.
     * @param cooldownDecrease The amount of time, in ticks, that must elapse before the enchant can proc again. Must be a long.
     */
    CEnchantments(String name, String typeName, int chance, int chanceIncrease, long cooldown, long cooldownDecrease) {
        this.name = name;
        this.typeName = typeName;
        this.chance = chance;
        this.chanceIncrease = chanceIncrease;
        this.hasChanceSystem = true;
        this.isHeroic = false;
        this.oldEnchant = null;
        this.cooldown = cooldown;
        this.cooldownDecrease = cooldownDecrease;
        this.miscTypeName = null;
    }

    /**
     *
     * @param name Name of the enchantment.
     * @param typeName Type of item it goes on.
     * @param chance The chance the enchantment has to activate.
     * @param chanceIncrease The amount the chance increases per level.
     * @param cooldown The amount of time, in ticks, that must elapse before the enchant can proc again. Must be a long.
     * @param isHeroic Whether the enchantment is heroic. Returns false if left empty.
     * @param oldEnchant The enchantment this will replace, if isHeroic is true. Returns null if left empty.
     */
    CEnchantments(String name, String typeName, int chance, int chanceIncrease, long cooldown, boolean isHeroic, CEnchantments oldEnchant) {
        this.name = name;
        this.typeName = typeName;
        this.chance = chance;
        this.chanceIncrease = chanceIncrease;
        this.hasChanceSystem = true;
        this.cooldown = cooldown;
        this.isHeroic = isHeroic;
        this.oldEnchant = oldEnchant;
        this.cooldownDecrease = 0;
        this.miscTypeName = null;
    }

    /**
     *
     * @param name Name of the enchantment.
     * @param typeName Type of items it goes on.
     * @param isHeroic Whether the enchantment is heroic. False by default.
     */
    CEnchantments(String name, String typeName, boolean isHeroic) {
        this.name = name;
        this.typeName = typeName;
        this.chance = 0;
        this.chanceIncrease = 0;
        this.hasChanceSystem = false;
        this.isHeroic = isHeroic;
        this.oldEnchant = null;
        this.cooldown = 0;
        this.cooldownDecrease = 0;
        this.miscTypeName = null;
    }
    CEnchantments(String name, String typeName, long cooldown) {
        this.name = name;
        this.typeName = typeName;
        this.cooldown = cooldown;
        this.chance = 0;
        this.chanceIncrease = 0;
        this.hasChanceSystem = false;
        this.isHeroic = false;
        this.oldEnchant = null;
        this.cooldownDecrease = 0;
        this.miscTypeName = null;
    }

    /**
     *
     * @param name Name of the enchantment.
     * @param typeName Type of items it goes on.
     * @param chance The chance the enchantment has to activate.
     * @param chanceIncrease The amount the chance increases per level.
     * @param isHeroic Whether the enchantment is heroic. False by default.
     * @param oldEnchant The enchantment being replaced, if the enchantment is heroic.
     */
    CEnchantments(String name, String typeName, int chance, int chanceIncrease, boolean isHeroic, CEnchantments oldEnchant) {
        this.name = name;
        this.typeName = typeName;
        this.chance = chance;
        this.chanceIncrease = chanceIncrease;
        this.hasChanceSystem = true;
        this.isHeroic = isHeroic;
        this.oldEnchant = oldEnchant;
        this.cooldown = 0;
        this.cooldownDecrease = 0;
        this.miscTypeName = null;
    }


    /**
     * @return The name of the enchantment.
     */
    public String getName() {
        return this.name;
    }

    /**
     *
     * @return The type of the enchantment, as a string.
     */
    public String getTypeName() {
        return this.typeName;
    }

    public String getMiscTypeName() {
        return this.miscTypeName;
    }

    /**
     * @return The custom name in the Enchantment.yml.
     */
    public String getCustomName() {
        return getEnchantment().getCustomName();
    }

    /**
     * Get the chance the enchantment will activate.
     * Not all enchantments have a chance to activate.
     *
     * @return The chance of the enchantment activating.
     */
    public int getChance() {
        return this.chance;
    }

    /**
     * Get the amount the enchantment chance increases by every level.
     * Not all enchantments have a chance to activate.
     *
     * @return The amount the chance increases by every level.
     */
    public int getChanceIncrease() {
        return this.chanceIncrease;
    }

    /**
     * @return The description of the enchantment in the Enchantments.yml.
     */
    public List<String> getDescription() {
        return getEnchantment().getInfoDescription();
    }

    /**
     * @return The type the enchantment is.
     */
    public EnchantmentType getType() {
        if (getEnchantment() == null || getEnchantment().getEnchantmentType() == null) {
            return this.methods.getFromName(this.typeName);
        } else {
            return getEnchantment().getEnchantmentType();
        }
    }

    /**
     * @return True if the enchantment is enabled and false if not.
     */
    public boolean isActivated() {
        return getEnchantment() != null && getEnchantment().isActivated();
    }

    /**
     * Get the enchantment that this is tied to.
     * @return The enchantment this is tied to.
     */
    public CEnchantment getEnchantment() {
        if (this.cachedEnchantment == null) this.cachedEnchantment = this.crazyManager.getEnchantmentFromName(this.name);

        return this.cachedEnchantment;
    }

    /**
     * Check to see if the enchantment's chance is successful.
     * @return True if the chance was successful and false if not.
     */
    public boolean chanceSuccessful(int level) {
        return this.chanceSuccessful(level, 1.0);
    }

    /**
     * Check to see if the enchantment's chance is successful.
     * @return True if the chance was successful and false if not.
     */
    public boolean chanceSuccessful(int level, double multiplier) {
        return getEnchantment().chanceSuccessful(level, multiplier);
    }

    /**
     * Check if the CEnchantments uses a chance system.
     */
    public boolean hasChanceSystem() {
        return this.hasChanceSystem;
    }

    /**
     * Checks if the enchantment is marked as heroic.
     * @return True if the enchantment is heroic, false if not.
     */
    public boolean isHeroic() {
        return this.isHeroic;
    }

    /**
     * Gets the enchantment this enchant will replace when it is upgraded to its heroic variant.
     * @return The enchantment that matches the condition, if present. Otherwise, returns null.
     */
    @ApiStatus.Experimental
    public CEnchantments getOldEnchant() {
        return this.oldEnchant;
    }


    /**
     * Check if the enchantment is off cooldown for a specific player.
     * If off cooldown and `applyCooldown` is true, it sets the cooldown.
     *
     * @param playerUUID UUID of a player using the enchantment.
     * @param applyCooldown Whether to apply the cooldown after check.
     * @return True if cooldown is over, false otherwise.
     */
    public boolean isOffCooldown(UUID playerUUID, int level, boolean applyCooldown) {
        if (cooldown <= 0) return true;

        long now = System.currentTimeMillis();
        long appliedCooldownTicks = Math.max(0L, cooldown - (cooldownDecrease * (level - 1)));
        long cooldownMs = appliedCooldownTicks * 50;
        long lastUsed = cooldowns.getOrDefault(playerUUID, 0L);

        if (now - lastUsed >= cooldownMs) {
            if (applyCooldown) {
                cooldowns.put(playerUUID, now);
            }
            return true;
        }

        return false;
    }
}
