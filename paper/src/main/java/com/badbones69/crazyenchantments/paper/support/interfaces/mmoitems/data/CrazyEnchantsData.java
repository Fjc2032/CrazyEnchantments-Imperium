package com.badbones69.crazyenchantments.paper.support.interfaces.mmoitems.data;

import java.util.Map;

import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import net.Indyuce.mmoitems.stat.data.type.StatData;

public class CrazyEnchantsData implements StatData {
    private final Map<CEnchantment, Integer> enchants;

    public CrazyEnchantsData(Map<CEnchantment, Integer> enchants) {
        this.enchants = enchants;
    }

    public Map<CEnchantment, Integer> getEnchants() {
        return this.enchants;
    }

    public boolean isEmpty() {
        return this.enchants.isEmpty();
    }
}
