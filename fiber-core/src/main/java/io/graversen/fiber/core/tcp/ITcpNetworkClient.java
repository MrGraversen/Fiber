package io.graversen.fiber.core.tcp;

import io.graversen.fiber.utils.IClient;

import java.nio.channels.AsynchronousSocketChannel;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public interface ITcpNetworkClient extends IClient {
    ClientNetworkDetails networkDetails();

    LocalDateTime connectedAt();

    <T> Optional<T> getAttribute(String key);

    <T> void setAttribute(String key, T attribute);

    Map<String, Object> attributes();

    AsynchronousSocketChannel socketChannel();

    AtomicBoolean pending();

    void close();
}
