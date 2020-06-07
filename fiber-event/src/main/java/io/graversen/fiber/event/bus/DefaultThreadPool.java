package io.graversen.fiber.event.bus;

import io.graversen.fiber.utils.NamedThreadFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class DefaultThreadPool extends ThreadPoolExecutor {
    public DefaultThreadPool(String threadGroupName, int threadPoolSize) {
        this(threadPoolSize, threadGroupName);
    }

    public DefaultThreadPool(int poolSize, String threadGroupName) {
        super(poolSize, poolSize, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new NamedThreadFactory(threadGroupName));
        super.prestartAllCoreThreads();
    }
}
