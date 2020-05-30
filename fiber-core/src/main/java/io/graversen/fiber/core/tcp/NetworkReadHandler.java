package io.graversen.fiber.core.tcp;

import io.graversen.fiber.core.NetworkMessage;
import io.graversen.fiber.core.hooks.NetworkHooksDispatcher;
import io.graversen.fiber.core.hooks.NetworkRead;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.CompletionHandler;
import java.util.function.BiConsumer;

@Slf4j
@RequiredArgsConstructor
public class NetworkReadHandler implements CompletionHandler<Integer, ReadContext> {
    private final NetworkHooksDispatcher networkHooksDispatcher;
    private final BiConsumer<ITcpNetworkClient, Throwable> failureCallback;

    @Override
    public void completed(Integer result, ReadContext readContext) {
        if (result > 0) {
            final var readBuffer = readContext.getReadBuffer();
            final var client = readContext.getNetworkClient();

            readBuffer.flip();
            final byte[] message = new byte[readBuffer.remaining()];
            readBuffer.get(message);

            networkHooksDispatcher.enqueue(new NetworkRead<>(client, new NetworkMessage(message, message.length)));
            client.socketChannel().read(readBuffer.clear(), readContext, this);
        } else if (result == -1) {
            failureCallback.accept(readContext.getNetworkClient(), new IOException("Disconnect from client endpoint"));
        }
    }

    @Override
    public void failed(Throwable throwable, ReadContext readContext) {
        log.error("Client {} network read error: {}", readContext.getNetworkClient().id(), throwable.getMessage());
        failureCallback.accept(readContext.getNetworkClient(), throwable);
    }
}
