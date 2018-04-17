package io.graversen.fiber.server.async;

import java.util.concurrent.ThreadFactory;

public final class DefaultThreadFactory implements ThreadFactory
{
    private final String groupName;

    public DefaultThreadFactory(String groupName)
    {
        this.groupName = groupName;
    }

    @Override
    public Thread newThread(Runnable r)
    {
        return new Thread(r, getName());
    }

    private String getName()
    {
        return String.format("%s-Worker", groupName);
    }
}
