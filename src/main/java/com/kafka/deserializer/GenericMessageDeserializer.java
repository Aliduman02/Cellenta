package com.kafka.deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.apache.kafka.common.serialization.Deserializer;
import java.util.Map;

public class GenericMessageDeserializer<T> implements Deserializer<T> {
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
            .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
    private Class<T> type;

    public GenericMessageDeserializer(Class<T> type) {
        this.type = type;
    }

    public GenericMessageDeserializer() {}

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {}

    @Override
    public T deserialize(String topic, byte[] data) {
        try {
            if (data == null || data.length == 0) return null;
            if (type == null) throw new IllegalStateException("Type must be set for GenericMessageDeserializer");
            return objectMapper.readValue(data, type);
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing message", e);
        }
    }

    @Override
    public void close() {}

    public void setType(Class<T> type) {
        this.type = type;
    }
} 