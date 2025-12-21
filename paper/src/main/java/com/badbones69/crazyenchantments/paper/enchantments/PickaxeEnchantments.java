package com.badbones69.crazyenchantments.paper.enchantments;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.Methods;
import com.badbones69.crazyenchantments.paper.Starter;
import com.badbones69.crazyenchantments.paper.api.CrazyManager;
import com.badbones69.crazyenchantments.paper.api.FileManager.Files;
import com.badbones69.crazyenchantments.paper.api.enums.CEnchantments;
import com.badbones69.crazyenchantments.paper.api.events.MassBlockBreakEvent;
import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import com.badbones69.crazyenchantments.paper.api.utils.EnchantUtils;
import com.badbones69.crazyenchantments.paper.api.utils.EventUtils;
import com.badbones69.crazyenchantments.paper.controllers.settings.EnchantmentBookSettings;
import net.Indyuce.mmoitems.api.event.ItemDropEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Collections;
import java.util.UUID;
import java.util.Set;

public class PickaxeEnchantments implements Listener {

    @NotNull
    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    @NotNull
    private final Starter starter = this.plugin.getStarter();

    @NotNull
    private final Methods methods = this.starter.getMethods();

    @NotNull
    private final CrazyManager crazyManager = this.starter.getCrazyManager();

    @NotNull
    private final EnchantmentBookSettings enchantmentBookSettings = this.starter.getEnchantmentBookSettings();

    private final HashMap<Player, HashMap<Block, BlockFace>> blocks = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();

        if (block == null || block.isEmpty() || !this.crazyManager.getBlastBlockList().contains(block.getType())) return;

        HashMap<Block, BlockFace> blockFace = new HashMap<>();
        blockFace.put(block, event.getBlockFace());
        this.blocks.put(player, blockFace);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlastBreak(BlockBreakEvent event) {
        if (!event.isDropItems() || EventUtils.isIgnoredEvent(event)) return;

        Player player = event.getPlayer();
        Block initialBlock = event.getBlock();
        ItemStack currentItem = this.methods.getItemInHand(player);
        Map<CEnchantment, Integer> enchantments = enchantmentBookSettings.getEnchantments(currentItem);
        boolean damage = Files.CONFIG.getFile().getBoolean("Settings.EnchantmentOptions.Blast-Full-Durability");

        if (!(this.blocks.containsKey(player) && this.blocks.get(player).containsKey(initialBlock))) return;
        if (!EnchantUtils.isMassBlockBreakActive(player, CEnchantments.DETONATE, enchantments)) return;

        Set<Block> blockList = getBlocks(initialBlock.getLocation(), blocks.get(player).get(initialBlock), (enchantmentBookSettings.getLevel(currentItem, CEnchantments.DETONATE.getEnchantment()) - 1));
        this.blocks.remove(player);

        if (massBlockBreakCheck(player, blockList)) return;
        event.setCancelled(true);

        for (Block block : blockList) {
            if (block.isEmpty() || !crazyManager.getBlastBlockList().contains(block.getType())) continue;
            if (this.methods.playerBreakBlock(player, block, currentItem, crazyManager.isDropBlocksBlast())) continue;
            if (damage) this.methods.removeDurability(currentItem, player);
        }
        if (!damage) this.methods.removeDurability(currentItem, player);
    }


    @EventHandler(priority =  EventPriority.LOW, ignoreCancelled = true)
    public void onVeinMinerBreak(BlockBreakEvent event) {
        if (!isOreBlock(event.getBlock().getType())
                || !event.isDropItems()
                || EventUtils.isIgnoredEvent(event))
            return;

        Player player = event.getPlayer();
        Block currentBlock = event.getBlock();
        ItemStack currentItem = methods.getItemInHand(player);
        Map<CEnchantment, Integer> enchantments = this.enchantmentBookSettings.getEnchantments(currentItem);
        boolean damage = Files.CONFIG.getFile().getBoolean("Settings.EnchantmentOptions.VeinMiner-Full-Durability", true);

        if (!EnchantUtils.isMassBlockBreakActive(player, CEnchantments.VEIN, enchantments)) return;

        HashSet<Block> blockList = getOreBlocks(currentBlock.getLocation(), enchantments.get(CEnchantments.VEIN.getEnchantment()));
        blockList.add(currentBlock);

        if (massBlockBreakCheck(player, blockList)) return;

        event.setCancelled(true);

        for (Block block : blockList) {
            if (block.isEmpty()) continue;
            if (this.methods.playerBreakBlock(player, block, currentItem, this.crazyManager.isDropBlocksVeinMiner())) continue;
            if (damage) this.methods.removeDurability(currentItem, player);
        }

        if (!damage) this.methods.removeDurability(currentItem, player);
    }

