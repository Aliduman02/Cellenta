package com.i2i.intern.cellenta.chf.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UsageType {
    MINUTES("minutes"),
    SMS("sms"),
    DATA("data");
    
    private final String value;
    
    UsageType(String value) {
        this.value = value;
    }
    
    @JsonValue
    public String getValue() {
        return value;
    }
    
    @JsonCreator
    public static UsageType fromString(String value) {
        for (UsageType type : UsageType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown usage type: " + value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}
