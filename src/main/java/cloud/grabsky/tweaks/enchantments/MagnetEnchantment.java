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
package cloud.grabsky.tweaks.enchantments;

import cloud.grabsky.tweaks.Module;
import cloud.grabsky.tweaks.Tweaks;
import cloud.grabsky.tweaks.configuration.PluginConfig;
import cloud.grabsky.tweaks.utils.Extensions;
import com.destroystokyo.paper.MaterialTags;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerCollectItem;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(Extensions.class)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class MagnetEnchantment implements Module, Listener {

    @Getter(AccessLevel.PUBLIC)
    public @NotNull Tweaks plugin;

    @Override
    public void load() {
        if (PluginConfig.ENABLED_MODULES_MAGNET_ENCHANTMENT == true)
            // Registering events.
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void unload() {
        // Unregistering events.
        HandlerList.unregisterAll(this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(final @NotNull BlockBreakEvent event) {
        final Player player = event.getPlayer();
        // Checking if player is in Survival game mode
        if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
            final Block block = event.getBlock();
            // Checking if player is breaking an ore
            if (MaterialTags.ORES.isTagged(block) == true || MaterialTags.RAW_ORE_BLOCKS.isTagged(block) == true) {
                final ItemStack tool = player.getInventory().getItemInMainHand();
                // Checking if player's tool is enchanted with Magnetic enchantment (my way of storing enchantments)
                if (tool.isEnchantedWith("firedot:magnet") == true) {
                    // Getting drops; Ores drop only one ItemStack, so we can safely get the first element from the Collection
                    event.getBlock().getDrops(tool).forEach(drop -> {
                        // Checking if player has space for an item
                        if (player.getInventory().hasSpace(drop) == true) {
                            final int experience = event.getExpToDrop();
                            // Setting drops to false as player has enough space for an item
                            event.setDropItems(false);
                            event.setExpToDrop(0);
                            // Adding drops directly to the player's inventory.
                            player.getInventory().addItem(drop);
                            if (experience != 0) player.getWorld().spawn(player.getLocation(), ExperienceOrb.class, CreatureSpawnEvent.SpawnReason.NATURAL, (orb) -> {
                                orb.setExperience(experience);
                            });
                            // Creating next entity identifier for use with packets.
                            final int id = Bukkit.getUnsafe().nextEntityId();
                            // Doing packet stuff... asynchronously.
                            plugin.getBedrockScheduler().runAsync(1L, (_) -> {
                                final Location location = new Location(block.getX() + 0.5D, block.getY() + 0.5D, block.getZ() + 0.5D, 0F, 0F);
                                // Creating PlayServerSpawnEntity packet.
                                final var PlayServerSpawnEntityPacket = new WrapperPlayServerSpawnEntity(id, UUID.randomUUID(), EntityTypes.ITEM, location, 0, 0, null);
                                // Creating PlayServerEntityMetadata packet.
                                final var PlayServerEntityMetadataPacket = new WrapperPlayServerEntityMetadata(id, List.of(new EntityData(8, EntityDataTypes.ITEMSTACK, SpigotConversionUtil.fromBukkitItemStack(drop))));
                                // Creating PlayServerCollectItem packet.
                                final var PlayServerCollectItemPacket = new WrapperPlayServerCollectItem(id, player.getEntityId(), drop.getAmount());
                                // Sending packets...
                                PacketEvents.getAPI().getPlayerManager().sendPacket(player, PlayServerSpawnEntityPacket);
                                PacketEvents.getAPI().getPlayerManager().sendPacket(player, PlayServerEntityMetadataPacket);
                                PacketEvents.getAPI().getPlayerManager().sendPacket(player, PlayServerCollectItemPacket);
                            });
                        }
                    });
                }
            }
        }
    }


}
