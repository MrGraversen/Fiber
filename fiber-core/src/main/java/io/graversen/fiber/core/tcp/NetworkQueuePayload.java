package io.graversen.fiber.core.tcp;

import lombok.NonNull;
import lombok.Value;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

@Value
class NetworkQueuePayload {
    private final @NonNull ByteBuffer byteBuffer;
    private final @NonNull ITcpNetworkClient client;
    private final AtomicInteger requeueCount = new AtomicInteger(0);

    public void registerRequeue() {
        requeueCount.incrementAndGet();
    }
}
