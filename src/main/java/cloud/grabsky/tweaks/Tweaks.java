/*
 * MIT License
 *
 * Copyright (c) 2024 Grabsky <44530932+Grabsky@users.noreply.github.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * HORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
import cloud.grabsky.tweaks.enchantments.MagnetEnchantment;
import cloud.grabsky.tweaks.enchantments.SonicShieldEnchantment;
import cloud.grabsky.tweaks.handlers.CampfireRegenerationHandler;
import cloud.grabsky.tweaks.handlers.ClockHandler;
import cloud.grabsky.tweaks.handlers.CompassHandler;
import cloud.grabsky.tweaks.handlers.CreeperIgniterHandler;
import cloud.grabsky.tweaks.handlers.EnderPortalFrameHandler;
import cloud.grabsky.tweaks.handlers.InvulnerableKeepInventoryHandler;
import cloud.grabsky.tweaks.handlers.BalancedKeepInventoryHandler;
import cloud.grabsky.tweaks.handlers.MapHandler;
import cloud.grabsky.tweaks.handlers.WeakerPhantomsHandler;
import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static cloud.grabsky.configuration.paper.util.Resources.ensureResourceExistence;

public final class Tweaks extends BedrockPlugin implements Listener {

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
                new ClockHandler(this),
                new CompassHandler(this),
                new MapHandler(this),
                new MagnetEnchantment(this),
                new BalancedKeepInventoryHandler(this),
                new InvulnerableKeepInventoryHandler(this),
                new SonicShieldEnchantment(this),
                new WeakerPhantomsHandler(this),
                new CreeperIgniterHandler(this),
                new CampfireRegenerationHandler(this),
                new EnderPortalFrameHandler(this)
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
        // Applying settings to this PacketEvents API instance.
        PacketEvents.getAPI().getSettings().reEncodeByDefault(false).checkForUpdates(true);
        // Loading PacketHandlers.
        PacketEvents.getAPI().load();
    }

    @Override
    public void onDisable() {
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
