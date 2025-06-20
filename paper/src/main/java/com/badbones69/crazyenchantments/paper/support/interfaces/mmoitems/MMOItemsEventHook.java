package com.badbones69.crazyenchantments.paper.support.interfaces.mmoitems;

import com.badbones69.crazyenchantments.paper.api.events.BookApplyEvent;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class MMOItemsEventHook implements Listener {

    public MMOItemsEventHook() {
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBookApply(BookApplyEvent event) {
        NBTItem item = MythicLib.plugin.getVersion().getWrapper().getNBTItem(event.getEnchantedItem());
        if (item.getBoolean("MMOITEMS_DISABLE_ADVANCED_ENCHANTS")) event.setCancelled(true);
    }
}
