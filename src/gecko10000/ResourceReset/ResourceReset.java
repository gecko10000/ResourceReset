package gecko10000.ResourceReset;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.TriState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import redempt.redlib.misc.FormatUtils;
import redempt.redlib.misc.Task;

import java.io.File;
import java.util.List;
import java.util.Set;

public class ResourceReset extends JavaPlugin {

    public final TeleportManager manager = new TeleportManager(this);

    @Override
    public void onEnable() {
        saveDefaultConfig();
        new Commands(this);
        reload();
    }

    public void reload() {
        saveDefaultConfig();
        reloadConfig();
        createWorlds();
    }

    /*public void regenerateWorlds() {
        regenerateWorld(Bukkit.getWorld(getConfig().getString("worlds.overworld.name")));
        regenerateWorld(Bukkit.getWorld(getConfig().getString("worlds.nether.name")));
        regenerateWorld(Bukkit.getWorld(getConfig().getString("worlds.end.name")));
    }

    private void regenerateWorld(World world) {
        String name = world.getName();
        World.Environment type = world.getEnvironment();
        Bukkit.unloadWorld(world, false);
        Task.asyncDelayed(() -> {
            world.getWorldFolder().delete();
            Task.syncDelayed(() -> createWorld(name, type));
        });
    }*/

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

    public void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(FormatUtils.color(message));
    }

}
