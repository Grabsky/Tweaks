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
package cloud.grabsky.tweaks.items;

import cloud.grabsky.tweaks.Module;
import cloud.grabsky.tweaks.Tweaks;
import cloud.grabsky.tweaks.configuration.PluginConfig;
import cloud.grabsky.tweaks.utils.Extensions;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(Extensions.class)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class EnderiteItem implements Module, Listener {

    @Getter(AccessLevel.PUBLIC)
    public @NotNull Tweaks plugin;

    @Override
    public void load() {
        if (PluginConfig.ENABLED_MODULES_ENDERITE == true)
            // Registering events.
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void unload() {
        // Unregistering events.
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onPrepareAnvilResult(final @NotNull PrepareAnvilEvent event) {
        // Getting base and addition items.
        final @Nullable ItemStack base = event.getInventory().getFirstItem();
        final @Nullable ItemStack addition = event.getInventory().getSecondItem();
        // Continuing only when base and addition items are in the GUI. This also allows for renaming.
        if (base != null && addition != null) {
            final @Nullable NamespacedKey baseModel = base.getItemMeta().getItemModel();
            final @Nullable NamespacedKey additionModel = addition.getItemMeta().getItemModel();
            // Continuing only for items we need to handle.
            if (baseModel != null && PluginConfig.ENDERITE_SETTINGS_BASE_MODELS.contains(baseModel) == true) {
                // Cancelling if addition model is not set and configured list is not empty.
                if (additionModel == null) {
                    event.setResult(null);
                    return;
                }
                // Cancelling repairing of base item with invalid addition item. Base item can also be repaired with the same tool type and quality.
                if (PluginConfig.ENDERITE_SETTINGS_ADDITION_MODELS.contains(additionModel) == false && additionModel.equals(baseModel) == false) {
                    event.setResult(null);
                    return;
                }
                // Otherwise, allowing the anvil interaction...
            }
        }
    }

    @EventHandler
    public void onPrepareSmithingResult(final @NotNull PrepareSmithingEvent event) {
        // Skipping calls that do not produce any result.
        if (event.getResult() == null)
            return;
        // Getting items.
        final @Nullable ItemStack equipment = event.getInventory().getInputEquipment();
        // Continuing only if equipment is not null.
        if (equipment != null) {
            // Getting equipment model.
            final @Nullable NamespacedKey equipmentModel = equipment.getItemMeta().getItemModel();
            // Continuing only when equipment's model is also specified in the configured list.
            if (equipmentModel != null && PluginConfig.ENDERITE_SETTINGS_BASE_MODELS.contains(equipmentModel) == true) {
                final @Nullable ItemStack template = event.getInventory().getInputTemplate();
                final @Nullable ItemStack mineral = event.getInventory().getInputMineral();
                // Setting result to null as to prevent upgrading enderite equipment.
                if (template != null && template.getItemMeta().getItemModel() != null) {
                    event.setResult(null);
                    return;
                }
                // Setting result to null as to prevent upgrading enderite equipment.
                if (mineral != null && mineral.getItemMeta().getItemModel() != null)
                    event.setResult(null);
            }
        }
    }

    @EventHandler
    public void onFurnaceStartSmelt(final FurnaceSmeltEvent event) {
        if (event.getRecipe() != null) {
            final ItemStack smelting = event.getSource();
            // Returning if smelting item is null or do not have an 'item_model' specified.
            if (smelting.getItemMeta().hasItemModel() == false)
                return;
            // Getting the 'item_model' value of the smelting item.
            final NamespacedKey smeltingModel = smelting.getItemMeta().getItemModel();
            // Overriding the result if applicable.
            if (PluginConfig.ENDERITE_SETTINGS_BASE_MODELS.contains(smeltingModel) == true)
                event.setResult(PluginConfig.ENDERITE_SETTINGS_FURNACE_SMELTING_RESULT);
        }
    }

}