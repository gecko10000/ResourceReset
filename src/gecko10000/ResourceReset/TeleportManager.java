package gecko10000.ResourceReset;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class TeleportManager {

    private final ResourceReset plugin;
    private final Random random = new Random();

    public TeleportManager(ResourceReset plugin) {
        this.plugin = plugin;
    }

    void randomTeleport(Player player, String worldName) {
        World world = Bukkit.getWorld(plugin.getConfig().getString("worlds." + worldName + ".world"));
        if (world == null) {
            plugin.sendMessage(player, "&c" + worldName + " is not configured.");
            return;
        }
        int range = plugin.getConfig().getInt("worlds." + worldName + ".teleportRange");
        Location center = world.getWorldBorder().getCenter();
        int lowX = center.getBlockX() - range, highX = center.getBlockX() + range,
        lowZ = center.getBlockZ() - range, highZ = center.getBlockZ() + range;
        tryTeleport(player, world, lowX, highX, lowZ, highZ, plugin.getConfig().getInt("teleportAttempts"));
    }

    // recursively and asynchronously searches for a spot to teleport to
    private CompletableFuture<Boolean> tryTeleport(Player player, World world, int lowX, int highX, int lowZ, int highZ, int attempts) {
        if (attempts <= 0) return CompletableFuture.completedFuture(false);
        int x = random.nextInt(highX - lowX) + lowX;
        int z = random.nextInt(highZ - lowZ) + lowZ;
        return world.getChunkAtAsync(x, z)
                .thenCompose(c -> {
                    Block highest = world.getHighestBlockAt(x, z);
                    // don't go on bedrock roof
                    if (world.getEnvironment() == World.Environment.NETHER) {
                        // find first air
                        while (!highest.getType().isAir()
                                && inWorld(world, highest)) {
                            highest = highest.getRelative(BlockFace.DOWN);
                        }
                        // search for non-air with 2 air above
                        Block head = highest, feet = highest.getRelative(BlockFace.DOWN);
                        highest = feet.getRelative(BlockFace.DOWN);
                        while ((highest.getType().isAir() || !feet.getType().isAir() || !head.getType().isAir())
                            && inWorld(world, highest)) {
                            head = feet;
                            feet = highest;
                            highest = highest.getRelative(BlockFace.DOWN);
                        }
                    }
                    Material type = highest.getType();
                    if (!type.isSolid() || type == Material.MAGMA_BLOCK) {
                        return tryTeleport(player, world, lowX, highX, lowZ, highZ, attempts - 1);
                    }
                    Location destination = highest.getRelative(BlockFace.UP).getLocation().toCenterLocation();
                    destination.setPitch(player.getLocation().getPitch());
                    destination.setYaw(player.getLocation().getYaw());
                    return player.teleportAsync(destination);
                });
    }

    private boolean inWorld(World world, Block block) {
        return block.getY() >= world.getMinHeight();
    }

}
