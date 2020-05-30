package io.graversen.fiber.core.tcp;

import io.graversen.fiber.utils.ControllableTaskLoop;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class NetworkWriteTask extends ControllableTaskLoop<NetworkQueuePayload> {
    private final ITcpNetworkClientRepository networkClientRepository;
    private final BlockingQueue<NetworkQueuePayload> networkQueue;
    private final Consumer<NetworkQueuePayload> sendCallback;

    @Override
    public void performTask(NetworkQueuePayload nextItem) {
        try {
            networkClientRepository.getClient(nextItem.getClient()).ifPresentOrElse(
                    client -> sendCallback.accept(nextItem),
                    handleClientUnavailable()
            );
        } catch (Exception e) {
            log.error("Client {} unexpected error: {}", nextItem.getClient().id(), e.getMessage());
        }
    }

    @Override
    public NetworkQueuePayload awaitNext() throws InterruptedException {
        return networkQueue.take();
    }

    private Runnable handleClientUnavailable() {
        return () -> log.debug("Discarding message because client is not available");
    }
}
