package gecko10000.ResourceReset;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public class Listeners implements Listener {

    private final ResourceReset plugin;

    public Listeners(ResourceReset plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        List<String> disallowedCommands = plugin.getConfig().getStringList("disallowedCommands");
        boolean isDisallowedInResource = disallowedCommands.stream()
                .anyMatch(s -> event.getMessage().startsWith(s));
        Player player = event.getPlayer();
        if (plugin.isInResourceWorld(player) && isDisallowedInResource) {
            event.setCancelled(true);
            plugin.sendMessage(player, "&cYou can't run that command here.");
        }
    }

}
