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
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerReplenishTradeEvent;

import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class BalancedVillagerRestockHandler implements Module, Listener {

    private final @NotNull Tweaks plugin;

    @Override
    public void load() {
        // Unregistering events.
        HandlerList.unregisterAll(this);
        // Enabling the module.
        if (PluginConfig.ENABLED_MODULES_BALANCED_VILLAGER_RESTOCK == true)
            // Registering events.
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void unload() { /* HANDLED INSIDE LOAD */ }

    @EventHandler(ignoreCancelled = true)
    public void onVillagerTradeReplenish(final @NotNull VillagerReplenishTradeEvent event) {
        if (event.getRecipe().getResult().getType() == Material.DIAMOND_HOE) {
            // Adjusting the uses and max uses of the recipe.
            event.getRecipe().setUses(0);
            event.getRecipe().setMaxUses(1);
        }
    }

}
