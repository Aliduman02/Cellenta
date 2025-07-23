package org.example;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TariffApiClient {

    private static final String MIDDLEWARE_BASE_URL = "http://34.123.86.69/api/v1/balance";

    public static String getTariffFromMiddleware(String number) {
        try {
            URL url = new URL(MIDDLEWARE_BASE_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            String jsonInputString = "{\"msisdn\": \"" + number + "\"}";

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int status = conn.getResponseCode();
            if (status == 500) {
                return "Numara sisteme kayıtlı değil. Lütfen kayıt olmak için şu sayfayı ziyaret edin:\nhttps://cellanta.com/register";
            }

            InputStream responseStream = conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(responseStream, "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine;

            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            String responseBody = response.toString();
            if (!responseBody.contains("packageName")) {
                return "Numara sisteme kayıtlı değil. Lütfen kayıt olmak için şu sayfayı ziyaret edin:\nhttps://cellanta.com/register";
            }

            Pattern packagePattern = Pattern.compile("\"packageName\"\\s*:\\s*\"(.*?)\"");
            Pattern remainingMinutesPattern = Pattern.compile("\"remainingMinutes\"\\s*:\\s*(\\d+)");
            Pattern remainingDataPattern = Pattern.compile("\"remainingData\"\\s*:\\s*(\\d+)");
            Pattern remainingSmsPattern = Pattern.compile("\"remainingSms\"\\s*:\\s*(\\d+)");
            Pattern sdatePattern = Pattern.compile("\"sdate\"\\s*:\\s*\"(.*?)\"");
            Pattern edatePattern = Pattern.compile("\"edate\"\\s*:\\s*\"(.*?)\"");
            Pattern pricePattern = Pattern.compile("\"price\"\\s*:\\s*(\\d+)");
            Pattern amountMinutesPattern = Pattern.compile("\"amountMinutes\"\\s*:\\s*(\\d+)");
            Pattern amountDataPattern = Pattern.compile("\"amountData\"\\s*:\\s*(\\d+)");
            Pattern amountSmsPattern = Pattern.compile("\"amountSms\"\\s*:\\s*(\\d+)");
            Pattern periodPattern = Pattern.compile("\"period\"\\s*:\\s*(\\d+)");

            Matcher pkg = packagePattern.matcher(responseBody);
            Matcher remMin = remainingMinutesPattern.matcher(responseBody);
            Matcher remData = remainingDataPattern.matcher(responseBody);
            Matcher remSms = remainingSmsPattern.matcher(responseBody);
            Matcher sd = sdatePattern.matcher(responseBody);
            Matcher ed = edatePattern.matcher(responseBody);
            Matcher pr = pricePattern.matcher(responseBody);
            Matcher amtMin = amountMinutesPattern.matcher(responseBody);
            Matcher amtData = amountDataPattern.matcher(responseBody);
            Matcher amtSms = amountSmsPattern.matcher(responseBody);
            Matcher per = periodPattern.matcher(responseBody);

            if (pkg.find() && remMin.find() && remData.find() && remSms.find()
                    && sd.find() && ed.find() && pr.find()
                    && amtMin.find() && amtData.find() && amtSms.find() && per.find()) {

                return "<html><body style='font-family:Segoe UI, Tahoma, Geneva, Verdana, sans-serif; " +
                        "font-size:16pt; font-weight:600; color:#222222;'>" +

                        "<div style='font-size:20pt; font-weight:700; margin-bottom:10px;'>" + pkg.group(1) + "</div>" +

                        "Dakika: " + remMin.group(1) + " / " + amtMin.group(1) + "<br>" +
                        "SMS: " + remSms.group(1) + " / " + amtSms.group(1) + "<br>" +
                        "İnternet: " + remData.group(1) + " / " + amtData.group(1) + " MB<br><br>" +

                        "<b>Başlangıç Tarihi:</b><br>" + sd.group(1).substring(0, 10) + "<br>" +
                        "<b>Bitiş Tarihi:</b><br>" + ed.group(1).substring(0, 10) + "<br>" +
                        "<b>Süre:</b> " + per.group(1) + " gün<br><br>" +

                        "<b>Fiyat:</b> " + pr.group(1) + " TL" +

                        "</body></html>";







            } else {
                return "Tarife bilgileri eksik ya da hatalı döndü.";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Bir hata oluştu.";
        }
    }
}
