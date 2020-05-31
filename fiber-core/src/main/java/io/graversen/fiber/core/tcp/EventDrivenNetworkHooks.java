package io.graversen.fiber.core.tcp;

import io.graversen.fiber.core.hooks.*;
import io.graversen.fiber.event.bus.IEventBus;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class EventDrivenNetworkHooks implements INetworkHooks<ITcpNetworkClient> {
    private final @NonNull IEventBus eventBus;

    @Override
    public void onNetworkRead(NetworkRead<ITcpNetworkClient> networkRead) {
        log.info("Read {} bytes: {}", networkRead.getNetworkMessage().getBytes(), networkRead.getNetworkMessage().toString());
    }

    @Override
    public void onNetworkWrite(NetworkWrite<ITcpNetworkClient> networkWrite) {

    }

    @Override
    public void onClientConnected(ClientConnected<ITcpNetworkClient> clientConnected) {
        log.info("onClientConnected");
    }

    @Override
    public void onClientDisconnected(ClientDisconnected<ITcpNetworkClient> clientDisconnected) {
        log.info("onClientDisconnected");
    }
}
