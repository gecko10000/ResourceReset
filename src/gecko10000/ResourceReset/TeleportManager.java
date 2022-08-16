package gecko10000.ResourceReset;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import redempt.redlib.misc.Task;

import java.time.Duration;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;

public class TeleportManager {

    private static final Duration TICK = Duration.ofMillis(50);

    private final ResourceReset plugin;
    private final Random random = new Random();

    public TeleportManager(ResourceReset plugin) {
        this.plugin = plugin;
    }

    Set<UUID> teleporting = new ConcurrentSkipListSet<>();

    void randomTeleport(Player player, String worldDisplayName) {
        UUID uuid = player.getUniqueId();
        if (teleporting.contains(uuid)) {
            plugin.sendMessage(player, "&cYou are already teleporting.");
            return;
        }
        if (worldDisplayName == null) {
            worldDisplayName = plugin.getResourceWorldNames().stream().findFirst().orElseThrow();
        }
        World world = Bukkit.getWorld(plugin.getConfig().getString("worlds." + worldDisplayName + ".world"));
        if (world == null) {
            plugin.sendMessage(player, "&c" + worldDisplayName + " is not configured.");
            return;
        }
        int range = plugin.getConfig().getInt("worlds." + worldDisplayName + ".teleportRange");
        Location center = world.getWorldBorder().getCenter();
        int lowX = center.getBlockX() - range, highX = center.getBlockX() + range,
        lowZ = center.getBlockZ() - range, highZ = center.getBlockZ() + range;
        int attempts = plugin.getConfig().getInt("teleportAttempts");
        int[] secondsElapsed = {0};
        teleporting.add(uuid);
        CompletableFuture<Void> teleportFuture = tryTeleport(player, world, lowX, highX, lowZ, highZ, attempts)
                .thenAccept(b -> {
                    plugin.sendMessage(player, b ? "&aFound a spot in " + secondsElapsed[0] + " seconds." : "&cCould not find a safe spot after " + attempts + " attempts.");
                    teleporting.remove(uuid);
                });
        Task.asyncRepeating(task -> {
            if (secondsElapsed[0] >= 60) {
                teleportFuture.cancel(true);
                plugin.sendMessage(player, "&cTimed out.");
                task.cancel();
                return;
            }
            if (teleportFuture.isDone()) {
                task.cancel();
                return;
            }
            player.showTitle(Title.title(titleComp("Searching..."),
                    titleComp(++secondsElapsed[0] + "s"),
                    Title.Times.times(Duration.ZERO, TICK.multipliedBy(70), TICK.multipliedBy(20))));
        }, 0, 20);
    }

    private Component titleComp(String content) {
        return Component.text(content).color(NamedTextColor.YELLOW);
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
