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
import cloud.grabsky.tweaks.handlers.ArmorStandHandler;
import cloud.grabsky.tweaks.handlers.BalancedKeepInventoryHandler;
import cloud.grabsky.tweaks.handlers.BasketHandler;
import cloud.grabsky.tweaks.handlers.CampfireHandler;
import cloud.grabsky.tweaks.handlers.ChairsHandler;
import cloud.grabsky.tweaks.handlers.ClockHandler;
import cloud.grabsky.tweaks.handlers.CompassHandler;
import cloud.grabsky.tweaks.handlers.CreeperIgniterHandler;
import cloud.grabsky.tweaks.handlers.DamageMultipliersHandler;
import cloud.grabsky.tweaks.handlers.EnderPortalFrameHandler;
import cloud.grabsky.tweaks.handlers.InvulnerableKeepInventoryHandler;
import cloud.grabsky.tweaks.handlers.MapHandler;
import cloud.grabsky.tweaks.handlers.ReusableVaultsHandler;
import cloud.grabsky.tweaks.handlers.SkullDataRecoveryHandler;
import cloud.grabsky.tweaks.handlers.WeakerPhantomsHandler;
import com.github.retrooper.packetevents.PacketEvents;
import de.tr7zw.changeme.nbtapi.NBT;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static cloud.grabsky.configuration.paper.util.Resources.ensureResourceExistence;

public final class Tweaks extends TweaksBase implements Listener {

    private ConfigurationMapper mapper;
    private RootCommandManager commands;
    private List<Module> modules;

    @Override
    public void onEnable() {
        super.onEnable();
        // Creating ConfigurationMapper instance.
        this.mapper = PaperConfigurationMapper.create();
        // Adding module(s) to a list.
        this.modules = List.of(
                // Enchantments
                new SonicShieldEnchantment(this),
                new MagnetEnchantment(this),
                new BaitEnchantment(this),
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
                new DamageMultipliersHandler(this)
        );
        // Reloading configuration and shutting the server down in case it fails.
        if (this.onReload() == false)
            this.getServer().shutdown();
        // Preloading Item NBT API and shutting the server down in case of a failure.
        if (NBT.preloadApi() == false) {
            this.getLogger().severe("An error occurred while initializing NBT-API. Shutting down the server.");
            this.getServer().shutdown();
        }
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
