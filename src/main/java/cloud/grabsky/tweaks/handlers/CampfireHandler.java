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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.type.Campfire;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

// This class actually handle two modules. It's easier and more performant that way.
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class CampfireHandler implements Module, Listener {

    private final @NotNull Tweaks plugin;

    // Responsible for tracking players in range of campfire.
    private @Nullable BukkitTask campfireTrackerTask = null;

    // Responsible for periodical regeneration of players near campfire.
    private @Nullable BukkitTask campfireRegenerationTask = null;

    // Stores UUIDs of players that are currently near a campfire. Updated every second by campfire tracker task.
    private final @NotNull Set<UUID> campfireTrackerStorage = new HashSet<>();

    @Override
    public void load() {
        // Clearing handlers list.
        HandlerList.unregisterAll(this);
        // Clearing campfire tracker task in case it is running.
        if (this.campfireTrackerTask != null) {
            // Cancelling existing task.
            campfireTrackerTask.cancel();
            // Clearing the set.
            campfireTrackerStorage.clear();
        }
        // Clearing campfire regeneration task in case it is running.
        if (this.campfireRegenerationTask != null) {
            // Cancelling existing task.
            campfireRegenerationTask.cancel();
        }
        // Scheduling the campfire tracker task. It runs every second.
        this.campfireTrackerTask = plugin.getBedrockScheduler().repeatAsync(0L, 20L, Long.MAX_VALUE, (_) -> {
            for (final Player player : Bukkit.getOnlinePlayers()) {
                if (isCampfireNearby(player.getLocation(), 2) == true) {
                    if (campfireTrackerStorage.contains(player.getUniqueId()) == false)
                        campfireTrackerStorage.add(player.getUniqueId());
                } else if (campfireTrackerStorage.contains(player.getUniqueId()) == true) {
                    campfireTrackerStorage.remove(player.getUniqueId());
                }
            }
            // ...
            return true;
        });
        // Scheduling campfire regeneration task, if enabled.
        if (PluginConfig.ENABLED_MODULES_CAMPFIRE_REGENERATION == true) {
            // Scheduling the task. It runs every 10 seconds.
            this.campfireRegenerationTask = plugin.getBedrockScheduler().repeat(0L, 200L, Long.MAX_VALUE, (_) -> {
                for (final Player player : Bukkit.getOnlinePlayers()) {
                    // Skipping if player is in the water.
                    if (player.isUnderWater() == true)
                        continue;
                    // Skipping invulnerable players or players that are not in range of any campfire.
                    if (player.isInvulnerable() == true || player.getGameMode().isInvulnerable() == true || campfireTrackerStorage.contains(player.getUniqueId()) == false)
                        continue;
                    // Healing player by 1 HP.
                    player.heal(1.0, EntityRegainHealthEvent.RegainReason.CUSTOM);
                }
                // ...
                return true;
            });
        }
        // Registering event handlers for "campfire-safety" module, if enabled.
        if (PluginConfig.ENABLED_MODULES_CAMPFIRE_PREVENTS_MOB_TRACKING == true)
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void unload() { /* HANDLED INSIDE LOAD */ }

    /* EVENT LISTENERS */

    @SuppressWarnings("UnstableApiUsage")
    @EventHandler(ignoreCancelled = true)
    public void onEntityTargetPlayer(final @NotNull EntityTargetLivingEntityEvent event) {
        if (event.getTarget() instanceof Player player && event.getEntity() instanceof Monster monster) {
            // Skipping if player is not in overworld or exposed to sky light during day.
            if (player.getWorld().getEnvironment() != World.Environment.NORMAL || (player.getWorld().isDayTime() == true && player.getLocation().getBlock().getLightFromSky() != 0) == true)
                return;
            // ...
            if (campfireTrackerStorage.contains(player.getUniqueId()) == true) {
                final @Nullable EntityDamageEvent lastDamage = monster.getLastDamageCause();
                // Skipping if player has attacked the entity.
                if (lastDamage != null && lastDamage.getDamageSource().getCausingEntity() != null && lastDamage.getDamageSource().getCausingEntity().equals(event.getTarget()) == true)
                    return;
                // ...
                event.setCancelled(true);
            }
        }
    }

    /* UTILITY METHODS */

    @SuppressWarnings("UnstableApiUsage")
    private boolean isCampfireNearby(final @NotNull Location location, final int radius) {
        for (int x = location.blockX() - radius; x <= location.blockX() + radius; x++) {
            for (int y = location.blockY() - radius; y <= location.blockY() + radius; y++) {
                for (int z = location.blockZ() - radius; z <= location.blockZ() + radius; z++) {
                    if (location.getWorld().getBlockData(x, y, z) instanceof Campfire campfire && campfire.isLit() == true)
                        return true;
                }
            }
        }
        return false;
    }

}
