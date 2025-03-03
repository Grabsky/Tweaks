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

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import static com.squareup.moshi.Types.getRawType;

@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EntityLootContainer {

    @Getter(AccessLevel.PUBLIC)
    private @UnknownNullability EntityLootEntry base;

    @Getter(AccessLevel.PUBLIC)
    private @UnknownNullability Map<String, EntityLootEntry> variants;

    public boolean hasVariants() {
        return variants != null && variants.isEmpty() == false;
    }

    /* SERIALIZATION */

    public enum Factory implements JsonAdapter.Factory {
        INSTANCE; // SINGLETON

        private static final Type MAP_STRING_ENTITY_LOOT_ENTRY = Types.newParameterizedType(Map.class, String.class, EntityLootEntry.class);

        @Override @SuppressWarnings("unchecked")
        public @Nullable JsonAdapter<EntityLootContainer> create(final @NotNull Type type, final @NotNull Set<? extends Annotation> annotations, final @NotNull Moshi moshi) {
            if (EntityLootContainer.class.isAssignableFrom(getRawType(type)) == false)
                return null;
            // Getting adapters...
            final var adapter0 = moshi.adapter(EntityLootEntry.class);
            final var adapter1 = moshi.adapter(MAP_STRING_ENTITY_LOOT_ENTRY);
            // Constructing and returning the JsonAdapter for this type.
            return new JsonAdapter<>() {

                @Override
                public @NotNull EntityLootContainer fromJson(final @NotNull JsonReader in) throws IOException {
                    final EntityLootContainer result = new EntityLootContainer();
                    in.beginObject();
                    while (in.hasNext() == true) {
                        // Getting the next key / property name of this object.
                        final String key = in.nextName().toLowerCase();
                        // Building the result object.
                        switch (key) {
                            case "base" -> result.base = adapter0.fromJson(in);
                            case "variants" -> result.variants = (Map<String, EntityLootEntry>) adapter1.fromJson(in);
                        }
                    }
                    in.endObject();
                    // Throwing exception if both, 'base' and 'variants' turn out to be null.
                    if (result.base == null && result.variants == null)
                        throw new JsonDataException("Expected 'base' or 'variants' property but neither were specified.");
                    // Returning the result object.
                    return result;
                }

                @Override
                public void toJson(final @NotNull JsonWriter out, final @Nullable EntityLootContainer value) {
                    throw new UnsupportedOperationException("NOT_IMPLEMENTED");
                }

            };

        }

    }
}
