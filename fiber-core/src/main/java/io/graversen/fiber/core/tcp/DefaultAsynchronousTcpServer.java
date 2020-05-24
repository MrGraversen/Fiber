package io.graversen.fiber.core.tcp;

public class DefaultAsynchronousTcpServer extends AsynchronousTcpServer {
    public DefaultAsynchronousTcpServer(ServerNetworkConfiguration serverNetworkConfiguration) {
        super(
                serverNetworkConfiguration,
                new ServerInternalsConfiguration(32768),
                new DefaultTcpClientRepository()
        );
    }
}
