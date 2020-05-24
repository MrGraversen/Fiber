package io.graversen.fiber.core;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class NetworkQueue extends LinkedBlockingQueue<NetworkPayload> implements BlockingQueue<NetworkPayload> {

}
