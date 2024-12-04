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
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Skull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(Extensions.class)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class SkullDataRecoveryHandler implements Module, Listener {

    @Getter(AccessLevel.PUBLIC)
    public @NotNull Tweaks plugin;

    private static final NamespacedKey SKULL_DATA_KEY = new NamespacedKey("tweaks", "skull_data");

    @Override
    public void load() {
        if (PluginConfig.ENABLED_MODULES_SKULL_DATA_RECOVERY == true)
            // Registering events.
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void unload() {
        // Unregistering events.
        HandlerList.unregisterAll(this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSkullPlace(final @NotNull BlockPlaceEvent event) {
        if (event.canBuild() == true && event.getBlock().getState() instanceof Skull state) {
            // Cloning the item player is currently holding in their hand.
            final ItemStack item = event.getItemInHand().clone();
            // Setting stack size of the cloned item to 1.
            item.setAmount(1);
            // Serializing to bytes.
            final byte[] bytes = item.serializeAsBytes();
            // Setting the data.
            state.getPersistentDataContainer().set(SKULL_DATA_KEY, PersistentDataType.BYTE_ARRAY, bytes);
            // Updating the state.
            state.update(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSkullBreak(final @NotNull BlockBreakEvent event) {
        if (event.getBlock().getState() instanceof Skull state && event.isDropItems() == true && event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            if (state.getPersistentDataContainer().has(SKULL_DATA_KEY) == true) {
                final byte[] bytes = state.getPersistentDataContainer().get(SKULL_DATA_KEY, PersistentDataType.BYTE_ARRAY);
                // Removing drops.
                event.setExpToDrop(0);
                event.setDropItems(false);
                // Deserializing the item.
                final ItemStack item = ItemStack.deserializeBytes(bytes);
                // Dropping the item.
                event.getBlock().getWorld().dropItem(event.getBlock().getLocation().add(0.5, 0.5, 0.5), item);
            }
        }
    }

}