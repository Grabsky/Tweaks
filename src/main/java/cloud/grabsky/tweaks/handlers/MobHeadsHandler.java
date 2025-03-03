/*
 * Tweaks (https://github.com/Grabsky/Tweaks)
 *
 * Copyright (C) 2025  Grabsky <michal.czopek.foss@proton.me>
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
package cloud.grabsky.tweaks.handlers;

import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.bedrock.helpers.ItemBuilder;
import cloud.grabsky.tweaks.Module;
import cloud.grabsky.tweaks.Tweaks;
import cloud.grabsky.tweaks.configuration.HeadsConfig;
import cloud.grabsky.tweaks.configuration.PluginConfig;
import cloud.grabsky.tweaks.configuration.object.EntityLootContainer;
import cloud.grabsky.tweaks.configuration.object.EntityLootEntry;
import cloud.grabsky.tweaks.utils.Extensions;
import net.kyori.adventure.text.Component;
import org.bukkit.DyeColor;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

import static cloud.grabsky.bedrock.helpers.Conditions.requirePresent;

@ExtensionMethod(Extensions.class)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class MobHeadsHandler implements Module, Listener {

    @Getter(AccessLevel.PUBLIC)
    public @NotNull Tweaks plugin;

    @Override
    public void load() {
        if (PluginConfig.ENABLED_MODULES_MOB_HEADS == true)
            // Registering events.
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void unload() {
        // Unregistering events.
        HandlerList.unregisterAll(this);
    }

    @SuppressWarnings("UnstableApiUsage")
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityDeath(final @NotNull EntityDeathEvent event) {
        final NamespacedKey key = event.getEntityType().getKey();
        // Filtering to only listen for mobs.
        if (HeadsConfig.ENTITIES.containsKey(key) == true) {
            final EntityLootContainer container = HeadsConfig.ENTITIES.get(key);
            // Getting the matching entry.
            EntityLootEntry entry = (container.hasVariants() == true) ? switch (event.getEntity()) {
                case Axolotl axolotl -> container.variants().getOrDefault(axolotl.getVariant().name().toLowerCase(), null);
                case Horse horse -> container.variants().getOrDefault(horse.getColor().name().toLowerCase(), null);
                case Llama llama -> container.variants().getOrDefault(llama.getColor().name().toLowerCase(), null);
                case Parrot parrot -> container.variants().getOrDefault(parrot.getVariant().name().toLowerCase(), null);
                case Rabbit rabbit -> container.variants().getOrDefault(rabbit.getRabbitType().name().toLowerCase(), null);
                case Villager villager -> container.variants().getOrDefault(villager.getVillagerType().getKey().asString(), null);
                case ZombieVillager zombieVillager -> container.variants().getOrDefault(zombieVillager.getVillagerType().getKey().asString(), null);
                case Cat cat -> container.variants().getOrDefault(cat.getCatType().getKey().asString(), null);
                case Fox fox -> container.variants().getOrDefault(fox.getFoxType().name().toLowerCase(), null);
                case Panda panda -> container.variants().getOrDefault(panda.getCombinedGene().name().toLowerCase(), null);
                case Frog frog -> container.variants().getOrDefault(frog.getVariant().getKey().asString(), null);
                case Sheep sheep -> container.variants().getOrDefault(requirePresent(sheep.getColor(), DyeColor.WHITE).name().toLowerCase(), null);
                case Shulker shulker -> container.variants().getOrDefault(requirePresent(shulker.getColor(), DyeColor.PURPLE).name().toLowerCase(), null);
                case Wolf wolf -> container.variants().getOrDefault(wolf.getVariant().getKey().asString(), null);
                default -> container.base();
            } : container.base();
            // Using base entry as default.
            if (entry == null && container.base() != null)
                entry = container.base();
            // Returning if final entry turned out to be null. This means neither any matching variants nor base is configured.
            if (entry == null)
                return;
            // Getting the type of the attacker.
            final @Nullable Entity attacker = (event.getDamageSource().getCausingEntity() != null) ? event.getDamageSource().getCausingEntity() : null;
            // Checking whether this attacker is allowed to initiate head drop. (Permission)
            final String requiredPermission = requirePresent(entry.requiredPermission(), HeadsConfig.DEFAULT_REQUIRED_PERMISSION);
            if (requiredPermission.isBlank() == false)
                if (!(attacker instanceof Player player) || player.hasPermission(requiredPermission) == false)
                    return;
            // Checking whether this attacker is allowed to initiate head drop.
            final List<NamespacedKey> requiredAttacker = requirePresent(entry.requiredAttacker(), HeadsConfig.DEFAULT_REQUIRED_ATTACKER);
            if (requiredAttacker.isEmpty() == false)
                if (attacker == null || requiredAttacker.contains(attacker.getType().getKey()) == false)
                    return;
            // Checking whether this damage cause is allowed to initiate head drop.
            final List<NamespacedKey> requiredDamage = requirePresent(entry.requiredDamage(), HeadsConfig.DEFAULT_REQUIRED_DAMAGE);
            if (requiredDamage.isEmpty() == false && requiredDamage.contains(event.getDamageSource().getDamageType().getKey()) == false)
                return;
            // Getting the level of 'minecraft:looting' enchantment on player's currently held item.
            final int level = (event.getDamageSource().getCausingEntity() instanceof Player player) ? player.getInventory().getItemInMainHand().getEnchantLevel(Enchantment.LOOTING) : 0;
            // Calculating the total chance for the head to drop.
            final float chance = requirePresent(entry.chance(), HeadsConfig.DEFAULT_CHANCE);
            final float chancePerLootingLevel = requirePresent(entry.chancePerLootingLevel(), HeadsConfig.DEFAULT_CHANCE_PER_LOOTING_LEVEL);
            final float totalChance = Math.clamp(chance + (level * chancePerLootingLevel), 0.0F, 1.0F);
            // Rolling the dice and continuing if lucky enough. This also checks for empty items.
            if (Math.random() <= totalChance && entry.item().isEmpty() == false) {
                final ItemStack item = (event.getEntity() instanceof Player player)
                        ? new ItemBuilder(entry.item()).setSkullTexture(player).build()
                        : entry.item();
                // Adding item to the drops.
                event.getDrops().add(item);
                // Optionally, sending message to the attacker.
                if (event.getDamageSource().getCausingEntity() instanceof Player player)
                    Message.of(entry.message() != null ? entry.message() : HeadsConfig.DEFAULT_MESSAGE)
                            .placeholder("item", Component.text("[", item.getItemMeta().itemName().color()).append(item.effectiveName().hoverEvent(item.asHoverEvent())).append(Component.text("]")))
                            .send(player);
            }
        }
    }


}