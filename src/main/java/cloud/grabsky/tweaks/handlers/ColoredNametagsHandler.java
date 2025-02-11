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
import com.destroystokyo.paper.MaterialTags;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(Extensions.class)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class ColoredNametagsHandler implements Module, Listener {

    @Getter(AccessLevel.PUBLIC)
    public @NotNull Tweaks plugin;

    private static final NamespacedKey COLORED_NAMETAG_RECIPE = new NamespacedKey("tweaks", "colored_nametag_recipe");

    private static final ShapelessRecipe RECIPE = new ShapelessRecipe(COLORED_NAMETAG_RECIPE, new ItemStack(Material.NAME_TAG));

    static {
        RECIPE.addIngredient(Material.NAME_TAG);
        RECIPE.addIngredient(new RecipeChoice.MaterialChoice(MaterialTags.DYES));
    }

    @Override
    public void load() {
        if (PluginConfig.ENABLED_MODULES_COLORED_NAMETAGS == true) {
            // Registering events...
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            // Registering recipes...
            plugin.getServer().addRecipe(RECIPE, true);
        }
    }

    @Override
    public void unload() {
        // Unregistering events...
        HandlerList.unregisterAll(this);
        // Removing recipes...
        plugin.getServer().removeRecipe(COLORED_NAMETAG_RECIPE, true);
    }

    @SuppressWarnings("UnstableApiUsage")
    @EventHandler(ignoreCancelled = true)
    public void onPrepareItemCraft(final @NotNull PrepareItemCraftEvent event) {
        if (event.getRecipe() instanceof CraftingRecipe recipe && recipe.getKey().equals(COLORED_NAMETAG_RECIPE) == true) {
            if (event.getInventory().getResult() != null) {
                // Getting the result item from crafting view.
                final ItemStack result = event.getInventory().getResult();
                // Getting the matrix items array.
                final ItemStack[] matrix = event.getInventory().getMatrix();
                // Finding items in the matrix array.
                @Nullable ItemStack nametag = Stream.of(matrix).filter(item -> item != null && item.getType() == Material.NAME_TAG).findFirst().orElse(null);
                @Nullable ItemStack dye = Stream.of(matrix).filter(item -> item != null && MaterialTags.DYES.isTagged(item) == true).findFirst().orElse(null);
                // Iterating over list of crafting ingredients in search for dye.
                if (nametag != null && dye != null) {
                    // Checking if nametag item has custom_name set, if not, removing the result.
                    if (nametag.hasData(DataComponentTypes.CUSTOM_NAME) == false) {
                        event.getInventory().setResult(null);
                        return;
                    }
                    // Getting the mapped TextColor from the configuration.
                    final @Nullable TextColor color = TextColor.fromHexString(PluginConfig.COLORED_NAMETAGS_SETTINGS_COLORS.getOrDefault(dye.getType(), NamedTextColor.WHITE.asHexString()));
                    // Returning if color turned out to be null.
                    if (color == null)
                        return;
                    // Updating color on the custom_name component.
                    result.setData(DataComponentTypes.CUSTOM_NAME, nametag.getData(DataComponentTypes.CUSTOM_NAME).color(color));
                    // Updating the result item.
                    event.getInventory().setResult(result);
                }
            }
        }
    }

}
