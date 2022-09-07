package gecko10000.ResourceReset;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class Placeholder extends PlaceholderExpansion {

    private final ResourceReset plugin;

    public Placeholder(ResourceReset plugin) {
        this.plugin = plugin;
        this.register();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "resource";
    }

    @Override
    public @NotNull String getAuthor() {
        return "gecko10000";
    }

    @Override
    public @NotNull String getVersion() {
        return "0.1";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (!params.equals("next_reset")) return null;
        return "" + plugin.automaticReset.remainingMilliseconds() / 1000;
    }

}
