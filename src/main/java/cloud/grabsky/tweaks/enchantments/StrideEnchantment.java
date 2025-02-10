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
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(Extensions.class)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class StrideEnchantment implements Module, Listener {

    @Getter(AccessLevel.PUBLIC)
    public @NotNull Tweaks plugin;

    private static final NamespacedKey FIREDOT_STRIDE = new NamespacedKey("firedot", "stride");

    @Override
    public void load() {
        if (PluginConfig.ENABLED_MODULES_BAIT_ENCHANTMENT == true)
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void unload() {
        HandlerList.unregisterAll(this);
    }

    @SuppressWarnings("UnstableApiUsage")
    @EventHandler(ignoreCancelled = true)
    public void onPlayerSneak(final @NotNull PlayerToggleSneakEvent event) {
        if (event.isSneaking() == false && event.getPlayer().getInventory().getItem(EquipmentSlot.LEGS).isEnchantedWith("firedot:stride") == true) {
            final @Nullable AttributeInstance attr = event.getPlayer().getAttribute(Attribute.STEP_HEIGHT);
            // Applying step height attribute modifier to the player.
            if (attr != null && attr.getModifier(FIREDOT_STRIDE) == null)
                attr.addTransientModifier(new AttributeModifier(FIREDOT_STRIDE, 1.0, Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.LEGS));
        } else {
            final @Nullable AttributeInstance attr = event.getPlayer().getAttribute(Attribute.STEP_HEIGHT);
            // Removing step height attribute modifier from the player.
            if (attr != null && attr.getModifier(FIREDOT_STRIDE) != null)
                attr.removeModifier(FIREDOT_STRIDE);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @EventHandler(ignoreCancelled = true)
    public void onPlayerArmorChange(final @NotNull PlayerArmorChangeEvent event) {
        if (event.getSlotType() == PlayerArmorChangeEvent.SlotType.LEGS) {
            if (event.getPlayer().isSneaking() == false && event.getNewItem().isEnchantedWith("firedot:stride") == true) {
                final @Nullable AttributeInstance attr = event.getPlayer().getAttribute(Attribute.STEP_HEIGHT);
                // Applying step height attribute modifier to the player.
                if (attr != null && attr.getModifier(FIREDOT_STRIDE) == null)
                    attr.addTransientModifier(new AttributeModifier(FIREDOT_STRIDE, 1.0, Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.LEGS));
            } else {
                final @Nullable AttributeInstance attr = event.getPlayer().getAttribute(Attribute.STEP_HEIGHT);
                // Removing step height attribute modifier from the player.
                if (attr != null && attr.getModifier(FIREDOT_STRIDE) != null)
                    attr.removeModifier(FIREDOT_STRIDE);
            }
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(final @NotNull PlayerJoinEvent event) {
        if (event.getPlayer().isSneaking() == false && event.getPlayer().getInventory().getItem(EquipmentSlot.LEGS).isEnchantedWith("firedot:stride") == true) {
            final @Nullable AttributeInstance attr = event.getPlayer().getAttribute(Attribute.STEP_HEIGHT);
            // Applying step height attribute modifier to the player.
            if (attr != null && attr.getModifier(FIREDOT_STRIDE) == null)
                attr.addTransientModifier(new AttributeModifier(FIREDOT_STRIDE, 1.0, Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.LEGS));
        }
    }
}
