package com.i2i.intern.cellenta.balance;

import com.i2i.intern.cellenta.VoltDbService;
import com.i2i.intern.cellenta.balance.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.voltdb.client.ClientResponse;
import org.voltdb.VoltTable;

import java.util.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@RestController
@RequestMapping("/api/v1/balance")
public class BalanceController {

    @PostMapping("/create")
    public ResponseEntity<?> createBalance(@RequestBody BalanceCreateRequest request) {
        Map<String, Object> response = new HashMap<>();
        VoltDbService voltDbService = null;
        try {
            voltDbService = new VoltDbService("34.10.105.56", 21212);
            Object[] params = {
                request.msisdn,
                request.packageId,
                request.remainingMinutes,
                request.remainingSms,
                request.remainingData,
                request.startDate,
                request.endDate
            };
            ClientResponse dbResponse = voltDbService.callProcedure("INSERT_BALANCE", params);
            String status = dbResponse.getStatusString();
            if (status == null || status.isEmpty()) {
                VoltTable[] results = dbResponse.getResults();
                if (results.length > 0 && results[0].advanceRow()) {
                    long modified = (long) results[0].get("modified_tuples", org.voltdb.VoltType.BIGINT);
                    status = (modified > 0) ? "SUCCESS" : "ERROR";
                } else {
                    status = "ERROR";
                }
            }
            response.put("status", status);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
        } finally {
            if (voltDbService != null) voltDbService.close();
        }
        return ResponseEntity.ok(response);
    }

    // --- YENİ: Paket tablosundan otomatik değerlerle balance create ---
    @PostMapping("/create-with-defaults")
    public ResponseEntity<?> createBalanceWithDefaults(@RequestBody BalanceCreateWithDefaultsRequest request) {
        Map<String, Object> response = new HashMap<>();
        VoltDbService voltDbService = null;
        try {
            voltDbService = new VoltDbService("34.10.105.56", 21212);
            Object[] params = {
                request.msisdn,
                request.packageId
            };
            ClientResponse dbResponse = voltDbService.callProcedure("INSERT_BALANCE_WITH_DEFAULTS", params);
            String status = dbResponse.getStatusString();
            if (status == null || status.isEmpty()) {
                VoltTable[] results = dbResponse.getResults();
                if (results.length > 0 && results[0].advanceRow()) {
                    long modified = (long) results[0].get("modified_tuples", org.voltdb.VoltType.BIGINT);
                    status = (modified > 0) ? "SUCCESS" : "ERROR";
                } else {
                    status = "ERROR";
                }
            }
            response.put("status", status);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
        } finally {
            if (voltDbService != null) voltDbService.close();
        }
        return ResponseEntity.ok(response);
    }
/*
    @PostMapping("/get")
    public ResponseEntity<?> getBalance(@RequestBody BalanceGetRequest request) {
        Map<String, Object> response = new HashMap<>();
        VoltDbService voltDbService = null;
        try {
            voltDbService = new VoltDbService("34.10.105.56", 21212);
            Object[] params = { request.msisdn, request.packageId };
            ClientResponse dbResponse = voltDbService.callProcedure("GET_BALANCE_BY_ID", params);

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
*/


