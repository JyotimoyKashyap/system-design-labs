package com.jyotimoykashyap.notification.subscriber;

import com.jyotimoykashyap.notification.event.Event;

public interface Subscriber {
    <T> void consume(Event<T> event);
}
