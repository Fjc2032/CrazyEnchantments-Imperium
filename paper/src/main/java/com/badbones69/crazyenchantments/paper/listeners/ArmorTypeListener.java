package com.badbones69.crazyenchantments.paper.listeners;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.api.enums.pdc.EnchantedBook;
import com.badbones69.crazyenchantments.paper.api.events.PreBookApplyEvent;
import com.badbones69.crazyenchantments.paper.controllers.ArmorTypeController;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class ArmorTypeListener implements Listener {

    @NotNull
    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    private EnchantedBook book;

    @EventHandler
    public void onBookApply(PreBookApplyEvent event) {
        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();
        final ArmorTypeController armorTypeController = new ArmorTypeController(player);

        this.book = new EnchantedBook(event.getEnchantment().getName(), event.getSuccessChance(), event.getDestroyChance(), event.getLevel());

        if (armorTypeController.isArmorGold()) this.book.setSuccessChance(event.getSuccessChance() + armorTypeController.getAmountOfGoldArmor());
    }
}
