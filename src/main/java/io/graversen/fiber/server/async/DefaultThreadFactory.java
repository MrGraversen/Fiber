package io.graversen.fiber.server.async;

import java.util.concurrent.ThreadFactory;

public final class DefaultThreadFactory implements ThreadFactory
{
    private final String groupName;

    private int currentThreadCount;

    public DefaultThreadFactory(String groupName)
    {
        this.groupName = groupName;
        this.currentThreadCount = 0;
    }

    @Override
    public Thread newThread(Runnable r)
    {
        currentThreadCount++;
        return new Thread(r, getName());
    }

    private String getName()
    {
        return String.format("%s-Worker-%d", groupName, currentThreadCount);
    }
}
