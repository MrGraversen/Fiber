package io.graversen.fiber.core.codec;

import io.graversen.fiber.core.tcp.ClientsContext;
import io.graversen.fiber.core.tcp.ITcpNetworkClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.function.BiConsumer;

@Getter
@RequiredArgsConstructor
public class EncodeContext<T> {
    private final IEncoder<T> encoder;
    private final BiConsumer<byte[], ITcpNetworkClient> sendCallback;

    public void encodeAndSend(T value, ITcpNetworkClient networkClient) {
        final var encodedValue = encoder.encode(value);
        sendCallback.accept(encodedValue, networkClient);
    }

    public void encodeAndSend(T value, ClientsContext clientsContext) {
        encodeAndSend(value, clientsContext.clients());
    }

    public void encodeAndSend(T value, Collection<? extends ITcpNetworkClient> networkClients) {
        final var encodedValue = encoder.encode(value);
        networkClients.forEach(networkClient -> sendCallback.accept(encodedValue, networkClient));
    }
}
