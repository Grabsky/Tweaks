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

import cloud.grabsky.tweaks.Module;
import cloud.grabsky.tweaks.Tweaks;
import cloud.grabsky.tweaks.configuration.PluginConfig;
import cloud.grabsky.tweaks.utils.Extensions;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Skull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(Extensions.class)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class SkullDataRecoveryHandler implements Module, Listener {

    @Getter(AccessLevel.PUBLIC)
    public @NotNull Tweaks plugin;

    private static final NamespacedKey SKULL_DATA_KEY = new NamespacedKey("tweaks", "skull_data");

    @Override
    public void load() {
        if (PluginConfig.ENABLED_MODULES_SKULL_DATA_RECOVERY == true)
            // Registering events.
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void unload() {
        // Unregistering events.
        HandlerList.unregisterAll(this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSkullPlace(final @NotNull BlockPlaceEvent event) {
        if (event.canBuild() == true && event.getBlock().getState() instanceof Skull state) {
            final byte[] bytes = event.getItemInHand().serializeAsBytes();
            // Setting the data.
            state.getPersistentDataContainer().set(SKULL_DATA_KEY, PersistentDataType.BYTE_ARRAY, bytes);
            // Updating the state.
            state.update();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSkullBreak(final @NotNull BlockBreakEvent event) {
        if (event.getBlock().getState() instanceof Skull state) {
            if (state.getPersistentDataContainer().has(SKULL_DATA_KEY) == true) {
                final byte[] bytes = state.getPersistentDataContainer().get(SKULL_DATA_KEY, PersistentDataType.BYTE_ARRAY);
                // Removing drops.
                event.setExpToDrop(0);
                event.setDropItems(false);
                // Deserializing item from PDC.
                final ItemStack item = ItemStack.deserializeBytes(bytes);
                // Dropping the item.
                event.getBlock().getWorld().dropItem(event.getBlock().getLocation().add(0.5, 0.5, 0.5), item);
            }
        }
    }

}