package com.i2i.intern.cellenta.packages;

import com.i2i.intern.cellenta.VoltDbService;
import com.i2i.intern.cellenta.packages.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.voltdb.client.ClientResponse;
import org.voltdb.VoltTable;

import java.util.*;

@RestController
@RequestMapping("/api/v1/package")
public class PackageController {

    @PostMapping("/create")
    public ResponseEntity<?> createPackage(@RequestBody PackageCreateRequest request) {
        Map<String, Object> response = new HashMap<>();
        VoltDbService voltDbService = null;
        try {
            voltDbService = new VoltDbService("34.10.105.56", 21212);
            Object[] params = {
                request.packageId,
                request.packageName,
                request.price,
                request.amountMinutes,
                request.amountData,
                request.amountSms,
                request.period
            };

            ClientResponse dbResponse = voltDbService.callProcedure("INSERT_PACKAGE", params);
            String status = dbResponse.getStatusString();
            response.put("status", (status == null || status.isEmpty()) ? "SUCCESS" : status);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
        } finally {
            if (voltDbService != null) voltDbService.close();
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/get")
    public ResponseEntity<?> getPackage(@RequestBody PackageGetRequest request) {
        Map<String, Object> response = new HashMap<>();
        VoltDbService voltDbService = null;
        try {
            voltDbService = new VoltDbService("34.10.105.56", 21212);
            Object[] params = { request.packageId };
            ClientResponse dbResponse = voltDbService.callProcedure("GET_PACKAGE_BY_ID", params);

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
            response.put("status", "SUCCESS");
            response.put("results", resultList);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
        } finally {
            if (voltDbService != null) voltDbService.close();
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/list")
    public ResponseEntity<?> getAllPackages() {
        Map<String, Object> response = new HashMap<>();
        VoltDbService voltDbService = null;
        try {
            voltDbService = new VoltDbService("34.10.105.56", 21212);
            ClientResponse dbResponse = voltDbService.callProcedure("GET_ALL_PACKAGES");

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
            response.put("status", "SUCCESS");
            response.put("results", resultList);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
        } finally {
            if (voltDbService != null) voltDbService.close();
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update")
    public ResponseEntity<?> updatePackage(@RequestBody PackageUpdateRequest request) {
        Map<String, Object> response = new HashMap<>();
        VoltDbService voltDbService = null;
        try {
            voltDbService = new VoltDbService("34.10.105.56", 21212);
            Object[] params = {
                request.packageName,
                request.price,
                request.amountMinutes,
                request.amountData,
                request.amountSms,
                request.period,
                request.packageId
            };
            ClientResponse dbResponse = voltDbService.callProcedure("UPDATE_PACKAGE", params);
            String status = dbResponse.getStatusString();
            response.put("status", (status == null || status.isEmpty()) ? "SUCCESS" : status);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
        } finally {
            if (voltDbService != null) voltDbService.close();
        }
        return ResponseEntity.ok(response);
    }

    // PATCH: Fiyat
    @PostMapping("/patch/price")
    public ResponseEntity<?> patchPackagePrice(@RequestBody PackagePatchPriceRequest request) {
        Map<String, Object> response = new HashMap<>();
        VoltDbService voltDbService = null;
        try {
            voltDbService = new VoltDbService("34.10.105.56", 21212);
            Object[] params = { request.price, request.packageId };
            ClientResponse dbResponse = voltDbService.callProcedure("PATCH_PACKAGE_PRICE", params);
            String status = dbResponse.getStatusString();
            response.put("status", (status == null || status.isEmpty()) ? "SUCCESS" : status);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
        } finally {
            if (voltDbService != null) voltDbService.close();
        }
        return ResponseEntity.ok(response);
    }

    // PATCH: Dakika
    @PostMapping("/patch/minutes")
    public ResponseEntity<?> patchPackageMinutes(@RequestBody PackagePatchMinutesRequest request) {
        Map<String, Object> response = new HashMap<>();
        VoltDbService voltDbService = null;
        try {
            voltDbService = new VoltDbService("34.10.105.56", 21212);
            Object[] params = { request.amountMinutes, request.packageId };
            ClientResponse dbResponse = voltDbService.callProcedure("PATCH_PACKAGE_MINUTES", params);
            String status = dbResponse.getStatusString();
            response.put("status", (status == null || status.isEmpty()) ? "SUCCESS" : status);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
        } finally {
            if (voltDbService != null) voltDbService.close();
        }
        return ResponseEntity.ok(response);
    }

    // PATCH: SMS
    @PostMapping("/patch/sms")
    public ResponseEntity<?> patchPackageSms(@RequestBody PackagePatchSmsRequest request) {
        Map<String, Object> response = new HashMap<>();
        VoltDbService voltDbService = null;
        try {
            voltDbService = new VoltDbService("34.10.105.56", 21212);
            Object[] params = { request.amountSms, request.packageId };
            ClientResponse dbResponse = voltDbService.callProcedure("PATCH_PACKAGE_SMS", params);
            String status = dbResponse.getStatusString();
            response.put("status", (status == null || status.isEmpty()) ? "SUCCESS" : status);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
        } finally {
            if (voltDbService != null) voltDbService.close();
        }
        return ResponseEntity.ok(response);
    }

    // PATCH: Data
    @PostMapping("/patch/data")
    public ResponseEntity<?> patchPackageData(@RequestBody PackagePatchDataRequest request) {
        Map<String, Object> response = new HashMap<>();
        VoltDbService voltDbService = null;
        try {
            voltDbService = new VoltDbService("34.10.105.56", 21212);
            Object[] params = { request.amountData, request.packageId };
            ClientResponse dbResponse = voltDbService.callProcedure("PATCH_PACKAGE_DATA", params);
            String status = dbResponse.getStatusString();
            response.put("status", (status == null || status.isEmpty()) ? "SUCCESS" : status);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
        } finally {
            if (voltDbService != null) voltDbService.close();
        }
        return ResponseEntity.ok(response);
    }

    // PATCH: Period
    @PostMapping("/patch/period")
    public ResponseEntity<?> patchPackagePeriod(@RequestBody PackagePatchPeriodRequest request) {
        Map<String, Object> response = new HashMap<>();
        VoltDbService voltDbService = null;
        try {
            voltDbService = new VoltDbService("34.10.105.56", 21212);
            Object[] params = { request.period, request.packageId };
            ClientResponse dbResponse = voltDbService.callProcedure("PATCH_PACKAGE_PERIOD", params);
            String status = dbResponse.getStatusString();
            response.put("status", (status == null || status.isEmpty()) ? "SUCCESS" : status);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
        } finally {
            if (voltDbService != null) voltDbService.close();
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deletePackage(@RequestBody PackageDeleteRequest request) {
        Map<String, Object> response = new HashMap<>();
        VoltDbService voltDbService = null;
        try {
            voltDbService = new VoltDbService("34.10.105.56", 21212);
            Object[] params = { request.packageId };
            ClientResponse dbResponse = voltDbService.callProcedure("DELETE_PACKAGE", params);
            String status = dbResponse.getStatusString();
            response.put("status", (status == null || status.isEmpty()) ? "SUCCESS" : status);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
        } finally {
            if (voltDbService != null) voltDbService.close();
        }
        return ResponseEntity.ok(response);
    }
}
