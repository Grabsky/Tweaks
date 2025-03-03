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

import cloud.grabsky.tweaks.Module;
import cloud.grabsky.tweaks.Tweaks;
import cloud.grabsky.tweaks.configuration.PluginConfig;
import cloud.grabsky.tweaks.utils.Extensions;
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
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(Extensions.class)
@SuppressWarnings("UnstableApiUsage")
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class MagnetEnchantment implements Module, Listener {

    @Getter(AccessLevel.PUBLIC)
    public @NotNull Tweaks plugin;

    private static final HashSet<BlockType> SUPPORTED_CROPS = new HashSet<>() {{
        add(BlockType.WHEAT);
        add(BlockType.CARROTS);
        add(BlockType.POTATOES);
        add(BlockType.BEETROOTS);
        add(BlockType.COCOA);
        add(BlockType.NETHER_WART);
        add(BlockType.MELON);
        add(BlockType.MELON_STEM);
        add(BlockType.PUMPKIN);
        add(BlockType.PUMPKIN_STEM);
    }};

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

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(final @NotNull BlockBreakEvent event) {
        final Player player = event.getPlayer();
        // Checking if player is in Survival game mode
        if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
            final ItemStack tool = player.getInventory().getItemInMainHand();
            // Checking if player's tool is enchanted with Magnet enchantment.
            if (tool.isEnchantedWith("firedot:magnet") == true) {
                final Block block = event.getBlock();
                if (MaterialTags.PICKAXES.isTagged(tool) == true && (MaterialTags.ORES.isTagged(block) == true || MaterialTags.RAW_ORE_BLOCKS.isTagged(block) == true) == false)
                    return;
                if (MaterialTags.HOES.isTagged(tool) == true && SUPPORTED_CROPS.contains(block.getType().asBlockType()) == false)
                    return;
                // Getting the experience player would get from destroying this block.
                final int experience = event.getExpToDrop();
                // Disabling vanilla drop of experience, will be added to the player in the next step.
                event.setExpToDrop(0);
                // Dropping experience directly at the player's location to make them pick it up instantly.
                if (experience != 0)
                    player.getWorld().spawn(player.getLocation(), ExperienceOrb.class, CreatureSpawnEvent.SpawnReason.NATURAL, (orb) -> {
                        orb.setExperience(experience);
                    });
                // Getting drops; Ores drop only one ItemStack, so we can safely get the first element from the Collection
                event.getBlock().getDrops(tool).forEach(drop -> {
                    // Checking if player has space for an item
                    if (player.getInventory().hasSpace(drop) == true) {
                        // Setting drops to false as player has enough space for an item
                        event.setDropItems(false);
                        // Adding drops directly to the player's inventory.
                        player.getInventory().addItem(drop);
                        // Creating next entity identifier for use with packets.
                        final int id = Bukkit.getUnsafe().nextEntityId();
                        // Scheduling packet stuff asynchronously.
                        plugin.getBedrockScheduler().runAsync(1L, (_) -> {
                            final Location location = new Location(block.getX() + 0.5D, block.getY() + 0.5D, block.getZ() + 0.5D, 0F, 0F);
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
