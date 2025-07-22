package com.i2i.intern.cellenta.aom.model.Voltdb;

import com.i2i.intern.cellenta.aom.model.Paket;
import lombok.Getter;

import java.util.List;

@Getter
public class VoltDBGetAllPackagesResponse {
    private String status;
    private List<Paket> results;

}