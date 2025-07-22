package com.kafka.deserializer;

import com.kafka.message.BalanceMessage;

public class BalanceMessageDeserializer extends GenericMessageDeserializer<BalanceMessage> {

    public BalanceMessageDeserializer() {
        super(BalanceMessage.class);
    }
} 