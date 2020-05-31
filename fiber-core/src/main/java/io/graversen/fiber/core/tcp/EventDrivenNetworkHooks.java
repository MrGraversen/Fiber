package io.graversen.fiber.core.tcp;

import io.graversen.fiber.core.hooks.*;
import io.graversen.fiber.core.tcp.events.ClientConnectedEvent;
import io.graversen.fiber.core.tcp.events.NetworkReadEvent;
import io.graversen.fiber.core.tcp.events.NetworkWriteEvent;
import io.graversen.fiber.event.bus.IEventBus;
import io.graversen.fiber.utils.Checks;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventDrivenNetworkHooks implements INetworkHooks<ITcpNetworkClient> {
    private final IEventBus eventBus;

    public EventDrivenNetworkHooks(IEventBus eventBus) {
        this.eventBus = Checks.nonNull(eventBus, "eventBus");
    }

    @Override
    public void onNetworkRead(NetworkRead<ITcpNetworkClient> networkRead) {
        final var event = new NetworkReadEvent(networkRead.getClient(), networkRead.getNetworkMessage());
        eventBus.emitEvent(event);
    }

    @Override
    public void onNetworkWrite(NetworkWrite<ITcpNetworkClient> networkWrite) {
        final var event = new NetworkWriteEvent(networkWrite.getClient(), networkWrite.getNetworkMessage());
        eventBus.emitEvent(event);
    }

    @Override
    public void onClientConnected(ClientConnected<ITcpNetworkClient> clientConnected) {
        final var event = new ClientConnectedEvent(clientConnected.getClient());
        eventBus.emitEvent(event);
    }

    @Override
    public void onClientDisconnected(ClientDisconnected<ITcpNetworkClient> clientDisconnected) {
        final var event = new ClientConnectedEvent(clientDisconnected.getClient());
        eventBus.emitEvent(event);
    }
}
