package gecko10000.ResourceReset;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import redempt.redlib.commandmanager.ArgType;
import redempt.redlib.commandmanager.CommandHook;
import redempt.redlib.commandmanager.CommandParser;

public class Commands {

    private final ResourceReset plugin;

    public Commands(ResourceReset plugin) {
        this.plugin = plugin;
        ArgType<String> resourceWorldType = new ArgType<>("resourceWorld", s -> {
            // check that config contains the world and it is therefore
            if (!nameInConfig(s)) {
                return null;
            }
            return s;
        }).tabStream(s -> plugin.getResourceWorldNames().stream());
        new CommandParser(plugin.getResource("command.rdcml"))
                .setArgTypes(resourceWorldType)
                .parse()
                .register("resource", this);
    }

    private boolean nameInConfig(String worldName) {
        return plugin.getConfig().getConfigurationSection("worlds")
                .getKeys(false).stream()
                .anyMatch(worldName::equals);
    }

    @CommandHook("rtp")
    public void rtp(CommandSender sender, String worldName, Player target) {
        if (target == null
            || (!sender.getName().equals(target.getName()) && !sender.hasPermission("resource.teleport.others"))) {
            plugin.sendMessage(sender, "&cInvalid player.");
            return;
        }
        plugin.manager.randomTeleport(target, worldName);
    }

    @CommandHook("reload")
    public void reload(CommandSender sender) {
        plugin.reload();
        plugin.sendMessage(sender, "&aConfig reloaded!");
    }

    @CommandHook("regenerate")
    public void regenerate(CommandSender sender) {
        plugin.regenerateWorlds();
    }
}
