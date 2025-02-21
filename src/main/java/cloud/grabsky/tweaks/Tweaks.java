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
package cloud.grabsky.tweaks;

import cloud.grabsky.bedrock.BedrockPlugin;
import cloud.grabsky.commands.RootCommandManager;
import cloud.grabsky.configuration.ConfigurationHolder;
import cloud.grabsky.configuration.ConfigurationMapper;
import cloud.grabsky.configuration.exception.ConfigurationMappingException;
import cloud.grabsky.configuration.paper.PaperConfigurationMapper;
import cloud.grabsky.tweaks.command.TweaksCommand;
import cloud.grabsky.tweaks.configuration.PluginConfig;
import cloud.grabsky.tweaks.enchantments.BaitEnchantment;
import cloud.grabsky.tweaks.enchantments.MagnetEnchantment;
import cloud.grabsky.tweaks.enchantments.SonicShieldEnchantment;
import cloud.grabsky.tweaks.enchantments.StrideEnchantment;
import cloud.grabsky.tweaks.handlers.ArmorStandHandler;
import cloud.grabsky.tweaks.handlers.BalancedKeepInventoryHandler;
import cloud.grabsky.tweaks.handlers.BalancedVillagerRestockHandler;
import cloud.grabsky.tweaks.handlers.BetterBoneMealHandler;
import cloud.grabsky.tweaks.handlers.ColoredNametagsHandler;
import cloud.grabsky.tweaks.items.BasketHandler;
import cloud.grabsky.tweaks.handlers.BreakingMultipliersHandler;
import cloud.grabsky.tweaks.handlers.CampfireHandler;
import cloud.grabsky.tweaks.handlers.ChairsHandler;
import cloud.grabsky.tweaks.handlers.ClockHandler;
import cloud.grabsky.tweaks.handlers.CompassHandler;
import cloud.grabsky.tweaks.handlers.CreeperIgniterHandler;
import cloud.grabsky.tweaks.handlers.DamageMultipliersHandler;
import cloud.grabsky.tweaks.handlers.DimensionSoftLockHandler;
import cloud.grabsky.tweaks.handlers.EnderPortalFrameHandler;
import cloud.grabsky.tweaks.handlers.InvulnerableKeepInventoryHandler;
import cloud.grabsky.tweaks.handlers.MapHandler;
import cloud.grabsky.tweaks.handlers.ImprovedEndPhantomsHandler;
import cloud.grabsky.tweaks.handlers.ReusableVaultsHandler;
import cloud.grabsky.tweaks.handlers.SkullDataRecoveryHandler;
import cloud.grabsky.tweaks.handlers.WeakerPhantomsHandler;
import cloud.grabsky.tweaks.handlers.WitherSpawnWhitelistHandler;
import cloud.grabsky.tweaks.items.EnderiteItem;
import cloud.grabsky.tweaks.items.ScrollItem;
import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;

import org.jetbrains.annotations.UnknownNullability;

import lombok.AccessLevel;
import lombok.Getter;

import static cloud.grabsky.configuration.paper.util.Resources.ensureResourceExistence;

public final class Tweaks extends BedrockPlugin implements Listener {

    @Getter(AccessLevel.PUBLIC)
    private static Tweaks instance;

    private ConfigurationMapper mapper;
    private RootCommandManager commands;
    private List<Module> modules;

    // This can only be null before plugin has been fully enabled.
    public static @UnknownNullability Executor MAIN_THREAD;

    @Override
    public void onEnable() {
        super.onEnable();
        // Updating the instance of the plugin.
        instance = this;
        // Updating the main thread executor.
        MAIN_THREAD = Bukkit.getScheduler().getMainThreadExecutor(this);
        // Creating ConfigurationMapper instance.
        this.mapper = PaperConfigurationMapper.create();
        // Adding module(s) to a list.
        this.modules = List.of(
                // Enchantments
                new SonicShieldEnchantment(this),
                new MagnetEnchantment(this),
                new BaitEnchantment(this),
                new StrideEnchantment(this),
                // Enhanced Items
                new CompassHandler(this),
                new ClockHandler(this),
                new MapHandler(this),
                // Inventory Rules
                new BalancedKeepInventoryHandler(this),
                new InvulnerableKeepInventoryHandler(this),
                // Chairs
                new ChairsHandler(this),
                // Basket
                new BasketHandler(this),
                // Other
                new WeakerPhantomsHandler(this),
                new CreeperIgniterHandler(this),
                new CampfireHandler(this),
                new EnderPortalFrameHandler(this),
                new ArmorStandHandler(this),
                new SkullDataRecoveryHandler(this),
                new ReusableVaultsHandler(this),
                new DamageMultipliersHandler(this),
                new BreakingMultipliersHandler(this),
                new ImprovedEndPhantomsHandler(this),
                new WitherSpawnWhitelistHandler(this),
                new DimensionSoftLockHandler(this),
                new BalancedVillagerRestockHandler(this),
                new ColoredNametagsHandler(this),
                new BetterBoneMealHandler(this),
                // Items
                new ScrollItem(this),
                new EnderiteItem(this)
        );
        // Reloading configuration and shutting the server down in case it fails.
        if (this.onReload() == false)
            this.getServer().shutdown();
        // Initializing PacketEvents.
        PacketEvents.getAPI().init();
        // Creating RootCommandManager instance.
        this.commands = new RootCommandManager(this);
        // Registering command(s).
        commands.registerDependency(Tweaks.class, this);
        commands.registerCommand(TweaksCommand.class);
    }

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        // Loading PacketEvents.
        PacketEvents.getAPI().load();
    }

    @Override
    public void onDisable() {
        // Terminating PacketEvents.
        PacketEvents.getAPI().terminate();
    }

    @Override
    public boolean onReload() throws ConfigurationMappingException, IllegalStateException {
        try {
            // Ensuring configuration file(s) exist.
            final File config = ensureResourceExistence(this, new File(this.getDataFolder(), "config.json"));
            // Mapping configuration file(s).
            mapper.map(
                    ConfigurationHolder.of(PluginConfig.class, config)
            );
            // Reloading module(s).
            this.modules.forEach(Module::reload);
            // Returning true, as everything seemed to reload properly.
            return true;
        } catch (final IOException e) {
            this.getLogger().severe("Reloading of the plugin failed due to following error(s):");
            this.getLogger().severe(" (1) " + e.getClass().getSimpleName() + ": " + e.getMessage());
            if (e.getCause() != null)
                this.getLogger().severe(" (2) " + e.getCause().getClass().getSimpleName() + ": " + e.getCause().getMessage());
            // Returning false, as plugin has failed to reload.
            return false;
        }
    }

}
