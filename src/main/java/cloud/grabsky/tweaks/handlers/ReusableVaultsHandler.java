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
package cloud.grabsky.tweaks.handlers;

import cloud.grabsky.bedrock.util.Interval;
import cloud.grabsky.bedrock.util.Interval.Unit;
import cloud.grabsky.tweaks.Module;
import cloud.grabsky.tweaks.Tweaks;
import cloud.grabsky.tweaks.configuration.PluginConfig;
import com.github.retrooper.packetevents.event.PacketListener;
import com.jeff_media.morepersistentdatatypes.DataType;
import de.tr7zw.changeme.nbtapi.NBT;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Vault;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.VaultDisplayItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static org.bukkit.block.data.type.Vault.State;

@SuppressWarnings("UnstableApiUsage")
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class ReusableVaultsHandler implements Module, Listener, PacketListener {

    @Getter(AccessLevel.PUBLIC)
    public @NotNull Tweaks plugin;

    private @Nullable Expansion expansion;

    private static final NamespacedKey VAULT_DATA_LAST_UNLOCK = new NamespacedKey("tweaks", "vault_data/last_unlock");
    private static final PersistentDataType<PersistentDataContainer, HashMap<UUID, Long>> HASH_MAP_UUID_TO_LONG = DataType.asHashMap(DataType.UUID, DataType.LONG);

    @Override
    public void load() {
        // Clearing handlers list.
        HandlerList.unregisterAll(this);
        // Unregistering PAPI expansion.
        if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI") == true) {
            if (expansion != null && expansion.isRegistered() == true)
                expansion.unregister();
            // ...
            expansion = new Expansion(plugin);
            expansion.register();
        }
        // Returning in case module is disabled.
        if (PluginConfig.ENABLED_MODULES_REUSABLE_VAULTS == false)
            return;
        // Registering event handlers.
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void unload() { /* HANDLED INSIDE LOAD */ }

    // NOTE: Most logic can eventually be moved to LootGenerateEvent once it's fixed. Report: https://github.com/PaperMC/Paper/issues/11680
    // Due to lack of proper API, PlayerInteractEvent must be used for the time being with no better workaround.
    @EventHandler(ignoreCancelled = true)
    public void onVaultPreOpen(final @NotNull PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.VAULT) {
            final org.bukkit.block.data.type.Vault blockData = (org.bukkit.block.data.type.Vault) event.getClickedBlock().getBlockData();
            final org.bukkit.block.Vault blockState = (Vault) event.getClickedBlock().getState();
            // Skipping states we don't need to handle.
            if (blockData.getVaultState() == State.UNLOCKING || blockData.getVaultState() == State.EJECTING)
                return;
            // Getting the key required to unlock this vault.
            final @Nullable ItemStack key = NBT.get(blockState, (nbt) -> (nbt.hasTag("config") == true) ? nbt.getCompound("config").getItemStack("key_item") : null);
            // Returning if key is not set. This is unlikely to ever happen but is technically possible.
            if (key == null)
                return;
            // Checking if player has key in their hands.
            if (event.getItem() != null && event.getItem().isSimilar(key) == true) {
                // Skipping off-hand if player already has a key in their main hand.
                if (event.getHand() == EquipmentSlot.OFF_HAND && event.getPlayer().getInventory().getItem(EquipmentSlot.HAND).isSimilar(key) == true)
                    return;
                // Getting unique id of the player.
                final UUID uniqueId = event.getPlayer().getUniqueId();
                // Getting vault config values.
                final String lootTable = NBT.get(blockState, (nbt) -> { return nbt.resolveOrDefault("config.loot_table", "minecraft:chests/trial_chambers/reward"); });
                // Skipping vaults that have no cooldown configured.
                if (PluginConfig.VAULTS_SETTINGS_COOLDOWNS.containsKey(lootTable) == false)
                    return;
                final Long cooldown = PluginConfig.VAULTS_SETTINGS_COOLDOWNS.get(lootTable);
                // Getting the map of last uses on a
                final HashMap<UUID, Long> lastUnlock = blockState.getPersistentDataContainer().getOrDefault(VAULT_DATA_LAST_UNLOCK, HASH_MAP_UUID_TO_LONG, new HashMap<>());
                // Cancelling the event if player is on cooldown.
                if (System.currentTimeMillis() - lastUnlock.getOrDefault(uniqueId, 0L) < cooldown * 1000) {
                    event.setCancelled(true);
                    return;
                }
                // Getting the current vault state.
                final var firstState = blockData.getVaultState();
                // Scheduling further checks next tick to see if the vault was actually unlocked or not.
                plugin.getBedrockScheduler().run(1L, (_) -> {
                    final var updatedBlockState = (Vault) event.getClickedBlock().getState();
                    // Using getAsString in second condition saves on screen space and improves code readability. (No inline cast)
                    if (firstState == State.ACTIVE && updatedBlockState.getBlockData().getAsString().contains("unlocking") == true) {
                        // Getting the updated map. In case it changed during this tick.
                        final HashMap<UUID, Long> lastUnlockUpdated = updatedBlockState.getPersistentDataContainer().getOrDefault(VAULT_DATA_LAST_UNLOCK, HASH_MAP_UUID_TO_LONG, new HashMap<>());
                        // Applying cooldown to the player.
                        lastUnlockUpdated.put(uniqueId, System.currentTimeMillis());
                        // Updating map in the PDC.
                        updatedBlockState.getPersistentDataContainer().set(VAULT_DATA_LAST_UNLOCK, HASH_MAP_UUID_TO_LONG, lastUnlockUpdated);
                        // Updating the block state. Otherwise changes won't be applied.
                        updatedBlockState.update();
                    }
                    // Clearing list of rewarded players as we're applying our own cooldown logic.
                    NBT.modify(updatedBlockState, (nbt) -> {
                        nbt.getOrCreateCompound("server_data").removeKey("rewarded_players");
                    });
                });
            }
        }
    }

    @EventHandler(ignoreCancelled = true) // NOTE: Suggested alternative event here: https://github.com/PaperMC/Paper/discussions/11679
    public void onVaultDisplayItemEvent(final @NotNull VaultDisplayItemEvent event) {
        final org.bukkit.block.data.type.Vault blockData = (org.bukkit.block.data.type.Vault) event.getBlock().getBlockData();
        final org.bukkit.block.Vault blockState = (Vault) event.getBlock().getState();
        // Getting the loot-table of vault associated with the event.
        final String lootTable = NBT.get(blockState, (nbt) -> { return nbt.resolveOrDefault("config.loot_table", "minecraft:chests/trial_chambers/reward"); });
        // Getting the activation range of vault associated with the event.
        final double activationRange = NBT.get(blockState, (nbt) -> { return nbt.resolveOrDefault("config.activation_range", 4.0); });
        // Getting the cooldown for this vault.
        final long cooldown = PluginConfig.VAULTS_SETTINGS_COOLDOWNS.getOrDefault(lootTable, Long.MAX_VALUE);
        // Skipping vaults that have no cooldown configured.
        if (PluginConfig.VAULTS_SETTINGS_COOLDOWNS.containsKey(lootTable) == false)
            return;
        // Getting the map of players that unlocked the vault.
        final HashMap<UUID, Long> lastUnlock = blockState.getPersistentDataContainer().getOrDefault(VAULT_DATA_LAST_UNLOCK, HASH_MAP_UUID_TO_LONG, new HashMap<>());
        // Iterating over the list of all players in range of vault and checking if all of them are on cooldown.
        if (event.getBlock().getLocation().getNearbyPlayers(activationRange).stream().allMatch(it -> System.currentTimeMillis() - lastUnlock.getOrDefault(it.getUniqueId(), 0L) < cooldown * 1000) == true) {
            // Cancelling the event.
            event.setCancelled(true);
            // Setting vault state to INACTIVE.
            blockData.setVaultState(State.INACTIVE);
            // Updating block data of this block state.
            blockState.setBlockData(blockData);
            // Updating the block state. Otherwise changes won't be applied.
            blockState.update();
        }
    }


    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class Expansion extends PlaceholderExpansion {

        private final @NotNull Tweaks plugin;

        // Responsible for storing 'vault_cooldown' placeholders. They must be cached in some way or another because retrieving their
        private final Map<String, String> cache = new HashMap<>();

        @Override
        public @NotNull String getIdentifier() {
            return plugin.getName().toLowerCase();
        }

        @Override
        public @NotNull String getAuthor() {
            return "Grabsky";
        }

        @Override
        public @NotNull String getVersion() {
            return plugin.getPluginMeta().getVersion();
        }

        @Override
        public @Nullable String onRequest(final @NotNull OfflinePlayer offlinePlayer, final @NotNull String params) {
            if (params.startsWith("vault_cooldown_") == true && offlinePlayer instanceof Player player && player.isOnline() == true) {
                System.out.println("Requested...");
                // Getting the location part of the param.
                final String[] part = params.replace("vault_cooldown_", "").split(";");
                // Making sure it's of the correct length.
                if (part.length == 4) {
                    // Parsing location part to actual values.
                    final @Nullable Double x = parseDouble(part[0]);
                    final @Nullable Double y = parseDouble(part[1]);
                    final @Nullable Double z = parseDouble(part[2]);
                    final @Nullable World world = Bukkit.getWorlds().stream().filter(it -> it.getName().equals(part[3])).findFirst().orElse(null);
                    // Checking if all values exist.
                    if (x != null && y != null && z != null && world != null) {
                        //
                        final Location location = new Location(world, x, y, z);
                        // ...
                        if (location.isChunkLoaded() == false || location.getBlock().getType() != Material.VAULT)
                            cache.put(player.getUniqueId() + "/" + params, "N/A");
                        // ...
                        plugin.getBedrockScheduler().run(1L, (_) -> {
                            final org.bukkit.block.Vault blockState = (Vault) location.getBlock().getState();
                            // Getting the loot-table of vault associated with the event.
                            final String lootTable = NBT.get(blockState, (nbt) -> { return nbt.resolveOrDefault("config.loot_table", "minecraft:chests/trial_chambers/reward"); });
                            // Other stuff can be read on the
                            plugin.getBedrockScheduler().runAsync(1L, (_) -> {
                                // Skipping vaults that have no cooldown configured.
                                if (PluginConfig.VAULTS_SETTINGS_COOLDOWNS.containsKey(lootTable) == false)
                                    return;
                                // Getting the cooldown for this vault. Multiplying by 1000 to convert seconds to milliseconds.
                                final long cooldown = PluginConfig.VAULTS_SETTINGS_COOLDOWNS.get(lootTable) * 1000;
                                // Getting the map of players that unlocked the vault.
                                final HashMap<UUID, Long> lastUnlock = blockState.getPersistentDataContainer().getOrDefault(VAULT_DATA_LAST_UNLOCK, HASH_MAP_UUID_TO_LONG, new HashMap<>());
                                // ...
                                final Interval difference = Interval.between(lastUnlock.getOrDefault(player.getUniqueId(), 0L) + cooldown, System.currentTimeMillis(), Unit.MILLISECONDS);
                                // ...
                                cache.put(player.getUniqueId() + "/" + params, (difference.as(Unit.MILLISECONDS) > 0) ? difference.toString() : "");
                            });
                        });
                    }
                }
                return cache.getOrDefault(player.getUniqueId() + "/" + params, "");
            }
            return null;
        }
    }


    /* HELPER METHODS */

    private static @Nullable Double parseDouble(final @NotNull String value) {
        try {
            return Double.parseDouble(value);
        } catch (final NumberFormatException _) {
            return null;
        }
    }

}