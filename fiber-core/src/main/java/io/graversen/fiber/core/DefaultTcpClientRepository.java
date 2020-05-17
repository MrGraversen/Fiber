package io.graversen.fiber.core;

import io.graversen.fiber.utils.IClient;

import java.util.Optional;
import java.util.Set;
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
        return Optional.empty();
    }

    @Override
    public Set<ITcpNetworkClient> getClients() {
        return Set.copyOf(tcpNetworkClients.values());
    }

    @Override
    public Set<ITcpNetworkClient> getClients(Predicate<ITcpNetworkClient> query) {
        return null;
    }

    @Override
    public Set<ITcpNetworkClient> removeClients(Predicate<ITcpNetworkClient> query) {
        return null;
    }
}
