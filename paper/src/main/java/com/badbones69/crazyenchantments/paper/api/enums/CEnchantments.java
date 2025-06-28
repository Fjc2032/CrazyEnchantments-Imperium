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
    ADRENALINE("Adrenaline", "Boots", 10, 5),
    ROCKET("Rocket", "Boots", 15, 5),
    SPRINGS("Springs", "Boots"),
    ANTIGRAVITY("AntiGravity", "Boots"),
    //	----------------Bows----------------  \\
    BOOM("Boom", "Bow", 20, 10),
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
    COMMANDER("Commander", "Helmet"),
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
    NUTRITION("Nutrition", "Sword", 15, 5),
    SKILLSWIPE("SkillSwipe", "Sword", 5, 5),
    INQUISITIVE("Inquisitive", "Sword", 50, 25),
    LIGHTWEIGHT("LightWeight", "Sword", 15, 5),
    DOUBLEDAMAGE("DoubleDamage", "Sword", 5, 1),
    DISORDER("Disorder", "Sword", 1, 0),
    CHARGE("Charge", "Sword"),
    LIFEBLOOM("Lifebloom", "Sword"),
    FAMISHED("Famished", "Sword", 10, 5),
    //	----------------Armor----------------  \\
    HULK("Hulk", "Armor"),
    VALOR("Valor", "Armor"),
    DRUNK("Drunk", "Armor"),
    NINJA("Ninja", "Armor"),
    ANGEL("Angel", "Armor"),
    TAMER("Tamer", "Armor"),
    GUARDS("Guards", "Armor"),
    VOODOO("Voodoo", "Armor", 15, 5),
    MOLTEN("Molten", "Armor", 10, 1),
    SAVIOR("Savior", "Armor", 15, 5),
    CACTUS("Cactus", "Armor", 25, 25),
    FREEZE("Freeze", "Armor", 10, 5),
    RECOVER("Recover", "Armor"),
    NURSERY("Nursery", "Armor", 5, 5),
    RADIANT("Radiant", "Armor"),
    FORTIFY("Fortify", "Armor", 10, 5),
    OVERLOAD("OverLoad", "Armor"),
    BLIZZARD("Blizzard", "Armor"),
    ACIDRAIN("AcidRain", "Armor", 5, 5),
    SANDSTORM("SandStorm", "Armor", 5, 5),
    SMOKEBOMB("SmokeBomb", "Armor", 5, 5),
    PAINGIVER("PainGiver", "Armor", 10, 5),
    INTIMIDATE("Intimidate", "Armor"),
    BURNSHIELD("BurnShield", "Armor"),
    LEADERSHIP("Leadership", "Armor", 10, 5),
    INFESTATION("Infestation", "Armor"),
    NECROMANCER("Necromancer", "Armor"),
    STORMCALLER("StormCaller", "Armor", 10, 5),
    ENLIGHTENED("Enlightened", "Armor", 10, 5),
    SELFDESTRUCT("SelfDestruct", "Armor"),
    CYBORG("Cyborg", "Armor"),
    BEEKEEPER("BeeKeeper", "Armor"),
    MANEUVER("Maneuver", "Armor", 10, 5),
    CROUCH("Crouch", "Armor", 10, 5),
    SHOCKWAVE("Shockwave", "Armor", 10, 5),
    SYSTEMREBOOT("SystemReboot", "Armor", 10, 5),
    //	----------------Axes----------------  \\
    REKT("Rekt", "Axe", 5, 1),
    CURSED("Cursed", "Axe", 10, 5),
    FEEDME("FeedMe", "Axe", 10, 5),
    BERSERK("Berserk", "Axe", 10, 1),
    BLESSED("Blessed", "Axe", 10, 5),
    BATTLECRY("BattleCry", "Axe", 10, 5),
    DEMONFORGED("DemonForged", "Axe", 10, 5),
    //	----------------PickAxes----------------  \\
    VEINMINER("VeinMiner", "Pickaxe"),
    BLAST("Blast", "Pickaxe"),
    //	----------------Tools----------------  \\
    TELEPATHY("Telepathy", "Tool"),
    //	----------------Hoes----------------  \\
    GREENTHUMB("GreenThumb", "Hoe", 10, 10),
    HARVESTER("Harvester", "Hoe"),
    TILLER("Tiller", "Hoe"),
    PLANTER("Planter", "Hoe"),
    //	----------------All----------------  \\
    HELLFORGED("HellForged", "Damaged-Items", 5, 5),
    //Imperiem Simple Enchantments
    //armour
    AQUATIC("Aquatic", "Helmet"),
    GLOWING("Glowing", "Helmet"),
    SHUFFLE("Shuffle", "Armor", 5, 1),
    //weapons
    HEADLESS("Headless", "Sword", 20, 20),
    OBLITERATE("Obliterate", "Sword", 10, 5),
    CONFUSION("Confusion", "Sword", 15, 5),
    INSOMNIA("Insomnia", "Swords", 10, 2),
    DECAPITATION("Decapitation", "Axe", 20, 20),
    DIZZY("Dizzy", "Axe", 15, 5),
    LIGHTNING("Lightning", "Bow", 24, 8),
    //tools
    AUTOSMELT("AutoSmelt", "Pickaxe", 30, 35, 100L),
    EXPERIENCE("Experience", "Pickaxe", 15, 15),
    FURNACE("Furnace", "Pickaxe", 100L),
    OXYGENATE("Oxygenate", "Tool"),
    HASTE("Haste", "Tool"),
    //missing
    //EPICNESS kinda poinless
    //THUNDERINGBLOW thunder i think it was just strike lighting more as an effect enchant
    //ETHERAL haste upon killing mobs kinda pointless
    //Strike a Trident lighting enchant but tridents do not exist in this plugin yet 
    
    //NEW - Imperium
    POISONED("Poisoned", "Armor", 10, 5),
    HARDENED("Hardened", "Armor", 30, 10),
    LONGBOW("Longbow", "Bow", 30, 10),
    UNFOCUS("Unfocus", "Bow", 10, 5),
    WARD("Ward", "Armor", 10, 5),
    VIRUS("Virus", "Bow", 16, 4),
    OBBYDESTROYER("ObbyDestroyer", "Pickaxe", 20, 15),
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
    FAT("Fat", "Armor", 12, 2),
    DEATHBRINGER("Deathbringer", "Armor", 14, 7),
    DESTRUCTION("Destruction", "Armor", 10, 5),
    DEATHGOD("DeathGod", "Armor", 8, 3),
    INSANITY("Insanity", "Axe", 50, 8),
    DIMINISH("Diminish", "Armor", 20, 6),
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
    CURSE("Curse", "Armor", 40, 10),
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
    MIGHTYCACTUS("MightyCactus", "Armor", 10, 10, true, CEnchantments.CACTUS.getEnchantment()),
    DEEPBLEED("DeepBleed", "Axe", 10, 10, true, CEnchantments.BLEED.getEnchantment()),
    BIDIRECTIONAL("BidirectionalTeleportation", "Bow", 15, 10, true, null),

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
    private final boolean hasChanceSystem;
    private final int chance;
    private final int chanceIncrease;
    private final CEnchantment oldEnchant;
    private final boolean isHeroic;
    private final long cooldown;

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
    CEnchantments(String name, String typeName, int chance, int chanceIncrease, long cooldown, boolean isHeroic, CEnchantment oldEnchant) {
        this.name = name;
        this.typeName = typeName;
        this.chance = chance;
        this.chanceIncrease = chanceIncrease;
        this.hasChanceSystem = true;
        this.cooldown = cooldown;
        this.isHeroic = isHeroic;
        this.oldEnchant = oldEnchant;
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
    CEnchantments(String name, String typeName, int chance, int chanceIncrease, boolean isHeroic, CEnchantment oldEnchant) {
        this.name = name;
        this.typeName = typeName;
        this.chance = chance;
        this.chanceIncrease = chanceIncrease;
        this.hasChanceSystem = true;
        this.isHeroic = isHeroic;
        this.oldEnchant = oldEnchant;
        this.cooldown = 0;
    }

    
    /**
     * @return The name of the enchantment.
     */
    public String getName() {
        return this.name;
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
    @Nullable
    public CEnchantment getOldEnchant() {
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
    public boolean isOffCooldown(UUID playerUUID, boolean applyCooldown) {
        if (cooldown <= 0) return true;

        long now = System.currentTimeMillis();
        long cooldownMs = cooldown * 50;
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
