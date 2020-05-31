package io.graversen.fiber.core.tcp;

import io.graversen.fiber.utils.IClient;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

public class DefaultTcpClientRepository implements ITcpNetworkClientRepository {
    private final ConcurrentMap<String, ITcpNetworkClient> tcpNetworkClients = new ConcurrentHashMap<>();

    @Override
    public ITcpNetworkClient addClient(ITcpNetworkClient networkClient) {
        return tcpNetworkClients.putIfAbsent(networkClient.id(), networkClient);
    }

    @Override
    public Optional<ITcpNetworkClient> getClient(IClient client) {
        return Optional.ofNullable(tcpNetworkClients.getOrDefault(client.id(), null));
    }

    @Override
    public Optional<ITcpNetworkClient> getClient(Predicate<ITcpNetworkClient> query) {
        return Optional.empty();
    }

    @Override
    public Optional<ITcpNetworkClient> removeClient(IClient client) {
        tcpNetworkClients.remove(client.id());
        return Optional.empty();
    }

    @Override
    public Collection<? extends ITcpNetworkClient> getClients() {
        return tcpNetworkClients.values();
    }

    @Override
    public Collection<? extends ITcpNetworkClient> getClients(Predicate<ITcpNetworkClient> query) {
        return null;
    }

    @Override
    public Collection<? extends ITcpNetworkClient> removeClients(Predicate<ITcpNetworkClient> query) {
        return null;
    }
}
