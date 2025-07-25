package com.cellenta;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class PackageHelper {

    private static final String BASE_URL = "http://34.123.86.69/api/v1/packages";
    private static final String CUSTOMER_BASE_URL = "http://34.123.86.69/api/v1/customers";

    public static JSONArray getAllPackages() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(BASE_URL))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return new JSONArray(response.body());
            } else {
                System.out.println("Paketler alınamadı. Hata kodu: " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject getPackageById(int packageId) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(BASE_URL + "/" + packageId))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return new JSONObject(response.body());
            } else {
                System.out.println("Paket bilgisi alınamadı. Hata kodu: " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Müşteriye paket ekler
     * @param customerId Müşteri ID'si
     * @param packageId Paket ID'si
     * @return PackageResponse nesnesi (success durumu ve mesaj içerir)
     */
    public static PackageResponse addPackageToCustomer(int customerId, int packageId) {
        try {
            System.out.println("📦 PackageHelper.addPackageToCustomer çağrıldı");
            System.out.println("👤 Customer ID: " + customerId);
            System.out.println("📋 Package ID: " + packageId);
            
            if (customerId <= 0) {
                System.out.println("❌ Invalid customer ID: " + customerId);
                return new PackageResponse(false, "Geçersiz müşteri ID: " + customerId, "");
            }
            
            if (packageId <= 0) {
                System.out.println("❌ Invalid package ID: " + packageId);
                return new PackageResponse(false, "Geçersiz paket ID: " + packageId, "");
            }

            String apiUrl = CUSTOMER_BASE_URL + "/" + customerId + "/package/" + packageId;
            System.out.println("🌐 API URL: " + apiUrl);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(15))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("User-Agent", "CellentaApp/1.0")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            System.out.println("📤 HTTP POST isteği gönderiliyor...");
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📥 Response alındı!");
            System.out.println("📊 Status Code: " + response.statusCode());
            System.out.println("📄 Response Body: '" + response.body() + "'");

            // Response'a göre sonuç döndür
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                System.out.println("✅ Paket başarıyla eklendi!");
                return new PackageResponse(true, "Paket başarıyla eklendi", response.body());

            } else if (response.statusCode() == 409) {
                System.out.println("⚠️ Paket zaten mevcut!");
                return new PackageResponse(false, "Bu paket zaten hesabınızda mevcut", response.body());

            } else if (response.statusCode() == 404) {
                System.out.println("❌ 404 - Müşteri veya paket bulunamadı!");
                return new PackageResponse(false, "Müşteri veya paket bulunamadı", response.body());

            } else if (response.statusCode() == 400) {
                System.out.println("❌ 400 - Bad Request!");
                return new PackageResponse(false, "Geçersiz istek - Customer ID: " + customerId + ", Package ID: " + packageId, response.body());

            } else if (response.statusCode() >= 500) {
                System.out.println("❌ " + response.statusCode() + " - Server Error!");
                return new PackageResponse(false, "Sunucu hatası (Kod: " + response.statusCode() + ")", response.body());

            } else {
                System.out.println("❓ Bilinmeyen status: " + response.statusCode());
                return new PackageResponse(false, "Bilinmeyen yanıt (Kod: " + response.statusCode() + ")", response.body());
            }

        } catch (java.net.ConnectException e) {
            System.err.println("🌐 Bağlantı hatası: " + e.getMessage());
            return new PackageResponse(false, "Sunucuya bağlanılamadı", e.getMessage());

        } catch (java.net.SocketTimeoutException e) {
            System.err.println("⏰ Zaman aşımı: " + e.getMessage());
            return new PackageResponse(false, "İstek zaman aşımına uğradı", e.getMessage());

        } catch (Exception e) {
            System.err.println("💥 Genel hata: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            return new PackageResponse(false, "Beklenmeyen hata: " + e.getMessage(), e.toString());
        }
    }

    /**
     * Müşterinin paketlerini getirir
     * @param customerId Müşteri ID'si
     * @return Müşterinin paketleri (JSONArray)
     */
    public static JSONArray getCustomerPackages(int customerId) {
        try {
            System.out.println("📋 Müşteri paketleri getiriliyor - Customer ID: " + customerId);
            
            if (customerId <= 0) {
                System.out.println("❌ Invalid customer ID: " + customerId);
                return new JSONArray(); // Empty array for invalid ID
            }

            String apiUrl = CUSTOMER_BASE_URL + "/" + customerId + "/packages";
            System.out.println("🌐 API URL: " + apiUrl);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(apiUrl))
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(20))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📥 Customer packages response: " + response.statusCode());
            System.out.println("📄 Response body: " + response.body());

            if (response.statusCode() == 200) {
                return new JSONArray(response.body());
            } else if (response.statusCode() == 404) {
                System.out.println("ℹ️ Müşterinin paketi yok");
                return new JSONArray(); // Boş array döndür
            } else {
                System.out.println("⚠️ Müşteri paketleri alınamadı. Hata kodu: " + response.statusCode());
                return null;
            }

        } catch (Exception e) {
            System.err.println("❌ getCustomerPackages hatası: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * API bağlantısını test eder
     * @return Bağlantı başarılı ise true
     */
    public static boolean testConnection() {
        try {
            System.out.println("🧪 API bağlantısı test ediliyor...");

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(BASE_URL))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("🧪 Test sonucu - Status: " + response.statusCode());

            return response.statusCode() == 200;

        } catch (Exception e) {
            System.err.println("🧪 Test hatası: " + e.getMessage());
            return false;
        }
    }

    /**
     * Paket ekleme işleminin sonucunu temsil eden sınıf
     */
    public static class PackageResponse {
        private final boolean success;
        private final String message;
        private final String responseBody;

        public PackageResponse(boolean success, String message, String responseBody) {
            this.success = success;
            this.message = message;
            this.responseBody = responseBody;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getResponseBody() {
            return responseBody;
        }

        @Override
        public String toString() {
            return "PackageResponse{" +
                    "success=" + success +
                    ", message='" + message + '\'' +
                    ", responseBody='" + responseBody + '\'' +
                    '}';
        }
    }
}