    private boolean massBlockBreakCheck(Player player, Set<Block> blockList) {
        MassBlockBreakEvent event = new MassBlockBreakEvent(player, blockList);
        this.plugin.getServer().getPluginManager().callEvent(event);

        return event.isCancelled();
    }

    //Imperium Enchant AutoSmelt/Furnace vvv
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDropAlter(BlockDropItemEvent event) {
        if (!isSmeltable(event.getBlockState().getType())) return;

        Player player = event.getPlayer();
        ItemStack itemInHand = this.methods.getItemInHand(player);
        Map<CEnchantment, Integer> enchants = this.enchantmentBookSettings.getEnchantments(itemInHand);

        List<Item> drops = event.getItems();

        CEnchantment furnanceEnchant = CEnchantments.FURNACE.getEnchantment();
        CEnchantment autosmeltEnchant = CEnchantments.AUTOSMELT.getEnchantment();

        if (EnchantUtils.isEventActive(CEnchantments.AUTOSMELT, player, itemInHand, enchants)) {
            int level = enchantmentBookSettings.getLevel(itemInHand, autosmeltEnchant);

            for (Item itemEntity : drops) {
                ItemStack drop = itemEntity.getItemStack();
                if (!isSmeltable(drop.getType())) continue;

                if (CEnchantments.AUTOSMELT.chanceSuccessful(level)) {
                    player.sendMessage("Autosmelt success.");
                    itemEntity.setItemStack(getSmeltedDrop(drop, drop.getAmount()));
                    changeDrop(event.getBlock().getLocation(), event);
                }
            }
            return;
        }

        if (this.enchantmentBookSettings.hasEnchantment(itemInHand.getItemMeta(), furnanceEnchant)) {

            int level = enchantmentBookSettings.getLevel(itemInHand, furnanceEnchant);

            if (CEnchantments.FURNACE.isOffCooldown(player.getUniqueId(), level, true)) {
                for (Item itemEntity : drops) {
                    ItemStack drop = itemEntity.getItemStack();
                    if (!isSmeltable(drop.getType())) continue;

                    itemEntity.setItemStack(getSmeltedDrop(drop, drop.getAmount()));
                    changeDrop(event.getBlock().getLocation(), event);
                }
            }
        }
    }
    //AutoSmelt/furnace ^^^
    //Imperium Enchant Experience vvv
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExperience(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        //check enchantments
        Map<CEnchantment, Integer> enchants = Optional.of(this.enchantmentBookSettings.getEnchantments(item)).orElse(Collections.emptyMap());

        if (EnchantUtils.isEventActive(CEnchantments.EXPERIENCE, player, item, enchants)) {
            CEnchantment enchant = CEnchantments.EXPERIENCE.getEnchantment();
            int level = this.enchantmentBookSettings.getLevel(item, enchant);

            if (CEnchantments.EXPERIENCE.isOffCooldown(player.getUniqueId(), level, true)) {

                //xp chance plus random xp (1-5)
                double initialChance = CEnchantments.EXPERIENCE.getChance();
                double chance = initialChance + (0.15 * level);
                if (Math.random() <= chance) {
                    int gain = (int) (event.getExpToDrop() + 2 + (Math.random() * 4));
                    //Some debug to see what's going on
                    //player.sendMessage("Experience triggered!");
                    //player.sendMessage("XP Gain: " + event.getExpToDrop() + " + " + (gain - event.getExpToDrop()));
                    event.setExpToDrop(gain);
                }
            } else {
                player.sendMessage("Experience on cooldown!");
            }
        }
    }
    //Experience ^^^

