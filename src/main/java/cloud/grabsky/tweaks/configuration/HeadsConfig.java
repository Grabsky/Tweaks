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
import cloud.grabsky.tweaks.configuration.object.EntityLootContainer;
import org.bukkit.NamespacedKey;

import java.util.List;
import java.util.Map;

public final class HeadsConfig implements JsonConfiguration {

    @JsonPath("default_chance")
    public static float DEFAULT_CHANCE;

    @JsonPath("default_chance_per_looting_level")
    public static float DEFAULT_CHANCE_PER_LOOTING_LEVEL;

    @JsonPath("default_required_permission")
    public static String DEFAULT_REQUIRED_PERMISSION;

    @JsonPath("default_required_attacker")
    public static List<NamespacedKey> DEFAULT_REQUIRED_ATTACKER;

    @JsonPath("default_required_damage")
    public static List<NamespacedKey> DEFAULT_REQUIRED_DAMAGE;

    @JsonPath("default_message")
    public static String DEFAULT_MESSAGE;

    @JsonPath("entities")
    public static Map<NamespacedKey, EntityLootContainer> ENTITIES;

}
