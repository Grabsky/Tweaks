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
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(Extensions.class)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class BalancedKeepInventoryHandler implements Module, Listener {

    @Getter(AccessLevel.PUBLIC)
    public @NotNull Tweaks plugin;

    private static final List<Integer> HOTBAR_SLOTS = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8);

    @Override
    public void load() {
        if (PluginConfig.ENABLED_MODULES_BALANCED_KEEP_INVENTORY == true)
            // Registering events.
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void unload() {
        // Unregistering events.
        HandlerList.unregisterAll(this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(final @NotNull PlayerDeathEvent event) {
        if (event.getPlayer().isInvulnerable() == false && event.getPlayer().getGameMode().isInvulnerable() == false) {
            final PlayerInventory inventory = event.getPlayer().getInventory();
            // Keeping hotbar and equipped armor throughout deaths.
            Stream.concat(
                    Stream.of(inventory.getArmorContents()),
                    HOTBAR_SLOTS.stream().map(inventory::getItem)
            ).filter(item -> item != null && item.isEmpty() == false && item.isEnchantedWith("minecraft:vanishing_curse") == false).forEach(item -> {
                event.getItemsToKeep().add(item);
                event.getDrops().remove(item);
            });
            // Keeping off-hand slot throughout deaths.
            if (inventory.getItemInOffHand().isEmpty() == false && inventory.getItemInOffHand().isEnchantedWith("minecraft:vanishing_curse") == false) {
                event.getItemsToKeep().add(inventory.getItemInOffHand());
                event.getDrops().remove(inventory.getItemInOffHand());
            }
        }
    }

}
