package com.badbones69.crazyenchantments.paper.support.interfaces.mmoitems.data;

import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import org.bukkit.NamespacedKey;

public interface EnchantPluginBuilder<T extends CEnchantment> {

    boolean isCrazyEnchantment(CEnchantment enchantment);

    void handleEnchant(ItemStackBuilder builder, T var, int level);

    NamespacedKey getNameSpacedKey(String key);
}
