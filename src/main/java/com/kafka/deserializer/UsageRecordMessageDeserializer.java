package com.kafka.deserializer;

import com.kafka.message.UsageRecordMessage;

public class UsageRecordMessageDeserializer extends GenericMessageDeserializer<UsageRecordMessage> {
    public UsageRecordMessageDeserializer() {
        super(UsageRecordMessage.class);
    }
} 