    private HashSet<Block> getOreBlocks(Location loc, int amount) {
        HashSet<Block> blocks = new HashSet<>(Set.of(loc.getBlock()));
        HashSet<Block> newestBlocks = new HashSet<>(Set.of(loc.getBlock()));

        int depth = 0;

        while (depth < amount) {
            HashSet<Block> tempBlocks = new HashSet<>();

            for (Block block1 : newestBlocks) {
                for (Block block : getSurroundingBlocks(block1.getLocation())) {
                    if (!blocks.contains(block) && isOreBlock(block.getType())) tempBlocks.add(block);
                }
            }

            blocks.addAll(tempBlocks);
            newestBlocks = tempBlocks;

            ++depth;
        }

        return blocks;
    } 
    
    private HashSet<Block> getSurroundingBlocks(Location loc) {
        HashSet<Block> locations = new HashSet<>();
        
        locations.add(loc.clone().add(0,1,0).getBlock());
        locations.add(loc.clone().add(0,-1,0).getBlock());
        locations.add(loc.clone().add(1,0,0).getBlock());
        locations.add(loc.clone().add(-1,0,0).getBlock());
        locations.add(loc.clone().add(0,0,1).getBlock());
        locations.add(loc.clone().add(0,0,-1).getBlock());
        
        return locations;
    }

    private HashSet<Block> getBlocks(Location loc, BlockFace blockFace, Integer depth) {
        Location loc2 = loc.clone();

        switch (blockFace) {
            case SOUTH -> {
                loc.add(-1, 1, -depth);
                loc2.add(1, -1, 0);
            }

            case WEST -> {
                loc.add(depth, 1, -1);
                loc2.add(0, -1, 1);
            }

            case EAST -> {
                loc.add(-depth, 1, 1);
                loc2.add(0, -1, -1);
            }

            case NORTH -> {
                loc.add(1, 1, depth);
                loc2.add(-1, -1, 0);
            }

            case UP -> {
                loc.add(-1, -depth, -1);
                loc2.add(1, 0, 1);
            }

            case DOWN -> {
                loc.add(1, depth, 1);
                loc2.add(-1, 0, -1);
            }

            default -> {}
        }

        return this.methods.getEnchantBlocks(loc, loc2);
    }

    private boolean isOreBlock(Material material) {
        return switch (material) {
            case COAL_ORE, DEEPSLATE_COAL_ORE,
                 COPPER_ORE, DEEPSLATE_COPPER_ORE,
                 DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE,
                 EMERALD_ORE, DEEPSLATE_EMERALD_ORE,
                 GOLD_ORE, DEEPSLATE_GOLD_ORE,
                 IRON_ORE, DEEPSLATE_IRON_ORE,
                 LAPIS_ORE, DEEPSLATE_LAPIS_ORE,
                 REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE,
                 NETHER_GOLD_ORE,
                 NETHER_QUARTZ_ORE -> true;
            default -> false;
        };
    }
    
    //check if the block is smeltable VVV
    private boolean isSmeltable(Material material) {
        //List of blocks/items that can be smelted
        return switch (material) {
            //ores,Stone and utility, Wood types on update add 1.21.5 all leave types ex: case OAK_LEAVES:
            case IRON_ORE, DEEPSLATE_IRON_ORE, RAW_IRON, COPPER_ORE, DEEPSLATE_COPPER_ORE, RAW_COPPER, GOLD_ORE,
                 DEEPSLATE_GOLD_ORE, RAW_GOLD, ANCIENT_DEBRIS, REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE, EMERALD_ORE,
                 DEEPSLATE_EMERALD_ORE, LAPIS_ORE, DEEPSLATE_LAPIS_ORE, COAL_ORE, DEEPSLATE_COAL_ORE, DIAMOND_ORE,
                 DEEPSLATE_DIAMOND_ORE, NETHER_QUARTZ_ORE, NETHER_GOLD_ORE, KELP, COBBLESTONE, STONE, STONE_BRICKS,
                 COBBLED_DEEPSLATE, DEEPSLATE_BRICKS, DEEPSLATE_TILES, SANDSTONE, RED_SANDSTONE, NETHER_BRICK, BASALT,
                 POLISHED_BLACKSTONE_BRICKS, QUARTZ_BLOCK, CLAY, SAND, RED_SAND, WET_SPONGE, CHORUS_FRUIT, SEA_PICKLE,
                 CACTUS, CLAY_BALL, NETHERRACK, RESIN_CLUMP, ACACIA_LOG, ACACIA_WOOD, STRIPPED_ACACIA_LOG,
                 STRIPPED_ACACIA_WOOD, OAK_LOG, OAK_WOOD, STRIPPED_OAK_LOG, STRIPPED_OAK_WOOD, BIRCH_LOG, BIRCH_WOOD,
                 STRIPPED_BIRCH_LOG, STRIPPED_BIRCH_WOOD, SPRUCE_LOG, SPRUCE_WOOD, STRIPPED_SPRUCE_LOG,
                 STRIPPED_SPRUCE_WOOD, JUNGLE_LOG, JUNGLE_WOOD, STRIPPED_JUNGLE_LOG, STRIPPED_JUNGLE_WOOD, DARK_OAK_LOG,
                 DARK_OAK_WOOD, STRIPPED_DARK_OAK_LOG, STRIPPED_DARK_OAK_WOOD, MANGROVE_LOG, MANGROVE_WOOD,
                 STRIPPED_MANGROVE_LOG, STRIPPED_MANGROVE_WOOD, CHERRY_LOG, CHERRY_WOOD, STRIPPED_CHERRY_LOG,
                 STRIPPED_CHERRY_WOOD, PALE_OAK_LOG, PALE_OAK_WOOD, STRIPPED_PALE_OAK_LOG, STRIPPED_PALE_OAK_WOOD ->
                    true;
            default -> false;
        };
    }

