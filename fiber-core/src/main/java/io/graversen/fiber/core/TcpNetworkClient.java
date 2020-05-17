package io.graversen.fiber.core;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.nio.channels.AsynchronousSocketChannel;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

@RequiredArgsConstructor
public class TcpNetworkClient implements ITcpNetworkClient {
    private final @NonNull String id;
    private final @NonNull ClientNetworkDetails networkDetails;
    private final @NonNull LocalDateTime connectedAt;
    private final @NonNull ConcurrentMap<String, Object> attributes;
    private final @NonNull AsynchronousSocketChannel socketChannel;

    @Override
    public String id() {
        return id;
    }

    @Override
    public ClientNetworkDetails networkDetails() {
        return networkDetails;
    }

    @Override
    public LocalDateTime connectedAt() {
        return connectedAt;
    }

    @Override
    public <T> Optional<T> getAttribute(String key) {
        return Optional.ofNullable((T) attributes.getOrDefault(key, null));
    }

    @Override
    public <T> void setAttribute(String key, T attribute) {
        attributes.put(key, attribute);
    }

    @Override
    public Map<String, Object> attributes() {
        return Map.copyOf(attributes);
    }

    @Override
    public AsynchronousSocketChannel socketChannel() {
        return socketChannel;
    }
}
