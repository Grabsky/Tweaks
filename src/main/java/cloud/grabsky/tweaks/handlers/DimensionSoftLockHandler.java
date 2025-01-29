/*
 * Tweaks (https://github.com/Grabsky/Tweaks)
 *
 * Copyright (C) 2025  Grabsky <michal.czopek.foss@proton.me>
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

import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.bedrock.util.Interval;
import cloud.grabsky.tweaks.Module;
import cloud.grabsky.tweaks.Tweaks;
import cloud.grabsky.tweaks.configuration.PluginConfig;
import cloud.grabsky.tweaks.utils.Extensions;
import net.kyori.adventure.sound.Sound;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Statistic;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

import static cloud.grabsky.bedrock.util.Interval.Unit;
import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

@ExtensionMethod(Extensions.class)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class DimensionSoftLockHandler implements Module, Listener {

    @Getter(AccessLevel.PUBLIC)
    public @NotNull Tweaks plugin;

    @Override
    public void load() {
        if (PluginConfig.ENABLED_MODULES_DIMENSION_SOFT_LOCK == true)
            // Registering events.
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void unload() {
        // Unregistering events.
        HandlerList.unregisterAll(this);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onDimensionChange(final @NotNull PlayerTeleportEvent event) {
        if (event.getCause() != TeleportCause.END_PORTAL && event.getCause() != TeleportCause.NETHER_PORTAL && event.getCause() != TeleportCause.PLUGIN)
            return;
        // Getting the world identifier.
        final NamespacedKey world = event.getTo().getWorld().getKey();
        // Skipping if destination world is the same.
        if (event.getFrom().getWorld().getKey().equals(world) == true)
            return;
        // Skipping if player has bypass permission.
        if (event.getPlayer().hasPermission("tweaks.bypass.dimension_soft_lock") == true)
            return;
        // Getting the soft lock time from config.
        final long seconds = PluginConfig.DIMENSION_SOFT_LOCK_SETTINGS_DIMENSIONS.getOrDefault(world, 0L);
        final Interval interval = Interval.between(seconds, event.getPlayer().getStatistic(Statistic.PLAY_ONE_MINUTE) / 20, Unit.SECONDS);
        // Checking if player has played long enough to be able to access the dimension.
        if (interval.as(Unit.SECONDS) >= 0L) {
            // Cancelling the event.
            event.setCancelled(true);
            // Sending error message to the player.
            Message.of(PluginConfig.DIMENSION_SOFT_LOCK_SETTINGS_ERROR_MESSAGES.getOrDefault(world, ""))
                    .placeholder("time_left", formatInterval(interval, 2))
                    .send(event.getPlayer());
            // Getting the error sound.
            final @Nullable Sound sound = PluginConfig.DIMENSION_SOFT_LOCK_SETTINGS_ERROR_SOUNDS.get(world);
            // Playing error sound to the player.
            if (sound != null)
                event.getPlayer().playSound(sound);
            // Teleporting player back to the closest END_PORTAL_FRAME block behind them.
            if (event.getCause() == TeleportCause.END_PORTAL) {
                final @Nullable Block block = getFirstBlockBehind(event.getPlayer(), 4, (b) -> b.getType() == Material.END_PORTAL_FRAME);
                if (block != null)
                    event.getPlayer().teleport(event.getPlayer().getLocation().set(block.getX() + 0.5, block.getY() + 1.0, block.getZ() + 0.5));
            }
        }
    }

    /** Returns the first {@link Block} behind the {@link Player} that matches specified {@link Predicate}. */
    @SuppressWarnings("UnstableApiUsage")
    private static @Nullable Block getFirstBlockBehind(final Player player, final int max, final Predicate<Block> predicate) {
        // Getting the location of the player.
        final Location location = player.getLocation().toBlockLocation();
        // Getting direction opposite to one player is currently facing.
        final Vector direction = player.getLocation().clone().setRotation(player.getYaw(), 0.0F).getDirection().multiply(-1);
        // Raytracing blocks in the direction opposite to the player.
        final RayTraceResult result = location.getWorld().rayTraceBlocks(location, direction, max, FluidCollisionMode.NEVER, true, predicate);
        // ...
        return (result != null) ? result.getHitBlock() : null;
    }

    /** Formats the output of {@link Interval#toString()} by showing only the first {@code max} parts of it, */
    private static @NotNull String formatInterval(final Interval interval, final int max) {
        // Splitting on whitespace.
        final String[] parts = interval.toString().split(" ");
        // Preparing the result StringBuilder.
        final StringBuilder result = new StringBuilder();
        // Iterating over parts of the split string.
        for (final String part : parts) {
            // Appending part to the result, if it is not empty.
            if (part.isEmpty() == false) {
                result.append(part).append(" ");
                // Breaking from the loop if result has reached the maximum length.
                if (result.toString().trim().split(" ").length == max)
                    break;
            }
        }
        // Returning the result.
        return result.toString().trim();
    }

}
