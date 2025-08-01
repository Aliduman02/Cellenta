package com.i2i.intern.cellenta.aom.controller;

import com.i2i.intern.cellenta.aom.repository.OracleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.i2i.cellenta.hazelcast.service.MsisdnService;

import java.util.List;

@Tag(name = "Test For Hazelcast - Don't use here", description = "")
@RestController
@RequestMapping("/api/v1/hazelcast/test")
public class TestHazelcastController {
    private final OracleRepository oracleRepository;

    public TestHazelcastController(OracleRepository oracleRepository) {
        this.oracleRepository = oracleRepository;
    }

    @PostMapping("/registerMsisdn")
    public ResponseEntity<Boolean> registerUserToHazelcast(@RequestBody String msisdn){
        MsisdnService.registerMsisdn(msisdn);
        return ResponseEntity.ok(true);
    }

    @GetMapping("/getAllRegisteredMsisdns")
    public ResponseEntity<List<String>> getAllRegisteredUser(){
        return ResponseEntity.ok(MsisdnService.getAllRegisteredMsisdns());
    }

    /*@DeleteMapping("/deleteRegisteredMsisdn")
    public ResponseEntity<Void> deleteRegisteredUser(@RequestParam("msisdn") String msisdn){
        MsisdnService.unregisterMsisdn(msisdn);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/deleteAllRegisteredMsisdns")
    public ResponseEntity<Void> deleteAllRegisteredUser(){
        MsisdnService.unregisterAllMsisdns();
        return ResponseEntity.noContent().build();
    }*/

    @GetMapping("/getMsisdnsHasPackageFromOracle")
    public ResponseEntity<List<String>> getMsisdnsHaspacage(){
        return ResponseEntity.ok(oracleRepository.getMsisdnsHasPackage());
    }

    @Operation(summary = "ATTENTİON - DONT USE THİS METOD")
    @GetMapping("/addHazelcastMsisdnsHasPackageFromOracle")
    public ResponseEntity<List<String>> addHazelcastMsisdnsHasPackageFromOracle(){
        MsisdnService.unregisterAllMsisdns();
        for (String s : oracleRepository.getMsisdnsHasPackage()) {
            MsisdnService.registerMsisdn(s);
        }
        return ResponseEntity.ok(oracleRepository.getMsisdnsHasPackage());
    }



}
