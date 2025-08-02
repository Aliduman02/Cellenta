package com.cellenta;

import java.net.http.*;
import java.net.URI;
import org.json.JSONObject;

public class BalanceHelper {

    public static JSONObject getBalanceJSON(String msisdn) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String jsonBody = String.format("{\"msisdn\":\"%s\"}", msisdn);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://34.123.86.69/api/v1/balance"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            System.out.println("Bakiye isteği gönderiliyor...");
            System.out.println("İstek JSON: " + jsonBody);

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status Code: " + response.statusCode());
            System.out.println("Response Body: " + response.body());

            if (response.statusCode() == 200) {
                return new JSONObject(response.body());
            } else {
                System.out.println("Bakiye alınamadı: " + response.statusCode());
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
