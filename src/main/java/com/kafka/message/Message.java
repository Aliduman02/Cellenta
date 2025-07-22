package com.kafka.message;

public interface Message {
    String getId();
    void setId(String id);
    String getTimestamp();
    void setTimestamp(String timestamp);
} 