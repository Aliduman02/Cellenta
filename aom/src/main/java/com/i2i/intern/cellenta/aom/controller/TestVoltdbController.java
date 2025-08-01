package com.i2i.intern.cellenta.aom.controller;

import com.i2i.intern.cellenta.aom.model.Balance;
import com.i2i.intern.cellenta.aom.model.Paket;
import com.i2i.intern.cellenta.aom.repository.OracleRepository;
import com.i2i.intern.cellenta.aom.repository.VoltdbRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/voltdb/test")
@RequiredArgsConstructor
@Tag(name = "Test For Voltdb - Don't use here", description = "")
public class TestVoltdbController {

    private final OracleRepository oracleRepository;
    private final VoltdbRepository voltdbRepository;

    /*@PostMapping("/addAllOraclesPackagesToVoltdb")
    public boolean addAllPackagesToVoltdb() {
        oracleRepository.connect();
        List<Paket> packages = oracleRepository.getAllPackages();
        oracleRepository.disconnect();
        for(Paket paket : packages) {
            voltdbRepository.createNewPackage(paket);
        }
        return false;
    }*/

    @GetMapping("/getAllBalanceByMsisdn")
    public ResponseEntity<List<Balance>> getAllBalancesByMsisdn(@RequestParam String msisdn) {
        return ResponseEntity.ok(voltdbRepository.getAllBalances(msisdn));
    }

    @GetMapping("getAllPackagesFromVoltdb")
    public ResponseEntity<List<Paket>> getAllPackagesFromVoltdb() {
        return ResponseEntity.ok(voltdbRepository.getAllPackages());
    }

    @GetMapping("getVoltdbIpAddress")
    public ResponseEntity<String> getVoltdbIpAddress() {
        return ResponseEntity.ok(voltdbRepository.mainUrl);
    }

    /*@PostMapping("/createBalance")
    public Boolean createNewBalance(@RequestBody VoltDBCreateBalanceRequest balance){
        return voltdbRepository.createNewBalance(balance);
    }*/

}
