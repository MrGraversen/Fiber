package io.graversen.fiber.server.management;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractNetworkClientManager
{
    private final Map<String, INetworkClient> clientStore;
    private final Map<String, String> connectionTupleToIdStore;

    public AbstractNetworkClientManager()
    {
        this.clientStore = new ConcurrentHashMap<>();
        this.connectionTupleToIdStore = new ConcurrentHashMap<>();
    }

    public Optional<INetworkClient> getClient(UUID id)
    {
        return Optional.ofNullable(clientStore.getOrDefault(id, null));
    }

    public Optional<INetworkClient> getClient(String connectionTuple)
    {
        return getClient(connectionTupleToIdStore.getOrDefault(connectionTuple, null));
    }

    public void storeClient(INetworkClient client)
    {
        clientStore.put(client.id(), client);
        connectionTupleToIdStore.put(client.connectionTuple(), client.id());
    }

    public boolean deleteClient(INetworkClient client)
    {
        return this.deleteClient(client.id());
    }

    public boolean deleteClient(String id)
    {
        final boolean clientExists = clientStore.containsKey(id);

        if (clientExists)
        {
            final INetworkClient networkClient = clientStore.get(id);
            clientStore.remove(id);
            connectionTupleToIdStore.remove(networkClient.connectionTuple());

            return true;
        }
        else
        {
            return false;
        }
    }
}
