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
package cloud.grabsky.tweaks.utils;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import lombok.experimental.ExtensionMethod;

@ExtensionMethod(Extensions.class)
public final class Extensions {

    private static final Registry<Enchantment> ENCHANTMENT_REGISTRY = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);

    /**
     * Returns {@code true} if this item is enchanted with specified enchantment.
     */
    public static boolean isEnchantedWith(final @NotNull ItemStack item, final @NotNull String enchantment) {
        final Key key = Key.key(enchantment);
        final Enchantment ench = ENCHANTMENT_REGISTRY.get(key);
        // ...
        return ench != null && item.getEnchantmentLevel(ench) > 0;
    }

    /**
     * Returns {@code true} if this item is enchanted with specified enchantment.
     */
    public static int getEnchantmentLevelOf(final @NotNull ItemStack item, final @NotNull String enchantment) {
        final Key key = Key.key(enchantment);
        final Enchantment ench = ENCHANTMENT_REGISTRY.get(key);
        // ...
        return (ench != null) ? item.getEnchantmentLevel(ench) : 0;
    }

    /**
     * Returns {@code true} if inventory have space for this item, {@code false} otherwise. Items are compared by type and amount.
     */
    public static boolean hasSpace(final @NotNull Inventory inventory, final @NotNull ItemStack item) {
        if (inventory.firstEmpty() == -1) {
            final Iterator<ItemStack> iterator = List.of(inventory.getStorageContents()).iterator();
            while (iterator.hasNext() == true) {
                final ItemStack next = iterator.next();
                if (next.getType() == item.getType() && next.getAmount() + item.getAmount() <= next.getMaxStackSize())
                    return true;
            }
            return false;
        }
        return true;
    }

}
