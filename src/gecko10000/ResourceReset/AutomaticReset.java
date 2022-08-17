package gecko10000.ResourceReset;

import redempt.redlib.misc.Task;

import java.time.Duration;

public class AutomaticReset {

    private final ResourceReset plugin;

    private Task task;

    public AutomaticReset(ResourceReset plugin) {
        this.plugin = plugin;
    }

    public void restartTask() {
        if (task != null) task.cancel();
        long lastRegen = plugin.getConfig().getLong("lastRegen");
        long hoursDelay = plugin.getConfig().getLong("resetIntervalHours");
        long remainingMillis = lastRegen + Duration.ofHours(hoursDelay).toMillis() - System.currentTimeMillis();
        task = Task.syncDelayed(() -> {
            plugin.regenerateWorlds();
            restartTask();
        }, Math.max(remainingMillis / 50, 0));
    }

}