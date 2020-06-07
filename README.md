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

It was started a while ago as a learning project for becoming more intimate with the challenges of concurrency in TCP networking frameworks.  
One of my favourite ways to learn the "how" and "why" of technology is to throw myself at the surrounding problems, and _solve them_.  

This project was inspired from NodeJS (its "event loop" processing model) and [Netty](https://netty.io/), in that it seeks to expose an event-driven network abstraction that never blocks.  
It is constructed by several levels of abstractions, allowing the user to choose freely how much "control" they want over the network engine implementation. 

### Goals

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

`// TODO`

## Examples

`// TODO`
