package io.graversen.fiber.core;

import io.graversen.fiber.core.tcp.ServerNetworkConfiguration;
import io.graversen.fiber.utils.IClient;

public interface IPlatform<C extends IClient> {
    void start(ServerNetworkConfiguration networkConfiguration);

    void stop();

    IServer<C> server();
}
