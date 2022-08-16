package gecko10000.ResourceReset;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.TriState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import redempt.redlib.misc.FormatUtils;
import redempt.redlib.misc.Task;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ResourceReset extends JavaPlugin {

    public final TeleportManager manager = new TeleportManager(this);

    @Override
    public void onEnable() {
        saveDefaultConfig();
        new Commands(this);
        new Listeners(this);
        reload();
    }

    public void reload() {
        saveDefaultConfig();
        reloadConfig();
        createWorlds();
    }

    private static final int DELAY = 5;

    public void regenerateWorlds() {
        Bukkit.broadcast(Component.text("Regenerating resource worlds in " + DELAY + " seconds! Prepare for a bit of lag.").color(NamedTextColor.RED));
        Task.syncDelayed(() -> {
            int[] order = {0};
            getResourceWorldNames().forEach(s -> {
                String worldName = getConfig().getString("worlds." + s + ".world");
                if (worldName == null) return;
                World world = Bukkit.getWorld(worldName);
                if (world == null) return;
                Task.syncDelayed(() -> regenerateWorld(world, s),
                        20L * DELAY * order[0]++);
            });
        }, 20 * DELAY);
    }

    private void regenerateWorld(World world, String displayName) {
        String name = world.getName();
        World.Environment type = world.getEnvironment();
        int borderSize = (int) world.getWorldBorder().getSize();
        World mainWorld = Bukkit.getWorlds().get(0);
        world.getPlayers().forEach(p -> p.teleport(mainWorld.getSpawnLocation()));
        Bukkit.unloadWorld(world, false);
        Task.asyncDelayed(() -> {
            world.getWorldFolder().delete();
            Task.syncDelayed(() -> {
                createWorld(name, type, borderSize);
                Bukkit.broadcast(Component.text("Regenerated " + displayName + ".").color(NamedTextColor.GREEN));
            });
        });
    }

    private void createWorlds() {
        for (String key : getResourceWorldNames()) {
            String worldName = getConfig().getString("worlds." + key + ".world");
            World.Environment environment = World.Environment.valueOf(getConfig().getString("worlds." + key + ".type"));
            int borderSize = getConfig().getInt("worlds." + key + ".size");
            createWorld(worldName, environment, borderSize);
        }
    }

    private void createWorld(String name, World.Environment e, int borderSize) {
        WorldCreator creator = new WorldCreator(name)
                .environment(e);
        creator.keepSpawnLoaded(TriState.FALSE);
        creator.createWorld().getWorldBorder().setSize(borderSize);
    }

    public Set<String> getResourceWorldNames() {
        return getConfig().getConfigurationSection("worlds").getKeys(false);
    }

    public boolean isInResourceWorld(Player player) {
        String playerWorld = player.getWorld().getName();
        return getResourceWorldNames().stream()
                .map(s -> getConfig().getString("worlds." + s + ".world"))
                .anyMatch(playerWorld::equals);
    }

    public void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(FormatUtils.color(message));
    }

}
