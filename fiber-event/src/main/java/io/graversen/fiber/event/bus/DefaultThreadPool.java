package io.graversen.fiber.event.bus;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class DefaultThreadPool extends ThreadPoolExecutor {
    public DefaultThreadPool(String threadGroupName, int threadPoolSize) {
        this(threadPoolSize, threadGroupName);
    }

    public DefaultThreadPool(int poolSize, String threadGroupName) {
        super(poolSize, poolSize, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new DefaultThreadFactory(threadGroupName));
        super.prestartAllCoreThreads();
    }
}
