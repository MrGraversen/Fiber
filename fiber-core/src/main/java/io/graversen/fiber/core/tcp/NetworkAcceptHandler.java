package io.graversen.fiber.core.tcp;

import io.graversen.fiber.utils.IdUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class NetworkAcceptHandler implements CompletionHandler<AsynchronousSocketChannel, Void> {
    private final Consumer<ITcpNetworkClient> networkClientAcceptCallback;

    @Override
    public void completed(AsynchronousSocketChannel socketChannel, Void attachment) {
        final var networkClient = new TcpNetworkClient(
                IdUtils.fastClientId(),
                ClientNetworkDetails.from(socketChannel),
                LocalDateTime.now(),
                new ConcurrentHashMap<>(),
                socketChannel
        );

        log.debug("Registering new client with ID {}", networkClient.id());
        networkClientAcceptCallback.accept(networkClient);
    }

    @Override
    public void failed(Throwable throwable, Void attachment) {
        log.error(throwable.getMessage(), throwable);
    }
}
