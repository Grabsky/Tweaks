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

import cloud.grabsky.bedrock.components.ComponentBuilder;
import cloud.grabsky.bedrock.helpers.Conditions;
import cloud.grabsky.tweaks.Module;
import cloud.grabsky.tweaks.Tweaks;
import cloud.grabsky.tweaks.configuration.PluginConfig;
import cloud.grabsky.tweaks.utils.Extensions;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Crafter;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
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

import java.util.Collections;
import java.util.List;

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
                            // Baskets should have maximum stack size of 1.
                            meta.setMaxStackSize(1);
                            // Applying entity data to the PDC.
                            meta.getPersistentDataContainer().set(DATA_KEY, PersistentDataType.BYTE_ARRAY, data);
                            // Applying enchantment glint if specified.
                            if (PluginConfig.BASKET_SETTINGS_APPLY_ENCHANTMENT_GLINT)
                                meta.setEnchantmentGlintOverride(true);
                            // Applying additional lore if specified.
                            if (PluginConfig.BASKET_SETTINGS_APPLY_ADDITIONAL_LORE) {
                                final @Nullable List<String> additionalLore = getAdditionalLore(entity);
                                // If available, applying additional information to the item lore.
                                if (additionalLore != null && additionalLore.isEmpty() == false)
                                    meta.lore(additionalLore.stream().map(str -> ComponentBuilder.EMPTY_NO_ITALIC.append(MiniMessage.miniMessage().deserialize(str))).toList());
                            }
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
                        // Applying 500ms cooldown to prevent accidental use.
                        event.getPlayer().setCooldown(item, 10);
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(final @NotNull PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.HAND) {
            if (event.getItem() != null && event.getItem().getPersistentDataContainer().has(DATA_KEY, PersistentDataType.BYTE_ARRAY) == true) {
                // Returning if player clicked on an interactable block.
                if (event.getClickedBlock() != null && event.getClickedBlock().isInteractable() == true && event.getPlayer().isSneaking() == false)
                    return;
                // Getting serialized data of entity stored by this item.
                final byte[] data = event.getItem().getPersistentDataContainer().getOrDefault(DATA_KEY, PersistentDataType.BYTE_ARRAY, new byte[0]);
                // Continuing for non-existent / empty values.
                if (data.length == 0)
                    return;
                // Cancelling the event.
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


    /* HELPER METHODS */

    // NOTE: 1.21.5 brings a lot of new mob variants that need special care.
    private static @Nullable List<String> getAdditionalLore(final @NotNull Mob mob) {
        final NamespacedKey entity = mob.getType().getKey();
        final @Nullable List<String> additionalLoreFormat = PluginConfig.BASKET_SETTINGS_ADDITIONAL_LORE_FORMAT.getOrDefault(entity, Collections.emptyList());
        // Returning if no additional lore format was specified for this entity.
        if (additionalLoreFormat == null || additionalLoreFormat.isEmpty() == true)
            return null;
        // Replacing placeholders with actual values. Switch must be used because variations are currently not standardized.
        return switch (mob) {
            // AXOLOTL
            case Axolotl axolotl -> additionalLoreFormat.stream()
                    .map(format -> format.replace("<variant>", "<lang:basket.axolotl_variant." + axolotl.getVariant().name().toLowerCase() + ">"))
                    .toList();
            // HORSE
            case Horse horse -> additionalLoreFormat.stream()
                    .map(format -> format.replace("<style>", "<lang:basket.horse_style." + horse.getStyle().name().toLowerCase() + ">"))
                    .map(format -> format.replace("<color>", "<lang:basket.horse_color." + horse.getColor().name().toLowerCase() + ">"))
                    .toList();
            // LLAMA
            case Llama llama -> additionalLoreFormat.stream()
                    .map(format -> format.replace("<color>", "<lang:basket.llama_color." + llama.getColor().name().toLowerCase() + ">"))
                    .toList();
            // PARROT
            case Parrot parrot -> additionalLoreFormat.stream()
                    .map(format -> format.replace("<variant>", "<lang:basket.parrot_variant." + parrot.getVariant().name().toLowerCase() + ">"))
                    .toList();
            // RABBIT
            case Rabbit rabbit -> additionalLoreFormat.stream()
                    .map(format -> format.replace("<type>", "<lang:basket.rabbit_type." + rabbit.getRabbitType().name().toLowerCase() + ">"))
                    .toList();
            // VILLAGER
            case Villager villager -> additionalLoreFormat.stream()
                    .map(format -> format.replace("<type>", "<lang:basket.villager_type." + villager.getVillagerType().getKey().getKey() + ">"))
                    .map(format -> format.replace("<profession>", "<lang:basket.villager_profession." + villager.getProfession().getKey().getKey() + ">"))
                    .toList();
            // CAT
            case Cat cat -> additionalLoreFormat.stream()
                    .map(format -> format.replace("<type>", "<lang:basket.cat_type." + cat.getCatType().getKey().getKey() + ">"))
                    .toList();
            // FOX
            case Fox fox -> additionalLoreFormat.stream()
                    .map(format -> format.replace("<type>", "<lang:basket.fox_type." + fox.getFoxType().name().toLowerCase() + ">"))
                    .toList();
            // PANDA
            case Panda panda -> additionalLoreFormat.stream()
                    .map(format -> format.replace("<gene>", "<lang:basket.panda_gene." + panda.getMainGene().name().toLowerCase() + ">"))
                    .toList();
            // FROG
            case Frog frog -> additionalLoreFormat.stream()
                    .map(format -> format.replace("<variant>", "<lang:basket.frog_variant." + frog.getVariant().getKey().getKey() + ">"))
                    .toList();
            // SHEEP
            case Sheep sheep -> additionalLoreFormat.stream()
                    .map(format -> format.replace("<color>", "<lang:basket.sheep_color." + Conditions.requirePresent(sheep.getColor(), DyeColor.WHITE).name().toLowerCase() + ">"))
                    .toList();
            // WOLF
            case Wolf wolf -> additionalLoreFormat.stream()
                    .map(format -> format.replace("<variant>", "<lang:basket.wolf_variant." + wolf.getVariant().getKey().getKey() + ">"))
                    .toList();
            // NO VARIANTS
            default -> null;
        };
    }
}