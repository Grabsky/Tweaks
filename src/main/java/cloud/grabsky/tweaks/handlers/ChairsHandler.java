package cloud.grabsky.tweaks.handlers;

import cloud.grabsky.tweaks.Module;
import cloud.grabsky.tweaks.Tweaks;
import cloud.grabsky.tweaks.configuration.PluginConfig;
import cloud.grabsky.tweaks.utils.Extensions;
import io.papermc.paper.event.block.BlockBreakBlockEvent;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(Extensions.class)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class ChairsHandler implements Module, Listener {

    @Getter(AccessLevel.PUBLIC)
    public @NotNull Tweaks plugin;

    private static final NamespacedKey CHAIR_ENTITY = new NamespacedKey("tweaks", "is_chair");

    @Override
    public void load() {
        if (PluginConfig.ENABLED_MODULES_CHAIRS == true)
            // Registering events.
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void unload() {
        // Unregistering events.
        HandlerList.unregisterAll(this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(final @NotNull PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getHand() == EquipmentSlot.HAND && event.isBlockInHand() == false) {
            final @Nullable Block block = event.getClickedBlock();
            // Should never happen but we should satisfy code analyzer.
            if (block == null)
                return;
            // Checking if block is any stairs.
            if (Tag.STAIRS.isTagged(block.getType()) == true) {
                block.getWorld().spawnEntity(block.getLocation().toCenterLocation(), EntityType.BLOCK_DISPLAY, CreatureSpawnEvent.SpawnReason.CUSTOM, (it) -> {
                    it.getPersistentDataContainer().set(CHAIR_ENTITY, PersistentDataType.BYTE, (byte) 1);
                    it.setPersistent(false);
                    it.addPassenger(event.getPlayer());
                });
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(final @NotNull EntityTeleportEvent event) {
        if (event.getEntity() instanceof BlockDisplay display && display.getPersistentDataContainer().get(CHAIR_ENTITY, PersistentDataType.BYTE) != null && display.getPassengers().isEmpty() == false)
            event.getEntity().remove();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDismount(final EntityDismountEvent event) {
        if (event.getDismounted() instanceof BlockDisplay display && display.getPersistentDataContainer().get(CHAIR_ENTITY, PersistentDataType.BYTE) != null && display.getPassengers().isEmpty() == false) {
            final Location center = event.getDismounted().getLocation().toCenterLocation();
            event.getEntity().teleport(event.getEntity().getLocation().set(center.x(), center.y(), center.z()), PlayerTeleportEvent.TeleportCause.DISMOUNT);
            event.getDismounted().remove();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(final @NotNull BlockBreakEvent event) {
        final Location center = event.getBlock().getLocation().toCenterLocation();
        event.getBlock().getWorld().getNearbyEntities(center, 0.1, 0.1, 0.1).forEach(it -> {
            if (it instanceof BlockDisplay display && display.getPersistentDataContainer().get(CHAIR_ENTITY, PersistentDataType.BYTE) != null && display.getPassengers().isEmpty() == false)
                it.remove();
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(final @NotNull BlockBreakBlockEvent event) {
        final Location center = event.getBlock().getLocation().toCenterLocation();
        event.getBlock().getWorld().getNearbyEntities(center, 0.1, 0.1, 0.1).forEach(it -> {
            if (it instanceof BlockDisplay display && display.getPersistentDataContainer().get(CHAIR_ENTITY, PersistentDataType.BYTE) != null && display.getPassengers().isEmpty() == false)
                it.remove();
        });
    }

    // We could also handle explosions and 999 more events here, but since the entity is not saved to disk anyway, I don't think it's worth it...

}
