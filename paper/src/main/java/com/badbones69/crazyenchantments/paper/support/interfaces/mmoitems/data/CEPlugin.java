package com.badbones69.crazyenchantments.paper.support.interfaces.mmoitems.data;

import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import org.bukkit.NamespacedKey;

public interface CEPlugin <T extends CEnchantment> {

    boolean isCustomEnchant(CEnchantment var1);

    void handleEnchant(ItemStackBuilder var1, T var2, int var3);

    NamespacedKey getNamespacedKey(String var1);
}
