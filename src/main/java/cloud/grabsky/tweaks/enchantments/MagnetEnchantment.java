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
package cloud.grabsky.tweaks.enchantments;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

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
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerCollectItem;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.ExperienceOrb;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;

@ExtensionMethod(Extensions.class)
@SuppressWarnings("UnstableApiUsage")
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class MagnetEnchantment implements Module, Listener {

    @Getter(AccessLevel.PUBLIC)
    public @NotNull Tweaks plugin;

    // Holds all blocks and drops supported by the HOE handler for the MAGNET enchantment.
    private static final MaterialSetTag SUPPORTED_CROPS = new MaterialSetTag(new NamespacedKey("tweaks", "supported_crops"))
            .add(Material.WHEAT)
            .add(Material.WHEAT_SEEDS)
            .add(Material.CARROT)
            .add(Material.CARROTS)
            .add(Material.POTATO)
            .add(Material.POTATOES)
            .add(Material.BEETROOT)
            .add(Material.BEETROOTS)
            .add(Material.BEETROOT_SEEDS)
            .add(Material.COCOA)
            .add(Material.COCOA_BEANS)
            .add(Material.NETHER_WART)
            .add(Material.MELON)
            .add(Material.MELON_STEM)
            .add(Material.MELON_SEEDS)
            .add(Material.MELON_SLICE)
            .add(Material.PUMPKIN)
            .add(Material.PUMPKIN_STEM)
            .add(Material.PUMPKIN_SEEDS)
            // Experimental
            .add(Material.SUGAR_CANE)
            .add(Material.BAMBOO)
            .add(Material.CACTUS)
            .lock();

    // Holds all blocks and drops supported by the PICKAXE handler for the MAGNET enchantment.
    private static final MaterialSetTag SUPPORTED_MINERALS = new MaterialSetTag(new NamespacedKey("tweaks", "supported_minerals"))
            .add(MaterialTags.ORES)
            .add(MaterialTags.RAW_ORES)
            .add(MaterialTags.RAW_ORE_BLOCKS)
            .add(MaterialTags.DEEPSLATE_ORES)
            .add(Material.DIAMOND)
            .add(Material.COAL)
            .add(Material.EMERALD)
            .add(Material.LAPIS_LAZULI)
            .add(Material.REDSTONE)
            .add(Material.QUARTZ)
            .add(Material.GOLD_NUGGET)
            .add(Material.AMETHYST_CLUSTER)
            .add(Material.AMETHYST_SHARD)
            // Echo Shard has a small chance to drop when destroying Amethyst Cluster on our server.
            .add(Material.ECHO_SHARD)
            .lock();

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
        // Checking if player is in Survival game mode
        if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
            final ItemStack tool = player.getInventory().getItemInMainHand();
            // Checking if player's tool is enchanted with Magnet enchantment.
            if (tool.isEnchantedWith("firedot:magnet") == true) {
                final Block block = event.getBlock();
                // Returning if pickaxe enchanted with magnet destroyed non-ore block.
                if (MaterialTags.PICKAXES.isTagged(tool) == true && SUPPORTED_MINERALS.isTagged(block) == false)
                    return;
                // Returning if hoe enchanted with magnet destroyed non-crop block.
                if (MaterialTags.HOES.isTagged(tool) == true && SUPPORTED_CROPS.isTagged(block) == false)
                    return;
                // Getting the experience player would get from destroying this block.
                final int experience = event.getExpToDrop();
                // Disabling vanilla drop of experience, will be added to the player in the next step.
                event.setExpToDrop(0);
                // Dropping experience directly at the player's location to make them pick it up instantly.
                if (experience != 0) player.getWorld().spawn(player.getLocation(), ExperienceOrb.class, CreatureSpawnEvent.SpawnReason.NATURAL, (orb) -> {
                    orb.setExperience(experience);
                });
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDropItem(final @NotNull BlockDropItemEvent event) {
        final Player player = event.getPlayer();
        // Checking if player is in Survival game mode
        if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
            final ItemStack tool = player.getInventory().getItemInMainHand();
            // Checking if player's tool is enchanted with Magnet enchantment.
            if (tool.isEnchantedWith("firedot:magnet") == true) {
                // Getting the BlockState associated with the event.
                final BlockState blockState = event.getBlockState();
                // Returning if pickaxe enchanted with magnet destroyed non-ore block.
                if (MaterialTags.PICKAXES.isTagged(tool) == true && SUPPORTED_MINERALS.isTagged(blockState) == false)
                    return;
                // Returning if hoe enchanted with magnet destroyed non-crop block.
                if (MaterialTags.HOES.isTagged(tool) == true && SUPPORTED_CROPS.isTagged(blockState) == false)
                    return;
                // ...
                if (blockState.getType() == Material.SUGAR_CANE || blockState.getType() == Material.BAMBOO || blockState.getType() == Material.CACTUS) {
                    // Iterating over blocks above the broken block.
                    Block relative = blockState.getWorld().getBlockAt(blockState.getX(), blockState.getY() + 1, blockState.getZ());
                    // ...
                    while (relative.getType() == blockState.getType()) {
                        // Iterating over drops and adding them to the player's inventory.
                        relative.getDrops(tool, player).forEach(item -> {
                            // Adding drops directly to the player's inventory.
                            player.getInventory().addItem(item);
                            // Creating next entity identifier for use with packets.
                            final int id = Bukkit.getUnsafe().nextEntityId();
                            // Scheduling packet stuff asynchronously.
                            plugin.getBedrockScheduler().runAsync(1L, (_) -> {
                                final Location location = new Location(event.getBlockState().getX() + 0.5D, event.getBlockState().getY() + 0.5D, event.getBlockState().getZ() + 0.5D, 0F, 0F);
                                // Creating PlayServerSpawnEntity packet.
                                final var PlayServerSpawnEntityPacket = new WrapperPlayServerSpawnEntity(id, UUID.randomUUID(), EntityTypes.ITEM, location, 0, 0, null);
                                // Creating PlayServerEntityMetadata packet.
                                final var PlayServerEntityMetadataPacket = new WrapperPlayServerEntityMetadata(id, List.of(new EntityData(8, EntityDataTypes.ITEMSTACK, SpigotConversionUtil.fromBukkitItemStack(item))));
                                // Creating PlayServerCollectItem packet.
                                final var PlayServerCollectItemPacket = new WrapperPlayServerCollectItem(id, player.getEntityId(), item.getAmount());
                                // Sending packets...
                                PacketEvents.getAPI().getPlayerManager().sendPacket(player, PlayServerSpawnEntityPacket);
                                PacketEvents.getAPI().getPlayerManager().sendPacket(player, PlayServerEntityMetadataPacket);
                                PacketEvents.getAPI().getPlayerManager().sendPacket(player, PlayServerCollectItemPacket);
                            });
                        });
                        // Playing the block break effect.
                        relative.getWorld().playEffect(relative.getLocation(), Effect.STEP_SOUND, relative.getBlockData());
                        // Removing the block from the world.
                        relative.setType(Material.AIR);
                        // Updating the relative block.
                        relative = relative.getRelative(BlockFace.UP);
                    }
                }
                // Removing items...
                event.getItems().removeIf(item -> {
                    // Skipping items that are not supported by the pickaxe.
                    if (MaterialTags.PICKAXES.isTagged(tool) == true && SUPPORTED_MINERALS.isTagged(item.getItemStack()) == false)
                        return false;
                    // Skipping items that are not supported by the hoe.
                    if (MaterialTags.HOES.isTagged(tool) == true && SUPPORTED_CROPS.isTagged(item.getItemStack()) == false)
                        return false;
                    // Checking if player has space for an item.
                    if (player.getInventory().hasSpace(item.getItemStack()) == true) {
                        // Adding drops directly to the player's inventory.
                        player.getInventory().addItem(item.getItemStack());
                        // Creating next entity identifier for use with packets.
                        final int id = Bukkit.getUnsafe().nextEntityId();
                        // Scheduling packet stuff asynchronously.
                        plugin.getBedrockScheduler().runAsync(1L, (_) -> {
                            final Location location = new Location(event.getBlockState().getX() + 0.5D, event.getBlockState().getY() + 0.5D, event.getBlockState().getZ() + 0.5D, 0F, 0F);
                            // Creating PlayServerSpawnEntity packet.
                            final var PlayServerSpawnEntityPacket = new WrapperPlayServerSpawnEntity(id, UUID.randomUUID(), EntityTypes.ITEM, location, 0, 0, null);
                            // Creating PlayServerEntityMetadata packet.
                            final var PlayServerEntityMetadataPacket = new WrapperPlayServerEntityMetadata(id, List.of(new EntityData(8, EntityDataTypes.ITEMSTACK, SpigotConversionUtil.fromBukkitItemStack(item.getItemStack()))));
                            // Creating PlayServerCollectItem packet.
                            final var PlayServerCollectItemPacket = new WrapperPlayServerCollectItem(id, player.getEntityId(), item.getItemStack().getAmount());
                            // Sending packets...
                            PacketEvents.getAPI().getPlayerManager().sendPacket(player, PlayServerSpawnEntityPacket);
                            PacketEvents.getAPI().getPlayerManager().sendPacket(player, PlayServerEntityMetadataPacket);
                            PacketEvents.getAPI().getPlayerManager().sendPacket(player, PlayServerCollectItemPacket);
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
                if (MaterialTags.SWORDS.isTagged(tool) == false && tool.getType().asItemType() != ItemType.BOW && tool.getType().asItemType() != ItemType.CROSSBOW)
                    return;
                // Returning for distances greater than 24 blocks. (Bow / Crossbow) (24x24 = 576)
                if (player.getLocation().distanceSquared(event.getEntity().getLocation()) > 576)
                    return;
                // Getting the experience player would get from destroying this block.
                final int experience = event.getDroppedExp();
                // Disabling vanilla drop of experience, will be added to the player in the next step.
                event.setDroppedExp(0);
                // Dropping experience directly at the player's location to make them pick it up instantly.
                if (experience != 0)
                    player.getWorld().spawn(player.getLocation(), ExperienceOrb.class, CreatureSpawnEvent.SpawnReason.NATURAL, (orb) -> {
                        orb.setExperience(experience);
                    });
                final List<ItemStack> drops = new ArrayList<>(event.getDrops());
                // Getting drops; Ores drop only one ItemStack, so we can safely get the first element from the Collection
                drops.forEach(drop -> {
                    // Checking if player has space for an item
                    if (player.getInventory().hasSpace(drop) == true) {
                        // Setting drops to false as player has enough space for an item
                        event.getDrops().remove(drop);
                        // Adding drops directly to the player's inventory.
                        player.getInventory().addItem(drop);
                        // Creating next entity identifier for use with packets.
                        final int id = Bukkit.getUnsafe().nextEntityId();
                        // Scheduling packet stuff asynchronously.
                        plugin.getBedrockScheduler().runAsync(1L, (_) -> {
                            final Location location = new Location(mob.getLocation().getX() + 0.5D, mob.getLocation().getY() + 0.5D, mob.getLocation().getZ() + 0.5D, 0F, 0F);
                            // Creating PlayServerSpawnEntity packet.
                            final var PlayServerSpawnEntityPacket = new WrapperPlayServerSpawnEntity(id, UUID.randomUUID(), EntityTypes.ITEM, location, 0, 0, null);
                            // Creating PlayServerEntityMetadata packet.
                            final var PlayServerEntityMetadataPacket = new WrapperPlayServerEntityMetadata(id, List.of(new EntityData(8, EntityDataTypes.ITEMSTACK, SpigotConversionUtil.fromBukkitItemStack(drop))));
                            // Creating PlayServerCollectItem packet.
                            final var PlayServerCollectItemPacket = new WrapperPlayServerCollectItem(id, player.getEntityId(), drop.getAmount());
                            // Sending packets...
                            PacketEvents.getAPI().getPlayerManager().sendPacket(player, PlayServerSpawnEntityPacket);
                            PacketEvents.getAPI().getPlayerManager().sendPacket(player, PlayServerEntityMetadataPacket);
                            PacketEvents.getAPI().getPlayerManager().sendPacket(player, PlayServerCollectItemPacket);
                        });
                    }
                });
            }
        }
    }

}
