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

@FunctionalInterface
public interface TriConsumer<K, V, S> {

    /**
     * Performs this operation on the given arguments.
     *
     * @param k the first input argument
     * @param v the second input argument
     * @param s the third input argument
     */
    void accept(final K k, final V v, final S s);

}
