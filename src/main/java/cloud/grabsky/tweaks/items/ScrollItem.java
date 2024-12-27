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

import cloud.grabsky.azure.api.AzureProvider;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.bedrock.util.Interval;
import cloud.grabsky.tweaks.Module;
import cloud.grabsky.tweaks.Tweaks;
import cloud.grabsky.tweaks.configuration.PluginConfig;
import cloud.grabsky.tweaks.utils.Extensions;
import cloud.grabsky.tweaks.utils.TriConsumer;
import cloud.grabsky.tweaks.utils.Utilities;
import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(Extensions.class)
@SuppressWarnings("UnstableApiUsage")
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class ScrollItem implements Module, Listener {

    @Getter(AccessLevel.PUBLIC)
    public @NotNull Tweaks plugin;

    private final Map<String, Long> lastScrollUse = new HashMap<>();

    // Scroll type to identify regular teleportation scrolls (spawn) from recovery scrolls (death location).
    private static final NamespacedKey SCROLL_TYPE = new NamespacedKey("firedot", "scroll_type");

    // Players with this permission can teleport without paying any configured teleportation costs.
    private static final String BYPASS_TELEPORT_COST = "tweaks.plugin.bypass_teleport_cost";

    // Stored as a separate runnable as this is also called on demand.
    private final TriConsumer<Player, Boolean, Boolean> taskConsumer = (it, isInitial, isForced) -> it.getInventory().forEach(item -> {
        if (item != null && item.getPersistentDataContainer().has(SCROLL_TYPE) == true && item.hasData(DataComponentTypes.USE_COOLDOWN) == true) {
            // Removing the cooldown if player has bypass permission.
            if (it.hasPermission(Utilities.BYPASS_TELEPORT_COOLDOWN) == true) {
                it.setCooldown(item, 0);
                return;
            }
            // Making sure to not re-apply cooldown on each iteration.
            if (it.hasCooldown(item) == false || isForced == true) {
                // Setting the initial cooldown, if called as such.
                if (isInitial == true) {
                    it.setCooldown(item, 20 * PluginConfig.TELEPORTATION_SETTINGS_DELAY);
                    return;
                }
                // Getting the cooldown on this scroll.
                final String type = item.getPersistentDataContainer().get(SCROLL_TYPE, PersistentDataType.STRING);
                // Getting the cooldown on this scroll.
                final int cooldownSeconds = (int) item.getData(DataComponentTypes.USE_COOLDOWN).seconds();
                // Calculating the cooldown left to the next teleport.
                final Interval cooldownLeft = Interval.of(lastScrollUse.getOrDefault(it.getUniqueId() + "/" + type, (long) 0) + (cooldownSeconds * 1000L), Interval.Unit.MILLISECONDS).remove(System.currentTimeMillis(), Interval.Unit.MILLISECONDS);
                // Setting the cooldown.
                if (cooldownLeft.as(Interval.Unit.MILLISECONDS) >= 0)
                    it.setCooldown(item, (int) cooldownLeft.as(Interval.Unit.TICKS));
            }
        }
    });

    // Repeating task which updates cooldown of scrolls in player inventories.
    private @Nullable BukkitTask task = null;

    @Override
    public void load() {
        // Clearing handlers list.
        HandlerList.unregisterAll(this);
        // Cancelling current task in case it already exists.
        if (this.task != null)
            task.cancel();
        // Starting the module, if enabled in config.
        if (PluginConfig.ENABLED_MODULES_SCROLLS == true) {
            // Registering events. Currently only the PlayerInventoryEvent is being listened to.
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            // Scheduling repeating task which updates cooldown of scrolls in player inventories.
            this.task = plugin.getBedrockScheduler().repeat(0L, 20L, Long.MAX_VALUE, (_) -> {
                // Running the task logic for each online player.
                plugin.getServer().getOnlinePlayers().forEach(it -> taskConsumer.accept(it, false, false));
                // Returning true, as this task should run indefinitely.
                return true;
            });
        }
    }

    @Override
    public void unload() { /* HANDLED INSIDE LOAD */ }

    @EventHandler
    public void onRightClick(final @NotNull PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.HAND && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) && event.getItem() != null) {
            // Getting player associated with the event.
            final Player player = event.getPlayer();
            // Getting item associated with the event.
            final ItemStack item = event.getItem();
            // Checking whether we're dealing with scroll items or not.
            if (item.hasData(DataComponentTypes.USE_COOLDOWN) == true && item.getPersistentDataContainer().has(SCROLL_TYPE, PersistentDataType.STRING) == true) {
                // Cancelling use of held item and interacted block.
                event.setUseItemInHand(Event.Result.DENY);
                event.setUseInteractedBlock(Event.Result.DENY);
                // Getting the scroll type.
                final String type = item.getPersistentDataContainer().get(SCROLL_TYPE, PersistentDataType.STRING);
                // Getting the cooldown on this scroll.
                final int cooldownSeconds = (int) item.getData(DataComponentTypes.USE_COOLDOWN).seconds();
                // Calculating the cooldown left to the next teleport.
                final Interval cooldownLeft = Interval.of(lastScrollUse.getOrDefault(player.getUniqueId() + "/" + type, (long) 0) + (cooldownSeconds * 1000L), Interval.Unit.MILLISECONDS).remove(System.currentTimeMillis(), Interval.Unit.MILLISECONDS);
                // Cancelling if player is currently on cooldown.
                if (player.hasPermission(Utilities.BYPASS_TELEPORT_COOLDOWN) == true || cooldownLeft.as(Interval.Unit.MILLISECONDS) <= 0) {
                    // Getting the destination location.
                    final Location destination = (type.equals("scroll_of_recovery") == true && player.getLastDeathLocation() != null)
                            ? player.getLastDeathLocation()
                            : AzureProvider.getAPI().getWorldManager().getSpawnPoint(AzureProvider.getAPI().getWorldManager().getPrimaryWorld());
                    // Check if player is not on (initial) cooldown.
                    if (player.hasCooldown(item) == false) {
                        // Updating (initial; per-use) visual cooldown of all scroll items,
                        taskConsumer.accept(player, true, false);
                        // Teleporting...
                        Utilities.teleport(player, destination, PluginConfig.TELEPORTATION_SETTINGS_DELAY,
                                // Cancelling teleportation if player no longer have scroll in their inventory.
                                (_, _) -> (player.hasPermission(BYPASS_TELEPORT_COST) == false && player.getInventory().contains(item) == false),
                                // Running post-teleportation tasks.
                                (isSuccess, previous, current) -> {
                                    if (isSuccess == true) {
                                        // Deducting one item from the player.
                                        if (player.hasPermission(BYPASS_TELEPORT_COST) == false && player.getInventory().contains(item) == true)
                                            item.setAmount(item.getAmount() - 1);
                                        // Setting cooldowns. Players with bypass do not have any cooldown applied.
                                        if (player.hasPermission(BYPASS_TELEPORT_COST) == false) {
                                            // Setting the logical cooldown. Required because "visual" cooldown is reset after re-logging.
                                            lastScrollUse.put(player.getUniqueId() + "/scroll_of_return", System.currentTimeMillis());
                                            lastScrollUse.put(player.getUniqueId() + "/scroll_of_recovery", System.currentTimeMillis());
                                            // Updating visual cooldown of all scroll items,
                                            taskConsumer.accept(player, false, true);
                                        }
                                        // Displaying visual effects of non-vanished players.
                                        if (AzureProvider.getAPI().getUserCache().getUser(player).isVanished() == false) {
                                            // Spawning particles.
                                            if (PluginConfig.TELEPORTATION_SETTINGS_PARTICLES != null) {
                                                PluginConfig.TELEPORTATION_SETTINGS_PARTICLES.forEach(it -> {
                                                    destination.getWorld().spawnParticle(it.getParticle(), player.getLocation().add(0, (player.getHeight() / 2), 0), it.getAmount(), it.getOffsetX(), it.getOffsetY(), it.getOffsetZ(), it.getSpeed());
                                                });
                                            }
                                            // Playing sounds.
                                            if (PluginConfig.TELEPORTATION_SETTINGS_SOUNDS_OUT != null)
                                                previous.getWorld().playSound(PluginConfig.TELEPORTATION_SETTINGS_SOUNDS_OUT, previous.x(), previous.y(), previous.z());
                                            if (PluginConfig.TELEPORTATION_SETTINGS_SOUNDS_IN != null)
                                                current.getWorld().playSound(PluginConfig.TELEPORTATION_SETTINGS_SOUNDS_IN, current.x(), current.y(), current.z());
                                        }
                                    }
                                }
                        );
                    }
                    // Returning...
                    return;
                }
                // Sending failure message to the player. Player is currently on cooldown.
                Message.of(PluginConfig.TELEPORTATION_SETTINGS_LANG_TELEPORT_FAILURE_ON_COOLDOWN)
                        .placeholder("cooldown_left", cooldownLeft.toString())
                        .sendActionBar(player);
            }
        }
    }

}