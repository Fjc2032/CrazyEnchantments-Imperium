package com.badbones69.crazyenchantments.paper.support.interfaces.mmoitems;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.Starter;
import com.badbones69.crazyenchantments.paper.api.CrazyManager;
import com.badbones69.crazyenchantments.paper.api.builders.ItemBuilder;
import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import com.badbones69.crazyenchantments.paper.support.interfaces.mmoitems.data.CEPlugin;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.kingdoms.utils.Validate;

@Deprecated
public class MMOItemsSupport implements CEPlugin<CEnchantment> {

    @NotNull
    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    @NotNull
    private final ItemBuilder itemBuilder = new ItemBuilder();

    @NotNull
    private final Starter starter = this.plugin.getStarter();

    @NotNull
    private final CrazyManager crazyManager = this.starter.getCrazyManager();

    @Override
    public boolean isCustomEnchant(CEnchantment enchantment) {
        return enchantment != null;
    }

    @Override
    public void handleEnchant(ItemStackBuilder builder, CEnchantment enchantment, int level) {
        Validate.isTrue(level > 0, "Level cannot be negative.");

        if (!builder.getMeta().hasItemFlag(ItemFlag.HIDE_ENCHANTS)) {
            builder.getLore().insert(0, itemBuilder.getUpdatedLore().toString());
        }
    }

    @Override
    public NamespacedKey getNamespacedKey(String key) {
        return new NamespacedKey(plugin, key);
    }
}
