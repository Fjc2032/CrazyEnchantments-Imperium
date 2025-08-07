package com.badbones69.crazyenchantments.paper.support.interfaces.mmoitems.data;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import com.badbones69.crazyenchantments.paper.api.utils.ColorUtils;
import com.badbones69.crazyenchantments.paper.api.utils.NumberUtils;
import com.badbones69.crazyenchantments.paper.controllers.settings.EnchantmentBookSettings;
import io.lumine.mythic.lib.api.item.ItemTag;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
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

public class CrazyEnchantsStat extends ItemStat<RandomStatData<com.badbones69.crazyenchantments.paper.support.interfaces.mmoitems.data.CrazyEnchantsData>,
        com.badbones69.crazyenchantments.paper.support.interfaces.mmoitems.data.CrazyEnchantsData>
        implements InternalStat {
    public CrazyEnchantsStat() {
        super("CRAZY_ENCHANT", Material.BOOK, "Advanced Enchants", new String[0], new String[]{"all"}, new Material[0]);
    }

    @NotNull
    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    @NotNull
    private final EnchantmentBookSettings enchantmentBookSettings = new EnchantmentBookSettings();

    @Override
    public void whenApplied(
            @NotNull ItemStackBuilder item,
            @NotNull com.badbones69.crazyenchantments.paper.support.interfaces.mmoitems.data.CrazyEnchantsData data) {

        Map<CEnchantment, Integer> enchants = data.getEnchants();

        for(Map.Entry<CEnchantment, Integer> entry : enchants.entrySet()) {
            CEnchantment enchantment = entry.getKey();
            int level = entry.getValue();
            item.getLore().insert(0, ColorUtils.color(enchantment.getCustomName() + " " + NumberUtils.convertLevelString(level)));
        }

    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
        Map<CEnchantment, Integer> enchants = enchantmentBookSettings.getEnchantments(mmoitem.getNBT().getItem());
        if (!enchants.isEmpty()) {
            mmoitem.setData(this, new CrazyEnchantsData(enchants));
        }

    }

    @Nullable
    @Override
    public RandomStatData<com.badbones69.crazyenchantments.paper.support.interfaces.mmoitems.data.CrazyEnchantsData> whenInitialized(Object object) {
        throw new NotImplementedException();
    }

    @Override
    public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
    }

    @Override
    public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
    }

    @Override
    public void whenDisplayed(List<String> lore, Optional<RandomStatData<com.badbones69.crazyenchantments.paper.support.interfaces.mmoitems.data.CrazyEnchantsData>> statData) {
    }

    @Nullable
    @Override
    public com.badbones69.crazyenchantments.paper.support.interfaces.mmoitems.data.CrazyEnchantsData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {
        return null;
    }

    @NotNull
    public ArrayList<ItemTag> getAppliedNBT(@NotNull com.badbones69.crazyenchantments.paper.support.interfaces.mmoitems.data.CrazyEnchantsData data) {
        return new ArrayList<>();
    }

    @Override
    public com.badbones69.crazyenchantments.paper.support.interfaces.mmoitems.data.@NotNull CrazyEnchantsData getClearStatData() {
        return new com.badbones69.crazyenchantments.paper.support.interfaces.mmoitems.data.CrazyEnchantsData(new HashMap<>());
    }
}
