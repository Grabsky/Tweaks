/*
 * Tweaks (https://github.com/Grabsky/Tweaks)
 *
 * Copyright (C) 2024  Grabsky <michal.czopek.foss@proton.me>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License v3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License v3 for more details.
 */
package cloud.grabsky.tweaks.modules.enchantments;

import cloud.grabsky.tweaks.Module;
import cloud.grabsky.tweaks.Tweaks;
import cloud.grabsky.tweaks.configuration.PluginConfig;
import cloud.grabsky.tweaks.utils.Extensions;
import com.destroystokyo.paper.MaterialSetTag;
import com.destroystokyo.paper.MaterialTags;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBundle;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerCollectItem;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

import static io.github.retrooper.packetevents.util.SpigotConversionUtil.fromBukkitLocation;

@ExtensionMethod(Extensions.class)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class MagnetEnchantment implements Module, Listener {

    @Getter(AccessLevel.PUBLIC)
    public @NotNull Tweaks plugin;

    private static final MaterialSetTag ALLOWED_FOR_HOE_MULTIBLOCK = new MaterialSetTag(new NamespacedKey("tweaks", "magnet/allowed_for_hoe/multiblock"))
            .add(Material.SUGAR_CANE)
            .add(Material.BAMBOO)
            .add(Material.CACTUS)
            .add(Material.KELP)
            .add(Material.KELP_PLANT)
            .lock();

    // Holds all blocks and drops supported by the HOE handler for the MAGNET enchantment.
    private static final MaterialSetTag ALLOWED_FOR_HOE = new MaterialSetTag(new NamespacedKey("tweaks", "magnet/allowed_for_hoe"))
            // Wheat
            .add(Material.WHEAT, Material.WHEAT_SEEDS)
            // Carrots
            .add(Material.CARROT, Material.CARROTS)
            // Potatoes
            .add(Material.POTATO, Material.POTATOES, Material.POISONOUS_POTATO)
            // Beetroots
            .add(Material.BEETROOT, Material.BEETROOTS, Material.BEETROOT_SEEDS)
            // Cocoa
            .add(Material.COCOA, Material.COCOA_BEANS)
            // Nether Warts
            .add(Material.NETHER_WART)
            // Melons / Melon Stems
            .add(Material.MELON, Material.MELON_SLICE)
            .add(Material.MELON_STEM, Material.MELON_SEEDS)
            // Pumpkins / Pumpkin Stems
            .add(Material.PUMPKIN)
            .add(Material.PUMPKIN_STEM, Material.PUMPKIN_SEEDS)
            // Berries
            .add(Material.SWEET_BERRIES)
            .add(Material.SWEET_BERRY_BUSH)
            // Extra
            .add(Material.HAY_BLOCK)
            .add(Material.DRIED_KELP_BLOCK)
            // Experimental
            .add(ALLOWED_FOR_HOE_MULTIBLOCK)
            .lock();

    // Holds all blocks and drops supported by the PICKAXE handler for the MAGNET enchantment.
    private static final MaterialSetTag ALLOWED_FOR_PICKAXE = new MaterialSetTag(new NamespacedKey("tweaks", "magnet/allowed_for_pickaxe"))
            // Block Ores
            .add(MaterialTags.ORES)
            .add(MaterialTags.RAW_ORES)
            .add(MaterialTags.RAW_ORE_BLOCKS)
            .add(MaterialTags.DEEPSLATE_ORES)
            // Raw Minerals
            .add(Material.DIAMOND)
            .add(Material.COAL)
            .add(Material.EMERALD)
            .add(Material.LAPIS_LAZULI)
            .add(Material.REDSTONE)
            .add(Material.QUARTZ)
            .add(Material.GOLD_NUGGET)
            .add(Material.AMETHYST_CLUSTER)
            .add(Material.AMETHYST_SHARD)
            .lock();

    // Holds all blocks and drops supported by the PICKAXE handler for the MAGNET enchantment.
    private static final MaterialSetTag ALLOWED_FOR_WEAPONS = new MaterialSetTag(new NamespacedKey("tweaks", "magnet/allowed_for_weapons"))
            .add(MaterialSetTag.ITEMS_ENCHANTABLE_SHARP_WEAPON.getValues())
            .add(Material.BOW)
            .add(Material.CROSSBOW)
            .lock();

    private static boolean isPickaxe(final ItemStack item) {
        return MaterialSetTag.ITEMS_PICKAXES.isTagged(item.getType());
    }

    private static boolean isHoe(final ItemStack item) {
        return MaterialSetTag.ITEMS_HOES.isTagged(item.getType());
    }

    private static boolean isWeapon(final ItemStack item) {
        return ALLOWED_FOR_WEAPONS.isTagged(item.getType());
    }

    @Override
    public void load() {
        if (PluginConfig.ENABLED_MODULES_MAGNET_ENCHANTMENT == true)
            // Registering events.
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void unload() {
        // Unregistering events.
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(final @NotNull BlockBreakEvent event) {
        final Player player = event.getPlayer();
        // Skipping for CREATIVE or SPECTATOR game modes.
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)
            return;
        final ItemStack tool = player.getInventory().getItemInMainHand();
        // Checking if player's tool is enchanted with Magnet enchantment.
        if (tool.isEnchantedWith("firedot:magnet") == true) {
            final Block block = event.getBlock();
            // Pickaxes
            if (isPickaxe(tool) == true) {
                handleBlockExperienceDrops(player,block, event.getExpToDrop(), ALLOWED_FOR_PICKAXE);
                event.setExpToDrop(0);
            // Hoes
            } else if (isHoe(tool) == true) {
                handleBlockExperienceDrops(player, block, event.getExpToDrop(), ALLOWED_FOR_HOE);
                event.setExpToDrop(0);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDropItem(final @NotNull BlockDropItemEvent event) {
        final Player player = event.getPlayer();
        // Skipping for CREATIVE or SPECTATOR game modes.
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)
            return;
        final ItemStack tool = player.getInventory().getItemInMainHand();
        // Checking if player's tool is enchanted with Magnet enchantment.
        if (tool.isEnchantedWith("firedot:magnet") == true) {
            // Getting the BlockState associated with the event.
            final BlockState blockState = event.getBlockState();
            // Returning if pickaxe enchanted with magnet, destroyed a non-ore block.
            if (isPickaxe(tool) == true)
                handleBlockDrops(player, blockState, event.getItems(), ALLOWED_FOR_PICKAXE);
            // Returning if hoe enchanted with magnet, destroyed a non-crop block.
            else if (isHoe(tool) == true) {
                handleBlockDrops(player, blockState, event.getItems(), ALLOWED_FOR_HOE);
                handleMultiBlockDrops(player, tool, blockState, ALLOWED_FOR_HOE_MULTIBLOCK);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onBerryHarvest(final PlayerHarvestBlockEvent event) {
        // Getting the tool in player's hand.
        final ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();
        // Checking if player's tool is enchanted with Magnet enchantment.
        if (tool.isEnchantedWith("firedot:magnet") == true && isHoe(tool) == true) {
            handleHarvestDrops(event.getPlayer(), event.getHarvestedBlock().getState(), event.getItemsHarvested(), ALLOWED_FOR_HOE);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDeath(final @NotNull EntityDeathEvent event) {
        if (event.getDamageSource().getCausingEntity() instanceof Player player && event.getEntity() instanceof Mob mob) {
            // Returning in case player is no longer online. Not sure if needed, just in case.
            if (player.isOnline() == false || player.isConnected() == false)
                return;
            // Getting the tool in player's hand.
            final ItemStack tool = player.getInventory().getItemInMainHand();
            // Checking if player's tool is enchanted with Magnet enchantment.
            if (tool.isEnchantedWith("firedot:magnet") == true) {
                // Returning for non-weapons.
                if (isWeapon(tool) == false)
                    return;
                // Returning for direct damage caused by bows and crossbows.
                if (tool.getType() == Material.BOW || tool.getType() == Material.CROSSBOW)
                    if (event.getDamageSource().isIndirect() == false)
                        return;
                // Returning if entities are in different worlds.
                if (player.getWorld().equals(event.getEntity().getWorld()) == false)
                    return;
                // Getting the max allowed distance between entities to allow Magnet to work. (4x16 blocks indirect, 1x16 blocks direct)
                final int maxDistance = event.getDamageSource().isIndirect() ? 4096 : 256;
                // Distance check, if it fails, handler won't run and items will drop on the ground.
                if (player.getLocation().distanceSquared(event.getEntity().getLocation()) > maxDistance)
                    return;
                // Getting the experience player would get from destroying this block.
                final int experience = event.getDroppedExp();
                // Disabling vanilla drop of experience will be added to the player in the next step.
                event.setDroppedExp(0);
                // Dropping experience directly at the player's location to make them pick it up instantly.
                if (experience != 0)
                    player.getWorld().spawn(player.getLocation(), ExperienceOrb.class, CreatureSpawnEvent.SpawnReason.NATURAL, (orb) -> {
                        orb.setExperience(experience);
                    });
                // Moving drops from drop list directly to players inventory.
                event.getDrops().removeIf(drop -> {
                    // Checking if player has space for an item
                    if (player.getInventory().hasSpace(drop) == true) {
                        // Adding drops directly to the player's inventory.
                        player.getInventory().addItem(drop);
                        // Scheduling packet stuff asynchronously.
                        plugin.getBedrockScheduler().runAsync(1L, (_) -> {
                            final Location location = fromBukkitLocation(mob.getLocation());
                            sendPackets(SpigotReflectionUtil.generateEntityId(player.getWorld()), player, location, drop);
                        });
                        // Returning true, which will cause the item to be removed from the list.
                        return true;
                    }
                    // Returning false, which will cause the item to not be removed.
                    return false;
                });
            }
        }
    }

    private void handleMultiBlockDrops(final Player player, final ItemStack tool, final BlockState blockState, final MaterialSetTag allowedTypes) {
        if (allowedTypes.isTagged(blockState) == false)
            return;
        // Iterating over blocks above the broken block.
        Block relative = blockState.getWorld().getBlockAt(blockState.getX(), blockState.getY() + 1, blockState.getZ());
        // Extra hard-coded check for Kelp since it can consist of multiple block types.
        while (relative.getType() == blockState.getType() || (relative.getType() == Material.KELP && blockState.getType() == Material.KELP_PLANT)) {
            // Iterating over drops and adding them to the player's inventory.
            relative.getDrops(tool, player).forEach(item -> {
                // Adding drops directly to the player's inventory.
                player.getInventory().addItem(item);
                // Scheduling packet stuff asynchronously.
                plugin.getBedrockScheduler().runAsync(1L, (_) -> {
                    final Location location = fromBukkitLocation(blockState.getLocation().toCenterLocation());
                    // Sending packets...
                    sendPackets(SpigotReflectionUtil.generateEntityId(player.getWorld()), player, location, item);
                });
            });
            // Playing the block break effect.
            // TO-DO: Replace with Effect.DESTROY_BLOCK once 26.1 support is dropped.
            relative.getWorld().playEffect(relative.getLocation(), Effect.DESTROY_BLOCK, relative.getBlockData());
            // Removing the block from the world.
            relative.setType(relative.getType() == Material.KELP || relative.getType() == Material.KELP_PLANT ? Material.WATER : Material.AIR);
            // Updating the relative block.
            relative = relative.getRelative(BlockFace.UP);
        }
    }

    private void handleBlockDrops(final Player player, final BlockState blockState, final Collection<Item> drops, final MaterialSetTag allowedTypes) {
        drops.removeIf(item -> {
            // Skipping items that are not supported by the pickaxe.
            if (allowedTypes.isTagged(item.getItemStack()) == false)
                return false;
            // Checking if player has space for an item.
            if (player.getInventory().hasSpace(item.getItemStack()) == true) {
                // Adding drops directly to the player's inventory.
                player.getInventory().addItem(item.getItemStack());
                // Scheduling packet stuff asynchronously.
                plugin.getBedrockScheduler().runAsync(1L, (_) -> {
                    final Location location = fromBukkitLocation(blockState.getLocation().toCenterLocation());
                    sendPackets(SpigotReflectionUtil.generateEntityId(player.getWorld()), player, location, item.getItemStack());
                });
                // Returning true, which will cause the item to be removed from the list.
                return true;
            }
            // Returning false, which will cause the item to not be removed.
            return false;
        });
    }

    private void handleHarvestDrops(final Player player, final BlockState blockState, final Collection<ItemStack> drops, final MaterialSetTag allowedTypes) {
        drops.removeIf(item -> {
            // Skipping items that are not supported by the pickaxe.
            if (allowedTypes.isTagged(item) == false)
                return false;
            // Checking if player has space for an item.
            if (player.getInventory().hasSpace(item) == true) {
                // Adding drops directly to the player's inventory.
                player.getInventory().addItem(item);
                // Scheduling packet stuff asynchronously.
                plugin.getBedrockScheduler().runAsync(1L, (_) -> {
                    final Location location = fromBukkitLocation(blockState.getLocation().toCenterLocation());
                    sendPackets(SpigotReflectionUtil.generateEntityId(player.getWorld()), player, location, item);
                });
                // Returning true, which will cause the item to be removed from the list.
                return true;
            }
            // Returning false, which will cause the item to not be removed.
            return false;
        });
    }

    private void handleBlockExperienceDrops(final Player player, final Block block, final int experience, final MaterialSetTag allowedTypes) {
        if (allowedTypes.isTagged(block) == false)
            return;
        // Dropping experience directly at the player's location to make them pick it up instantly.
        if (experience != 0) {
            player.getWorld().spawn(player.getLocation(), ExperienceOrb.class, CreatureSpawnEvent.SpawnReason.NATURAL, (orb) -> {
                orb.setExperience(experience);
            });
        }
    }

    private void sendPackets(final int id, final @NotNull Player player, final @NotNull Location location, final @NotNull ItemStack item) {
        // Creating PlayServerSpawnEntity packet.
        final var PlayServerSpawnEntityPacket = new WrapperPlayServerSpawnEntity(id, UUID.randomUUID(), EntityTypes.ITEM, location, 0, 0, null);
        // Creating PlayServerEntityMetadata packet.
        final var PlayServerEntityMetadataPacket = new WrapperPlayServerEntityMetadata(id, List.of(new EntityData(8, EntityDataTypes.ITEMSTACK, SpigotConversionUtil.fromBukkitItemStack(item))));
        // Creating PlayServerCollectItem packet.
        final var PlayServerCollectItemPacket = new WrapperPlayServerCollectItem(id, player.getEntityId(), item.getAmount());
        // Sending packets...
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerBundle());
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, PlayServerSpawnEntityPacket);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, PlayServerEntityMetadataPacket);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, PlayServerCollectItemPacket);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerBundle());
    }

}
