package com.kafka.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.kafka.common.serialization.Serializer;
import java.util.Map;

public class GenericMessageSerializer<T> implements Serializer<T> {
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    public GenericMessageSerializer() {}

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {}

    @Override
    public byte[] serialize(String topic, T data) {
        try {
            if (data == null) return null;
            return objectMapper.writeValueAsBytes(data);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing message", e);
        }
    }

    @Override
    public void close() {}
} 