package cloud.grabsky.tweaks.handlers;

import cloud.grabsky.tweaks.Module;
import cloud.grabsky.tweaks.Tweaks;
import cloud.grabsky.tweaks.configuration.PluginConfig;
import cloud.grabsky.tweaks.utils.Extensions;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(Extensions.class)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class BetterBoneMealHandler implements Module, Listener {

    @Getter(AccessLevel.PUBLIC)
    public @NotNull Tweaks plugin;

    @Override
    public void load() {
        if (PluginConfig.ENABLED_MODULES_BETTER_BONE_MEAL == true)
            // Registering events.
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void unload() {
        // Unregistering events.
        HandlerList.unregisterAll(this);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBoneMealUse(final @NotNull PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getHand() == EquipmentSlot.HAND && event.getItem() != null && event.getItem().getType() == Material.BONE_MEAL) {
            final @Nullable Block block = event.getClickedBlock();
            if (block != null) {
                if (block.getType() == Material.SUGAR_CANE) {
                    // Getting the lowest and highest blocks of this plant.
                    final Block lowest = getLastRelativeOf(block, BlockFace.DOWN);
                    final Block highest = getLastRelativeOf(block, BlockFace.UP);
                    // Returning if plant is exceeding the it's maximum height.
                    if (highest.getY() - lowest.getY() >= 2)
                        return;
                    // Getting the block at which the sugar cane is about extend upon.
                    final Block newBlock = highest.getRelative(BlockFace.UP);
                    // Returning if block above is not empty.
                    if (newBlock.isEmpty() == false)
                        return;
                    // Removing 1x Bone Meal from the stack.
                    if (event.getPlayer().getGameMode() != GameMode.CREATIVE)
                        event.getPlayer().getInventory().getItemInMainHand().setAmount(event.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
                    // Attempting to grow the plant with 50% chance.
                    if (Math.random() > 0.5) {
                        newBlock.setType(Material.SUGAR_CANE);
                        // Playing growth sound.
                        block.getWorld().playSound(newBlock.getLocation().toCenterLocation(), Sound.BLOCK_GRASS_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    }
                    // Spawning particles and playing sound.
                    block.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, block.getLocation().toCenterLocation(), 15, 0.25, 0.25, 0.25);
                    block.getWorld().playSound(block.getLocation().toCenterLocation(), Sound.ITEM_BONE_MEAL_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                } else if (block.getType() == Material.CACTUS) {
                    // Getting the lowest and highest blocks of this plant.
                    final Block lowest = getLastRelativeOf(block, BlockFace.DOWN);
                    final Block highest = getLastRelativeOf(block, BlockFace.UP);
                    // Returning if plant is exceeding the it's maximum height.
                    if (highest.getY() - lowest.getY() >= 2)
                        return;
                    // Getting the block at which the cactus is about extend upon.
                    final Block newBlock = highest.getRelative(BlockFace.UP);
                    // Returning if block above is not empty.
                    if (newBlock.isEmpty() == false)
                        return;
                    // Returning if neighbour blocks are not empty.
                    if (newBlock.getRelative(BlockFace.NORTH).isEmpty() == false || newBlock.getRelative(BlockFace.EAST).isEmpty() == false || newBlock.getRelative(BlockFace.SOUTH).isEmpty() == false || newBlock.getRelative(BlockFace.WEST).isEmpty() == false)
                        return;
                    // Removing 1x Bone Meal from the stack.
                    if (event.getPlayer().getGameMode() != GameMode.CREATIVE)
                        event.getPlayer().getInventory().getItemInMainHand().setAmount(event.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
                    // Attempting to grow the plant with 50% chance.
                    if (Math.random() > 0.5) {
                        newBlock.setType(Material.CACTUS);
                        // Playing growth sound.
                        block.getWorld().playSound(newBlock.getLocation().toCenterLocation(), Sound.BLOCK_WOOL_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    // Showing particles that would otherwise be covered by the block.
                    } else {
                        block.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, block.getLocation().toCenterLocation().add(0.0, 0.55, 0.0), 10, 0.25, 0.0, 0.25);
                    }
                    // Spawning particles and playing sound.
                    block.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, block.getLocation().toCenterLocation().add(0.55, 0.0, 0.0), 10, 0.0, 0.25, -0.25);
                    block.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, block.getLocation().toCenterLocation().add(-0.55, 0.0, 0.0), 10, 0.0, 0.25, 0.25);
                    block.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, block.getLocation().toCenterLocation().add(0.0, 0.0, 0.55), 10, -0.25, 0.25, 0.0);
                    block.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, block.getLocation().toCenterLocation().add(0.0, 0.0, -0.55), 10, 0.25, 0.25, 0.0);

                    block.getWorld().playSound(block.getLocation().toCenterLocation(), Sound.ITEM_BONE_MEAL_USE, 1.0F, 1.0F);
                } else if (shouldDuplicate(block) == true) {
                    // Removing one Bone Meal from the player's hand.
                    if (event.getPlayer().getGameMode() != GameMode.CREATIVE)
                        event.getPlayer().getInventory().getItemInMainHand().setAmount(event.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
                    // Duplicating the item and dropping it on the ground.
                    block.getDrops().forEach(drop -> block.getLocation().getWorld().dropItemNaturally(block.getLocation().toCenterLocation(), drop));
                    // Spawning particles and playing sound.
                    block.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, block.getLocation().toCenterLocation(), 15, 0.25, 0.25, 0.25);
                    block.getWorld().playSound(block.getLocation().toCenterLocation(), Sound.ITEM_BONE_MEAL_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                }
            }
        }
    }

    private static @NotNull Block getLastRelativeOf(final @NotNull Block block, final @NotNull BlockFace face) {
        // Preparing the result.
        Block result = block;
        // Checking blocks below.
        while (result.getRelative(face).getType() == block.getType())
            result = result.getRelative(face);
        // Returning the result.
        return result;
    }

    // Returns true if specified block can be "duplicated" with bone meal.
    private static boolean shouldDuplicate(final @NotNull Block block) {
        return switch (block.getType()) {
            case // FLOWERS
                 Material.DANDELION,
                 Material.POPPY,
                 Material.BLUE_ORCHID,
                 Material.ALLIUM,
                 Material.AZURE_BLUET,
                 Material.RED_TULIP,
                 Material.ORANGE_TULIP,
                 Material.WHITE_TULIP,
                 Material.PINK_TULIP,
                 Material.OXEYE_DAISY,
                 Material.CORNFLOWER,
                 Material.LILY_OF_THE_VALLEY,
                 Material.TORCHFLOWER,
                 // LILY PAD
                 Material.LILY_PAD -> true;
            default -> false;
        };

    }

}
