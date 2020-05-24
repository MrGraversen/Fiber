package io.graversen.fiber.core.tcp;

import io.graversen.fiber.utils.IClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ClientQueues {
    private final ConcurrentMap<IClient, BlockingQueue<NetworkQueuePayload>> clientQueues = new ConcurrentHashMap<>();

    public BlockingQueue<NetworkQueuePayload> getClientQueue(IClient client) {
        return clientQueues.computeIfAbsent(client, c -> new LinkedBlockingQueue<>());
    }

    public void remove(IClient client) {
        clientQueues.remove(client);
    }
}
