package com.i2i.intern.cellenta.aom.service.impl;

import com.i2i.intern.cellenta.aom.dto.mapper.PackageMapper;
import com.i2i.intern.cellenta.aom.dto.response.PackageResponse;
import com.i2i.intern.cellenta.aom.exception.PackageNotFoundException;
import com.i2i.intern.cellenta.aom.model.Paket;
import com.i2i.intern.cellenta.aom.repository.OracleRepository;
import com.i2i.intern.cellenta.aom.service.PackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PackageServiceImpl implements PackageService {

    private final OracleRepository oracleRepository;
    private final PackageMapper packageMapper;

    @Override
    public List<PackageResponse> getAllPackages() {
        oracleRepository.connect();
        List<Paket> packages = null;
        try{
            packages = oracleRepository.getAllPackages();
        }finally {
            oracleRepository.disconnect();
        }

        return packages.stream().map(packageMapper::toPackageResponse).toList();
    }

    @Override
    public PackageResponse getPackageById(Long id) {
        oracleRepository.connect();
        Paket paket = null;
        try{
            paket =  oracleRepository.getPaketById(id)
                    .orElseThrow(() -> new PackageNotFoundException("Package not found with: " + id));
        }finally {
            oracleRepository.disconnect();
        }
        return packageMapper.toPackageResponse(paket);
    }

}
