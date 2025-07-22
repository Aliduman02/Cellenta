package com.kafka.deserializer;

import com.kafka.message.NotificationMessage;

public class NotificationMessageDeserializer extends GenericMessageDeserializer<NotificationMessage> {

    public NotificationMessageDeserializer() {
        super(NotificationMessage.class);
    }
} 