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
        long remainingMillis = remainingMilliseconds();
        task = Task.asyncDelayed(() -> {
            plugin.regenerateWorlds();
            restartTask();
        }, Math.max(remainingMillis / 50, 0));
    }

    public long remainingMilliseconds() {
        long lastRegen = plugin.getConfig().getLong("lastRegen");
        long hoursDelay = plugin.getConfig().getLong("resetIntervalHours");
        return lastRegen + Duration.ofHours(hoursDelay).toMillis() - System.currentTimeMillis();
    }

}
