package com.cellenta;

import org.json.JSONObject;

import javax.swing.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LoginHelper {

    public static JSONObject login(String msisdn, String password) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String jsonBody = String.format("{\"msisdn\":\"%s\", \"password\":\"%s\"}", msisdn, password);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://34.123.86.69/api/v1/auth/login"))
                    .header("Content-Type", "application/json")
                    .header("Device-Type", "DESKTOP")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("API response body: " + response.body());

            if (response.statusCode() == 200) {
                JSONObject jsonResponse = new JSONObject(response.body());

                if (jsonResponse.has("cust_id")) {
                    return jsonResponse;
                } else {
                    JOptionPane.showMessageDialog(null, "⚠ 'cust_id' bulunamadı.");
                }
            } else {
                JOptionPane.showMessageDialog(null, "🚫 Giriş başarısız! Kod: " + response.statusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "❌ Hata: " + e.getMessage());
        }

        return null;
    }
}
