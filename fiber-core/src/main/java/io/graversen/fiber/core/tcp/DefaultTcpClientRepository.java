package io.graversen.fiber.core.tcp;

import io.graversen.fiber.utils.IClient;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class DefaultTcpClientRepository implements ITcpNetworkClientRepository {
    private final ConcurrentMap<String, ITcpNetworkClient> networkClients = new ConcurrentHashMap<>();

    @Override
    public ITcpNetworkClient addClient(ITcpNetworkClient networkClient) {
        return networkClients.putIfAbsent(networkClient.id(), networkClient);
    }

    @Override
    public Optional<ITcpNetworkClient> getClient(IClient client) {
        return Optional.ofNullable(networkClients.getOrDefault(client.id(), null));
    }

    @Override
    public Optional<ITcpNetworkClient> getClient(Predicate<ITcpNetworkClient> query) {
        return Optional.empty();
    }

    @Override
    public Optional<ITcpNetworkClient> removeClient(IClient client) {
        networkClients.remove(client.id());
        return Optional.empty();
    }

    @Override
    public Collection<? extends ITcpNetworkClient> getClients() {
        return networkClients.values();
    }

    @Override
    public Collection<? extends ITcpNetworkClient> getClients(Predicate<ITcpNetworkClient> query) {
        return networkClients.values().stream()
                .filter(query)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Collection<? extends ITcpNetworkClient> removeClients(Predicate<ITcpNetworkClient> query) {
        return getClients(query).stream()
                .map(removeClient())
                .collect(Collectors.toUnmodifiableList());
    }

    private UnaryOperator<ITcpNetworkClient> removeClient() {
        return networkClient -> networkClients.remove(networkClient.id());
    }
}
