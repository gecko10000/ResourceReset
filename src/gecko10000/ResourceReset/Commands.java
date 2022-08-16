package gecko10000.ResourceReset;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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

    @CommandHook("overworld")
    public void overworld(CommandSender sender, Player target) {
        if (target == null) {
            plugin.sendMessage(sender, "&cInvalid player.");
        }
        plugin.manager.randomTeleport(target, "overworld");
    }

    @CommandHook("nether")
    public void nether(CommandSender sender, Player target) {

    }

    @CommandHook("end")
    public void end(CommandSender sender, Player target) {

    }

    @CommandHook("reload")
    public void reload(CommandSender sender) {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        plugin.sendMessage(sender, "&aConfig reloaded!");
    }
}
