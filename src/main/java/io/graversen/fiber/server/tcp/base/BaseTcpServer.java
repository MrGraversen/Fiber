package io.graversen.fiber.server.tcp.base;

import io.graversen.fiber.config.tcp.TcpServerConfig;
import io.graversen.fiber.event.bus.IEventBus;
import io.graversen.fiber.event.common.*;
import io.graversen.fiber.server.async.DefaultThreadFactory;
import io.graversen.fiber.server.base.BaseNetworkingServer;
import io.graversen.fiber.server.management.BaseNetworkClientManager;
import io.graversen.fiber.server.management.INetworkClient;
import io.graversen.fiber.server.management.NetworkMessage;
import io.graversen.fiber.server.tcp.management.TcpNetworkClient;
import io.graversen.fiber.util.NetworkUtil;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.ThreadFactory;

public class BaseTcpServer extends BaseNetworkingServer
{
    private final TcpSocketServerWrapper tcpSocketServerWrapper;
    private final ThreadFactory threadFactory;
    private final Thread eventLoopRunner;
    private final TcpServerConfig serverConfig;

    public BaseTcpServer(TcpServerConfig serverConfig, BaseNetworkClientManager networkClientManager, IEventBus eventBus)
    {
        super(serverConfig, networkClientManager, eventBus);
        this.serverConfig = serverConfig;
        this.tcpSocketServerWrapper = new TcpSocketServerWrapper(this);
        this.threadFactory = new DefaultThreadFactory(getClass().getSimpleName());
        this.eventLoopRunner = threadFactory.newThread(tcpSocketServerWrapper.getEventLoop());
    }

    @Override
    public void start()
    {
        this.tcpSocketServerWrapper.start();
        this.eventLoopRunner.start();

        final ServerReadyEvent serverReadyEvent = new ServerReadyEvent(this);
        getEventBus().emitEvent(serverReadyEvent, true);
    }

    @Override
    public void stop(Exception reason, boolean gently)
    {
        if (gently)
        {
            getNetworkClientManager().getAllClients().forEach(networkClient -> disconnect(networkClient, reason));
        }

        tcpSocketServerWrapper.stop();
        final ServerClosedEvent serverClosedEvent = new ServerClosedEvent(this, reason);
        getEventBus().emitEvent(serverClosedEvent, true);
    }

    @Override
    public void broadcast(byte[] messageData)
    {
        getNetworkClientManager().getAllClients().forEach(networkClient -> send(networkClient, messageData));
    }

    @Override
    public void disconnect(String networkClientId, Exception reason)
    {
        final Optional<INetworkClient> networkClientById = getNetworkClientManager().getClient(networkClientId);
        networkClientById.ifPresent(networkClient -> disconnect(networkClient, reason));
    }

    @Override
    public void disconnect(INetworkClient networkClient, Exception reason)
    {
        final TcpNetworkClient tcpNetworkClient = ((TcpNetworkClient) networkClient);

        try
        {
            tcpNetworkClient.getSocketChannel().close();

            final ClientDisconnectedEvent clientDisconnectedEvent = new ClientDisconnectedEvent(tcpNetworkClient, new IOException(reason));
            getEventBus().emitEvent(clientDisconnectedEvent, true);
        }
        catch (IOException e)
        {
            // Ignored
        }
    }

    @Override
    public void send(String networkClientId, byte[] messageData)
    {
        final Optional<INetworkClient> networkClientById = getNetworkClientManager().getClient(networkClientId);
        networkClientById.ifPresent(networkClient -> send(networkClient, messageData));
    }

    @Override
    public void send(INetworkClient networkClient, byte[] messageData)
    {
        final TcpNetworkClient tcpNetworkClient = ((TcpNetworkClient) networkClient);

        tcpNetworkClient.putOnNetworkQueue(new NetworkMessage(messageData));
        tcpSocketServerWrapper.removeInterest(tcpNetworkClient.getSelectionKey(), SelectionKey.OP_READ);
        tcpSocketServerWrapper.addInterest(tcpNetworkClient.getSelectionKey(), SelectionKey.OP_WRITE);
    }

