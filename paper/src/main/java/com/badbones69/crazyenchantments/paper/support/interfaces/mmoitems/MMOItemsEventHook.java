package com.badbones69.crazyenchantments.paper.support.interfaces.mmoitems;

import com.badbones69.crazyenchantments.paper.api.events.BookApplyEvent;
import com.badbones69.crazyenchantments.paper.api.events.PreBookApplyEvent;
import com.badbones69.crazyenchantments.paper.support.PluginSupport;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class MMOItemsEventHook implements Listener {

    public MMOItemsEventHook() {
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBookApply(PreBookApplyEvent event) {
        if (!PluginSupport.SupportedPlugins.MCMMO.isPluginLoaded()) return;

        NBTItem item = MythicLib.plugin.getVersion().getWrapper().getNBTItem(event.getEnchantedItem());
        if (item.getBoolean("MMOITEMS_DISABLE_ADVANCED_ENCHANTS")) event.setCancelled(true);
    }
}
