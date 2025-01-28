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
import com.destroystokyo.paper.loottable.LootableInventory;
import io.papermc.paper.math.BlockPosition;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class BreakingMultipliersHandler implements Module, Listener {

    private final @NotNull Tweaks plugin;

    private final Map<UUID, BlockPosition> lastTargetBlock = new HashMap<>();

    private @Nullable BukkitTask task = null;

    private static final NamespacedKey BREAKING_MULTIPLIER_KEY = new NamespacedKey("tweaks", "breaking_multiplier");

    @Override
    public void load() {
        // Clearing handlers list.
        HandlerList.unregisterAll(this);
        // Cancelling the task if exist.
        if (this.task != null) {
            task.cancel();
            lastTargetBlock.clear();
        }
        // Returning in case enhanced compass is disabled.
        if (PluginConfig.ENABLED_MODULES_BREAKING_MULTIPLIERS == false)
            return;
        // Registering event handlers.
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        // Scheduling the task.
        this.task = plugin.getBedrockScheduler().repeat(1L, 1L, Long.MAX_VALUE, (_) -> {
            for (final Player player : Bukkit.getOnlinePlayers()) {
                // Getting the interaction range of the player.
                final int range = (int) Math.ceil(player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE).getValue());
                // Getting the block player is looking at.
                final @Nullable Block block = player.getTargetBlockExact(range, FluidCollisionMode.NEVER);
                // Removing the attribute and skipping further instructions if player is not looking at any block.
                if (block == null) {
                    player.getAttribute(Attribute.BLOCK_BREAK_SPEED).removeModifier(BREAKING_MULTIPLIER_KEY);
                    continue;
                }
                // Continuing only if player is looking at different block.
                if (block.getLocation().toBlock().equals(lastTargetBlock.get(player.getUniqueId())) == false) {
                    lastTargetBlock.put(player.getUniqueId(), block.getLocation().toBlock());
                    // Removing the existing modifier.
                    player.getAttribute(Attribute.BLOCK_BREAK_SPEED).removeModifier(BREAKING_MULTIPLIER_KEY);
                    // Getting the multiplier for this block. Defaults to 1.
                    final float multiplier = PluginConfig.BREAKING_MULTIPLIER_SETTINGS_BLOCKS.getOrDefault(block.getType(), 1.0F);
                    // Skipping containers without loot-tables. (Configurable)
                    if (PluginConfig.BREAKING_MULTIPLIER_SETTINGS_CHECK_LOOT_TABLE == true && block.getState() instanceof LootableInventory container && container.hasLootTable() == false)
                        continue;
                    // Checking if the multiplier is not default and if player don't have the modifier already.
                    if (multiplier != 1.0F && player.getAttribute(Attribute.BLOCK_BREAK_SPEED).getModifier(BREAKING_MULTIPLIER_KEY) == null) {
                        player.getAttribute(Attribute.BLOCK_BREAK_SPEED).addTransientModifier(
                                new AttributeModifier(BREAKING_MULTIPLIER_KEY, multiplier, AttributeModifier.Operation.MULTIPLY_SCALAR_1)
                        );
                    }
                }
            }
            // ...
            return true;
        });
    }

    @Override
    public void unload() { /* HANDLED INSIDE LOAD */ }

}
