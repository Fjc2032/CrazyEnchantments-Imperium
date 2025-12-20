package com.badbones69.crazyenchantments.paper.controllers;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.Methods;
import com.badbones69.crazyenchantments.paper.Starter;
import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import com.badbones69.crazyenchantments.paper.controllers.settings.EnchantmentBookSettings;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
@ApiStatus.Experimental
public class AttributeController {

    @NotNull
    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    @NotNull
    private final Starter starter = this.plugin.getStarter();

    @NotNull
    private final Methods methods = this.starter.getMethods();

    @NotNull
    private final EnchantmentBookSettings enchantmentBookSettings = this.starter.getEnchantmentBookSettings();

    @NotNull
    private static final Map<UUID, AttributeController> dataset = new ConcurrentHashMap<>();

    @NotNull
    private final Collection<Attribute> attributes = new ConcurrentLinkedDeque<>();

    @NotNull
    private final Collection<AttributeModifier> modifiers = new ConcurrentLinkedDeque<>();

    private Attribute attribute;

    private AttributeModifier modifier;

    private ScheduledTask[] task;

    public boolean isAttributeHandlingEnabled = true;

    /**
     * Adds an associated modifier to the specified player
     * @param player The player to target
     * @param attribute The attribute
     * @param modifier The modifier
     * @return The resulting player for chaining, or null if not applicable
     * @apiNote May return null if the specified attribute/modifier can't be applied to the player
     */
    public @Nullable Player addAttributes(Player player, Attribute attribute, AttributeModifier modifier) {
        var instance = player.getAttribute(attribute);
        if (instance == null) return null;

        NamespacedKey identifier = modifier.getKey();
        AttributeModifier mapper = instance.getModifier(identifier);


        if (instance.getModifiers().contains(mapper)) return player;
        instance.addModifier(modifier);

        return player;
    }

    public boolean addAttribute(Player player, Attribute attribute, NamespacedKey key, double amount) {
        AttributeInstance inst = player.getAttribute(attribute);
        if (inst == null) return false;

        if (inst.getModifier(key) != null) return false;

        AttributeModifier modifier1 = new AttributeModifier(key, amount, AttributeModifier.Operation.ADD_NUMBER);


        inst.addModifier(modifier1);
        add(attribute, modifier1);
        return true;
    }

    public boolean removeModifier(Player player, Attribute attribute, NamespacedKey key) {
        AttributeInstance inst = player.getAttribute(attribute);
        if (inst == null) return false;

        if (inst.getModifier(key) == null) return false;

        inst.removeModifier(key);
        remove(attribute, key);
        return true;
    }

    /**
     * Removes the associated modifier from the player
     * @param player The player to target
     * @param attribute The attribute
     * @param modifier The modifier's identifiable key
     * @return The resulting player, for chaining
     */
    public Player removeAttribute(Player player, Attribute attribute, NamespacedKey modifier) {
        var instance = player.getAttribute(attribute);
        if (instance == null) return null;

        instance.removeModifier(modifier);

        return player;
    }

    /**
     * Starts a task calculation that will remove the attribute modifier when the item is no longer valid.
     * <p>
     * This function is probably not performance friendly, due to the fact that it executes frequently and can theoretically run forever.
     * Use with caution.
     * @param player The player this task will target
     * @param attribute The attribute being affected
     * @param modifier The modifier that will affect the attribute
     * @param enchantment The enchantment this attribute combination is tied to
     */
    public void updateAttributes(Player player, @Nullable Attribute attribute, @Nullable AttributeModifier modifier, CEnchantment enchantment) {
        if (player == null || attribute == null) return;

        this.attribute = attribute;
        this.modifier = modifier;

        this.task[0] = player.getScheduler().runAtFixedRate(plugin, check -> {
            for (ItemStack armor : player.getInventory().getArmorContents()) {
                if (armor == null) break;
                if (modifier == null) break;
                if (!this.enchantmentBookSettings.hasEnchantment(armor.getItemMeta(), enchantment)) {
                    player.getAttribute(attribute).removeModifier(modifier);
                    check.cancel();
                }

                if (armor.isEmpty()) check.cancel();
            }
        }, null, 10L, 5L);
    }

