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

import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.tweaks.Tweaks;
import cloud.grabsky.tweaks.configuration.PluginConfig;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiPredicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(Extensions.class)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Utilities {

    // Players with this permission can teleport instantly.
    public static final String BYPASS_TELEPORT_COOLDOWN = "tweaks.plugin.bypass_teleport_cooldown";

    private static void showFadeIn(final Audience audience) {
        // Sending the title, but only if configured to do so.
        if (PluginConfig.TELEPORTATION_SETTINGS_FADE_IN_FADE_OUT_ANIMATION_TRANSLATION.isBlank() == false)
            audience.showRichTitle(Component.translatable(PluginConfig.TELEPORTATION_SETTINGS_FADE_IN_FADE_OUT_ANIMATION_TRANSLATION), Component.empty(), 8, 16, 8);
    }

    /**
     * Performs a teleportation of specified {@link HumanEntity} to specified {@link Location}.
     */
    private static CompletableFuture<Boolean> performTeleport(final @NotNull HumanEntity source, final @NotNull Location destination) {
        final CompletableFuture<Boolean> future = new CompletableFuture<>();
        // Fading-in the black screen.
        showFadeIn(source);
        // Scheduling teleport 8 ticks after fading-in player's screen. So that teleport is hidden.
        Tweaks.getInstance().getBedrockScheduler().run(8L, (_) -> {
            source.teleportAsync(destination, PlayerTeleportEvent.TeleportCause.PLUGIN).thenAccept(isSuccess -> {
                // Fading-out the black screen.
                if (PluginConfig.TELEPORTATION_SETTINGS_FADE_IN_FADE_OUT_ANIMATION_TRANSLATION.isBlank() == false)
                    source.fadeOutTitle(8, 8);
                // ...
                if (isSuccess) {
                    // Showing success message on the action bar.
                    Message.of(PluginConfig.TELEPORTATION_SETTINGS_LANG_TELEPORT_SUCCESS).sendActionBar(source);
                    // Making player invulnerable for next 5 seconds.
                    source.setNoDamageTicks(100);
                }
                // Completing the future.
                future.complete(isSuccess);
            });
        });
        // Returning the future.
        return future;
    }

    /**
     * Performs a teleportation of specified {@link Player} to specified {@link Location}.
     * For delays greater than 0, action bar countdown is generated. Smooth fade-in/fade-out animations are included via {@link Utilities#performTeleport(HumanEntity, Location) Utilities#performTeleport}.
     */
    public static void teleport(final @NotNull Player source, final @NotNull Location destination, final int delay, final @Nullable BiPredicate<Location, Location> shouldCancel, final @Nullable TriConsumer<Boolean, Location, Location> then) {
        // Getting the initial position of the player.
        final Location sourceInitialLocation = source.getLocation();
        // Handling teleports with no (or bypassed) delay.
        if (delay == 0 || source.hasPermission(BYPASS_TELEPORT_COOLDOWN) == true) {
            // Fading-in to the black screen, teleporting asynchronously, sending messages and fading-out.
            performTeleport(source, destination).thenAccept(isSuccess -> {
                if (isSuccess == true && then != null) then.accept(isSuccess, sourceInitialLocation, destination);
            });
            // Returning, as this has been already handled.
            return;
        }
        // Sending action bar message with delay information.
        Message.of(PluginConfig.TELEPORTATION_SETTINGS_LANG_TELEPORT_IN_PROGRESS).placeholder("delay", delay).sendActionBar(source);
        // Submitting an asynchronous countdown task, after which the player will be teleported.
        CompletableFuture.supplyAsync(() -> {
            try {
                for (int delayLeft = delay; delayLeft != 0; delayLeft--) {
                    source.playSound(source, "minecraft:block.note_block.hat", SoundCategory.MASTER, 0.5F, 2.0F);
                    // Showing message with delay information on the action bar.
                    Message.of(PluginConfig.TELEPORTATION_SETTINGS_LANG_TELEPORT_IN_PROGRESS).placeholder("delay", delayLeft).sendActionBar(source);
                    // Handling teleport interrupt. (moving)
                    if (source.getLocation().distanceSquared(sourceInitialLocation) > 1.0) {
                        // Showing failure message on the action bar. Interrupted.
                        Message.of(PluginConfig.TELEPORTATION_SETTINGS_LANG_TELEPORT_FAILURE_MOVED).sendActionBar(source);
                        // ...
                        return false;
                    }
                    // Sleeping for one second.
                    Thread.sleep(1000);
                }
                // Running 'shouldCancel' predicate and cancelling the teleport in case it fails.
                if (shouldCancel != null && shouldCancel.test(sourceInitialLocation, destination) == true) {
                    // Playing timer 'ticking' sound.
                    source.playSound(source, "minecraft:block.note_block.hat", SoundCategory.MASTER, 0.5F, 2.0F);
                    // Showing failure message on the action bar. Cancelled.
                    Message.of(PluginConfig.TELEPORTATION_SETTINGS_LANG_TELEPORT_FAILURE_UNKNOWN).sendActionBar(source);
                    // ...
                    return false;
                }
                // The end has been reached, returning 'true' so that a teleport is attempted in the next step.
                return true;
            } catch (final InterruptedException _) {
                return false;
            }
        })
        // Fading-in to the black screen, teleporting asynchronously, sending messages and fading-out.
        .thenCompose(isSuccess -> (isSuccess == true) ? performTeleport(source, destination) : CompletableFuture.completedFuture(false))
        // Running post-teleportation tasks.
        .thenAccept(isSuccess -> {
            if (isSuccess == true && then != null) then.accept(isSuccess, sourceInitialLocation, destination);
        });
    }

}
