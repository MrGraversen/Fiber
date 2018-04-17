package io.graversen.fiber.server.async;

import org.ownzone.lib.network.util.Environment;

import java.util.concurrent.ScheduledThreadPoolExecutor;

public final class DefaultThreadPool extends ScheduledThreadPoolExecutor
{
    public DefaultThreadPool(String threadGroupName)
    {
        this(Environment.availableProcessors(), threadGroupName);
    }

    public DefaultThreadPool(int poolSize, String threadGroupName)
    {
        super(poolSize, new DefaultThreadFactory(threadGroupName));
    }
}
