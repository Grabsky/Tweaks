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
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class CompassHandler implements Module, Listener {

    private final Tweaks plugin;
    private final HashMap<UUID, BossBar> storage = new HashMap<>();

    private @Nullable BukkitTask task = null;

    private static final DecimalFormat COORD_FORMAT = new DecimalFormat("#,###");

    @Override
    public void load() {
        // Clearing handlers list.
        HandlerList.unregisterAll(this);
        // Clearing current task in case it already exist.
        if (this.task != null) {
            // Cancelling existing task.
            this.task.cancel();
            // Removing viewers from currently stored boss bars.
            storage.values().forEach(bar -> bar.viewers().forEach(viewer -> {
                // Trying to cast to an Audience, which is very unlikely to fail.
                if (viewer instanceof Audience audience)
                    // Removing this audience from viewers.
                    bar.removeViewer(audience);
            }));
            // Clearing the map, boss bars should (hopefully) be cleaned by GC.
            storage.clear();
        }
        // Returning in case enhanced compass is disabled.
        if (PluginConfig.ENABLED_MODULES_ENHANCED_COMPASS == false)
            return;
        // Registering event handlers.
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        // Scheduling the task.
        this.task = plugin.getBedrockScheduler().repeatAsync(PluginConfig.COMPASS_SETTINGS_REFRESH_RATE, PluginConfig.COMPASS_SETTINGS_REFRESH_RATE, Long.MAX_VALUE, (cycles) -> {
            for (final Player player : Bukkit.getOnlinePlayers()) {
                // Getting or computing boss bar.
                final BossBar bar = storage.computeIfAbsent(player.getUniqueId(), (___) -> {
                    return BossBar.bossBar(Component.empty(), 0.0F, PluginConfig.COMPASS_SETTINGS_BOSSBAR.getColor(), PluginConfig.COMPASS_SETTINGS_BOSSBAR.getOverlay());
                });
                if (player.getInventory().getItemInMainHand().getType() == Material.COMPASS || player.getInventory().getItemInOffHand().getType() == Material.COMPASS) {
                    final Component text = Component.text(getFormattedCoords(player));
                    // Updating the name in case different.
                    if (bar.name().equals(text) == false)
                        bar.name(text);
                    // Showing in case hidden.
                    if (bar.viewers().iterator().hasNext() == false)
                        bar.addViewer(player);
                } else if (bar.viewers().iterator().hasNext() == true)
                    bar.removeViewer(player);
            }
            // ...
            return true;
        });
    }

    @Override
    public void unload() { /* HANDLED INSIDE LOAD */ }

    @EventHandler // Clean-ups boss bar related stuff when player leaves. Apparently boss bars are untracked by the server and can be subject to memory leaks.
    public void onPlayerQuit(final @NotNull PlayerQuitEvent event) {
        final UUID uniqueId = event.getPlayer().getUniqueId();
        // Removing boss bar from the map.
        final @Nullable BossBar bar = storage.remove(uniqueId);
        // Removing player from viewers of this boss bar.
        if (bar != null)
            event.getPlayer().hideBossBar(bar);
    }

    /**
     * Formats and returns human-readable coordinates ready to be converted to a {@link Component}.
     */
    private static @NotNull String getFormattedCoords(final @NotNull Player player) {
        return PluginConfig.COMPASS_SETTINGS_BOSSBAR.getText()
                .replace("<location_x>", COORD_FORMAT.format(player.getLocation().getBlockX()))
                .replace("<location_y>", COORD_FORMAT.format(player.getLocation().getBlockY()))
                .replace("<location_z>", COORD_FORMAT.format(player.getLocation().getBlockZ()));
    }

}
