package io.graversen.fiber.server.tcp.base;

import io.graversen.fiber.config.tcp.TcpServerConfig;
import io.graversen.fiber.event.*;
import io.graversen.fiber.server.async.DefaultThreadFactory;
import io.graversen.fiber.server.base.AbstractNetworkingServer;
import io.graversen.fiber.server.management.AbstractNetworkClientManager;
import io.graversen.fiber.server.management.INetworkClient;
import io.graversen.fiber.server.management.NetworkMessage;
import io.graversen.fiber.server.tcp.management.TcpNetworkClient;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ThreadFactory;

public class AbstractTcpServer extends AbstractNetworkingServer
{
    private final TcpSocketServerWrapper tcpSocketServerWrapper;
    private final ThreadFactory threadFactory;
    private final Thread eventLoopRunner;
    private final TcpServerConfig serverConfig;

    public AbstractTcpServer(TcpServerConfig serverConfig, AbstractNetworkClientManager networkClientManager, EventBus eventBus)
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
        getEventBus().publishEvent(serverReadyEvent, true);
    }

    @Override
    public void stop(Exception reason, boolean gently)
    {

    }

    @Override
    public void broadcast(byte[] messageData)
    {

    }

    @Override
    public void disconnect(String networkClientId, Exception reason)
    {

    }

    @Override
    public void disconnect(INetworkClient networkClient, Exception reason)
    {

    }

    @Override
    public void send(String networkClientId, byte[] messageData)
    {

    }

    @Override
    public void send(INetworkClient networkClient, byte[] messageData)
    {

    }

    private class TcpSocketServerWrapper
    {
        private final AbstractTcpServer abstractTcpServer;

        private final ServerSocketChannel serverSocketChannel;
        private final Selector defaultSelector;

        public TcpSocketServerWrapper(AbstractTcpServer abstractTcpServer)
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
                            catch (CancelledKeyException cke)
                            {
                                // Ignore
                            }
                            catch (Exception e)
                            {
                                close(selectionKey, e);
                                throw new RuntimeException(e);
                            }
                        });
                    }
                    catch (Exception e)
                    {
                        final ServerErrorEvent serverErrorEvent = new ServerErrorEvent(abstractTcpServer, e);
                        getEventBus().publishEvent(serverErrorEvent, true);
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

                final InetAddress socketAddress = socketChannel.socket().getInetAddress();

                final TcpNetworkClient tcpNetworkClient = new TcpNetworkClient(socketChannel, socketAddress.getHostAddress(), socketChannel.socket().getPort());
                getNetworkClientManager().storeClient(tcpNetworkClient);

                final ClientConnectedEvent clientConnectedEvent = new ClientConnectedEvent(tcpNetworkClient);
                getEventBus().publishEvent(clientConnectedEvent, true);
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

                final TcpNetworkClient tcpNetworkClient = new TcpNetworkClient(socketChannel, socketAddress.getHostAddress(), socketChannel.socket().getPort());
                final NetworkMessage networkMessage = new NetworkMessage(dataTrimmed);

                final NetworkMessageReceivedEvent networkMessageReceivedEvent = new NetworkMessageReceivedEvent(tcpNetworkClient, networkMessage);
                getEventBus().publishEvent(networkMessageReceivedEvent, true);
            }
        }

        private void write(SelectionKey selectionKey)
        {
            final SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
            final InetAddress socketAddress = socketChannel.socket().getInetAddress();
            final TcpNetworkClient tcpNetworkClient = new TcpNetworkClient(socketChannel, socketAddress.getHostAddress(), socketChannel.socket().getPort());

            

            selectionKey.interestOps(SelectionKey.OP_READ);
        }

        private void close(SelectionKey selectionKey, Exception reason)
        {
            final SocketChannel socketChannel = ((SocketChannel) selectionKey.channel());
            final InetAddress socketAddress = socketChannel.socket().getInetAddress();

            final TcpNetworkClient tcpNetworkClient = new TcpNetworkClient(socketChannel, socketAddress.getHostAddress(), socketChannel.socket().getPort());
            getNetworkClientManager().deleteClient(tcpNetworkClient);

            try
            {
                selectionKey.cancel();
                selectionKey.channel().close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            final ClientDisconnectedEvent clientDisconnectedEvent = new ClientDisconnectedEvent(tcpNetworkClient, new IOException(reason));
            getEventBus().publishEvent(clientDisconnectedEvent, true);
        }

        private byte[] trimByteArray(byte[] bytes)
        {
            int i = bytes.length - 1;

            while (i >= 0 && bytes[i] == 0) --i;

            return Arrays.copyOf(bytes, i + 1);
        }
    }
}
