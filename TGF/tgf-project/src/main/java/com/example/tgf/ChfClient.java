package com.example.tgf;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class ChfClient {

    public void sendChargingRequest(String msisdn, String usageType, int amount, long timestamp, String calledNumber) {
        try {
            @SuppressWarnings("deprecation")
            URL url = new URL("http://35.241.210.255:8080/chf/usage");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // JSON: called_number varsa ekle
            String jsonInput = (calledNumber == null)
                ? String.format(Locale.US,
                    "{\"msisdn\":\"%s\",\"usage_type\":\"%s\",\"amount\":%d,\"timestamp\":%d}",
                    msisdn, usageType.toLowerCase(Locale.ROOT), amount, timestamp)
                : String.format(Locale.US,
                    "{\"msisdn\":\"%s\",\"usage_type\":\"%s\",\"amount\":%d,\"timestamp\":%d,\"called_number\":\"%s\"}",
                    msisdn, usageType.toLowerCase(Locale.ROOT), amount, timestamp, calledNumber);

            System.out.println("JSON gönderiliyor: " + jsonInput);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInput.getBytes());
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            System.out.println("POST Response Code: " + responseCode);

            if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_CREATED) {
                System.err.println("Hata: CHF'den beklenmeyen yanıt: " + responseCode);
            }

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
