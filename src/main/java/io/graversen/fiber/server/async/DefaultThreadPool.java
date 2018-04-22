package io.graversen.fiber.server.async;

import io.graversen.fiber.util.Environment;

import java.util.concurrent.*;

public final class DefaultThreadPool extends ThreadPoolExecutor
{
    public DefaultThreadPool(String threadGroupName)
    {
        this(Environment.availableProcessors(), threadGroupName);
    }

    public DefaultThreadPool(int poolSize, String threadGroupName)
    {
        super(poolSize, poolSize, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new DefaultThreadFactory(threadGroupName));
        super.prestartAllCoreThreads();
    }
}
