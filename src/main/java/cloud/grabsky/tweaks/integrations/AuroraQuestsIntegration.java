package cloud.grabsky.tweaks.integrations;

import cloud.grabsky.tweaks.Tweaks;
import gg.auroramc.aurora.api.events.region.RegionBlockBreakEvent;
import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.quests.api.event.objective.PlayerLootEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;

public enum AuroraQuestsIntegration {
    INSTANCE; // SINGLETON

    private static boolean IS_INITIALIZED = false;

    public static void initialize(final @NotNull Tweaks plugin) {
        if (IS_INITIALIZED == false) {
            if (plugin.getServer().getPluginManager().getPlugin("AuroraQuests") != null) {
                // Marking the integration as initialized.
                IS_INITIALIZED = true;
            }
            // Logging warning and returning false if integration could not be initialized.
            plugin.getLogger().warning("AuroraQuests integration could not be initialized. (DEPENDENCY_NOT_ENABLED)");
            return;
        }
        // Logging warning and returning false if integration could not be initialized.
        plugin.getLogger().warning("AuroraQuests integration could not be initialized. (ALREADY_INITIALIZED)");
    }

    public static void progressFarm(final Player player, final Material material, final int amount) {
        if (IS_INITIALIZED == true)
            new PlayerLootEvent(player, TypeId.from(material), amount, PlayerLootEvent.Source.FARM).callEvent();
    }

    public static void progressBlockBreak(final Player player, final Block block) {
        if (IS_INITIALIZED == true)
            new RegionBlockBreakEvent(player, block, true).callEvent();
    }

}
