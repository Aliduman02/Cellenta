package com.cellenta.service;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ChatBotService {
    private static final String API_KEY = "AIzaSyDHonW75FNZ5uq9NjR_eRz5r6QQifMhRyM"; // ← API anahtarınızı buraya yazın
    private static final String ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    public static String sendMessage(String prompt) {
        try {
            URL url = new URL(ENDPOINT + "?key=" + API_KEY);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Request body oluştur
            JSONObject requestBody = createRequestBody(prompt);

            // Request gönder
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Response oku
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }

                    return parseResponse(response.toString());
                }
            } else {
                // Hata durumunda error stream'i oku
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        errorResponse.append(responseLine.trim());
                    }
                    System.err.println("API Error (" + responseCode + "): " + errorResponse.toString());
                    return "Üzgünüm, şu anda yanıt veremiyorum. Lütfen daha sonra tekrar deneyin.";
                }
            }

        } catch (Exception e) {
            System.err.println("ChatBot Error: " + e.getMessage());
            e.printStackTrace();
            return "Bağlantı hatası oluştu. Lütfen internet bağlantınızı kontrol edin.";
        }
    }

    private static JSONObject createRequestBody(String prompt) {
        JSONObject requestBody = new JSONObject();

        // System instruction
        JSONObject systemInstruction = new JSONObject();
        systemInstruction.put("role", "system");

        JSONArray systemParts = new JSONArray();
        JSONObject systemTextPart = new JSONObject();
        systemTextPart.put("text", """
            You are Cellenta Bot, a virtual assistant exclusively for the Cellenta Online Charging System. You only respond to questions related to the Cellenta app, including:

            - account access (login, signup, password recovery),
            - billing and balance inquiries,
            - remaining usage (data, minutes, SMS),
            - subscription packages,
            - SMS inquiries (e.g., sending 'KALAN' to 4848),
            - CRM and Order Management-related support.

            If a user asks a question that is not related to the Cellenta system, politely respond:
            - In English: "This assistant is only able to help with questions related to the Cellenta app."
            - In Turkish: "Bu asistan sadece Cellenta uygulaması ile ilgili sorulara yardımcı olabilir."

            Do not provide general knowledge, entertainment, or personal advice. Stay professional, clear, and polite. Answer in the language the user speaks.

            Here is how the Cellenta app workflow functions:

            1. If the user does *not have an account*, guide them to sign up using:
               - First name (must be alphabetical and less than 60 characters)
               - Last name  (must be alphabetical and less than 60 characters)
               - Phone number (must start with 5 and be 10 digits total)
               - Password   (must be more than 8 characters, consisting of at least one uppercase letter, one lowercase letter and one number)

            2. If the user *has an account*, prompt them to log in with their:
               - Phone number (must start with 5 and be 10 digits total)
               - Password   (must be more than 8 characters, consisting of at least one uppercase letter, one lowercase letter and one number)

            3. If the user *forgets their password*, ask them to:
               - Click "Forgot your password?" at Login page
               - Enter their recovery email
               - Wait for a 6-digit code (takes 10 minutes max)
               - Enter the code to reset their password

            4. After login, here is what the app provides:
               - *Home*: Remaining minutes, internet GB, and number of SMS left
               - *Store*: View and purchase available packages
               - *Bills*: See past payment history
               - *Profile*: View profile details and log out

            🏷 *About Cellenta Packages*:

            - All packages are valid for *30 days*.
            - Each package includes specific minutes, SMS, and GBs.
            - Users can select packages from the Store tab.

            📦 *Package Selection Guidance*:
            Use the following logic to recommend packages:

            1. *If the user is a student or looking for a cheap package with internet*, recommend:
               - *Mini Öğrenci* (50 mins, 50 SMS, 1 GB, 25 TL)
               - *Mini Konuşma* (100 mins, 50 SMS, 250 GB, 30 TL)

            2. *If the user needs a lot of internet and is cost-sensitive*, suggest:
               - *Mini İnternet* (3 GB, 50 mins, 30 SMS, 40 TL)
               - *Genç Tarife* (4 GB, 200 mins, 100 SMS, 55 TL)
               - *Sosyal Medya Paketi* (5 GB, 100 mins, 100 SMS, 60 TL)

            3. *If the user needs heavy data for streaming or remote work*, suggest:
               - *Süper İnternet* (20 GB, 100 mins, 100 SMS, 80 TL)
               - *Full Paket* (10 GB, 1000 mins, 500 SMS, 100 TL)

            4. *If they want unlimited or family-style coverage*, suggest:
               - *Aile Paketi* (8 GB, 1500 mins, 400 SMS, 120 TL)

            5. *For users mostly calling*, suggest:
               - *Mega Konuşma* (1000 mins, 250 SMS, 1 GB, 75 TL)
               - *Standart Konuşma* (250 mins, 100 SMS, 500 GB, 50 TL)

            Always guide users based on their priorities: budget, internet need, or call time. Ask clarifying questions like:
            - "Do you use more internet or minutes?"
            - "Are you looking for the cheapest option or something more complete?"

            Assume the user may be confused or unsure about which step comes next. Help them with patience.
            """);
        systemParts.put(systemTextPart);
        systemInstruction.put("parts", systemParts);

        requestBody.put("system_instruction", systemInstruction);

        // Contents
        JSONArray contents = new JSONArray();
        JSONObject content = new JSONObject();
        JSONArray parts = new JSONArray();
        JSONObject textPart = new JSONObject();
        textPart.put("text", prompt);
        parts.put(textPart);
        content.put("parts", parts);
        contents.put(content);

        requestBody.put("contents", contents);

        return requestBody;
    }

    private static String parseResponse(String responseJson) {
        try {
            JSONObject response = new JSONObject(responseJson);
            JSONArray candidates = response.getJSONArray("candidates");
            if (candidates.length() > 0) {
                JSONObject candidate = candidates.getJSONObject(0);
                JSONObject content = candidate.getJSONObject("content");
                JSONArray parts = content.getJSONArray("parts");
                if (parts.length() > 0) {
                    JSONObject part = parts.getJSONObject(0);
                    return part.getString("text");
                }
            }
            return "Yanıt alınamadı.";
        } catch (Exception e) {
            System.err.println("Response parsing error: " + e.getMessage());
            return "Yanıt işlenirken hata oluştu.";
        }
    }
}