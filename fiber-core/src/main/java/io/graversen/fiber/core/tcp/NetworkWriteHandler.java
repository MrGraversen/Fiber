package io.graversen.fiber.core.tcp;

import io.graversen.fiber.core.NetworkMessage;
import io.graversen.fiber.core.hooks.NetworkHooksDispatcher;
import io.graversen.fiber.core.hooks.NetworkWrite;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.CompletionHandler;
import java.util.function.BiConsumer;

@Slf4j
@RequiredArgsConstructor
public class NetworkWriteHandler implements CompletionHandler<Integer, NetworkQueuePayload> {
    private final NetworkHooksDispatcher networkHooksDispatcher;
    private final ClientQueues clientQueues;
    private final NetworkQueue networkQueue;
    private final BiConsumer<ITcpNetworkClient, Throwable> failureCallback;

    @Override
    public void completed(Integer result, NetworkQueuePayload networkQueuePayload) {
        final var message = networkQueuePayload.getByteBuffer();
        final var client = networkQueuePayload.getClient();
        client.writePending().set(false);

        if (result == -1) {
            failureCallback.accept(client, new IOException("Disconnect from client endpoint"));
        }

        networkHooksDispatcher.enqueue(
                new NetworkWrite<>(client, new NetworkMessage(message.array(), message.array().length))
        );

        final var clientQueue = clientQueues.getClientQueue(client);
        final var nextNetworkPayloadOrNull = clientQueue.poll();
        if (nextNetworkPayloadOrNull != null) {
            networkQueue.offer(nextNetworkPayloadOrNull);
        }
    }

    @Override
    public void failed(Throwable throwable, NetworkQueuePayload networkQueuePayload) {
        log.error("Client {} network write error: {}", networkQueuePayload.getClient().id(), throwable.getMessage());
        failureCallback.accept(networkQueuePayload.getClient(), throwable);
    }

}
