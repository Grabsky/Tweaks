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
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.entity.CraftItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.persistence.PersistentDataType;

import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(Extensions.class)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class ReasonableItemDespawnHandler implements Module, Listener {

    @Getter(AccessLevel.PUBLIC)
    public @NotNull Tweaks plugin;

    private static final NamespacedKey DEATH_DROP_MARKER = new NamespacedKey("tweaks", "death_drop_marker");

    @Override
    public void load() {
        if (PluginConfig.ENABLED_MODULES_REASONABLE_ITEM_DESPAWN == true)
            // Registering events.
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void unload() {
        // Unregistering events.
        HandlerList.unregisterAll(this);
    }

    @EventHandler(ignoreCancelled = true, priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onPlayerDeath(final @NotNull PlayerDeathEvent event) {
        // Returning in case keep inventory is enabled.
        if (event.getKeepInventory() == true)
            return;
        // Marking each dropped item as a death drop, which is then handled (and removed) within ItemSpawnEvent listener.
        event.getDrops().stream().filter(it -> event.getItemsToKeep().contains(it) == false).forEach(it -> {
            it.editMeta(meta -> {
                meta.getPersistentDataContainer().set(DEATH_DROP_MARKER, PersistentDataType.BYTE, (byte) 1);
            });
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemSpawn(final @NotNull ItemSpawnEvent event) {
        if (event.getEntity().getItemStack().getPersistentDataContainer().has(DEATH_DROP_MARKER) == true) {
            // Removing the marker.
            event.getEntity().getItemStack().editMeta(meta -> {
                meta.getPersistentDataContainer().remove(DEATH_DROP_MARKER);
            });
            // Decreasing item's age to increase it's despawn time.
            final CraftItem item = (CraftItem) event.getEntity();
            item.getHandle().age -= PluginConfig.REASONABLE_ITEM_DESPAWN_SETTINGS_ADDITIONAL_TICKS;
        }
    }

}
