package io.graversen.fiber.server.management;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractNetworkClientManager
{
    private final Map<UUID, INetworkClient> clientStore;
    private final Map<String, UUID> ipPortToIdStore;

    public AbstractNetworkClientManager()
    {
        this.clientStore = new ConcurrentHashMap<>();
        ipPortToIdStore = new ConcurrentHashMap<>();
    }

    public Optional<INetworkClient> getClient(UUID id)
    {
        return Optional.ofNullable(clientStore.getOrDefault(id, null));
    }

    public Optional<INetworkClient> getClient(String ipAndPort)
    {
        return getClient(ipPortToIdStore.getOrDefault(ipAndPort, null));
    }

    public void storeClient(INetworkClient client)
    {
        clientStore.put(client.id(), client);
        ipPortToIdStore.put(client.ipAndPort(), client.id());
    }
}
