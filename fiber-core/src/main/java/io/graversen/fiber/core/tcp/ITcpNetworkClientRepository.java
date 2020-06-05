package io.graversen.fiber.core.tcp;

import io.graversen.fiber.utils.IClient;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public interface ITcpNetworkClientRepository {
    ITcpNetworkClient addClient(ITcpNetworkClient networkClient);

    Optional<ITcpNetworkClient> getClient(IClient client);

    Optional<ITcpNetworkClient> getClient(Predicate<ITcpNetworkClient> query);

    Optional<ITcpNetworkClient> removeClient(IClient client);

    Collection<? extends ITcpNetworkClient> getClients();

    Collection<? extends ITcpNetworkClient> getClients(Predicate<ITcpNetworkClient> query);

    Collection<? extends ITcpNetworkClient> removeClients(Predicate<ITcpNetworkClient> query);
}