    /**
     * Runs a check that will remove the attribute from the item once conditions are met.
     * Specifically targets held items.
     * @param player The player this task will target
     * @param attribute The attribute being affected
     * @param modifier The modifier that will affect the attribute
     * @param enchantment The enchantment this attribute combination is tied to
     * @param tool The ItemStack this is tied to. Can be any item, as long as the player is holding it.
     */
    public void updateAttributes(Player player, @Nullable Attribute attribute, @Nullable AttributeModifier modifier, CEnchantment enchantment, @NotNull ItemStack tool) {
        if (player == null || attribute == null || modifier == null || enchantment == null) return;

        this.attribute = attribute;
        this.modifier = modifier;

        ItemMeta meta = tool.getItemMeta();
        if (meta == null) return;

        EquipmentSlot slot = tool.getItemMeta().getEquippable().getSlot();
        if (!player.getEquipment().getItem(slot).equals(tool)) return;
        if (!this.enchantmentBookSettings.hasEnchantment(meta, enchantment)) {

            var inst = meta.getAttributeModifiers(attribute);
            if (inst == null) return;

            //Filter out any modifiers that don't match this key (like from other plugins)
            inst.stream()
                    .filter(Objects::nonNull)
                    .filter(obj -> obj.getKey().equals(modifier.getKey()))
                    .forEach(obj -> {
                        meta.removeAttributeModifier(attribute, obj);
                        remove(attribute, obj);
                    });

            tool.setItemMeta(meta);
        }
    }

    /**
     * Runs a check that will remove the attribute from the item once conditions are met.
     * Specifically targets armor. Will probably return null if the item can't be equipped.
     * @param player The player this will target
     * @param attribute The attribute being affected
     * @param modifier The modifier that will affect the attribute
     * @param enchantment The enchantment this is tied to
     * @param armor The ItemStack this will be tied to (should be armor)
     * @param slot The EquipmentSlot this armor piece should occupy. If null, the plugin will attempt to guess the slot.
     */
    public void updateAttributes(Player player, @Nullable Attribute attribute, @Nullable AttributeModifier modifier, CEnchantment enchantment, @NotNull ItemStack armor, @Nullable EquipmentSlot slot) {
        if (player == null || attribute == null || modifier == null || enchantment == null) return;

        this.attribute = attribute;
        this.modifier = modifier;

        if (slot == null) slot = armor.getItemMeta().getEquippable().getSlot();
        if (player.getEquipment().getItem(slot).equals(armor)) return;
        if (!this.enchantmentBookSettings.hasEnchantment(armor.getItemMeta(), enchantment)) {
            ItemMeta meta = armor.getItemMeta();
            if (meta == null) return;

            var instance = meta.getAttributeModifiers(attribute);
            if (instance == null) return;

            //Filter out any modifiers that don't match this key
            instance.stream()
                    .filter(Objects::nonNull)
                    .filter(obj -> obj.getKey().equals(modifier.getKey()))
                    .forEach(obj -> {
                        meta.removeAttributeModifier(attribute, obj);
                        remove(attribute, obj);
                    });

            armor.setItemMeta(meta);
        }
    }

    /**
     * Clears all attributes that were added to the player.
     * @param player The player to clear the attributes from.
     */
    public void clearAllAttributes(Player player) {
        for (Attribute attribute : Registry.ATTRIBUTE) for (AttributeModifier modifier : this.modifiers) {
            player.getAttribute(attribute).removeModifier(modifier);
            this.clear();
        }
    }

    public void add(Attribute attribute) {
        this.attributes.add(attribute);
    }

    public void add(AttributeModifier element) {
        this.modifiers.add(element);
    }

    public void add(Attribute attribute, AttributeModifier element) {
        this.attributes.add(attribute);
        this.modifiers.add(element);
    }

