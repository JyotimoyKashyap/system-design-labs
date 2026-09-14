package com.jyotimoykashyap.notification.publisher;

import com.jyotimoykashyap.notification.event.Event;
import com.jyotimoykashyap.notification.subscriber.Subscriber;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class DefaultEventPublisher implements EventPublisher {

    private final CopyOnWriteArrayList<Subscriber> subscribers;
    private final Executor executor;
    private static final Logger LOGGER =
            Logger.getLogger(DefaultEventPublisher.class.getName());

    public DefaultEventPublisher (Executor executor) {
        this.executor = Objects.requireNonNull(executor);
        this.subscribers = new CopyOnWriteArrayList<>();
    }

    // default constructors for sync and async
    public static DefaultEventPublisher sync() {
        return new DefaultEventPublisher(Runnable::run);
    }

    // async
    public static DefaultEventPublisher async() {
        return new DefaultEventPublisher(Executors.newVirtualThreadPerTaskExecutor());
    }

    @Override
    public void addSubscriber(Subscriber subscriber) {
        Objects.requireNonNull(subscriber, "Subscriber cannot be null");
        // this `contains` check takes O(n) but that is fine for a
        // small list that mostly doesn't change much
        boolean added = subscribers.addIfAbsent(subscriber);
        if (!added) {
            LOGGER.warning("Subscriber is already present");
        }
    }

    @Override
    public void removeSubscriber(Subscriber subscriber) {
        Objects.requireNonNull(subscriber, "Subscriber cannot be null");
        subscribers.remove(subscriber);
    }

    @Override
    public <T> void notify(Event<T> event) {
        Objects.requireNonNull(event, "Event cannot be null");

        subscribers.forEach(subscriber -> {
            executor.execute(() -> {
                // delegate execution to the configured executor
                try {
                    subscriber.consume(event);
                } catch (Exception e) {
                    System.err.println("Failed to notify subscriber: "
                            + e.getMessage());
                }
            });
        });
    }
}
