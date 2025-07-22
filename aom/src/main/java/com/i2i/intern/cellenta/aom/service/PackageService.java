package com.i2i.intern.cellenta.aom.service;

import com.i2i.intern.cellenta.aom.dto.response.PackageResponse;

import java.util.List;

public interface PackageService {

    List<PackageResponse> getAllPackages();
    PackageResponse getPackageById(Long id);

}
