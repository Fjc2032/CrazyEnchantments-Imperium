package com.badbones69.crazyenchantments.paper.support.interfaces.mmoitems;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import com.badbones69.crazyenchantments.paper.support.interfaces.mmoitems.data.EnchantPluginBuilder;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.comp.enchants.EnchantPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.java.JavaPlugin;
import org.kingdoms.utils.Validate;

public class MMOItemsSupport implements EnchantPluginBuilder<CEnchantment> {


    @Override
    public boolean isCrazyEnchantment(CEnchantment enchantment) {
        return enchantment != null;
    }

    @Override
    public void handleEnchant(ItemStackBuilder builder, CEnchantment var, int level) {
        Validate.isTrue(level > 0, "Level must not be negative.");
    }

    @Override
    public NamespacedKey getNameSpacedKey(String key) {
        return new NamespacedKey(JavaPlugin.getPlugin(CrazyEnchantments.class), key);
    }
}