    @PostMapping("/get")
    public ResponseEntity<?> getBalance(@RequestBody BalanceGetRequest request) {
        Map<String, Object> response = new HashMap<>();
        VoltDbService voltDbService = null;
        try {
            voltDbService = new VoltDbService("34.10.105.56", 21212);
            Object[] params = { request.msisdn, request.packageId };
            ClientResponse dbResponse = voltDbService.callProcedure("GET_BALANCE_BY_ID", params);

            List<Map<String, Object>> resultList = new ArrayList<>();
            if (dbResponse.getResults().length > 0) {
                VoltTable vt = dbResponse.getResults()[0];

                // Tarih formatı - görseldeki gibi
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");

                while (vt.advanceRow()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 0; i < vt.getColumnCount(); i++) {
                        String colName = vt.getColumnName(i);
                        Object value = vt.get(i, vt.getColumnType(i));


                        // Sadece _DATE ile biten kolonlar için özel kontrol
                        if (colName.endsWith("_DATE")) {
                            String formatted = null;
                            try {
                                if (value instanceof java.sql.Timestamp) {
                                    formatted = sdf.format((java.sql.Timestamp) value);
                                } else if (value instanceof Map) {
                                    Map<?,?> valMap = (Map<?,?>) value;
                                    if (valMap.containsKey("time")) {
                                        long microSeconds = ((Number) valMap.get("time")).longValue();
                                        long millis = microSeconds / 1000;
                                        java.sql.Timestamp ts = new java.sql.Timestamp(millis);
                                        formatted = sdf.format(ts);
                                    }
                                } else if (value instanceof Long) {
                                    long microSeconds = (Long) value;
                                    long millis = microSeconds / 1000;
                                    java.sql.Timestamp ts = new java.sql.Timestamp(millis);
                                    formatted = sdf.format(ts);
                                } else if (value != null && value.getClass().getName().contains("Volt")) {
                                    // Bazı VoltDB özel tipleri olabilir, String'e çevirerek koy
                                    formatted = value.toString();
                                } else if (value instanceof String) {
                                    formatted = value.toString();
                                } else {
                                    // Fallback: Herhangi bir başka format
                                    formatted = value != null ? value.toString() : null;
                                }
                                row.put(colName, formatted);
                            } catch (Exception ex) {
                                row.put(colName, value); // hata olursa orijinalini koy
                            }
                        } else {
                            row.put(colName, value);
                        }
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
    public ResponseEntity<?> updateBalance(@RequestBody BalanceUpdateRequest request) {
        Map<String, Object> response = new HashMap<>();
        VoltDbService voltDbService = null;
        try {
            voltDbService = new VoltDbService("34.10.105.56", 21212);
            Object[] params = {
                request.remainingMinutes,
                request.remainingSms,
                request.remainingData,
                request.startDate,
                request.endDate,
                request.msisdn,
                request.packageId
            };
            ClientResponse dbResponse = voltDbService.callProcedure("UPDATE_BALANCE", params);

            String status = dbResponse.getStatusString();
            if (status == null || status.isEmpty()) {
                VoltTable[] results = dbResponse.getResults();
                if (results.length > 0 && results[0].advanceRow()) {
                    long modified = (long) results[0].get("modified_tuples", org.voltdb.VoltType.BIGINT);
                    status = (modified > 0) ? "SUCCESS" : "ERROR";
                } else {
                    status = "ERROR";
                }
            }
            response.put("status", status);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
        } finally {
            if (voltDbService != null) voltDbService.close();
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update-by-msisdn")
    public ResponseEntity<?> updateBalanceByMsisdn(@RequestBody BalanceUpdateByMsisdnRequest request) {
        Map<String, Object> response = new HashMap<>();
        VoltDbService voltDbService = null;
        try {
            voltDbService = new VoltDbService("34.10.105.56", 21212);
            Object[] params = {
                request.remainingMinutes,
                request.remainingSms,
                request.remainingData,
                request.msisdn
            };
            ClientResponse dbResponse = voltDbService.callProcedure("UPDATE_BALANCE_BY_MSISDN", params);

            String status = dbResponse.getStatusString();
            if (status == null || status.isEmpty()) {
                VoltTable[] results = dbResponse.getResults();
                if (results.length > 0 && results[0].advanceRow()) {
                    long modified = (long) results[0].get("modified_tuples", org.voltdb.VoltType.BIGINT);
                    status = (modified > 0) ? "SUCCESS" : "ERROR";
                } else {
                    status = "ERROR";
                }
            }
            response.put("status", status);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
        } finally {
            if (voltDbService != null) voltDbService.close();
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/patch/minutes")
    public ResponseEntity<?> patchBalanceMinutes(@RequestBody BalancePatchMinutesRequest request) {
        Map<String, Object> response = new HashMap<>();
        VoltDbService voltDbService = null;
        try {
            voltDbService = new VoltDbService("34.10.105.56", 21212);
            Object[] params = {
                request.remainingMinutes,
                request.msisdn,
                request.packageId
            };
            ClientResponse dbResponse = voltDbService.callProcedure("PATCH_BALANCE_MINUTES", params);

            String status = dbResponse.getStatusString();
            if (status == null || status.isEmpty()) {
                VoltTable[] results = dbResponse.getResults();
                if (results.length > 0 && results[0].advanceRow()) {
                    long modified = (long) results[0].get("modified_tuples", org.voltdb.VoltType.BIGINT);
                    status = (modified > 0) ? "SUCCESS" : "ERROR";
                } else {
                    status = "ERROR";
                }
            }
            response.put("status", status);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
        } finally {
            if (voltDbService != null) voltDbService.close();
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/patch/sms")
    public ResponseEntity<?> patchBalanceSms(@RequestBody BalancePatchSmsRequest request) {
        Map<String, Object> response = new HashMap<>();
        VoltDbService voltDbService = null;
        try {
            voltDbService = new VoltDbService("34.10.105.56", 21212);
            Object[] params = {
                request.remainingSms,
                request.msisdn,
                request.packageId
            };
            ClientResponse dbResponse = voltDbService.callProcedure("PATCH_BALANCE_SMS", params);

            String status = dbResponse.getStatusString();
            if (status == null || status.isEmpty()) {
                VoltTable[] results = dbResponse.getResults();
                if (results.length > 0 && results[0].advanceRow()) {
                    long modified = (long) results[0].get("modified_tuples", org.voltdb.VoltType.BIGINT);
                    status = (modified > 0) ? "SUCCESS" : "ERROR";
                } else {
                    status = "ERROR";
                }
            }
            response.put("status", status);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
        } finally {
            if (voltDbService != null) voltDbService.close();
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/patch/data")
    public ResponseEntity<?> patchBalanceData(@RequestBody BalancePatchDataRequest request) {
        Map<String, Object> response = new HashMap<>();
        VoltDbService voltDbService = null;
        try {
            voltDbService = new VoltDbService("34.10.105.56", 21212);
            Object[] params = {
                request.remainingData,
                request.msisdn,
                request.packageId
            };
            ClientResponse dbResponse = voltDbService.callProcedure("PATCH_BALANCE_DATA", params);

            String status = dbResponse.getStatusString();
            if (status == null || status.isEmpty()) {
                VoltTable[] results = dbResponse.getResults();
                if (results.length > 0 && results[0].advanceRow()) {
                    long modified = (long) results[0].get("modified_tuples", org.voltdb.VoltType.BIGINT);
                    status = (modified > 0) ? "SUCCESS" : "ERROR";
                } else {
                    status = "ERROR";
                }
            }
            response.put("status", status);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
        } finally {
            if (voltDbService != null) voltDbService.close();
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteBalance(@RequestBody BalanceDeleteRequest request) {
        Map<String, Object> response = new HashMap<>();
        VoltDbService voltDbService = null;
        try {
            voltDbService = new VoltDbService("34.10.105.56", 21212);
            Object[] params = { request.msisdn, request.packageId };
            ClientResponse dbResponse = voltDbService.callProcedure("DELETE_BALANCE", params);

            String status = dbResponse.getStatusString();
            if (status == null || status.isEmpty()) {
                VoltTable[] results = dbResponse.getResults();
                if (results.length > 0 && results[0].advanceRow()) {
                    long modified = (long) results[0].get("modified_tuples", org.voltdb.VoltType.BIGINT);
                    status = (modified > 0) ? "SUCCESS" : "ERROR";
                } else {
                    status = "ERROR";
                }
            }
            response.put("status", status);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
        } finally {
            if (voltDbService != null) voltDbService.close();
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/list-by-msisdn")
    public ResponseEntity<?> listBalancesByMsisdn(@RequestBody BalanceByMsisdnRequest request) {
        Map<String, Object> response = new HashMap<>();
        VoltDbService voltDbService = null;

        try {
            voltDbService = new VoltDbService("34.10.105.56", 21212);
            Object[] params = { request.msisdn };
            ClientResponse dbResponse = voltDbService.callProcedure("GET_BALANCES_BY_MSISDN", params);

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

    // --- MSISDN'e göre balance + package bilgisi ---
    @PostMapping("/with-package-info")
    public ResponseEntity<?> getBalanceWithPackageInfo(@RequestBody BalanceByMsisdnRequest request) {
        Map<String, Object> response = new HashMap<>();
        VoltDbService voltDbService = null;
        try {
            voltDbService = new VoltDbService("34.10.105.56", 21212);
            Object[] params = { request.msisdn };
            ClientResponse dbResponse = voltDbService.callProcedure("GET_BALANCE_WITH_PACKAGE_INFO_BY_MSISDN", params);

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




    @PostMapping("/switch-package")
    public ResponseEntity<?> switchPackage(@RequestBody Map<String, Object> request) {
        long msisdn = Long.parseLong(request.get("msisdn").toString());
        int newPackageId = Integer.parseInt(request.get("newPackageId").toString());

        Map<String, Object> response = new HashMap<>();
        VoltDbService voltDbService = null;
        try {
            voltDbService = new VoltDbService("34.10.105.56", 21212);

            ClientResponse balances = voltDbService.callProcedure("GET_BALANCES_BY_MSISDN", msisdn);
            VoltTable vt = balances.getResults()[0];
            while (vt.advanceRow()) {
                int currentPackageId = (int) vt.getLong("PACKAGE_ID");
                voltDbService.callProcedure("DELETE_BALANCE", msisdn, currentPackageId);
            }


            ClientResponse packageResponse = voltDbService.callProcedure("GET_PACKAGE_BY_ID", newPackageId);
            VoltTable result = packageResponse.getResults()[0];
            result.advanceRow();
            int minutes = (int) result.getLong("AMOUNT_MINUTES");
            int sms = (int) result.getLong("AMOUNT_SMS");
            int data = (int) result.getLong("AMOUNT_DATA");
            int period = (int) result.getLong("PERIOD");

            Timestamp startDate = new Timestamp(System.currentTimeMillis());
            Timestamp endDate = new Timestamp(startDate.getTime() + (long)period * 24 * 60 * 60 * 1000);

  
            voltDbService.callProcedure("INSERT_BALANCE", msisdn, newPackageId, minutes, sms, data, startDate, endDate);

            response.put("status", "SUCCESS");
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
        } finally {
            if (voltDbService != null) voltDbService.close();
        }
        return ResponseEntity.ok(response);
    }

}
