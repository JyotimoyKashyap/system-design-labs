package com.jyotimoykashyap.notification.publisher;

import com.jyotimoykashyap.notification.event.Event;
import com.jyotimoykashyap.notification.subscriber.Subscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SimpleEventPublisher implements EventPublisher {

    private final List<Subscriber> subscribers;

    public SimpleEventPublisher() {
        this.subscribers = new ArrayList<>();
    }

    @Override
    public void addSubscriber(Subscriber subscriber) {
        Objects.requireNonNull(subscriber);
        this.subscribers.add(subscriber);
    }

    @Override
    public void removeSubscriber(Subscriber subscriber) {
        Objects.requireNonNull(subscriber);
        this.subscribers.remove(subscriber);
    }

    @Override
    public <T> void notify(Event<T> event) {
        Objects.requireNonNull(event);
        subscribers.forEach(subscriber -> {
            subscriber.consume(event);
        });
    }
}
