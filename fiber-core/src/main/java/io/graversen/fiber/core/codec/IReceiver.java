package io.graversen.fiber.core.codec;

import io.graversen.fiber.core.tcp.events.NetworkReadEvent;

public interface IReceiver<T> {
    void onDecode(DecodedMessage<T> value);

    void onDecodeError(NetworkReadEvent networkReadEvent, Throwable reason);
}
