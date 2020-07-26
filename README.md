# Fiber
[ ![Codeship Status for MrGraversen/Fiber](https://app.codeship.com/projects/f7eaf010-295b-0136-174e-0a7c6efe79c9/status?branch=master)](https://app.codeship.com/projects/287302) [![](https://jitpack.io/v/MrGraversen/Fiber.svg)](https://jitpack.io/#MrGraversen/Fiber)

## Install

You may use JitPack to install this from the GitHub releases.  
Add the following to your `pom.xml` if using Maven (click the little JitPack badge for other build systems):

```
<repositories>
	<repository>
		<id>jitpack.io</id>
		<url>https://jitpack.io</url>
	</repository>
</repositories>
```

```
<dependency>
    <groupId>com.github.MrGraversen.Fiber</groupId>
    <artifactId>fiber-core</artifactId>
    <version>0.1</version>
</dependency>
```
## What is Fiber?

_Fiber_ is a zero-dependency, event-driven TCP networking framework written in Java, based on the `java.nio` asynchronous I/O package.

It started a long time ago as a learning project for becoming more intimate with the challenges of concurrency in TCP networking frameworks.
One of my favourite ways to learn the "how" and "why" of technology is to throw myself at the surrounding problems, and try to _solve them_. Good old exposure therapy.

### Inspiration

The primary sources of inspiration for this project came from [Netty](https://netty.io/), an event-driven AIO TCP network engine with resource sharing, and NodeJS.  
While it is not practical to recreate NodeJS in Java, in that the event loop is the primary mechanism of the runtime, it is a good source of inspiration for such a project like this. Its event loop ensures that the I/O thread is never blocked by longer-running tasks feeding off of or into the the I/O streams. This is a central design concept of _Fiber_.

## Goals

* Implement a multi-server, multi-client framework encapsulating somewhat complex asynchronous I/O concepts.
* Expose server internals using an event-driven programming model.
* Handle network client management (disconnect from both directions, attributes, etc).
* Provide a non-blocking, non-throttled network write interface.
* Avoid network client "modes" (clients and server are always allowed to write, irrespective of state).
* Provide configuration handles for server socket bind, buffer sizes, etc.

### Non-goals

* Creating a viable alternative to existing Java networking engines, such as [Netty](https://netty.io/).

In practice, you (and I) should use a well-established approach and technology to handle high-scale production-grade workloads.

## Design and Concepts

### Networking

An initial goal of _Fiber_ was to design and implement and engine that handled TCP and UDP networking under the same higher-level interface. This was fairly soon deemed impractical, as the lower-level differences of the two network stacks are so different that a common interface for both would be way too restrictive.

_Fiber_ is there a TCP-only network engine with the goal that it should be practical to implement TCP-based protocols and applications on top of it.

### Asynchronous I/O

Two of the most central design goals of _Fiber_ are:

* Requesting I/O should never block.
* Consuming I/O should never block the I/O thread.

To accomplish these goals, all network endpoints should share the same resources, and consuming I/O streams should be deferred, to avoid blocking the I/O loop. I/O should be as lean as possible, and it should cause zero side effects.

It is also desireable that all clients (and servers) are able to read and write irrespective of client (or server) state. In other words, it should not be required to manage "network modes", "interest ops" (as we've known from the old(er) `java.nio` implements), or similar. The only requirement is that a network (and the I/O loop) is available. 

### Event Choreography

In seeking to solve the above described challenge of deferring I/O stream consumption, an event bus was devised. Typically, I/O causes side effects. An incoming HTTP requests eventually makes it way down to the RDBMS and affects one or more rows according to some criteria. This is not ideal when dealing with a multi-client scenario. To keep the I/O low-overhead, the event bus takes care of any long-running tasks, so the I/O loop is able to do what it does best: Read and write.

The _Fiber_ event bus technology is purely in-memory, by design. It provides near-zero latency event multicasting as transport mechanism for _Fiber_ internals. It is not desireable to deal with the internal workings of the network engine. Just subscribe to `NetworkReadEvent` and consume the data!

**Built-in events**

* `ClientConnectedEvent`
* `ClientDisconnectedEvent`
* `NetworkReadEvent`
* `NetworkWriteEvent`
* `ServerStartedEvent`
* `ServerStoppedEvent`

It is of course possible to define your own events. Simply subclass `BaseEvent` and fire away. Events will only ever be propagated once per listener and it is expected that the listener also provides any required error handling. Listener execution is synchronous, so take care to not block your other listeners.

The `fiber-event` project is also available to use without the rest of the components enclosed within this repository. Need a fast in-memory event bus for something else? Feel free to use it.

### Cross-platform Considerations

Under the hood, _Fiber_ uses the `java.nio` abstractions for AIO concepts, which will conveniently deliver an implementation of network channels based on the host OS. Nice! However, some limitations may arise depending on the platform. For example, on Windows, the underlying network channel is not able to receive write requests while it is still writing. This causes problems in any high-frequency network scenario.

To solve this, _Fiber_ will always optimistically attempt to write to the underlying channel. If this fails, the request is bounced off to a network client specific queue, and re-introduced to the main network queue whenever the network channel signals that the previous write was accomplished. This ensures that high-frequency network exchanges are handled gracefully under every platform.

![](assets/high-level-diagram.png)

The general usage of _Fiber_ will involve three major concepts:
* **TCP Server**  
The central network engine of _Fiber_ and encapsulation of `java.nio` AIO concepts.
* **Event Bus**  
General-purpose, in-memory event bus to hand-off operations for long-running tasks, primarily freeing the I/O resources from user code processing.
* **Platform**  
A high-level, event driven platform. Network signalling and emitted events are tied together with a "network hooks" bridge, allowing separation of concerns of the *TCP Server* and *Event Bus* abstractions.

Each concept / abstraction layer exists independently and is open for modification. It is possible for the user to override many internal components, providing custom implementations, if special use cases require so.  

![](assets/event-driven-platform.png)

## Examples

See `fiber-examples` project enclosed within this repository.
