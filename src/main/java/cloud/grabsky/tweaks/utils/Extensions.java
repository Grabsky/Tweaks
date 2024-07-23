/*
 * MIT License
 *
 * Copyright (c) 2024 Grabsky <44530932+Grabsky@users.noreply.github.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * HORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
