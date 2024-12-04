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
import cloud.grabsky.configuration.JsonPath;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.NamespacedKey;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus.Internal;

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

    // Basket Settings

    @JsonPath("basket_settings.allowed_mobs")
    public static List<NamespacedKey> BASKET_SETTINGS_ALLOWED_MOBS;

    // Vaults Settings

    @JsonPath("vaults_settings.cooldowns")
    public static Map<String, Long> VAULTS_SETTINGS_COOLDOWNS;


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
