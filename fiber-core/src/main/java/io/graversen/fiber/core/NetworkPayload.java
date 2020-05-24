package io.graversen.fiber.core;

import io.graversen.fiber.utils.IClient;
import lombok.NonNull;
import lombok.Value;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

@Value
public class NetworkPayload {
    private final @NonNull ByteBuffer byteBuffer;
    private final @NonNull IClient client;
    private final AtomicInteger requeueCount = new AtomicInteger(0);

    public void registerRequeue() {
        requeueCount.incrementAndGet();
    }
}
