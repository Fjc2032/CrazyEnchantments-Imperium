package com.badbones69.crazyenchantments.paper.api.events;

import com.badbones69.crazyenchantments.paper.api.objects.CEBook;
import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class HeroicBookApplyEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final int level;
    private boolean cancelled;
    private boolean isHeroic;
    private final CEnchantment enchantment;
    private final ItemStack enchantedItem;
    private final CEBook ceBook;

    public HeroicBookApplyEvent(Player player, ItemStack enchantedItem, CEBook ceBook, boolean isHeroic) {
        this.level = ceBook.getLevel();
        this.player = player;
        this.enchantment = ceBook.getEnchantment();
        this.ceBook = ceBook;
        this.enchantedItem = enchantedItem;
        this.cancelled = false;
        this.isHeroic = isHeroic;
    }

    public Player getPlayer() {
        return this.player;
    }
    public int getLevel() {
        return this.level;
    }
    public boolean isHeroic() {
        return isHeroic;
    }
    public void setHeroic(boolean heroic) {
        this.isHeroic = heroic;
    }
    public CEnchantment getEnchantment() {
        return this.enchantment;
    }
    public ItemStack getEnchantedItem() {
        return this.enchantedItem;
    }
    public CEBook getCEBook() {
        return this.ceBook;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }

}
