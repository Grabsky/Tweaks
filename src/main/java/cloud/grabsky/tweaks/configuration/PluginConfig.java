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
package cloud.grabsky.tweaks.configuration;

import cloud.grabsky.configuration.JsonConfiguration;
import cloud.grabsky.configuration.JsonNullable;
import cloud.grabsky.configuration.JsonPath;
import cloud.grabsky.tweaks.configuration.object.Particles;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Internal
public final class PluginConfig implements JsonConfiguration {

    // Enabled Modules > Enchantments

    @JsonPath("enabled_modules.magnet_enchantment")
    public static boolean ENABLED_MODULES_MAGNET_ENCHANTMENT;

    @JsonPath("enabled_modules.sonic_shield_enchantment")
    public static boolean ENABLED_MODULES_SONIC_SHIELD_ENCHANTMENT;

    @JsonPath("enabled_modules.bait_enchantment")
    public static boolean ENABLED_MODULES_BAIT_ENCHANTMENT;

    // Enabled Modules > Enhanced Items

    @JsonPath("enabled_modules.enhanced_clock")
    public static boolean ENABLED_MODULES_ENHANCED_CLOCK;

    @JsonPath("enabled_modules.enhanced_compass")
    public static boolean ENABLED_MODULES_ENHANCED_COMPASS;

    @JsonPath("enabled_modules.enhanced_map")
    public static boolean ENABLED_MODULES_ENHANCED_MAP;

    // Enabled Modules > Chairs

    @JsonPath("enabled_modules.chairs")
    public static boolean ENABLED_MODULES_CHAIRS;

    // Enabled Modules > Basket

    @JsonPath("enabled_modules.basket")
    public static boolean ENABLED_MODULES_BASKET;

    // Enabled Modules > Inventory Rules

    @JsonPath("enabled_modules.balanced_keep_inventory")
    public static boolean ENABLED_MODULES_BALANCED_KEEP_INVENTORY;

    @JsonPath("enabled_modules.invulnerable_players_keep_inventory")
    public static boolean ENABLED_MODULES_INVULNERABLE_PLAYERS_KEEP_INVENTORY;

    // Enabled Modules > Other

    @JsonPath("enabled_modules.weaker_phantoms")
    public static boolean ENABLED_MODULES_WEAKER_PHANTOMS;

    @JsonPath("enabled_modules.creeper_ignites_on_fire_damage")
    public static boolean ENABLED_MODULES_CREEPER_IGNITES_ON_FIRE_DAMAGE;

    @JsonPath("enabled_modules.campfire_regeneration")
    public static boolean ENABLED_MODULES_CAMPFIRE_REGENERATION;

    @JsonPath("enabled_modules.campfire_prevents_mob_tracking")
    public static boolean ENABLED_MODULES_CAMPFIRE_PREVENTS_MOB_TRACKING;

    @JsonPath("enabled_modules.ender_portal_frame_mini_game")
    public static boolean ENABLED_MODULES_ENDER_PORTAL_FRAME_MINI_GAME;

    @JsonPath("enabled_modules.armor_stand_spawns_with_arms")
    public static boolean ENABLED_MODULES_ARMOR_STAND_SPAWNS_WITH_ARMS;

    @JsonPath("enabled_modules.skull_data_recovery")
    public static boolean ENABLED_MODULES_SKULL_DATA_RECOVERY;

    @JsonPath("enabled_modules.reusable_vaults")
    public static boolean ENABLED_MODULES_REUSABLE_VAULTS;

    @JsonPath("enabled_modules.damage_multipliers")
    public static boolean ENABLED_MODULES_DAMAGE_MULTIPLIERS;

    @JsonPath("enabled_modules.improved_end_phantoms")
    public static boolean ENABLED_MODULES_IMPROVED_END_PHANTOMS;

    // Enabled Modules > Items

    @JsonPath("enabled_modules.scrolls")
    public static boolean ENABLED_MODULES_SCROLLS;

    @JsonPath("enabled_modules.enderite")
    public static boolean ENABLED_MODULES_ENDERITE;

