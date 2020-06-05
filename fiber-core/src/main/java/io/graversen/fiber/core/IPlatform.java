package io.graversen.fiber.core;

import io.graversen.fiber.core.codec.EncodeContext;
import io.graversen.fiber.core.codec.IDecoder;
import io.graversen.fiber.core.codec.IEncoder;
import io.graversen.fiber.core.codec.IReceiver;
import io.graversen.fiber.core.tcp.ClientsContext;
import io.graversen.fiber.core.tcp.ITcpNetworkClient;
import io.graversen.fiber.core.tcp.ServerNetworkConfiguration;
import io.graversen.fiber.utils.IClient;

import java.util.function.Predicate;

public interface IPlatform<C extends IClient> {
    void start(ServerNetworkConfiguration networkConfiguration);

    void stop();

    IServer<C> server();

    <T> void registerDecoder(IDecoder<T> codec, IReceiver<T> receiver);

    <T> EncodeContext<T> registerEncoder(IEncoder<T> encoder);

    ClientsContext getClients(Predicate<ITcpNetworkClient> query);
}
