package io.graversen.fiber.server.management;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractNetworkClientManager
{
    private final Map<String, INetworkClient> clientStore;
    private final Map<String, String> connectionTupleToIdStore;
    private final Map<String, Map<String, String>> clientDataStore;

    public AbstractNetworkClientManager()
    {
        this.clientStore = new ConcurrentHashMap<>();
        this.connectionTupleToIdStore = new ConcurrentHashMap<>();
        this.clientDataStore = new ConcurrentHashMap<>();
    }

    public Optional<INetworkClient> getClient(String id)
    {
        return Optional.ofNullable(clientStore.getOrDefault(id, null));
    }

    public Optional<INetworkClient> getClientByConnectionTuple(String connectionTuple)
    {
        return getClient(connectionTupleToIdStore.getOrDefault(connectionTuple, null));
    }

    public List<INetworkClient> getAllClients()
    {
        return Collections.unmodifiableList(new ArrayList<>(clientStore.values()));
    }

    public void storeClient(INetworkClient client)
    {
        clientStore.put(client.id(), client);
        connectionTupleToIdStore.put(client.connectionTuple(), client.id());
        clientDataStore.put(client.id(), new ConcurrentHashMap<>());
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
            clientDataStore.remove(id);

            return true;
        }
        else
        {
            return false;
        }
    }

    public String getData(String clientId, String dataKey)
    {
        return clientDataStore.get(clientId).get(dataKey);
    }

    public void putData(String clientId, String dataKey, String dataValue)
    {
        clientDataStore.get(clientId).put(dataKey, dataValue);
    }

    public void removeData(String clientId, String dataKey)
    {
        clientDataStore.get(clientId).remove(dataKey);
    }
}
