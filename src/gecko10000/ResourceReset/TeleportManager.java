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

    void randomTeleport(Player player, String worldType) {
        World world = Bukkit.getWorld(plugin.getConfig().getString(worldType + ".name", "resource"));
        if (world == null) {
            plugin.sendMessage(player, "&cThe " + worldType + " is not configured.");
            return;
        }
        int range = plugin.getConfig().getInt(worldType + ".teleportRange");
        Location center = world.getWorldBorder().getCenter();
        int lowX = center.getBlockX() - range, highX = center.getBlockX() + range,
        lowZ = center.getBlockZ() - range, highZ = center.getBlockZ() + range;
        tryTeleport(player, world, lowX, highX, lowZ, highZ, plugin.getConfig().getInt("teleportAttempts"));
    }

    private CompletableFuture<Boolean> tryTeleport(Player player, World world, int lowX, int highX, int lowZ, int highZ, int attempts) {
        if (attempts <= 0) return CompletableFuture.completedFuture(false);
        int x = random.nextInt(highX - lowX) + lowX;
        int z = random.nextInt(highZ - lowZ) + lowZ;
        return world.getChunkAtAsync(x, z)
                .thenCompose(c -> {
                    Block highest = world.getHighestBlockAt(x, z);
                    Material type = highest.getType();
                    if (!type.isSolid() || type == Material.MAGMA_BLOCK) {
                        return tryTeleport(player, world, lowX, highX, lowZ, highZ, attempts - 1);
                    }
                    return player.teleportAsync(highest.getRelative(BlockFace.UP).getLocation());
                });
    }

}
