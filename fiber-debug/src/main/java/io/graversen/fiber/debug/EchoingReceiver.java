package io.graversen.fiber.debug;

import io.graversen.fiber.core.codec.DecodedMessage;
import io.graversen.fiber.core.codec.EncodeContext;
import io.graversen.fiber.core.codec.IReceiver;
import io.graversen.fiber.core.tcp.events.NetworkReadEvent;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class EchoingReceiver implements IReceiver<byte[]> {
    private final @NonNull EncodeContext<byte[]> encodeContext;

    @Override
    public void onDecode(DecodedMessage<byte[]> value) {
        encodeContext.encodeAndSend(value.getValue(), value.getClient());
    }

    @Override
    public void onDecodeError(NetworkReadEvent networkReadEvent, Throwable reason) {
        log.error(reason.getMessage(), reason);
    }
}
