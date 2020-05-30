package io.graversen.fiber.core.tcp;

import io.graversen.fiber.core.hooks.*;
import io.graversen.fiber.event.bus.IEventBus;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EventDrivenNetworkHooks implements INetworkHooks<ITcpNetworkClient> {
    private final @NonNull IEventBus eventBus;

    @Override
    public void onNetworkRead(NetworkRead<ITcpNetworkClient> networkRead) {

    }

    @Override
    public void onNetworkWrite(NetworkWrite<ITcpNetworkClient> networkWrite) {

    }

    @Override
    public void onClientConnected(ClientConnected<ITcpNetworkClient> clientConnected) {

    }

    @Override
    public void onClientDisconnected(ClientDisconnected<ITcpNetworkClient> clientDisconnected) {

    }
}
