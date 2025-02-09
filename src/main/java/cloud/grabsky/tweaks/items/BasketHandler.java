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
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Crafter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.CrafterCraftEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.persistence.PersistentDataType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(Extensions.class)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class BasketHandler implements Module, Listener {

    @Getter(AccessLevel.PUBLIC)
    public @NotNull Tweaks plugin;

    private static final NamespacedKey DATA_KEY = new NamespacedKey("tweaks", "entity_data");

    @Override
    public void load() {
        if (PluginConfig.ENABLED_MODULES_BASKET == true)
            // Registering events.
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void unload() {
        // Unregistering events.
        HandlerList.unregisterAll(this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(final @NotNull PlayerInteractEntityEvent event) {
        if (event.getHand() == EquipmentSlot.HAND && event.getRightClicked() instanceof Mob entity && entity.isInsideVehicle() == false && entity.getPassengers().isEmpty() == true) {
            // Checking if player has ANY basket in their hand.
            if (event.getPlayer().getInventory().getItemInMainHand().getPersistentDataContainer().has(DATA_KEY) == true) {
                // Cancelling the event because otherwise player would be able to eg. spawn baby variants of entities, which is not supported.
                event.setCancelled(true);
                // Checking if player has EMPTY basket in their hand.
                if (event.getPlayer().getInventory().getItemInMainHand().getPersistentDataContainer().has(DATA_KEY, PersistentDataType.BOOLEAN) == true) {
                    // Getting key of EntityType associated with this event.
                    final NamespacedKey key = entity.getType().getKey();
                    // Checking if clicked entity is allowed to be picked up.
                    if (PluginConfig.BASKET_SETTINGS_ALLOWED_MOBS.contains(key) == true) {
                        // Serializing entity to bytes.
                        final byte[] data = Bukkit.getUnsafe().serializeEntity(entity);
                        // "Generating" item key based on context. Hopefully this is valid for all entity types.
                        final NamespacedKey itemKey = NamespacedKey.minecraft(entity.getType().getKey().value() + "_spawn_egg");
                        // Getting material from the item key.
                        final ItemStack item = ItemStack.of(Registry.MATERIAL.get(itemKey), 1);
                        // Modifying item.
                        item.editMeta(meta -> {
                            meta.setEnchantmentGlintOverride(true);
                            meta.getPersistentDataContainer().set(DATA_KEY, PersistentDataType.BYTE_ARRAY, data);
                        });
                        // Getting location of the entity. Might be used in a later step.
                        final Location location = entity.getLocation().add(0.0F, entity.getHeight() / 2.0F, 0.0F);
                        // Removing entity from the world.
                        entity.remove();
                        // Spawning particles.
                        if (PluginConfig.BASKET_SETTINGS_PICKUP_PARTICLES != null) {
                            PluginConfig.BASKET_SETTINGS_PICKUP_PARTICLES.forEach(it -> {
                                location.getWorld().spawnParticle(it.getParticle(), location.clone().add(0, entity.getHeight() / 2, 0), it.getAmount(), it.getOffsetX(), it.getOffsetY(), it.getOffsetZ(), it.getSpeed());
                            });
                        }
                        // Playing sounds.
                        if (PluginConfig.BASKET_SETTINGS_PICKUP_SOUNDS != null) {
                            PluginConfig.BASKET_SETTINGS_PICKUP_SOUNDS.forEach(it -> {
                                location.getWorld().playSound(it, location.x(), location.y(), location.z());
                            });
                        }
                        // Removing item from player's inventory.
                        if (event.getPlayer().getGameMode() == GameMode.SURVIVAL)
                            event.getPlayer().getInventory().getItemInMainHand().setAmount(event.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
                        // Adding item to player's inventory, or dropping on the ground if full.
                        if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR)
                            event.getPlayer().getInventory().setItemInMainHand(item);
                        else if (event.getPlayer().getInventory().firstEmpty() != -1)
                            event.getPlayer().getInventory().addItem(item);
                        else location.getWorld().dropItemNaturally(event.getPlayer().getLocation().add(0.0F, event.getPlayer().getHeight() / 2.0F, 0.0F), item);
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(final @NotNull PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.HAND) {
            if (event.getItem() != null && event.getItem().getPersistentDataContainer().has(DATA_KEY, PersistentDataType.BYTE_ARRAY) == true) {
                // Getting serialized data of entity stored by this item.
                final byte[] data = event.getItem().getPersistentDataContainer().getOrDefault(DATA_KEY, PersistentDataType.BYTE_ARRAY, new byte[0]);
                // Continuing for non-existent / empty values.
                if (data.length == 0)
                    return;
                // Cancelling the event, as we're dealing with a valid item.
                event.setCancelled(true);
                // Removing item from player's hand.
                if (event.getPlayer().getGameMode() == GameMode.SURVIVAL)
                    event.getPlayer().getInventory().getItemInMainHand().setAmount(event.getItem().getAmount() - 1);
                // Deserializing entity from bytes. The only thing that should be different is UUID. Otherwise creative players would be unable to duplicate entities.
                final Entity entity = Bukkit.getUnsafe().deserializeEntity(data, event.getPlayer().getWorld(), false);
                // Getting block face of the clicked block.
                final BlockFace blockFace = event.getBlockFace();
                // Calculating spawn location to be more-less on par with vanilla spawn eggs.
                final Location spawnLocation = event.getClickedBlock().getLocation().toCenterLocation().add(
                        (blockFace.getModX() != 0) ? blockFace.getModX() : 0,
                        (blockFace == BlockFace.UP || blockFace == BlockFace.DOWN) ? (blockFace == BlockFace.UP) ? 0.5 : -1.0 : -0.5,
                        (blockFace.getModZ() != 0) ? blockFace.getModZ() : 0
                );
                // Spawning the entity.
                entity.spawnAt(spawnLocation, CreatureSpawnEvent.SpawnReason.SPAWNER_EGG);
                // Spawning particles.
                if (PluginConfig.BASKET_SETTINGS_PLACE_PARTICLES != null) {
                    PluginConfig.BASKET_SETTINGS_PLACE_PARTICLES.forEach(it -> {
                        spawnLocation.getWorld().spawnParticle(it.getParticle(), spawnLocation, it.getAmount(), it.getOffsetX(), it.getOffsetY(), it.getOffsetZ(), it.getSpeed());
                    });
                }
                // Playing sounds.
                if (PluginConfig.BASKET_SETTINGS_PLACE_SOUNDS != null) {
                    PluginConfig.BASKET_SETTINGS_PLACE_SOUNDS.forEach(it -> {
                        spawnLocation.getWorld().playSound(it, spawnLocation.x(), spawnLocation.y(), spawnLocation.z());
                    });
                }
            }
        }
    }

    // BLOCKING BASKETS FROM BEING USED BY DISPENSERS
    @EventHandler(ignoreCancelled = true)
    public void onBlockDispense(final BlockDispenseEvent event) {
        if (event.getItem().hasItemMeta() == true && event.getItem().getItemMeta() instanceof SpawnEggMeta meta)
            if (meta.getPersistentDataContainer().has(DATA_KEY) == true)
                event.setCancelled(true);
    }

    // BLOCKING BASKETS FROM BEING USED AS CRAFTING INGREDIENT (CRAFTING TABLE)
    @EventHandler(ignoreCancelled = true)
    public void onCraftPrepare(final PrepareItemCraftEvent event) {
        // Skipping event calls we don't need to handle.
        if (event.getRecipe() == null)
            return;
        // Cancelling craft result when using crate key as an ingredient.
        for (final @Nullable ItemStack item : event.getInventory().getMatrix())
            if (item != null)
                if (item.getPersistentDataContainer().has(DATA_KEY) == true)
                    event.getInventory().setResult(null);
    }

    // BLOCKING BASKETS FROM BEING USED AS CRAFTING INGREDIENT (CRAFTER)
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCrafterCraft(final CrafterCraftEvent event) {
        final Crafter crafter = (Crafter) event.getBlock().getState();
        for (final ItemStack item : crafter.getInventory()) {
            if (item != null && item.getPersistentDataContainer().has(DATA_KEY) == true) {
                event.setCancelled(true);
                return;
            }
        }
    }

}