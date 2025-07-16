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
import cloud.grabsky.tweaks.enchantments.GardenerEnchantment;
import cloud.grabsky.tweaks.enchantments.MagnetEnchantment;
import cloud.grabsky.tweaks.enchantments.SonicShieldEnchantment;
import cloud.grabsky.tweaks.enchantments.StrideEnchantment;
import cloud.grabsky.tweaks.handlers.ArmorStandHandler;
import cloud.grabsky.tweaks.handlers.BalancedKeepInventoryHandler;
import cloud.grabsky.tweaks.handlers.BalancedVillagerRestockHandler;
import cloud.grabsky.tweaks.handlers.BetterBoneMealHandler;
import cloud.grabsky.tweaks.handlers.BreakingMultipliersHandler;
import cloud.grabsky.tweaks.handlers.CampfireHandler;
import cloud.grabsky.tweaks.handlers.ChairsHandler;
import cloud.grabsky.tweaks.handlers.ClockHandler;
import cloud.grabsky.tweaks.handlers.ColoredNametagsHandler;
import cloud.grabsky.tweaks.handlers.CompassHandler;
import cloud.grabsky.tweaks.handlers.CreeperIgniterHandler;
import cloud.grabsky.tweaks.handlers.DamageMultipliersHandler;
import cloud.grabsky.tweaks.handlers.DimensionSoftLockHandler;
import cloud.grabsky.tweaks.handlers.EnderPortalFrameHandler;
import cloud.grabsky.tweaks.handlers.ForeverYoungHandler;
import cloud.grabsky.tweaks.handlers.ImprovedEndPhantomsHandler;
import cloud.grabsky.tweaks.handlers.InvulnerableKeepInventoryHandler;
import cloud.grabsky.tweaks.handlers.MapHandler;
import cloud.grabsky.tweaks.handlers.ReusableVaultsHandler;
import cloud.grabsky.tweaks.handlers.SkullDataRecoveryHandler;
import cloud.grabsky.tweaks.handlers.WeakerPhantomsHandler;
import cloud.grabsky.tweaks.handlers.WitherSpawnWhitelistHandler;
import cloud.grabsky.tweaks.items.BasketHandler;
import cloud.grabsky.tweaks.items.EnderiteItem;
import cloud.grabsky.tweaks.items.ScrollItem;
import com.github.retrooper.packetevents.PacketEvents;
import com.google.gson.Gson;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
                new GardenerEnchantment(this),
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
                new ForeverYoungHandler(this),
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

    /* PLUGIN LOADER; FOR USE WITH PLUGIN-YML FOR GRADLE */

    @SuppressWarnings("UnstableApiUsage")
    public static final class PluginLoader implements io.papermc.paper.plugin.loader.PluginLoader {

        @Override
        public void classloader(final @NotNull PluginClasspathBuilder classpathBuilder) throws IllegalStateException {
            final MavenLibraryResolver resolver = new MavenLibraryResolver();
            // Parsing the file.
            try (final InputStream in = getClass().getResourceAsStream("/paper-libraries.json")) {
                final PluginLibraries libraries = new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), PluginLibraries.class);
                // Adding repositories to the maven library resolver.
                libraries.asRepositories().forEach(resolver::addRepository);
                // Adding dependencies to the maven library resolver.
                libraries.asDependencies().forEach(resolver::addDependency);
                // Adding library resolver to the classpath builder.
                classpathBuilder.addLibrary(resolver);
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            }
        }

        @RequiredArgsConstructor(access = AccessLevel.PUBLIC)
        private static class PluginLibraries {

            private final Map<String, String> repositories;
            private final List<String> dependencies;

            public Stream<RemoteRepository> asRepositories() {
                return repositories.entrySet().stream().map(entry -> {
                    try {
                        // Replacing Maven Central repository with a pre-configured mirror.
                        // See: https://docs.papermc.io/paper/dev/getting-started/paper-plugins/#loaders
                        if (entry.getValue().contains("maven.org") == true || entry.getValue().contains("maven.apache.org") == true) {
                            return new RemoteRepository.Builder(entry.getKey(), "default", MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR).build();
                        }
                        return new RemoteRepository.Builder(entry.getKey(), "default", entry.getValue()).build();
                    } catch (final NoSuchFieldError e) {
                        return new RemoteRepository.Builder(entry.getKey(), "default", "https://maven-central.storage-download.googleapis.com/maven2").build();
                    }
                });
            }

            public Stream<Dependency> asDependencies() {
                return dependencies.stream().map(value -> new Dependency(new DefaultArtifact(value), null));
            }
        }
    }

}