    //switch drops of given item when mined
    private ItemStack getSmeltedDrop(ItemStack drop, int amount) {
        return switch (drop.getType()) {
            //ores
            case IRON_ORE, DEEPSLATE_IRON_ORE, RAW_IRON, NETHER_GOLD_ORE -> new ItemStack(Material.IRON_INGOT, amount);
            case GOLD_ORE, DEEPSLATE_GOLD_ORE, RAW_GOLD -> new ItemStack(Material.GOLD_INGOT, amount);
            case COAL_ORE, DEEPSLATE_COAL_ORE -> new ItemStack(Material.COAL, amount);
            case LAPIS_ORE, DEEPSLATE_LAPIS_ORE -> new ItemStack(Material.LAPIS_LAZULI, amount);
            case REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE -> new ItemStack(Material.REDSTONE, amount);
            case EMERALD_ORE, DEEPSLATE_EMERALD_ORE -> new ItemStack(Material.EMERALD, amount);
            case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE -> new ItemStack(Material.DIAMOND, amount);
            case COPPER_ORE, DEEPSLATE_COPPER_ORE, RAW_COPPER -> new ItemStack(Material.COPPER_INGOT, amount);
            case NETHER_QUARTZ_ORE -> new ItemStack(Material.QUARTZ, amount);
            //wood
            case ACACIA_LOG, ACACIA_WOOD, STRIPPED_ACACIA_LOG, STRIPPED_ACACIA_WOOD, OAK_LOG, OAK_WOOD,
                 STRIPPED_OAK_LOG, STRIPPED_OAK_WOOD, BIRCH_LOG, BIRCH_WOOD, STRIPPED_BIRCH_LOG, STRIPPED_BIRCH_WOOD,
                 SPRUCE_LOG, SPRUCE_WOOD, STRIPPED_SPRUCE_LOG, STRIPPED_SPRUCE_WOOD, JUNGLE_LOG, JUNGLE_WOOD,
                 STRIPPED_JUNGLE_LOG, STRIPPED_JUNGLE_WOOD, DARK_OAK_LOG, DARK_OAK_WOOD, STRIPPED_DARK_OAK_LOG,
                 STRIPPED_DARK_OAK_WOOD, MANGROVE_LOG, MANGROVE_WOOD, STRIPPED_MANGROVE_LOG, STRIPPED_MANGROVE_WOOD,
                 CHERRY_LOG, CHERRY_WOOD, STRIPPED_CHERRY_LOG, STRIPPED_CHERRY_WOOD, PALE_OAK_LOG, PALE_OAK_WOOD,
                 STRIPPED_PALE_OAK_LOG, STRIPPED_PALE_OAK_WOOD -> new ItemStack(Material.CHARCOAL, amount);
            //other
            case COBBLESTONE -> new ItemStack(Material.STONE, amount);
            case STONE -> new ItemStack(Material.SMOOTH_STONE, amount);
            case COBBLED_DEEPSLATE -> new ItemStack(Material.DEEPSLATE, amount);
            case DEEPSLATE_BRICKS -> new ItemStack(Material.CRACKED_DEEPSLATE_BRICKS, amount);
            case DEEPSLATE_TILES -> new ItemStack(Material.CRACKED_DEEPSLATE_TILES, amount);
            case SANDSTONE -> new ItemStack(Material.SMOOTH_SANDSTONE, amount);
            case RED_SANDSTONE -> new ItemStack(Material.SMOOTH_RED_SANDSTONE, amount);
            case NETHER_BRICK -> new ItemStack(Material.CRACKED_NETHER_BRICKS, amount);
            case BASALT -> new ItemStack(Material.SMOOTH_BASALT, amount);
            case POLISHED_BLACKSTONE_BRICKS -> new ItemStack(Material.CRACKED_POLISHED_BLACKSTONE_BRICKS, amount);
            case QUARTZ_BLOCK -> new ItemStack(Material.SMOOTH_QUARTZ, amount);
            case CLAY -> new ItemStack(Material.TERRACOTTA, amount);
            case SAND, RED_SAND -> new ItemStack(Material.GLASS, amount);
            case WET_SPONGE -> new ItemStack(Material.SPONGE, amount);
            case CHORUS_FRUIT -> new ItemStack(Material.POPPED_CHORUS_FRUIT, amount);
            case SEA_PICKLE -> new ItemStack(Material.LIME_DYE, amount);
            case CLAY_BALL -> new ItemStack(Material.BRICK, amount);
            case NETHERRACK -> new ItemStack(Material.NETHER_BRICK, amount);
            case RESIN_CLUMP -> new ItemStack(Material.RESIN_BRICK, amount);
            case KELP -> new ItemStack(Material.DRIED_KELP, amount);
            //GLAZED TERRACOTTA
            case RED_TERRACOTTA -> new ItemStack(Material.RED_GLAZED_TERRACOTTA, amount);
            case ORANGE_TERRACOTTA -> new ItemStack(Material.ORANGE_GLAZED_TERRACOTTA, amount);
            case YELLOW_TERRACOTTA -> new ItemStack(Material.YELLOW_GLAZED_TERRACOTTA, amount);
            case LIME_TERRACOTTA -> new ItemStack(Material.LIME_GLAZED_TERRACOTTA, amount);
            case GREEN_TERRACOTTA -> new ItemStack(Material.GREEN_GLAZED_TERRACOTTA, amount);
            case CYAN_TERRACOTTA -> new ItemStack(Material.CYAN_GLAZED_TERRACOTTA, amount);
            case LIGHT_BLUE_TERRACOTTA -> new ItemStack(Material.LIGHT_BLUE_GLAZED_TERRACOTTA, amount);
            case BLUE_TERRACOTTA -> new ItemStack(Material.BLUE_GLAZED_TERRACOTTA, amount);
            case PURPLE_TERRACOTTA -> new ItemStack(Material.PURPLE_GLAZED_TERRACOTTA, amount);
            case MAGENTA_TERRACOTTA -> new ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA, amount);
            case PINK_TERRACOTTA -> new ItemStack(Material.PINK_GLAZED_TERRACOTTA, amount);
            case BROWN_TERRACOTTA -> new ItemStack(Material.BROWN_GLAZED_TERRACOTTA, amount);
            case BLACK_TERRACOTTA -> new ItemStack(Material.BLACK_GLAZED_TERRACOTTA, amount);
            case GRAY_TERRACOTTA -> new ItemStack(Material.GRAY_GLAZED_TERRACOTTA, amount);
            case LIGHT_GRAY_TERRACOTTA -> new ItemStack(Material.LIGHT_GRAY_GLAZED_TERRACOTTA, amount);
            case WHITE_TERRACOTTA -> new ItemStack(Material.WHITE_GLAZED_TERRACOTTA, amount);
            default -> drop; //if no swtich found give normal item
        };
    }

    /**
     * Moves the drop to a specific location.
     * Looking at this now, why did I make this?
     * @param location The location where this will happen
     * @param event You must use an instance of BlockDropItemEvent
     */
    @ApiStatus.Experimental
    private void changeDrop(Location location, BlockDropItemEvent event) {
        World world = location.getWorld();
        final List<Item> drops = event.getItems();
        for (Item item : drops) {
            ItemStack result = item.getItemStack();
            world.dropItemNaturally(location, result);
        }
        event.setCancelled(true);
    }
}
