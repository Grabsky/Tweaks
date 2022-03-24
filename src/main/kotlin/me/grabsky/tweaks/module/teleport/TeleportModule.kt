package me.grabsky.tweaks.module.teleport

import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent
import me.grabsky.indigo.extensions.sendMessageOrIgnore
import me.grabsky.indigo.utils.Sounds
import me.grabsky.tweaks.Tweaks
import me.grabsky.tweaks.module.PluginModule
import org.bukkit.Material
import org.bukkit.block.data.type.RespawnAnchor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerBedEnterEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerRespawnEvent

class TeleportModule(tweaks: Tweaks) : PluginModule(tweaks) {
    override fun onModuleEnable() {
        // Registering events for this module
        tweaks.server.pluginManager.registerEvents(ModuleListener(), tweaks)
    }

    override fun onModuleDisable() {
        // Unregistering events registered by module
        HandlerList.unregisterAll(ModuleListener())
    }

    inner class ModuleListener : Listener {
        // Disables possibility for players to override their spawn location
        @EventHandler(priority = EventPriority.LOWEST)
        fun onSpawnSet(event: PlayerSetSpawnEvent) {
            if (event.cause == PlayerSetSpawnEvent.Cause.PLUGIN) return
            event.isCancelled = true
        }

        // Handles player respawn; player only respawn
        @EventHandler
        fun onPlayerRespawn(event: PlayerRespawnEvent) {
            event.respawnLocation = event.player.location.world.spawnLocation
            //
        }

        // Disables clicking on Respawn Anchor and opens a menu teleport menu instead
        @EventHandler
        fun onAnchorInteract(event: PlayerInteractEvent) {
            // Skipping for blocks other than Respawn Anchor
            if (event.clickedBlock?.type != Material.RESPAWN_ANCHOR) return
            if (event.action == Action.RIGHT_CLICK_BLOCK || (event.item?.type == Material.AIR && !event.player.isSneaking)) {
                val anchorBlockData = event.clickedBlock!!.blockData as RespawnAnchor
                if (anchorBlockData.charges == anchorBlockData.maximumCharges) {
                    event.isCancelled = true
                    // Handle spawn override
                }
            }
        }

        @EventHandler
        fun onBedInteract(event: PlayerBedEnterEvent) {
            //
        }

        @EventHandler
        fun onAnchorPlace(event: BlockPlaceEvent) {
            // Skipping invalid actions
            if (event.isCancelled || !event.canBuild()) return
            // Checking if placed block is Respawn Anchor
            if (event.blockPlaced.type == Material.RESPAWN_ANCHOR) {
                // Getting BlockData and casting it to RespawnAnchor(Data)
                val anchorBlockData = event.blockPlaced.blockData as RespawnAnchor
                // Fully charging the block
                anchorBlockData.charges = anchorBlockData.maximumCharges
                // Setting BlockData
                event.blockPlaced.blockData = anchorBlockData
                // Playing sound
                val loc = event.blockPlaced.location
                event.blockPlaced.world.playSound(Sounds.of("block.respawn_anchor.set_spawn"), loc.x, loc.y, loc.z)
                // Sending a message
                event.player.sendMessageOrIgnore("<gold>FD<dark_gray> » <gray>Teleport został utworzony. Naciśnij <gold>PPM<gray> na blok, aby otworzyć menu teleportacji.")
            }
        }
    }
}