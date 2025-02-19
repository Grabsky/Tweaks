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
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
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

    public static void showRichTitle(final @NotNull Audience audience, final @NotNull Component title, final @NotNull Component subtitle, final long fadeInTicks, final long stayTicks, final long fadeOutTicks) {
        audience.showTitle(
                Title.title(title, subtitle, Title.Times.times(
                        Duration.of(fadeInTicks * 50, ChronoUnit.MILLIS),
                        Duration.of(stayTicks * 50, ChronoUnit.MILLIS),
                        Duration.of(fadeOutTicks * 50, ChronoUnit.MILLIS)
                ))
        );
    }

    public static void fadeOutTitle(final @NotNull Audience audience, final long delay, final long fadeOutTicks) {
        audience.sendTitlePart(TitlePart.TIMES, Title.Times.times(Duration.ZERO, Duration.of(delay * 50, ChronoUnit.MILLIS), Duration.of(fadeOutTicks * 50, ChronoUnit.MILLIS)));
    }

    // Returns true if block is interactable with bare hand or non-special item.
    // NOTE: This also block interaction with blocks of coal, iron, gold, diamond, emerald and netherite until Claims API can be used.
    public static boolean isInteractable(final @NotNull Block block) {
        final Material material = block.getType();
        // Returning 'true' if block is door, trapdoor or fence gate of any type.
        if (Tag.DOORS.isTagged(material) == true || Tag.TRAPDOORS.isTagged(material) == true || Tag.FENCE_GATES.isTagged(material) == true)
            return true;
        // Returning 'true' if block is shulker box of any color.
        if (Tag.SHULKER_BOXES.isTagged(material) == true)
            return true;
        // Returning 'true' if block is bed of any color.
        if (Tag.BEDS.isTagged(material) == true)
            return true;
        // Returning 'true' if block is sign of any type.
        if (Tag.ALL_SIGNS.isTagged(material) == true || Tag.ALL_HANGING_SIGNS.isTagged(material) == true)
            return true;
        // Returning 'true' if block is button of any type.
        if (Tag.BUTTONS.isTagged(material) == true)
            return true;
        // Checking the rest of blocks individually, as they may not be tagged.
        return switch (block.getType()) {
            case CHEST, TRAPPED_CHEST, BARREL, ENDER_CHEST,
                 // Bees
                 BEEHIVE, BEE_NEST,
                 // Furnaces
                 FURNACE, BLAST_FURNACE, SMOKER,
                 // Stations
                 ANVIL, CHIPPED_ANVIL, DAMAGED_ANVIL,
                 CRAFTING_TABLE, CRAFTER,
                 CARTOGRAPHY_TABLE,
                 ENCHANTING_TABLE,
                 SMITHING_TABLE,
                 BREWING_STAND,
                 GRINDSTONE,
                 LECTERN,
                 BEACON,
                 LOOM,
                 // Pots
                 DECORATED_POT, FLOWER_POT,
                 // Music
                 JUKEBOX, NOTE_BLOCK,
                 // Redstone
                 DISPENSER, DROPPER, HOPPER,
                 LEVER, REPEATER,
                 // Misc
                 BELL, LODESTONE, RESPAWN_ANCHOR, VAULT, CHISELED_BOOKSHELF,
                 // Operator Blocks
                 COMMAND_BLOCK, CHAIN_COMMAND_BLOCK, REPEATING_COMMAND_BLOCK, STRUCTURE_BLOCK, JIGSAW,
                 // Claim Blocks (Hardcoded; To be replaced with Claims or WorldGuard API in the future)
                 COAL_BLOCK, IRON_BLOCK, GOLD_BLOCK, DIAMOND_BLOCK, EMERALD_BLOCK, NETHERITE_BLOCK -> true;
            // ...
            default -> false;
        };
    }

}
