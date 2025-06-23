package com.badbones69.crazyenchantments.paper.support.interfaces.mmoitems;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.Methods;
import com.badbones69.crazyenchantments.paper.Starter;
import com.badbones69.crazyenchantments.paper.api.builders.ItemBuilder;
import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import com.badbones69.crazyenchantments.paper.api.utils.NumberUtils;
import com.badbones69.crazyenchantments.paper.controllers.settings.EnchantmentBookSettings;
import io.lumine.mythic.lib.api.item.ItemTag;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.comp.enchants.CrazyEnchantsData;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.type.InternalStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CrazyEnchantStats extends ItemStat<RandomStatData<CrazyEnchantsData>, CrazyEnchantsData> implements InternalStat {


    public CrazyEnchantStats() {
        super("CRAZY_ENCHANTS", Material.BOOK, "CrazyEnchantments", new String[0], new String[]{"all"}, new Material[0]);
    }

    @Override
    public RandomStatData<CrazyEnchantsData> whenInitialized(Object o) {
        return null;
    }
    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);
    private final Starter starter = this.plugin.getStarter();
    private final Methods methods = this.starter.getMethods();

    @NotNull
    private final EnchantmentBookSettings enchantmentBookSettings = this.starter.getEnchantmentBookSettings();

    public Methods getMethods() {
        return this.methods;
    }


    @Override
    public void whenApplied(@NotNull ItemStackBuilder itemStackBuilder, @NotNull CrazyEnchantsData crazyEnchantsData) {
        Map<CEnchantment, Integer> enchants = com.badbones69.crazyenchantments.paper.support.interfaces.mmoitems.data.CrazyEnchantsData.getEnchants();
        Iterator var4 = enchants.entrySet().iterator();

        while (var4.hasNext()) {
            Map.Entry<CEnchantment, Integer> entry = (Map.Entry) var4.next();
            CEnchantment enchantment = entry.getKey();
            int level = (Integer) entry.getValue();
            itemStackBuilder.getLore().insert(0, methods.addLore(itemStackBuilder.getItemStack(), (enchantment.getCustomName() + " " + NumberUtils.convertLevelString(level))).toString());
        }
    }

    @Override
    public @NotNull ArrayList<ItemTag> getAppliedNBT(@NotNull CrazyEnchantsData crazyEnchantsData) {
        return new ArrayList<>();
    }

    @Override
    public void whenClicked(@NotNull EditionInventory editionInventory, @NotNull InventoryClickEvent inventoryClickEvent) {
        throw new NotImplementedException();
    }

    @Override
    public void whenInput(@NotNull EditionInventory editionInventory, @NotNull String s, Object... objects) {
        throw new NotImplementedException();
    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem readMMOItem) {
        Map<CEnchantment, Integer> enchants = this.enchantmentBookSettings.getEnchantments(readMMOItem.getNBT().getItem());
        if (enchants.size() > 0) {
            readMMOItem.setData(this, new com.badbones69.crazyenchantments.paper.support.interfaces.mmoitems.data.CrazyEnchantsData(enchants));
        }
    }

    @Override
    public @Nullable CrazyEnchantsData getLoadedNBT(@NotNull ArrayList<ItemTag> arrayList) {
        return null;
    }

    @Override
    public void whenDisplayed(List<String> list, Optional<RandomStatData<CrazyEnchantsData>> optional) {
        throw new NotImplementedException();
    }

    @Override
    public @NotNull CrazyEnchantsData getClearStatData() throws NotImplementedException {
        return new CrazyEnchantsData(new HashMap());
    }
}
