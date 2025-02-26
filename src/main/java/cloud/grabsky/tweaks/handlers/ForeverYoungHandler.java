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
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Breedable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(Extensions.class)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class ForeverYoungHandler implements Module, Listener {

    @Getter(AccessLevel.PUBLIC)
    public @NotNull Tweaks plugin;

    @Override
    public void load() {
        if (PluginConfig.ENABLED_MODULES_FOREVER_YOUNG == true)
            // Registering events.
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void unload() {
        // Unregistering events.
        HandlerList.unregisterAll(this);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityInteract(final @NotNull PlayerInteractEntityEvent event) {
        if (event.getHand() == EquipmentSlot.HAND && event.getRightClicked() instanceof Breedable breedable && breedable.isAdult() == false) {
            final ItemStack item = event.getPlayer().getInventory().getItem(EquipmentSlot.HAND);
            if (item.isSimilar(PluginConfig.FOREVER_YOUNG_SETTINGS_AGE_LOCK_ITEM) == true && breedable.getAgeLock() == false) {
                event.setCancelled(true);
                // Swinging player's hand.
                event.getPlayer().swingMainHand();
                // Removing item from the player.
                item.subtract(1);
                // Playing sound.
                event.getRightClicked().getWorld().playSound(event.getRightClicked().getLocation(), Sound.ENTITY_FOX_EAT, SoundCategory.NEUTRAL, 0.5F, 1.0F);
                // Locking the age.
                breedable.setAgeLock(true);
            } else if (item.isSimilar(PluginConfig.FOREVER_YOUNG_SETTINGS_AGE_UNLOCK_ITEM) == true && breedable.getAgeLock() == true) {
                event.setCancelled(true);
                // Swinging player's hand.
                event.getPlayer().swingMainHand();
                // Removing item from the player.
                item.subtract(1);
                // Playing sound.
                event.getRightClicked().getWorld().playSound(event.getRightClicked().getLocation(), Sound.ENTITY_FOX_EAT, SoundCategory.NEUTRAL, 0.5F, 1.0F);
                // Unlocking the age.
                breedable.setAgeLock(false);
            }
        }
    }

}