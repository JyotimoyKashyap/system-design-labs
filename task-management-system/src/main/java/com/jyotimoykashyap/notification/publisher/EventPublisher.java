package com.jyotimoykashyap.notification.publisher;

import com.jyotimoykashyap.notification.event.Event;
import com.jyotimoykashyap.notification.subscriber.Subscriber;

public interface EventPublisher {
    void addSubscriber(Subscriber subscriber);
    void removeSubscriber(Subscriber subscriber);
    <T> void notify(Event<T> event);
}
