package gecko10000.ResourceReset;

import redempt.redlib.misc.Task;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AutomaticReset {

    private final ResourceReset plugin;

    public ScheduledFuture<?> task = null;
    private ScheduledExecutorService service = Executors.newScheduledThreadPool(1);

    public AutomaticReset(ResourceReset plugin) {
        this.plugin = plugin;
    }

    public void restartTask() {
        if (task != null) task.cancel(false);
        task = service.scheduleAtFixedRate(plugin::regenerateWorlds,
                remainingMilliseconds(),
                TimeUnit.HOURS.toMillis(plugin.getConfig().getLong("resetIntervalHours")),
                TimeUnit.MILLISECONDS);
    }

    public long remainingMilliseconds() {
        long lastRegen = plugin.getConfig().getLong("lastRegen");
        long hoursDelay = plugin.getConfig().getLong("resetIntervalHours");
        long remaining = lastRegen + Duration.ofHours(hoursDelay).toMillis() - System.currentTimeMillis();
        return Math.max(remaining, 0);
    }

}
