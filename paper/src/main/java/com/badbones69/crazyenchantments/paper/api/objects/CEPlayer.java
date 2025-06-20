package com.badbones69.crazyenchantments.paper.api.objects;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.Methods;
import com.badbones69.crazyenchantments.paper.api.enums.CEnchantments;
import com.badbones69.crazyenchantments.paper.api.objects.gkitz.GKitz;
import com.badbones69.crazyenchantments.paper.api.objects.gkitz.GkitCoolDown;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

//todo() register gkit permissions
public class CEPlayer {

    @NotNull
    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    @NotNull
    private final Methods methods = this.plugin.getStarter().getMethods();

    private final Player player;
    private final List<GkitCoolDown> gkitCoolDowns;
    private Double rageMultiplier;
    private boolean hasRage;
    private int rageLevel;
    private ScheduledTask rageTask;
    private final Set<CEnchantments> onCooldown = new HashSet<>();
    
    /**
     * Used to make a new CEPlayer.
     * @param player The player.
     * @param gkitCoolDowns The cool-downs the player has.
     */
    public CEPlayer(Player player, List<GkitCoolDown> gkitCoolDowns) {
        this.player = player;
        this.gkitCoolDowns = gkitCoolDowns;
        this.hasRage = false;
        this.rageLevel = 0;
        this.rageMultiplier = 0.0;
        this.rageTask = null;
    }
    
    /**
     * Get the player from the CEPlayer.
     * @return Player from the CEPlayer.
     */
    public Player getPlayer() {
        return this.player;
    }
    
