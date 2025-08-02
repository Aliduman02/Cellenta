package com.i2i.intern.cellenta.aom.model.Voltdb;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class MicroTime {
    private long usec;
    private long time;

    public Timestamp toTimestamp() {
        return new Timestamp(time / 1000);
    }
}