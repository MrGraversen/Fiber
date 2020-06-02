package io.graversen.fiber.core.codec;

import io.graversen.fiber.core.tcp.ITcpNetworkClient;

import java.util.function.BiConsumer;

public interface EncodeContext<T> extends BiConsumer<T, ITcpNetworkClient> {

}
