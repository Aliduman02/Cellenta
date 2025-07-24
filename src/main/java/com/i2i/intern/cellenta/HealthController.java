package com.i2i.intern.cellenta;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.voltdb.client.ClientResponse;
import org.voltdb.VoltTable;

import java.util.*;

@RestController
public class HealthController {

    @GetMapping("/health")
    public String health() {
        return "VoltDB Gateway is running and accessible from network!";
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "VoltDB Gateway");
        status.put("status", "UP");
        status.put("port", 8081);
        status.put("network_accessible", true);
        status.put("timestamp", System.currentTimeMillis());
        return status;
    }

    @PostMapping("/chf-test")
    public ResponseEntity<Map<String, Object>> chfConnectionTest(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        response.put("gateway_status", "OK");
        response.put("message", "CHF module connected successfully");
        response.put("received_from_chf", request);
        response.put("timestamp", System.currentTimeMillis());
        response.put("server_info", "VoltDB Gateway ready for CHF requests");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/procedure")
    public ResponseEntity<?> callVoltDbProcedure(@RequestBody Map<String, Object> request) {
        String procedureName = (String) request.get("procedure");
        List<Object> params = (List<Object>) request.get("params");
        Map<String, Object> response = new HashMap<>();
        VoltDbService voltDbService = null;

        try {
            voltDbService = new VoltDbService("34.10.105.56", 21212); // Sunucu ve portunu gerektiği gibi ayarlayabilirsin
            Object[] paramArray = (params != null) ? params.toArray() : new Object[0];
            ClientResponse dbResponse = voltDbService.callProcedure(procedureName, paramArray);

            response.put("status", dbResponse.getStatusString());

            // VoltTable'ı düzgün şekilde JSON'a çevir
            List<Map<String, Object>> resultList = new ArrayList<>();
            if (dbResponse.getResults().length > 0) {
                VoltTable vt = dbResponse.getResults()[0];
                while (vt.advanceRow()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 0; i < vt.getColumnCount(); i++) {
                        String colName = vt.getColumnName(i);
                        row.put(colName, vt.get(i, vt.getColumnType(i)));
                    }
                    resultList.add(row);
                }
            }
            response.put("results", resultList);
        } catch (Exception e) {
            response.put("error", e.getMessage());
        } finally {
            if (voltDbService != null) voltDbService.close();
        }
        return ResponseEntity.ok(response);
    }
}
