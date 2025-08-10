package com.badbones69.crazyenchantments.paper.support.interfaces.mmoitems.data;

import java.util.Map;

import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import com.badbones69.crazyenchantments.paper.controllers.settings.EnchantmentBookSettings;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.bukkit.inventory.ItemStack;

public class CrazyEnchantsData implements StatData {
    private final Map<CEnchantment, Integer> enchants;

    private final EnchantmentBookSettings enchantmentBookSettings = new EnchantmentBookSettings();

    public CrazyEnchantsData(Map<CEnchantment, Integer> enchants) {
        this.enchants = enchants;
    }

    public Map<CEnchantment, Integer> getEnchants() {
        return this.enchants;
    }

    public Map<CEnchantment, Integer> getEnchants(ItemStack item) {
        return this.enchantmentBookSettings.getEnchantments(item);
    }

    public boolean isEmpty() {
        return this.enchants.isEmpty();
    }
}