    public void remove(Attribute attribute) {
        this.attributes.remove(attribute);
    }

    public void remove(AttributeModifier element) {
        this.modifiers.remove(element);
    }

    public void remove(Attribute attribute, AttributeModifier element) {
        this.attributes.remove(attribute);
        this.modifiers.remove(element);
    }

    public void remove(Attribute attribute, NamespacedKey element) {
        this.attributes.remove(attribute);
        this.modifiers.removeIf(modifier1 -> modifier1.getKey() == element);
    }

    public void clear() {
        this.attributes.clear();
        this.modifiers.clear();
    }

    @Deprecated
    public static @NotNull Map<UUID, AttributeController> getDataset() {
        return Map.copyOf(dataset);
    }

    /**
     * Gets a list of all current active attributes
     * @return All active attributes, as a Collection
     */
    public @NotNull Collection<Attribute> getAttributes() {
        return List.copyOf(this.attributes);
    }

    /**
     * Gets a list of all current active modifiers
     * @return All active modifiers, as a Collection
     */
    public @NotNull Collection<AttributeModifier> getModifiers() {
        return List.copyOf(this.modifiers);
    }

    /**
     * Gets the last attribute that was in use.
     * This does NOT return a list of all attributes in use!! Use {@link AttributeController#getAttributes()}
     * if you want every attribute. The function linked will give you a Collection.
     * @return The last attribute that was used, or null if no attribute was used.
     */
    public @Nullable Attribute getAttribute() {
        return this.attribute;
    }

    /**
     * Gets the last modifier that was in use.
     * This does NOT return a list of all modifiers in use!! Use {@link AttributeController#getModifiers()}
     * if you want every modifier. The function linked will give you a Collection.
     * @return The last attribute that was used, or null if not applicable.
     */
    public @Nullable AttributeModifier getModifier() {
        return this.modifier;
    }

    public ScheduledTask[] getTask() {
        return this.task.clone();
    }

    public @Nullable ItemStack[] getHotbar(Player player) {
        ItemStack[] iterator = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            iterator[i] = player.getInventory().getItem(i);
        }

        return iterator.clone();
    }

    /**
     * Reconstructs an ItemStack with attribute modifiers.
     * @param player The player
     * @param item The item being targeted
     * @param attribute The attribute that will be modified
     * @param modifier The modifier targeting the attribute
     * @return The new ItemStack with attributes
     */
    public ItemStack build(Player player, ItemStack item, Attribute attribute, AttributeModifier modifier) {
        Inventory inventory = player.getInventory();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.addAttributeModifier(attribute, modifier);
        item.setItemMeta(meta);

        ItemStack newItem = item.clone();
        inventory.remove(item);

        inventory.addItem(newItem);

        this.add(attribute, modifier);
        return newItem;
    }

    /**
     * Removes attributes from a selected ItemStack, then rebuilds it
     * @param player The player
     * @param item The item being targeted
     * @param attribute The attribute that will be targeted
     * @param modifier The modifier that should be removed
     * @return The new ItemStack without the attribute modifier
     */
    public ItemStack removeModifiers(Player player, ItemStack item, @Nullable Attribute attribute, @Nullable AttributeModifier modifier) {
        Inventory inventory = player.getInventory();
        ItemMeta meta = item.getItemMeta();
        if (meta == null || attribute == null || modifier == null) return item;

        var instance = meta.getAttributeModifiers(attribute);
        if (instance == null) return item;

        instance.stream()
                        .filter(Objects::nonNull)
                        .filter(obj -> obj.getKey().equals(modifier.getKey()))
                        .forEach(obj -> {
                            meta.removeAttributeModifier(attribute, obj);
                            remove(attribute, obj);
                        });

        item.setItemMeta(meta);

        ItemStack newItem = item.clone();
        inventory.remove(item);

        inventory.addItem(newItem);

        return newItem;
    }

    public boolean isAttributeHandlingEnabled() {
        return this.isAttributeHandlingEnabled;
    }
}
