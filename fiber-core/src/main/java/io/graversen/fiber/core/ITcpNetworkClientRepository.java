package io.graversen.fiber.core;

import io.graversen.fiber.utils.IClient;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public interface ITcpNetworkClientRepository {
    ITcpNetworkClient addClient(ITcpNetworkClient networkClient);

    Optional<ITcpNetworkClient> getClient(IClient client);

    Optional<ITcpNetworkClient> getClient(Predicate<ITcpNetworkClient> query);

    Optional<ITcpNetworkClient> removeClient(IClient client);

    Set<ITcpNetworkClient> getClients();

    Set<ITcpNetworkClient> getClients(Predicate<ITcpNetworkClient> query);

    Set<ITcpNetworkClient> removeClients(Predicate<ITcpNetworkClient> query);
}
