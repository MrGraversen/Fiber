package io.graversen.fiber.server.async;

import java.util.concurrent.ThreadFactory;

public final class DefaultThreadFactory implements ThreadFactory
{
    private final String groupName;
    private int threadCount;

    public DefaultThreadFactory(String groupName)
    {
        this.groupName = groupName;
        this.threadCount = 0;
    }

    @Override
    public Thread newThread(Runnable r)
    {
        threadCount++;
        return new Thread(r, getName());
    }

    private String getName()
    {
        return String.format("%s-Worker-%d", groupName, threadCount);
    }
}
