package io.graversen.fiber.utils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {
    private final String groupName;
    private final AtomicInteger currentThreadCount = new AtomicInteger(0);

    public NamedThreadFactory(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, getName());
    }

    private String getName() {
        return String.format("%s-Worker-%d", groupName, currentThreadCount.getAndIncrement());
    }
}