    // Compass Settings

    @JsonPath("compass_settings.refresh_rate")
    public static long COMPASS_SETTINGS_REFRESH_RATE;

    @JsonPath("compass_settings.bossbar")
    public static BossBarProperties COMPASS_SETTINGS_BOSSBAR;

    // Clock Settings

    @JsonPath("clock_settings.refresh_rate")
    public static long CLOCK_SETTINGS_REFRESH_RATE;

    @JsonPath("clock_settings.bossbar")
    public static BossBarProperties CLOCK_SETTINGS_BOSSBAR;

    // Map Settings

    @JsonPath("map_settings.refresh_rate")
    public static long MAP_SETTINGS_REFRESH_RATE;

    @JsonPath("map_settings.bossbar")
    public static BossBarProperties MAP_SETTINGS_BOSSBAR;

    // Damage Multiplier Settings

    @JsonPath("damage_multiplier_settings.outgoing")
    public static Map<EntityType, Float>  DAMAGE_MULTIPLIER_SETTINGS_OUTGOING;

    @JsonPath("damage_multiplier_settings.incoming")
    public static Map<EntityType, Float>  DAMAGE_MULTIPLIER_SETTINGS_INCOMING;

    // Basket Settings

    @JsonPath("basket_settings.allowed_mobs")
    public static List<NamespacedKey> BASKET_SETTINGS_ALLOWED_MOBS;

    // Vaults Settings

    @JsonPath("vaults_settings.cooldowns")
    public static Map<String, Long> VAULTS_SETTINGS_COOLDOWNS;

    // Teleportation Settings

    @JsonPath("teleportation_settings.delay")
    public static int TELEPORTATION_SETTINGS_DELAY;

    @JsonPath("teleportation_settings.fade_in_fade_out_animation_translation")
    public static String TELEPORTATION_SETTINGS_FADE_IN_FADE_OUT_ANIMATION_TRANSLATION;

    @JsonNullable
    @JsonPath("teleportation_settings.sounds.out")
    public static @Nullable Sound TELEPORTATION_SETTINGS_SOUNDS_OUT;

    @JsonNullable @JsonPath("teleportation_settings.sounds.in")
    public static @Nullable Sound TELEPORTATION_SETTINGS_SOUNDS_IN;

    @JsonNullable @JsonPath("teleportation_settings.particles")
    public static @Nullable List<Particles> TELEPORTATION_SETTINGS_PARTICLES;

    @JsonPath("teleportation_settings.lang.teleport_in_progress")
    public static String TELEPORTATION_SETTINGS_LANG_TELEPORT_IN_PROGRESS;

    @JsonPath("teleportation_settings.lang.teleport_success")
    public static Component TELEPORTATION_SETTINGS_LANG_TELEPORT_SUCCESS;

    @JsonPath("teleportation_settings.lang.teleport_failure_moved")
    public static Component TELEPORTATION_SETTINGS_LANG_TELEPORT_FAILURE_MOVED;

    @JsonPath("teleportation_settings.lang.teleport_failure_unknown")
    public static Component TELEPORTATION_SETTINGS_LANG_TELEPORT_FAILURE_UNKNOWN;

    @JsonPath("teleportation_settings.lang.teleport_failure_on_cooldown")
    public static String TELEPORTATION_SETTINGS_LANG_TELEPORT_FAILURE_ON_COOLDOWN;

    // Enderite Settings

    @JsonPath("enderite_settings.base_models")
    public static List<NamespacedKey> ENDERITE_SETTINGS_BASE_MODELS;

    @JsonPath("enderite_settings.addition_models")
    public static List<NamespacedKey> ENDERITE_SETTINGS_ADDITION_MODELS;


    // Moshi should be able to create instance of the object despite the constructor being private.
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class BossBarProperties {

        @Getter(AccessLevel.PUBLIC)
        private final BossBar.Color color;

        @Getter(AccessLevel.PUBLIC)
        private final BossBar.Overlay overlay;

        @Getter(AccessLevel.PUBLIC)
        private final String text;

    }

}
