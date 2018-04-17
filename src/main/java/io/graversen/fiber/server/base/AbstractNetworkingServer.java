package io.graversen.fiber.server.base;

import org.ownzone.lib.network.event.EventBus;
import org.ownzone.lib.network.server.management.AbstractNetworkClientManager;

public abstract class AbstractNetworkingServer
{
    protected final AbstractNetworkClientManager networkClientManager;
    protected final EventBus eventBus;

    public AbstractNetworkingServer(AbstractNetworkClientManager networkClientManager, EventBus eventBus)
    {
        this.networkClientManager = networkClientManager;
        this.eventBus = eventBus;
    }
}
