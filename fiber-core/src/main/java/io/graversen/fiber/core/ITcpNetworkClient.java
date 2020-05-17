package io.graversen.fiber.core;

import io.graversen.fiber.utils.IClient;

import java.nio.channels.AsynchronousSocketChannel;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

public interface ITcpNetworkClient extends IClient {
    ClientNetworkDetails networkDetails();

    LocalDateTime connectedAt();

    <T> Optional<T> getAttribute(String key);

    <T> void setAttribute(String key, T attribute);

    Map<String, Object> attributes();

    AsynchronousSocketChannel socketChannel();
}
