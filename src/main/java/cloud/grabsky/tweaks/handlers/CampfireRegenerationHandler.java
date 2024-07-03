package cloud.grabsky.tweaks.handlers;

import cloud.grabsky.tweaks.Module;
import cloud.grabsky.tweaks.Tweaks;
import cloud.grabsky.tweaks.configuration.PluginConfig;
import cloud.grabsky.tweaks.utils.Extensions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.data.type.Campfire;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.scheduler.BukkitTask;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(Extensions.class)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class CampfireRegenerationHandler implements Module, Listener {

    @Getter(AccessLevel.PUBLIC)
    public @NotNull Tweaks plugin;

    private @Nullable BukkitTask task = null;

    @Override
    public void load() {
        // Clearing handlers list.
        HandlerList.unregisterAll(this);
        // Cancelling current task in case it already exist.
        if (this.task != null)
            this.task.cancel();
        // Returning in case module is disabled.
        if (PluginConfig.ENABLED_MODULES_CAMPFIRE_REGENERATION == false)
            return;
        // Scheduling the task.
        this.task = plugin.getBedrockScheduler().repeat(0L, 200L, Long.MAX_VALUE, (_) -> {
            Bukkit.getOnlinePlayers().forEach(player -> {
                // Skipping invulnerable players or players that are not in range of any campfire.
                if (player.isInvulnerable() == true || isCampfireNearby(player.getLocation(), 3) == false)
                    return;
                // Healing player by 1 HP.
                player.heal(1.0, EntityRegainHealthEvent.RegainReason.CUSTOM);
            });
            return true;
        });
    }

    @SuppressWarnings("UnstableApiUsage")
    private boolean isCampfireNearby(final @NotNull Location location, final int radius) {
        for (int x = location.blockX() - radius; x <= location.blockX() + radius; x++) {
            for (int y = location.blockY() - radius; y <= location.blockY() + radius; y++) {
                for (int z = location.blockZ() - radius; z <= location.blockZ() + radius; z++) {
                    if (location.getWorld().getBlockData(x, y, z) instanceof Campfire campfire && campfire.isLit() == true)
                        return true;
                }
            }
        }
        return false;
    }

    @Override
    public void unload() {

    }

}