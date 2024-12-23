package cloud.grabsky.tweaks;

import cloud.grabsky.bedrock.BedrockPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import revxrsal.zapper.DependencyManager;
import revxrsal.zapper.RuntimeLibPluginConfiguration;
import revxrsal.zapper.classloader.URLClassLoaderWrapper;
import revxrsal.zapper.util.ClassLoaderReader;

import java.io.File;
import java.net.URLClassLoader;

public abstract class TweaksBase extends BedrockPlugin {

    static {
        final RuntimeLibPluginConfiguration config = RuntimeLibPluginConfiguration.parse();
        final File libraries = new File(ClassLoaderReader.getDataFolder(TweaksBase.class), config.getLibsFolder());
        if (libraries.exists() == false) {
            PluginDescriptionFile pdf = ClassLoaderReader.getDescription(TweaksBase.class);
            Bukkit.getLogger().info("[" + pdf.getName() + "] It appears you're running " + pdf.getName() + " for the first time.");
            Bukkit.getLogger().info("[" + pdf.getName() + "] Please give me a few seconds to install dependencies. This is a one-time process.");
        }
        DependencyManager dependencyManager = new DependencyManager(
                libraries,
                URLClassLoaderWrapper.wrap((URLClassLoader) TweaksBase.class.getClassLoader())
        );
        config.getDependencies().forEach(dependencyManager::dependency);
        config.getRepositories().forEach(dependencyManager::repository);
        config.getRelocations().forEach(dependencyManager::relocate);
        dependencyManager.load();
    }

}
