package gecko10000.ResourceReset;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.TriState;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import redempt.redlib.misc.FormatUtils;
import redempt.redlib.misc.Task;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ResourceReset extends JavaPlugin {

    public final TeleportManager manager = new TeleportManager(this);
    private AutomaticReset automaticReset;

    @Override
    public void onEnable() {
        automaticReset = new AutomaticReset(this);
        reload();
        new Commands(this);
        new Listeners(this);
    }

    public void reload() {
        saveDefaultConfig();
        reloadConfig();
        automaticReset.restartTask();
        Task.syncDelayed(this::createWorlds);
    }

    private static final int DELAY = 5;

    public void regenerateWorlds() {
        Bukkit.broadcast(Component.text("Regenerating resource worlds in " + DELAY + " seconds! Prepare for a bit of lag.").color(NamedTextColor.RED));
        getConfig().set("lastRegen", System.currentTimeMillis());
        saveConfig();
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
            recursiveDelete(world.getWorldFolder());
            Task.syncDelayed(() -> {
                createWorld(name, type, borderSize);
                Bukkit.broadcast(Component.text("Regenerated " + displayName + ".").color(NamedTextColor.GREEN));
            });
        });
    }

    private void recursiveDelete(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                recursiveDelete(child);
            }
        }
        file.delete();
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
        if (Bukkit.getWorld(name) != null) return;
        WorldCreator creator = new WorldCreator(name)
                .environment(e);
        creator.keepSpawnLoaded(TriState.FALSE);
        World world = creator.createWorld();
        world.getWorldBorder().setSize(borderSize);
        world.setGameRule(GameRule.KEEP_INVENTORY, getConfig().getBoolean("gamerules.keepInventory"));
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
