package com.badbones69.crazyenchantments.paper.support.interfaces.mmoitems.data;

import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import net.Indyuce.mmoitems.stat.data.type.StatData;

import java.util.Arrays;
import java.util.Map;

public class CrazyEnchantsData implements StatData {

    private static Map<CEnchantment, Integer> enchants = Map.of();

    public CrazyEnchantsData(Map<CEnchantment, Integer> enchants) {
        CrazyEnchantsData.enchants = Map.copyOf(enchants);
    }

    public static Map<CEnchantment, Integer> getEnchants() {
        return CrazyEnchantsData.enchants;
    }
    @Override
    public boolean isEmpty() {
        return enchants.isEmpty();
    }
}
