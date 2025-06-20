package com.badbones69.crazyenchantments.paper.listeners;

import com.badbones69.crazyenchantments.paper.controllers.settings.EnchantmentBookSettings;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerListener implements Listener {

    private final EnchantmentBookSettings enchantmentBookSettings = new EnchantmentBookSettings();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLeave(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        if (enchantmentBookSettings.playerCooldowns.containsKey(playerId)) enchantmentBookSettings.playerCooldowns.remove(playerId);
    }

}
