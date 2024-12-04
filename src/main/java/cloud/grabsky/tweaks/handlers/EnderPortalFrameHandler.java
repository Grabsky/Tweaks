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
package cloud.grabsky.tweaks.handlers;

import cloud.grabsky.tweaks.Module;
import cloud.grabsky.tweaks.Tweaks;
import cloud.grabsky.tweaks.configuration.PluginConfig;
import cloud.grabsky.tweaks.utils.Extensions;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.EndPortalFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(Extensions.class)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class EnderPortalFrameHandler implements Module, Listener {

    @Getter(AccessLevel.PUBLIC)
    public @NotNull Tweaks plugin;

    @Override
    public void load() {
        if (PluginConfig.ENABLED_MODULES_ENDER_PORTAL_FRAME_MINI_GAME == true)
            // Registering events.
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void unload() {
        // Unregistering events.
        HandlerList.unregisterAll(this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(final @NotNull PlayerInteractEvent event) {
        if (event.getItem() == null || event.getItem().getType() != Material.ENDER_EYE)
            return;
        if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.END_PORTAL_FRAME) {
            final EndPortalFrame clickedFrame = (EndPortalFrame) event.getClickedBlock().getBlockData();
            // ...
            if (clickedFrame.hasEye() == false) {
                // Getting random portal frame near the clicked block.
                final @Nullable Block randomFrame = getRandomFrame(event.getClickedBlock());
                // Skipping if null.
                if (randomFrame == null)
                    return;
                // Getting block data.
                final EndPortalFrame data = (EndPortalFrame) randomFrame.getBlockData();
                // Removing eye from the frame.
                data.setEye(false);
                // Spawning eye item at the location of the frame.
                randomFrame.getWorld().dropItemNaturally(randomFrame.getLocation().add(0, 0.5, 0), ItemStack.of(Material.ENDER_EYE));
                // Updating block data.
                randomFrame.setBlockData(data);

            }
        }
    }

    private static @Nullable Block getRandomFrame(final @NotNull Block block) {
        final int radius = 4;
        // Creating list which is going to hold all nearby end frame blocks.
        final List<Block> blocks = new ArrayList<>(0);
        // Iterating over nearby blocks...
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                // Skipping 'self' block, which is the block that has been passed as method argument.
                if (x == 0 && z == 0)
                    continue;
                // Getting relative block.
                final Block nearbyBlock = block.getRelative(x, 0, z);
                // Verifying if the block is end portal frame and has an eye in it. Adding to the list.
                if (nearbyBlock.getBlockData() instanceof EndPortalFrame frame && frame.hasEye() == true)
                    blocks.add(nearbyBlock);
            }
        }
        // Returning list or null if empty.
        return (blocks.isEmpty() == false)
                ? blocks.get(new Random().nextInt(Math.max(0, blocks.size() - 1)))
                : null;
    }
}
