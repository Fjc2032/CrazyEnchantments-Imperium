package com.badbones69.crazyenchantments.paper.api.builders;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.api.objects.enchants.EnchantmentType;
import com.badbones69.crazyenchantments.paper.api.objects.gkitz.GKitz;
import com.badbones69.crazyenchantments.paper.api.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
public abstract class InventoryBuilder implements InventoryHolder {

    @NotNull
    protected final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    private final Inventory inventory;
    private Inventory secondaryInv = null;
    private final Player player;
    private String title;
    private int size;
    private int page;

    private EnchantmentType enchantmentType;

    private GKitz kit;

    protected InventoryBuilder(Player player, int size, String title) {
        this.title = title;
        this.size = size;

        this.player = player;

        this.kit = null;

        try {
            this.inventory = this.plugin.getServer().createInventory(this, this.size, ColorUtils.legacyTranslateColourCodes(title));
            if (getFilledSlots(this.inventory) > this.inventory.getSize()) {
                List<ItemStack> extras = Arrays.stream(this.inventory.getContents())
                        .skip(this.inventory.getSize() - 1)
                        .collect(Collectors.toUnmodifiableList());

                int secondarySlot = this.inventory.getSize() - 1;
                this.inventory.setItem(secondarySlot, buildNextPageIcon(this.inventory));
                InventoryClickEvent event = new InventoryClickEvent(this.getInventoryView(), InventoryType.SlotType.CONTAINER, secondarySlot, ClickType.LEFT, InventoryAction.NOTHING);
                if (event.getWhoClicked().equals(player)) {
                    this.secondaryInv = this.plugin.getServer().createInventory(this, this.size, ColorUtils.legacyTranslateColourCodes(title));
                    player.sendMessage("Opening page 2...");
                }
            }
        } catch (IllegalArgumentException exception) {
            plugin.getLogger().severe(exception.toString());
            throw new IllegalArgumentException("Error! Probably out of bounds.");
        }
    }

    protected InventoryBuilder(Player player, int size, String title, GKitz kit) {
        this.title = title;
        this.size = size;

        this.player = player;

        this.kit = kit;

        this.inventory = this.plugin.getServer().createInventory(this, this.size, ColorUtils.legacyTranslateColourCodes(title));
    }

    public abstract InventoryBuilder build();

    public InventoryBuilder setEnchantmentType(EnchantmentType enchantmentType) {
        this.enchantmentType = enchantmentType;

        return this;
    }

    public EnchantmentType getEnchantmentType() {
        return this.enchantmentType;
    }

    public GKitz getKit() {
        return this.kit;
    }

    public void size(int size) {
        this.size = size;
    }

    public int getSize() {
        return this.size;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPage() {
        return this.page;
    }

    public void title(String title) {
        this.title = title;
    }

    public boolean contains(String message) {
        return this.title.contains(message);
    }

    public Player getPlayer() {
        return this.player;
    }

    public InventoryView getInventoryView() {
        return getPlayer().getOpenInventory();
    }

    @Override
    @NotNull
    public Inventory getInventory() {
        if (isInventoryFull(this.inventory)) return this.secondaryInv;
        return this.inventory;
    }

    public int getFilledSlots(Inventory target) {
        int amount = 0;
        for (ItemStack item : target.getContents()) {
            if (item != null && item.getType() != Material.AIR) amount++;
        }
        return amount;
    }
    public boolean isInventoryFull(Inventory inventory) {
        return Arrays.stream(inventory.getContents())
                .allMatch(item -> item != null && item.getType() == Material.AIR);
    }
    public ItemStack buildNextPageIcon(Inventory target) {
        ItemStack nextPage = new ItemStack(Material.NETHER_STAR, getPage());
        ItemMeta meta = nextPage.getItemMeta();
        meta.customName(Component.text("Next Page"));

        nextPage.setItemMeta(meta);
        return nextPage;
    }
}