    private class TcpSocketServerWrapper
    {
        private final BaseTcpServer abstractTcpServer;

        private final ServerSocketChannel serverSocketChannel;
        private final Selector defaultSelector;

        public TcpSocketServerWrapper(BaseTcpServer abstractTcpServer)
        {
            try
            {
                this.abstractTcpServer = abstractTcpServer;
                this.serverSocketChannel = ServerSocketChannel.open();
                this.defaultSelector = Selector.open();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        public void start()
        {
            try
            {
                this.serverSocketChannel.socket().bind(serverConfig.getServerAddress());
                this.serverSocketChannel.configureBlocking(false);
                this.serverSocketChannel.register(defaultSelector, SelectionKey.OP_ACCEPT);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        public void stop()
        {
            try
            {
                this.serverSocketChannel.close();
            }
            catch (IOException e)
            {
                // Ignored
            }
        }

        public Runnable getEventLoop()
        {
            return () -> {
                while (!Thread.currentThread().isInterrupted() && serverSocketChannel.isOpen())
                {
                    try
                    {
                        defaultSelector.select();
                        final Iterator<SelectionKey> selectionKeys = defaultSelector.selectedKeys().iterator();

                        selectionKeys.forEachRemaining(selectionKey -> {
                            try
                            {
                                if (selectionKey.isValid())
                                {
                                    if (selectionKey.isAcceptable())
                                    {
                                        accept(selectionKey);
                                    }

                                    if (selectionKey.isReadable())
                                    {
                                        read(selectionKey);
                                    }

                                    if (selectionKey.isWritable())
                                    {
                                        write(selectionKey);
                                    }
                                }
                            }
                            catch (IOException | CancelledKeyException e1)
                            {
                                close(selectionKey, e1);
                            }
                            catch (Exception e2)
                            {
                                close(selectionKey, e2);
                                throw new RuntimeException(e2);
                            }
                        });
                    }
                    catch (Exception e)
                    {
                        final ServerErrorEvent serverErrorEvent = new ServerErrorEvent(abstractTcpServer, e);
                        getEventBus().emitEvent(serverErrorEvent, true);
                    }

                    try
                    {
                        Thread.sleep(1);
                    }
                    catch (InterruptedException e)
                    {
                        Thread.currentThread().interrupt();
                    }
                }
            };
        }

        private void accept(SelectionKey selectionKey) throws IOException
        {
            final SocketChannel socketChannel = serverSocketChannel.accept();

            if (socketChannel != null)
            {
                socketChannel.configureBlocking(false);

                socketChannel.socket().setReceiveBufferSize(serverConfig.getClientReceiveBufferBytes());
                socketChannel.socket().setSendBufferSize(serverConfig.getClientSendBufferBytes());

                socketChannel.register(defaultSelector, SelectionKey.OP_READ);
                while (!socketChannel.finishConnect()) ;

                final InetAddress socketAddress = socketChannel.socket().getInetAddress();

                final TcpNetworkClient tcpNetworkClient = new TcpNetworkClient(socketChannel, socketChannel.keyFor(defaultSelector), socketAddress.getHostAddress(), socketChannel.socket().getPort());
                getNetworkClientManager().storeClient(tcpNetworkClient);

                final ClientConnectedEvent clientConnectedEvent = new ClientConnectedEvent(tcpNetworkClient);
                getEventBus().emitEvent(clientConnectedEvent, true);
            }
        }

        private void read(SelectionKey selectionKey) throws IOException
        {
            final SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
            final ByteBuffer readBuffer = ByteBuffer.allocateDirect(serverConfig.getClientReceiveBufferBytes());

            final int readBytes = socketChannel.read(readBuffer);

            if (readBytes == -1)
            {
                close(selectionKey, new IOException("Client disconnected"));
                return;
            }

            if (readBytes > 0)
            {
                readBuffer.flip();
                final byte[] dataBuffer = new byte[serverConfig.getClientReceiveBufferBytes()];
                readBuffer.get(dataBuffer, 0, readBytes);
                final byte[] dataTrimmed = trimByteArray(dataBuffer);

                final InetAddress socketAddress = socketChannel.socket().getInetAddress();

                final String connectionTuple = NetworkUtil.getConnectionTuple(socketAddress.getHostAddress(), socketChannel.socket().getPort());
                final Optional<INetworkClient> tcpNetworkClientByConnectionTuple = getNetworkClientManager().getClientByConnectionTuple(connectionTuple);

                tcpNetworkClientByConnectionTuple.ifPresent(tcpNetworkClient -> {
                    final NetworkMessage networkMessage = new NetworkMessage(dataTrimmed);

                    final NetworkMessageReceivedEvent networkMessageReceivedEvent = new NetworkMessageReceivedEvent(tcpNetworkClient, networkMessage);
                    getEventBus().emitEvent(networkMessageReceivedEvent, true);
                });
            }

            removeInterest(selectionKey, SelectionKey.OP_READ);
            addInterest(selectionKey, SelectionKey.OP_WRITE);
        }

        private void write(SelectionKey selectionKey)
        {
            final SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
            final InetAddress socketAddress = socketChannel.socket().getInetAddress();
            final String connectionTuple = NetworkUtil.getConnectionTuple(socketAddress.getHostAddress(), socketChannel.socket().getPort());
            final Optional<INetworkClient> tcpNetworkClientByConnectionTuple = getNetworkClientManager().getClientByConnectionTuple(connectionTuple);

            tcpNetworkClientByConnectionTuple.ifPresent(networkClient -> {
                final TcpNetworkClient tcpNetworkClient = ((TcpNetworkClient) networkClient);

                tcpNetworkClient.pollAllFromNetworkQueue().forEach(networkMessage -> {
                    try
                    {
                        socketChannel.write(ByteBuffer.wrap(networkMessage.getMessageData()));

                        final NetworkMessageSentEvent networkMessageSentEvent = new NetworkMessageSentEvent(tcpNetworkClient, networkMessage);
                        getEventBus().emitEvent(networkMessageSentEvent, true);
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException(e);
                    }
                });
            });

            removeInterest(selectionKey, SelectionKey.OP_WRITE);
            addInterest(selectionKey, SelectionKey.OP_READ);
        }

        private void close(SelectionKey selectionKey, Exception reason)
        {
            final SocketChannel socketChannel = ((SocketChannel) selectionKey.channel());
            final InetAddress socketAddress = socketChannel.socket().getInetAddress();
            final String connectionTuple = NetworkUtil.getConnectionTuple(socketAddress.getHostAddress(), socketChannel.socket().getPort());
            final Optional<INetworkClient> tcpNetworkClientByConnectionTuple = getNetworkClientManager().getClientByConnectionTuple(connectionTuple);

            tcpNetworkClientByConnectionTuple.ifPresent(networkClient -> {
                getNetworkClientManager().deleteClient(networkClient);

                try
                {
                    selectionKey.cancel();
                    selectionKey.channel().close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                if (reason instanceof CancelledKeyException) return;

                final ClientDisconnectedEvent clientDisconnectedEvent = new ClientDisconnectedEvent(networkClient, new IOException(reason));
                getEventBus().emitEvent(clientDisconnectedEvent, true);
            });
        }

        private byte[] trimByteArray(byte[] bytes)
        {
            int i = bytes.length - 1;
            while (i >= 0 && bytes[i] == 0) --i;
            return Arrays.copyOf(bytes, i + 1);
        }

        private void addInterest(SelectionKey selectionKey, int interest)
        {
            int interests = selectionKey.interestOps();
            selectionKey.interestOps(interests | interest);
        }

        private void removeInterest(SelectionKey selectionKey, int interest)
        {
            int interests = selectionKey.interestOps();
            selectionKey.interestOps(interests & ~interest);
        }
    }
}
