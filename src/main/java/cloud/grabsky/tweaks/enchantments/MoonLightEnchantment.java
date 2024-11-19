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
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Equippable;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(Extensions.class)
@SuppressWarnings("UnstableApiUsage")
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class MoonLightEnchantment implements Module, Listener {

    @Getter(AccessLevel.PUBLIC)
    public @NotNull Tweaks plugin;

    private static final NamespacedKey MOON_LIGHT_OVERLAY_KEY = new NamespacedKey("firedot", "moon_light_overlay");

    @Override
    public void load() {
        if (PluginConfig.ENABLED_MODULES_BAIT_ENCHANTMENT == true)
            // Registering events.
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void unload() {
        // Unregistering events.
        HandlerList.unregisterAll(this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onArmorChange(final @NotNull PlayerArmorChangeEvent event) {
        if (event.getNewItem().isEmpty() == true)
            return;
        // ...
        final ItemStack item = event.getNewItem();
        // Getting the 'equippable' component from this item.
        final @Nullable Equippable equippable = item.getData(DataComponentTypes.EQUIPPABLE);
        // ...
        if (equippable != null && item.isEnchantedWith("firedot:moon_light") == true) {
            // Checking if 'equippable' component is not null and 'camera_overlay' is not set.
            if (equippable.cameraOverlay() == null) {
                // Updating the 'equippable' component with camera overlay.
                item.setData(DataComponentTypes.EQUIPPABLE, equippable.toBuilder().cameraOverlay(MOON_LIGHT_OVERLAY_KEY));
                // Hacky workaround for ItemStack returned by PlayerArmorChangeEvent#getNewItem being immutable.
                plugin.getBedrockScheduler().run(1L, (_) -> {
                    item.setData(DataComponentTypes.EQUIPPABLE, equippable.toBuilder().cameraOverlay(MOON_LIGHT_OVERLAY_KEY));
                    event.getPlayer().getInventory().setItem(translateSlot(event.getSlotType()), item);
                });
            }
        // Removing the overlay if item is no longer enchanted.
        } else if (equippable != null && item.isEnchantedWith("firedot:moon_light") == false && equippable.cameraOverlay() != null && equippable.cameraOverlay().equals(MOON_LIGHT_OVERLAY_KEY) == true) {
            // Removing camera overlay from the 'equippable' component.
            item.setData(DataComponentTypes.EQUIPPABLE, equippable.toBuilder().cameraOverlay(null));
            // Hacky workaround for ItemStack returned by PlayerArmorChangeEvent#getNewItem being immutable.
            plugin.getBedrockScheduler().run(1L, (_) -> {
                item.setData(DataComponentTypes.EQUIPPABLE, equippable.toBuilder().cameraOverlay(MOON_LIGHT_OVERLAY_KEY));
                event.getPlayer().getInventory().setItem(translateSlot(event.getSlotType()), item);
            });
        }
    }

    /** Returns EquipmentSlot equivalent of provided SlotType. */
    private static @NotNull EquipmentSlot translateSlot(final @NotNull PlayerArmorChangeEvent.SlotType slot) {
        return switch (slot) {
            case HEAD -> EquipmentSlot.HEAD;
            case CHEST -> EquipmentSlot.CHEST;
            case LEGS -> EquipmentSlot.LEGS;
            case FEET -> EquipmentSlot.FEET;
        };
    }

}
