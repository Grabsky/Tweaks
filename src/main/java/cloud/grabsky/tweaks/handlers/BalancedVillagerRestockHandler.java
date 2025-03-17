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
import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.entity.VillagerReplenishTradeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import java.util.Set;

import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class BalancedVillagerRestockHandler implements Module, Listener {

    private final @NotNull Tweaks plugin;

    /* MODULE LIFE-CYCLE */

    @Override
    public void load() {
        if (PluginConfig.ENABLED_MODULES_BALANCED_VILLAGER_RESTOCK == true)
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void unload() {
        HandlerList.unregisterAll(this);
    }

    /* VILLAGER EVENT LISTENERS */

    @EventHandler(ignoreCancelled = true)
    public void onVillagerTradeReplenish(final @NotNull VillagerReplenishTradeEvent event) {
        handleStock(event.getRecipe());
    }

    @EventHandler(ignoreCancelled = true)
    public void onVillagerAcquireTrade(final @NotNull VillagerAcquireTradeEvent event) {
        handleStock(event.getRecipe());
    }

    /* HELPER METHODS */

    @SuppressWarnings("UnstableApiUsage")
    private static void handleStock(final @NotNull MerchantRecipe recipe) {
        final ItemStack result = recipe.getResult();
        // Diamond Hoe Restock: 3 ➞ 1
        if (result.getType() == Material.DIAMOND_HOE) {
            recipe.setUses(0);
            recipe.setMaxUses(1);
        }
        // Saddle Restock: 12 ➞ 1
        else if (result.getType() == Material.SADDLE) {
            recipe.setUses(0);
            recipe.setMaxUses(1);
        }
        // Enchanted Book Restock: ??? ➞ ???
        else if (result.getType() == Material.ENCHANTED_BOOK && result.hasData(DataComponentTypes.STORED_ENCHANTMENTS) == true) {
            // Getting the list of enchantments from the enchanted book.
            final Set<Enchantment> enchantments = result.getData(DataComponentTypes.STORED_ENCHANTMENTS).enchantments().keySet();
            // Setting the initial value for the max uses count of the recipe. (-1 means it should not be handled)
            int maxUses = -1;
            // Handling specific enchantments and setting their max uses count.
            if (enchantments.contains(Enchantment.MENDING) == true)
                maxUses = 1;
            else if (enchantments.contains(Enchantment.SILK_TOUCH) == true)
                maxUses = 1;
            else if (enchantments.contains(Enchantment.INFINITY) == true)
                maxUses = 1;
            else if (enchantments.contains(Enchantment.FROST_WALKER) == true)
                maxUses = 1;
            else if (recipe.getMaxUses() > 4)
                maxUses = 4;
            // Adjusting the final uses and max uses counts of the recipe.
            if (maxUses > 0) {
                recipe.setUses(0);
                recipe.setMaxUses(maxUses);
            }
        }
    }

}
