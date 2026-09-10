package com.jyotimoykashyap.notification.subscriber;

import com.jyotimoykashyap.notification.event.Event;

public class EmailNotifier implements Subscriber {

    @Override
    public <T> void consume(Event<T> event) {
        String oldVal = event.getOldValue() == null ? "None" : event.getOldValue().toString();
        String newVal = event.getNewValue() == null ? "None" : event.getNewValue().toString();

        System.out.println("📧 [EMAIL NOTIFICATION] Event: " + event.getEventName()
                + " | Entity ID: " + event.getEntityId()
                + " | Old: " + oldVal
                + " -> New: " + newVal
                + " | At: " + event.getEventTime());
    }
}
