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
        new CommandParser(plugin.getResource("command.rdcml"))
                .parse()
                .register("resource", this);
    }

    private boolean nameInConfig(String worldName) {
        return plugin.getConfig().getConfigurationSection("worlds")
                .getKeys(false).stream()
                .anyMatch(worldName::equals);
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
