package io.graversen.fiber.utils;

import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class Tasks {
    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Tasks"));

    public static void after(Duration duration, Runnable task) {
        executor.schedule(task, duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    public static void interval(Duration interval, Runnable task) {
        executor.scheduleAtFixedRate(task, interval.toMillis(), interval.toMillis(), TimeUnit.MILLISECONDS);
    }

    public static void stop() {
        executor.shutdownNow();
    }
}
