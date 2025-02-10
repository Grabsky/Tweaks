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
package cloud.grabsky.tweaks.enchantments;

import cloud.grabsky.tweaks.Module;
import cloud.grabsky.tweaks.Tweaks;
import cloud.grabsky.tweaks.configuration.PluginConfig;
import cloud.grabsky.tweaks.utils.Extensions;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.security.SecureRandom;

import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(Extensions.class)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class BaitEnchantment implements Module, Listener {

    @Getter(AccessLevel.PUBLIC)
    public @NotNull Tweaks plugin;

    @Override
    public void load() {
        if (PluginConfig.ENABLED_MODULES_BAIT_ENCHANTMENT == true)
            // Registering events.
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void unload() {
        // Unregistering events.
        HandlerList.unregisterAll(this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFishingCaughtEntity(final @NotNull PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            final EquipmentSlot slot = event.getHand();
            // Checking if item associated with this event is enchanted with 'firedot:bait' enchantment.
            if (event.getPlayer().getInventory().getItem(slot).isEnchantedWith("firedot:bait") == true) {
                // Enchantment has 15% chance to activate.
                if (new SecureRandom().nextInt(100) < 15) {
                    final Entity entity = event.getCaught();
                    // Checking whether caught entity is an item. (Should always be the case)
                    if (entity instanceof Item item)
                        // Updating the amount of item player will receive. Stack is increased by a random number between 1 and 2.
                        item.getItemStack().setAmount(Math.min(item.getItemStack().getAmount() + new SecureRandom().nextInt(2) + 1, item.getItemStack().getMaxStackSize()));
                }
            }
        }
    }

}
