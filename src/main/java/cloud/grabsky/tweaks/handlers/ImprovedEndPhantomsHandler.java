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
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Phantom;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class ImprovedEndPhantomsHandler implements Module, Listener {

    private final @NotNull Tweaks plugin;

    // Task responsible for updating phantoms' target entity to nearby player.
    private @Nullable BukkitTask task = null;

    private static final NamespacedKey THE_END = NamespacedKey.minecraft("the_end");

    @Override
    public void load() {
        // Unregistering events.
        HandlerList.unregisterAll(this);
        // // Cancelling existing task.
        if (this.task != null)
            task.cancel();
        // ...
        if (PluginConfig.ENABLED_MODULES_IMPROVED_END_PHANTOMS == true) {
            // Scheduling target task.
            this.task = plugin.getBedrockScheduler().repeat(0L, 20L, Long.MAX_VALUE, (_) -> {
                // Skipping when plugin has not been fully enabled yet, or server is marked as paused.
                if (plugin.isEnabled() == false || plugin.getServer().isPaused() == true)
                    return true;
                // Getting the world instance.
                final World world = plugin.getServer().getWorld(THE_END);
                // Skipping when world is not loaded.
                if (world == null)
                    return true;
                // Iterating over all players in the world.
                world.getPlayers().forEach(player -> {
                    // Skipping for players that are invulnerable.
                    if (player.getGameMode().isInvulnerable() == true || player.isInvulnerable() == true)
                        return;
                    // Iterating over nearby entities, looking for phantoms.
                    world.getNearbyLivingEntities(player.getLocation(), 8, 16, 8).forEach(entity -> {
                        // If entity is a phantom with no target specified, marking player as it's target.
                        if (entity instanceof Phantom phantom && phantom.getTarget() == null)
                            phantom.setTarget(player);
                    });
                });
                return true;
            });
            // Registering events.
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    @Override
    public void unload() { /* HANDLED INSIDE LOAD */ }

    // Responsible for correcting spawn height of phantoms.
    @EventHandler(ignoreCancelled = true)
    public void onPhantomSpawn(final @NotNull CreatureSpawnEvent event) {
        // Returning if entity did not spawn naturally.
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL)
            return;
        // Checking whether spawned entity is phantom and it spawned in the end dimension.
        if (event.getEntity() instanceof Phantom phantom && event.getLocation().getWorld().key().equals(THE_END) == true) {// Correcting spawn height.
            // Correcting spawn location, it 10-12 blocks higher than initial spawn location.
            final Location location = event.getLocation().clone().add(0.0, new Random().nextInt(10, 13), 0.0);
            // Setting anchor location to corrected spawn location.
            phantom.setAnchorLocation(location);
            // Teleporting to the corrected spawn location.
            phantom.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
        }
    }

}