    /**
     * Give a player a gkit.
     * @param kit The gkit you wish to give them.
     */
    public void giveGKit(GKitz kit) {
        for (ItemStack item : kit.getKitItems()) {
            if (kit.canAutoEquip()) {
                switch (item.getType().toString().contains("_") ? item.getType().toString().toLowerCase().split("_")[1] : "No") {
                    case "helmet" -> {
                        if (this.player.getEquipment().getHelmet() != null) break;
                        this.player.getEquipment().setHelmet(item);
                        continue;
                    }
                    case "chestplate" -> {
                        if (this.player.getEquipment().getChestplate() != null) break;
                        this.player.getEquipment().setChestplate(item);
                        continue;
                    }
                    case "leggings" -> {
                        if (this.player.getEquipment().getLeggings() != null) break;
                        this.player.getEquipment().setLeggings(item);
                        continue;
                    }
                    case "boots" -> {
                        if (this.player.getEquipment().getBoots() != null) break;
                        this.player.getEquipment().setBoots(item);
                        continue;
                    }
                }

            }

            this.methods.addItemToInventory(this.player, item);
        }

        for (String cmd : kit.getCommands()) {
            this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), cmd
            .replace("%Player%", this.player.getName()).replace("%player%", this.player.getName()));
        }
    }
    
    /**
     * If the player has permission to use the gkit.
     * @param kit The gkit you are checking.
     * @return True if they can use it and false if they can't.
     */
    public boolean hasGkitPermission(GKitz kit) {
        return this.player.hasPermission("crazyenchantments.bypass.gkitz") || this.player.hasPermission("crazyenchantments.gkitz." + kit.getName().toLowerCase());
    }
    
    /**
     * If the player can use the gkit. Checks their cool-downs and permissions.
     * @param kit The gkit you want to check.
     * @return True if they don't have a cool-down, and they have permission.
     */
    public boolean canUseGKit(GKitz kit) {
        if (this.player.hasPermission("crazyenchantments.bypass.gkitz")) {
            return true;
        } else {
            if (this.player.hasPermission("crazyenchantments.gkitz." + kit.getName().toLowerCase())) {
                for (GkitCoolDown gkitCooldown : getCoolDowns()) {
                    if (gkitCooldown.getGKitz() == kit) return gkitCooldown.isCoolDownOver();
                }
            } else {
                return false;
            }
        }

        return true;
    }
    
    /**
     * Get all the cool-downs the player has.
     * @return The cool-downs the player has.
     */
    public List<GkitCoolDown> getCoolDowns() {
        return this.gkitCoolDowns;
    }
    
    /**
     * Get a cool-down of a gkit.
     * @param kit The gkit you are checking.
     * @return The cool-down object the player has.
     */
    public GkitCoolDown getCoolDown(GKitz kit) {
        for (GkitCoolDown gkitCoolDown : this.gkitCoolDowns) {
            if (gkitCoolDown.getGKitz() == kit) return gkitCoolDown;
        }

        return null;
    }
    
    /**
     * Add a cool-down to a player.
     * @param gkitCoolDown The cool-down you are adding.
     */
    public void addCoolDown(GkitCoolDown gkitCoolDown) {
        List<GkitCoolDown> playerGkitCoolDowns = new ArrayList<>();

        for (GkitCoolDown c : getCoolDowns()) {
            if (c.getGKitz().getName().equalsIgnoreCase(gkitCoolDown.getGKitz().getName())) playerGkitCoolDowns.add(c);
        }

        this.gkitCoolDowns.removeAll(playerGkitCoolDowns);
        this.gkitCoolDowns.add(gkitCoolDown);
    }
    
    /**
     * Add a cool-down of a gkit to a player.
     * @param kit The gkit you want to get the cool-down for.
     */
    public void addCoolDown(GKitz kit) {
        Calendar coolDown = Calendar.getInstance();

        for (String i : kit.getCooldown().toLowerCase().split(" ")) {
            if (i.contains("d")) coolDown.add(Calendar.DATE, Integer.parseInt(i.replace("d", "")));

            if (i.contains("h")) coolDown.add(Calendar.HOUR, Integer.parseInt(i.replace("h", "")));

            if (i.contains("m")) coolDown.add(Calendar.MINUTE, Integer.parseInt(i.replace("m", "")));

            if (i.contains("s")) coolDown.add(Calendar.SECOND, Integer.parseInt(i.replace("s", "")));
        }

        addCoolDown(new GkitCoolDown(kit, coolDown));
    }
    
    /**
     * Remove a cool-down from a player.
     * @param gkitCoolDown The cool-down you want to remove.
     */
    public void removeCoolDown(GkitCoolDown gkitCoolDown) {
        this.gkitCoolDowns.remove(gkitCoolDown);
    }
    
    /**
     * Remove a cool-down from a player.
     * @param kit The gkit cool-down you want to remove.
     */
    public void removeCoolDown(GKitz kit) {
        List<GkitCoolDown> playerGkitCoolDowns = new ArrayList<>();

        for (GkitCoolDown gkitCoolDown : getCoolDowns()) {
            if (gkitCoolDown.getGKitz().getName().equalsIgnoreCase(kit.getName())) playerGkitCoolDowns.add(gkitCoolDown);
        }

        this.gkitCoolDowns.removeAll(playerGkitCoolDowns);
    }
    
    /**
     * Get the player's rage damage multiplier.
     */
    public Double getRageMultiplier() {
        return this.rageMultiplier;
    }
    
    /**
     * Set the player's rage damage multiplier.
     * @param rageMultiplier The player's new rage damage multiplier.
     */
    public void setRageMultiplier(Double rageMultiplier) {
        this.rageMultiplier = rageMultiplier;
    }
    
    /**
     * Check if the player is in rage.
     */
    public boolean hasRage() {
        return this.hasRage;
    }
    
    /**
     * Toggle on/off the player's rage.
     * @param hasRage If the player has rage.
     */
    public void setRage(boolean hasRage) {
        this.hasRage = hasRage;
    }
    
    /**
     * Get the level of rage the player is in.
     */
    public int getRageLevel() {
        return this.rageLevel;
    }
    
    /**
     * Set the level of rage the player is in.
     * @param rageLevel The player's new rage level.
     */
    public void setRageLevel(int rageLevel) {
        this.rageLevel = rageLevel;
    }
    
    /**
     * Get the cooldown task the player's rage has till they calm down.
     */
    public ScheduledTask getRageTask() {
        return this.rageTask;
    }
    
    /**
     * Set the new cooldown task for the player's rage.
     * @param rageTask The new cooldown task for the player.
     */
    public void setRageTask(ScheduledTask rageTask) {
        this.rageTask = rageTask;
    }

    /**
     * Checks if the player currently has the specified enchant on cooldown.
     * If not on cooldown, adds one to the player for the specified enchant.
     * @param enchant {@link CEnchantments} to check for.
     * @param delay Delay in ticks to add a cooldown for.
     * @return True if they already had a cooldown.
     */
    public boolean onEnchantCooldown(CEnchantments enchant, int delay) {
        if (this.onCooldown.contains(enchant)) return true;

        this.onCooldown.add(enchant);
        // Limit players to using each enchant only once per second.
        this.plugin.getServer().getAsyncScheduler().runDelayed(this.plugin, (task) -> this.onCooldown.remove(enchant), delay * 50L, TimeUnit.MILLISECONDS);

        return false;
    }
}