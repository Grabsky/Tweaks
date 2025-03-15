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
import com.destroystokyo.paper.MaterialTags;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.SoundCategory;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(Extensions.class)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class GardenerEnchantment implements Module, Listener {

    @Getter(AccessLevel.PUBLIC)
    public @NotNull Tweaks plugin;

    private static final Map<Material, Material> GET_CROP_ITEM = new HashMap<>() {{
        put(Material.WHEAT, Material.WHEAT_SEEDS);
        put(Material.CARROTS, Material.CARROT);
        put(Material.POTATOES, Material.POTATO);
        put(Material.BEETROOTS, Material.BEETROOT_SEEDS);
        put(Material.COCOA, Material.COCOA_BEANS);
        put(Material.NETHER_WART, Material.NETHER_WART);
        put(Material.MELON_STEM, Material.MELON_SEEDS);
        put(Material.PUMPKIN_STEM, Material.PUMPKIN_SEEDS);
        // NOTE: Following blocks are not implemented (yet?) as they need special treatment, like getting the lowest block.
        //  put(Material.SUGAR_CANE, Material.SUGAR_CANE);
        //  put(Material.CACTUS, Material.CACTUS);
        //  put(Material.BAMBOO, Material.BAMBOO);
    }};

    @Override
    public void load() {
        if (PluginConfig.ENABLED_MODULES_GARDENER_ENCHANTMENT == true)
            // Registering events.
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void unload() {
        // Unregistering events.
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockDropItem(final @NotNull BlockDropItemEvent event) {
        final Player player = event.getPlayer();
        // Checking if player is in Survival or Adventure game mode.
        if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
            final ItemStack tool = player.getInventory().getItemInMainHand();
            // Checking if player's tool is enchanted with Magnet enchantment.
            if (MaterialTags.HOES.isTagged(tool) == true && tool.isEnchantedWith("firedot:gardener") == true) {
                final Material blockType = event.getBlockState().getType();
                // Returning if broken block is not a supported crop type.
                if (GET_CROP_ITEM.containsKey(blockType) == false)
                    return;
                // Returning if crop was not planted on a farmland, excluding cocoa and nether warts.
                if (blockType != Material.COCOA && blockType != Material.NETHER_WART && event.getBlockState().getLocation().getBlock().getRelative(0, -1, 0).getType() != Material.FARMLAND)
                    return;
                // Getting the item type that was used to place this block.
                final Material cropItem = GET_CROP_ITEM.get(blockType);
                // ...
                boolean isConsumed = false;
                // Trying to remove seed from drops.
                for (final Item item : event.getItems()) {
                    if (item != null && item.getItemStack().getType() == cropItem) {
                        // Subtracting one seed from the drops.
                        item.getItemStack().subtract(1);
                        // Marking as consumed.
                        isConsumed = true;
                        // Breaking from the loop because only one seed needs to be consumed.
                        break;
                    }
                }
                // In case no seed was dropped, trying to remove seed from the player's inventory.
                if (isConsumed == false) {
                    for (final ItemStack item : player.getInventory()) {
                        if (item != null && item.getType() == cropItem) {
                            // Subtracting one seed from item stack in player's inventory.
                            item.subtract(1);
                            // Marking as consumed.
                            isConsumed = true;
                            // Breaking from the loop because only one seed needs to be consumed.
                            break;
                        }
                    }
                }
                // Continuing with the logic, planting the seed on the ground.
                if (isConsumed == true) {
                    // Damaging player's tool. This should take Unbreaking into account.
                    tool.damage(1, player);
                    // Creating BlockData of the same block that has been destroyed.
                    final BlockData data = event.getBlockState().getType().createBlockData();
                    // Rotating the block, if necessary. (eg. cocoa)
                    if (event.getBlockState().getBlockData() instanceof Directional oldData && data instanceof Directional newData)
                        newData.setFacing(oldData.getFacing());
                    // Setting the block.
                    event.getBlockState().getWorld().setBlockData(event.getBlockState().getLocation(), data);
                    // Playing the plant sound.
                    event.getBlockState().getWorld().playSound(event.getBlockState().getLocation(), event.getBlockState().getBlockData().getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
                    // Spawning particles.
                    switch (blockType) {
                        case WHEAT, CARROTS, POTATOES, BEETROOTS, NETHER_WART, MELON_STEM, PUMPKIN_STEM -> {
                            event.getBlockState().getWorld().spawnParticle(Particle.EGG_CRACK, event.getBlockState().getLocation().clone().add(0.5D, 0.15D, 0.5D), 8, 0.25, 0.05, 0.25);
                        }
                        case COCOA -> {
                            // EMPTY; QUITE DIFFICULT TO GET THIS LOOK GOOD
                        }
                    }
                }
            }
        }
    }

}
