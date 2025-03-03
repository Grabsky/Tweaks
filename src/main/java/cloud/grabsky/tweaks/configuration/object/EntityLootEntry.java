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
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
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
public final class EntityLootEntry {

    @Getter(AccessLevel.PUBLIC)
    private @Nullable Float chance;

    @Getter(AccessLevel.PUBLIC)
    private @Nullable Float chancePerLootingLevel;

    @Getter(AccessLevel.PUBLIC)
    private @Nullable String requiredPermission;

    @Getter(AccessLevel.PUBLIC)
    private @Nullable List<NamespacedKey> requiredAttacker;

    @Getter(AccessLevel.PUBLIC)
    private @Nullable List<NamespacedKey> requiredDamage;

    @Getter(AccessLevel.PUBLIC)
    private @Nullable String message;

    @Getter(AccessLevel.PUBLIC)
    private @UnknownNullability ItemStack item;

    /* SERIALIZATION */

    public enum Factory implements JsonAdapter.Factory {
        INSTANCE; // SINGLETON

        private static final Type LIST_NAMESPACEDKEY = Types.newParameterizedType(List.class, NamespacedKey.class);

        @Override @SuppressWarnings("unchecked")
        public @Nullable JsonAdapter<EntityLootEntry> create(final @NotNull Type type, final @NotNull Set<? extends Annotation> annotations, final @NotNull Moshi moshi) {
            if (EntityLootEntry.class.isAssignableFrom(getRawType(type)) == false)
                return null;
            // Getting adapters...
            final var adapter0 = moshi.adapter(ItemStack.class);
            final var adapter1 = moshi.adapter(LIST_NAMESPACEDKEY);
            // Constructing and returning the JsonAdapter for this type.
            return new JsonAdapter<>() {

                @Override
                public @NotNull EntityLootEntry fromJson(final @NotNull JsonReader in) throws IOException {
                    final EntityLootEntry result = new EntityLootEntry();
                    // ...
                    in.beginObject();
                    while (in.hasNext() == true) {
                        // Getting the next key / property name of this object.
                        final String key = in.nextName().toLowerCase();
                        // Building the result object.
                        switch (key) {
                            case "chance" -> result.chance = (float) in.nextDouble();
                            case "chance_per_looting_level" -> result.chancePerLootingLevel = (float) in.nextDouble();
                            case "required_permission" -> result.requiredPermission = in.nextString();
                            case "required_attacker" -> result.requiredAttacker = (List<NamespacedKey>) adapter1.fromJson(in);
                            case "required_damage" -> result.requiredDamage = (List<NamespacedKey>) adapter1.fromJson(in);
                            case "message" -> result.message = in.nextString();
                            case "item" -> result.item = adapter0.fromJson(in);
                        }
                    }
                    in.endObject();
                    // Throwing exception if both, 'base' and 'variants' turn out to be null.
                    if (result.item == null)
                        throw new JsonDataException("Expected 'item' property but found nothing.");
                    // Returning the result object.
                    return result;
                }

                @Override
                public void toJson(final @NotNull JsonWriter out, final @Nullable EntityLootEntry value) {
                    throw new UnsupportedOperationException("NOT_IMPLEMENTED");
                }

            };

        }

    }

}
