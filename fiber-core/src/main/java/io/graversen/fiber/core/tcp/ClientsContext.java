package io.graversen.fiber.core.tcp;

import io.graversen.fiber.core.IServer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

@RequiredArgsConstructor
public class ClientsContext {
    private final @NonNull IServer<ITcpNetworkClient> server;
    private final @NonNull Collection<? extends ITcpNetworkClient> clients;

    public void send(byte[] message) {
        clients.forEach(networkClient -> server.send(networkClient, message));
    }

    public void disconnect(Throwable reason) {
        clients.forEach(networkClient -> server.disconnect(networkClient, reason));
    }

    public Collection<? extends ITcpNetworkClient> clients() {
        return clients;
    }
}
