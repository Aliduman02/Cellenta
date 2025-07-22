package com.i2i.intern.cellenta.aom.controller;

import com.i2i.intern.cellenta.aom.dto.response.PackageResponse;
import com.i2i.intern.cellenta.aom.service.PackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/packages")
@RequiredArgsConstructor
@Tag(name = "Package API", description = "Paket işlemleri")
public class PackageController {

    private final PackageService packageService;

    @Operation(summary = "Kullanılabilir tüm tarife paketlerinin listesi döndürülür")
    @GetMapping
    public ResponseEntity<List<PackageResponse>> getPackages() {
        List<PackageResponse> packages = packageService.getAllPackages();
        return ResponseEntity.ok(packages);
    }

    @Operation(summary = "Id bazlı tarife paketi bilgisi döndürülür")
    @GetMapping("/{packageId}")
    public ResponseEntity<PackageResponse> getPackageById(@PathVariable Long packageId) {
        return ResponseEntity.ok(packageService.getPackageById(packageId));
    }

}