package io.graversen.fiber.core.codec;

import io.graversen.fiber.core.tcp.events.NetworkReadEvent;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DecodeContext<T> {
    private final @NonNull IDecoder<T> codec;
    private final @NonNull IReceiver<T> receiver;

    public void decode(NetworkReadEvent networkReadEvent) {
        try {
            final var decoded = codec.decode(networkReadEvent.getNetworkMessage().getMessage());
            final var decodedMessage = new DecodedMessage<>(decoded, networkReadEvent.getNetworkClient());
            receiver.onDecode(decodedMessage);
        } catch (Exception e) {
            receiver.onDecodeError(networkReadEvent, e);
        }
    }
}
