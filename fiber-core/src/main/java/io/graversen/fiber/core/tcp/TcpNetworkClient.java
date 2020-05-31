package io.graversen.fiber.core.tcp;

import io.graversen.fiber.utils.ChannelUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.nio.channels.AsynchronousSocketChannel;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
public class TcpNetworkClient implements ITcpNetworkClient {
    private final @NonNull String id;
    private final @NonNull ClientNetworkDetails networkDetails;
    private final @NonNull LocalDateTime connectedAt;
    private final @NonNull ConcurrentMap<String, Object> attributes;
    private final @NonNull AsynchronousSocketChannel socketChannel;
    private final AtomicBoolean pending = new AtomicBoolean(false);

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

    @Override
    public AtomicBoolean pending() {
        return pending;
    }

    @Override
    public void close() {
        ChannelUtils.close(socketChannel);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TcpNetworkClient that = (TcpNetworkClient) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id;
    }
}
