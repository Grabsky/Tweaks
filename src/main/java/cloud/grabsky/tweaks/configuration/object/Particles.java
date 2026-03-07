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
package cloud.grabsky.tweaks.configuration.object;

import com.google.gson.annotations.SerializedName;
import org.bukkit.Particle;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PUBLIC)
public final class Particles {

    @Getter(AccessLevel.PUBLIC)
    private final Particle particle;

    @Getter(AccessLevel.PUBLIC)
    private final int amount;

    @Getter(AccessLevel.PUBLIC)
    private final float speed;

    @SerializedName("offset_x")
    @Getter(AccessLevel.PUBLIC)
    private final double offsetX;

    @SerializedName("offset_y")
    @Getter(AccessLevel.PUBLIC)
    private final double offsetY;

    @SerializedName("offset_z")
    @Getter(AccessLevel.PUBLIC)
    private final double offsetZ;

}
