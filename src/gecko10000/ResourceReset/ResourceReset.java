package gecko10000.ResourceReset;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import redempt.redlib.misc.FormatUtils;

public class ResourceReset extends JavaPlugin {

    public final TeleportManager manager = new TeleportManager(this);

    @Override
    public void onEnable() {
        saveDefaultConfig();
        new Commands(this);
    }

    public void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(FormatUtils.color(message));
    }

}
