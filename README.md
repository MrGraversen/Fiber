# Fiber
_A Java networking library._

[ ![Codeship Status for MrGraversen/Fiber](https://app.codeship.com/projects/f7eaf010-295b-0136-174e-0a7c6efe79c9/status?branch=master)](https://app.codeship.com/projects/287302)

## Install

Will add JitPack once this library matures a little.

## What is Fiber?

_Fiber_ is a close-to-zero-dependency networking library written in Java. It exposes a somewhat unopinionated interface to build networking servers.

Currently, these types of servers are supported:

* Asynchronous I/O TCP — Using pure `java.nio`
* [RFC 6455](http://tools.ietf.org/html/rfc6455) WebSocket — Using `org.java_websocket` by [TooTallNate](https://github.com/TooTallNate/Java-WebSocket)

### Why is Fiber?

It's a good question. In practice, you probably want to use [Netty](https://netty.io/) instead of trying to create your own networking engine.
I'm not trying to make the next Netty; I'm just trying to get a little more intimate with the challenges of networking and concurrency, with the added challenge of using very few depedencies (Let's face it, I'm not going to implement the whole WebSocket RFC).

I will eventually try to implement different kinds of generic servers (e.g. a web server, local network file sharing, etc.) on top of _Fiber_ as a sort of acceptance test. Fun stuff!

## Design and Concepts

_Fiber_ heavily employs Observer Pattern to notify event subscribers of interesting occurrences. It uses an event bus to dispatch events, using threads to offload the server thread, which produced the event. One of the design goals is that I/O should be as _lean_ as possible; thus requiring event propagation and handler execution to take place on threads seperate from the network I/O.

Smells a lot like the NodeJS processing model...

## Examples

The following example is also found in the `io.graversen.fiber.examples.SimpleTcpServerExample` class.

```java
// First, let's configure the TCP server instance - will listen on port 1337
final TcpServerConfig tcpServerConfig = new AllNetworkInterfacesTcpServerConfig(1337);

// Declare an implementation of the Event Bus
final AbstractEventBus eventBus = new DefaultEventBus();

// Declare an implementation of Network Client Manager
final DefaultNetworkClientManager networkClientManager = new DefaultNetworkClientManager();

// Bundle it all together to form a Simple TCP Server
final AbstractNetworkingServer tcpServer = new SimpleTcpServer(tcpServerConfig, networkClientManager, eventBus);

// Add a Network Event Listener to the Event Bus - it will just print events to System.out
networkClientEventListener(eventBus);

// Let's add another listener to the Event Bus, for the NetworkMessageReceivedEvent, exposing a small protocol to the network
eventBus.registerEventListener(NetworkMessageReceivedEvent.class, new AbstractEventListener<NetworkMessageReceivedEvent>()
{
	@Override
	public void onEvent(NetworkMessageReceivedEvent event)
	{
		final String message = new String(event.getNetworkMessage().getMessageData());

		if ("Hello".equals(message))
		{
			tcpServer.send(event.getNetworkClient(), "World".getBytes());
		}
		else if ("Bye".equals(message))
		{
			tcpServer.stop(new Exception("Until next time!"), true);
		}
	}
});

// Let's go!
tcpServer.start();
```

Running the example with just some random TCP client yields the following console output:

```
Event - ServerReadyEvent - /0.0.0.0:1337
Event - ClientConnectedEvent - 127.0.0.1:50623
Event - NetworkMessageReceivedEvent - 127.0.0.1:50623 (5 bytes): Hello
Event - NetworkMessageSentEvent - 127.0.0.1:50623 (5 bytes): World
Event - NetworkMessageReceivedEvent - 127.0.0.1:50623 (3 bytes): Bye
Event - ServerClosedEvent - Reason: Until next time!
Event - ClientDisconnectedEvent - 127.0.0.1:50623: java.lang.Exception: Until next time!
```

### `// TODO: `

* Write JavaDoc
* Write unit tests
* Implement factories to reduce the amount of setup code required to bootstrap servers
* Implement a UDP server
* Probably (a lot) more
