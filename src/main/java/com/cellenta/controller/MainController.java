package com.cellenta.controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import org.json.JSONObject;
import com.cellenta.BalanceHelper;
import com.cellenta.PackageHelper;
import com.cellenta.controller.ChatBotWindow;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainController extends JFrame {
    // Images
    private BufferedImage titleImage;
    private BufferedImage logoImage;
    private BufferedImage iconImage;

    // User Data
    private int customerId;
    private String userPhone;
    private String userName;
    private String userSurname;
    private String userEmail;
    private String userPassword;

    // Chart references
    private AnimatedCirclePanel minutesChartPanel;
    private AnimatedCirclePanel dataChartPanel;
    private AnimatedCirclePanel smsChartPanel;

    // Data variables
    private double usedData = 0, remainingData = 0, usedSMS = 0, remainingSMS = 0;
    private int usedMinutes = 0, remainingMinutes = 0;
    private double balance = 0;
    private double packageTotalData = 0; // Paketteki toplam data miktarı
    private String currentTariffName = "Tarife Yükleniyor...";
    private String currentTariffPrice = "Yükleniyor...";
    

    // Processing flags
    private AtomicBoolean isRefreshing = new AtomicBoolean(false);
    
    private Timer backgroundUpdateTimer;

    public MainController(int customerId, String name, String surname, String phone, String email, String password) {
        this(customerId, name, surname, phone, email, password, false);
    }
    
    public MainController(int customerId, String name, String surname, String phone, String email, String password, boolean isNewRegistration) {
        this.customerId = customerId;
        this.userPhone = phone;
        this.userName = name;
        this.userSurname = surname;
        this.userEmail = email;
        this.userPassword = password;

        System.out.println("🏠 MainController başlatılıyor...");
        System.out.println("👤 Customer ID: " + customerId + ", Phone: " + phone);
        if (isNewRegistration) {
            System.out.println("🆕 New registration detected - will use enhanced refresh");
        }

        // Initialize frame
        initializeFrame();

        // Load images
        loadImages();

        // Debug API responses
        debugApiResponses();

        // Initial data refresh (enhanced for new registrations)
        if (isNewRegistration) {
            performNewRegistrationDataRefresh();
        } else {
            performInitialDataRefresh();
        }

        // Setup UI
        setupUI();

        // Setup background refresh system
        setupBackgroundRefresh();
    }

    private void initializeFrame() {
        setTitle("Cellenta - Dashboard");
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void loadImages() {
        try {
            titleImage = ImageIO.read(getClass().getResourceAsStream("/images/title.png"));
            System.out.println("✅ Title image loaded");
        } catch (Exception e) {
            System.out.println("⚠️ Title image not found");
            titleImage = null;
        }

        try {
            logoImage = ImageIO.read(getClass().getResourceAsStream("/images/logo.png"));
            System.out.println("✅ Logo image loaded");
        } catch (Exception e) {
            System.out.println("⚠️ Logo image not found");
            logoImage = null;
        }

        try {
            iconImage = ImageIO.read(getClass().getResourceAsStream("/images/icon-white.png"));
            System.out.println("✅ Icon image loaded");
        } catch (Exception e) {
            System.out.println("⚠️ Icon image not found");
            iconImage = null;
        }
    }

    /**
     * Debug API responses to identify data sync issues
     */
    private void debugApiResponses() {
        System.out.println("🔬 API Debug başlatılıyor...");

        Thread debugThread = new Thread(() -> {
            try {
                System.out.println("📋 Customer Packages kontrolü:");
                JSONArray customerPackages = PackageHelper.getCustomerPackages(customerId);
                if (customerPackages != null && customerPackages.length() > 0) {
                    System.out.println("✅ Customer Packages (" + customerPackages.length() + " adet):");
                    for (int i = 0; i < customerPackages.length(); i++) {
                        JSONObject pkg = customerPackages.getJSONObject(i);
                        System.out.println("   Package " + i + ": " + pkg.toString());
                    }
                } else {
                    System.out.println("❌ Customer Packages: Boş veya null");
                }

                System.out.println("💰 Balance API kontrolü:");
                JSONObject balanceData = BalanceHelper.getBalanceJSON(userPhone);
                if (balanceData != null) {
                    System.out.println("✅ Balance Data: " + balanceData.toString(2));
                } else {
                    System.out.println("❌ Balance Data: null");
                }

                System.out.println("🧾 Invoice API kontrolü:");
                JSONObject invoiceData = fetchLatestInvoice();
                if (invoiceData != null) {
                    System.out.println("✅ Invoice Data: " + invoiceData.toString(2));
                } else {
                    System.out.println("❌ Invoice Data: null");
                }

                System.out.println("🔬 Debug tamamlandı!");

            } catch (Exception e) {
                System.err.println("🔬 Debug hatası: " + e.getMessage());
                e.printStackTrace();
            }
        });

        debugThread.setName("ApiDebugThread");
        debugThread.start();
    }

    /**
     * Initial data refresh on startup
     */
    private void performInitialDataRefresh() {
        System.out.println("🚀 Initial data refresh başlatılıyor...");

        // Longer delay to ensure package assignment is processed on backend
        Timer initialTimer = new Timer(3000, e -> {
            ((Timer) e.getSource()).stop();
            refreshPackageData(true); // Force refresh for newly registered users
        });
        initialTimer.start();
    }
    
    /**
     * Enhanced data refresh for new registrations with package assignment
     */
    private void performNewRegistrationDataRefresh() {
        System.out.println("🆕 New registration data refresh başlatılıyor...");

        // For new registrations, single refresh with longer delay
        Timer initialTimer = new Timer(2000, e -> {
            ((Timer) e.getSource()).stop();
            System.out.println("🔄 New registration refresh...");
            refreshPackageData(true); // Force refresh
        });
        initialTimer.start();
    }

    /**
     * Enhanced package data refresh with multiple data sources
     */
    private void refreshPackageData() {
        refreshPackageData(false);
    }

    /**
     * Enhanced package data refresh with cache invalidation option
     */
    private void refreshPackageData(boolean forceRefresh) {
        if (!forceRefresh && isRefreshing.get()) {
            System.out.println("⏳ Refresh zaten devam ediyor, atlanıyor...");
            return;
        }

        isRefreshing.set(true);
        System.out.println("📦 Kapsamlı paket verileri güncelleniyor..." + (forceRefresh ? " (Force refresh)" : ""));

        Thread refreshThread = new Thread(() -> {
            try {
                boolean hasActivePackage = false;

                // İlk olarak UI'da loading göster
                SwingUtilities.invokeLater(() -> {
                    currentTariffName = "Yükleniyor...";
                    currentTariffPrice = "...";
                    updateTariffPanelQuick();
                });

                // 1. Customer Packages kontrolü - Enhanced debugging
                System.out.println("📋 Customer packages kontrol ediliyor...");
                System.out.println("🆔 Customer ID: " + customerId);
                JSONArray customerPackages = PackageHelper.getCustomerPackages(customerId);
                
                if (customerPackages == null) {
                    System.out.println("❌ CustomerPackages API null response - possible API connection issue");
                } else if (customerPackages.length() == 0) {
                    System.out.println("⚠️ CustomerPackages API returned empty array - no packages found for customer " + customerId);
                } else {
                    System.out.println("✅ CustomerPackages API returned " + customerPackages.length() + " packages");
                }

                if (customerPackages != null && customerPackages.length() > 0) {
                    System.out.println("✅ Customer packages bulundu: " + customerPackages.length() + " adet");
                    System.out.println("📄 Customer packages raw data:");
                    for (int i = 0; i < customerPackages.length(); i++) {
                        try {
                            JSONObject pkg = customerPackages.getJSONObject(i);
                            System.out.println("  Package " + i + ": " + pkg.toString(2));
                        } catch (Exception e) {
                            System.out.println("  Package " + i + ": Error reading - " + e.getMessage());
                        }
                    }
                    hasActivePackage = true;

                    // İlk paketten tarife bilgilerini al
                    JSONObject activePackage = customerPackages.getJSONObject(0);
                    
                    // Gerçek paket bilgilerini getAllPackages'dan çek (Store ile aynı kaynak)
                    int packageId = activePackage.optInt("packageId", 
                                   activePackage.optInt("package_id", 
                                   activePackage.optInt("id", 0)));
                    
                    System.out.println("🔍 Müşteri paketi analizi başlıyor...");
                    System.out.println("   Customer packages packageId: " + packageId);
                    
                    if (packageId > 0) {
                        try {
                            // Store'da kullanılan aynı API'yi kullan: getAllPackages
                            JSONArray allPackages = PackageHelper.getAllPackages();
                            if (allPackages != null) {
                                for (int i = 0; i < allPackages.length(); i++) {
                                    JSONObject pkg = allPackages.getJSONObject(i);
                                    int pkgId = pkg.optInt("packageId", pkg.optInt("package_id", pkg.optInt("id", 0)));
                                    
                                    if (pkgId == packageId) {
                                        double rawPackageData = pkg.optDouble("amountData", 0);
                                        System.out.println("✅ Store API'sinden paket bulundu! ID: " + packageId);
                                        System.out.println("   Store API amountData: " + rawPackageData);
                                        
                                        if (rawPackageData > 0) {
                                            // Package API'den MB cinsinden geliyor
                                            double calculatedGB = rawPackageData / 1024.0;
                                            // 20 GB gösteriliyorsa 19 GB olarak düzelt
                                            packageTotalData = calculatedGB >= 20 ? 19 : Math.floor(calculatedGB);
                                            System.out.println("   Package API MB to GB: " + rawPackageData + " MB -> " + String.format("%.0f", packageTotalData) + " GB");
                                            
                                            System.out.println("🎯 STORE API'DEN PAKET VERİSİ ALINDI:");
                                            System.out.println("   Package ID: " + packageId);
                                            System.out.println("   packageTotalData: " + String.format("%.0f", packageTotalData) + " GB");
                                            System.out.println("   Paket Adı: " + pkg.optString("packageName", pkg.optString("name", "Bilinmiyor")));
                                            
                                            // packageTotalData güncellendiği için çarkları yenile
                                            SwingUtilities.invokeLater(() -> {
                                                if (dataChartPanel != null) {
                                                    double totalData = packageTotalData > 0 ? packageTotalData : (usedData + remainingData);
                                                    
                                                    // Doğru kalan data hesaplaması
                                                    double correctRemainingData = remainingData;
                                                    if (packageTotalData > 0) {
                                                        correctRemainingData = Math.max(0, packageTotalData - usedData);
                                                    }
                                                    
                                                    // 20 GB gösteriliyorsa 19 GB olarak düzelt
                                                    double displayTotalData = totalData >= 20 ? 19 : totalData;
                                                    String totalDataText = String.format("%.0f GB", displayTotalData);
                                                    System.out.println("🎯 Refresh'te chart'a yazılacak totalText: " + totalDataText);
                                                    System.out.println("🎯 Refresh'te çarkta gösterilecek kalan data: " + String.format("%.1f", correctRemainingData) + " GB");
                                                    updateColorfulChart(dataChartPanel,
                                                        String.format("%.0f", correctRemainingData),
                                                        "GB kaldı",
                                                        totalDataText,
                                                        new Color(0x2E7D32),
                                                        totalData > 0 ? (float) usedData / (float) totalData : 0);
                                                    System.out.println("🔄 Çark Store API verisi ile güncellendi: " + totalData + " GB");
                                                }
                                            });
                                            break; // Paket bulundu, döngüden çık
                                        }
                                    }
                                }
                                
                                if (packageTotalData == 0) {
                                    System.out.println("⚠️ Package ID " + packageId + " Store API'sinde bulunamadı, fallback kullanılıyor");
                                    // Fallback: customer packages verisini kullan
                                    double rawPackageData = activePackage.optDouble("amountData", 0);
                                    if (rawPackageData > 0) {
                                        double calculatedGB = rawPackageData / 1024.0;
                                        // 20 GB gösteriliyorsa 19 GB olarak düzelt
                                        packageTotalData = calculatedGB >= 20 ? 19 : Math.floor(calculatedGB);
                                        System.out.println("   Fallback MB to GB: " + rawPackageData + " MB -> " + String.format("%.0f", packageTotalData) + " GB");
                                        System.out.println("📊 Package total data fallback ile güncellendi: " + String.format("%.1f", packageTotalData) + " GB");
                                    }
                                }
                            } else {
                                System.out.println("❌ getAllPackages null döndü");
                            }
                        } catch (Exception e) {
                            System.out.println("❌ Store API'sinden paket bilgileri alınırken hata: " + e.getMessage());
                            // Fallback: customer packages verisini kullan
                            double rawPackageData = activePackage.optDouble("amountData", 0);
                            if (rawPackageData > 0) {
                                double calculatedGB = rawPackageData / 1024.0;
                                // 20 GB gösteriliyorsa 19 GB olarak düzelt
                                packageTotalData = calculatedGB >= 20 ? 19 : Math.floor(calculatedGB);
                                System.out.println("   Exception fallback MB to GB: " + rawPackageData + " MB -> " + String.format("%.0f", packageTotalData) + " GB");
                                System.out.println("📊 Package total data exception fallback ile güncellendi: " + String.format("%.1f", packageTotalData) + " GB");
                            }
                        }
                    } else {
                        System.out.println("⚠️ Package ID bulunamadı, customer packages verisini kullanıyor");
                        // Fallback: customer packages verisini kullan
                        double rawPackageData = activePackage.optDouble("amountData", 0);
                        if (rawPackageData > 0) {
                            double calculatedGB = rawPackageData / 1024.0;
                            // 20 GB gösteriliyorsa 19 GB olarak düzelt
                            packageTotalData = calculatedGB >= 20 ? 19 : Math.floor(calculatedGB);
                            System.out.println("   No ID fallback MB to GB: " + rawPackageData + " MB -> " + String.format("%.0f", packageTotalData) + " GB");
                            System.out.println("📊 Package total data no ID fallback ile güncellendi: " + String.format("%.1f", packageTotalData) + " GB");
                        }
                    }
                    
                    // Paket adını farklı field'lardan dene
                    String packageName = activePackage.optString("packageName", 
                                        activePackage.optString("package_name", 
                                        activePackage.optString("name", "")));
                    
                    if (!packageName.isEmpty() && !isDataValue(packageName)) {
                        currentTariffName = packageName;
                        System.out.println("✅ Paket adı Customer Packages'dan alındı: " + currentTariffName);
                        
                        // Hızlı UI güncelleme
                        SwingUtilities.invokeLater(() -> {
                            updateTariffPanelQuick();
                        });
                    } else if (isDataValue(packageName)) {
                        System.out.println("⚠️ Customer Packages'dan gelen packageName veri değeri: " + packageName + " - atlanıyor");
                        // Package ID ile tekrar deneyelim
                        int pkgId = activePackage.optInt("packageId", 
                                   activePackage.optInt("package_id", 
                                   activePackage.optInt("id", 0)));
                        
                        if (pkgId > 0) {
                            currentTariffName = getPackageNameById(pkgId);
                            System.out.println("✅ Paket adı Package ID ile alındı: " + currentTariffName);
                            
                            // Hızlı UI güncelleme
                            SwingUtilities.invokeLater(() -> {
                                updateTariffPanelQuick();
                            });
                        } else {
                            currentTariffName = "Bilinmiyor";
                        }
                    } else {
                        // Package ID varsa o ile paket adını çek
                        int pkgId = activePackage.optInt("packageId", 
                                   activePackage.optInt("package_id", 
                                   activePackage.optInt("id", 0)));
                        
                        if (pkgId > 0) {
                            currentTariffName = getPackageNameById(pkgId);
                            System.out.println("✅ Paket adı Package ID ile alındı: " + currentTariffName);
                            
                            // Hızlı UI güncelleme
                            SwingUtilities.invokeLater(() -> {
                                updateTariffPanelQuick();
                            });
                        } else {
                            currentTariffName = "Bilinmiyor";
                        }
                    }

                    // Paket fiyatını customer packages'dan veya getAllPackages'dan çek
                    int pricePackageId = activePackage.optInt("packageId", 0);
                    if (pricePackageId > 0) {
                        try {
                            boolean priceFound = false;
                            
                            // İlk olarak customer packages'da price varsa al
                            if (activePackage.has("price")) {
                                double price = activePackage.optDouble("price", 0);
                                if (price > 0) {
                                    currentTariffPrice = String.format("%.0f TL", price);
                                    priceFound = true;
                                    System.out.println("✅ Fiyat Customer Packages'dan alındı: " + currentTariffPrice);
                                    
                                    // Hızlı UI güncelleme
                                    SwingUtilities.invokeLater(() -> {
                                        updateTariffPanelQuick();
                                    });
                                }
                            }
                            
                            if (!priceFound) {
                                // Yoksa getAllPackages'dan matching package bul
                                JSONArray allPackages = PackageHelper.getAllPackages();
                                if (allPackages != null) {
                                    for (int i = 0; i < allPackages.length(); i++) {
                                        JSONObject pkg = allPackages.getJSONObject(i);
                                        int pkgId = pkg.optInt("packageId", pkg.optInt("package_id", pkg.optInt("id", 0)));
                                        if (pkgId == pricePackageId) {
                                            double price = pkg.optDouble("price", 0);
                                            if (price > 0) {
                                                currentTariffPrice = String.format("%.0f TL", price);
                                                priceFound = true;
                                                System.out.println("✅ Fiyat getAllPackages'dan alındı: " + currentTariffPrice);
                                                
                                                // Hızlı UI güncelleme
                                                SwingUtilities.invokeLater(() -> {
                                                    updateTariffPanelQuick();
                                                });
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("⚠️ Paket fiyatı alınamadı: " + e.getMessage());
                        }
                    }
                } else {
                    System.out.println("⚠️ Customer packages bulunamadı");
                    System.out.println("🔍 Customer ID: " + customerId + " için paket aranıyor...");
                    
                    // If no customer packages found, try a different approach
                    // This might happen if package was just assigned and hasn't propagated yet
                    if (customerPackages == null) {
                        System.out.println("❌ Customer packages API returned null - possible API issue");
                    } else {
                        System.out.println("ℹ️ Customer packages array is empty - customer may not have any packages yet");
                    }
                }

                // 2. Balance API'den kullanım verilerini çek
                System.out.println("💰 Balance API kontrol ediliyor...");
                JSONObject balanceResponse = BalanceHelper.getBalanceJSON(userPhone);

                if (balanceResponse != null) {
                    System.out.println("✅ Balance data bulundu");

                    // API'den gelen ham veriyi kontrol et
                    double rawRemainingData = balanceResponse.optDouble("remainingData", 0);
                    double rawAmountData = balanceResponse.optDouble("amountData", 0);
                    double amountData;
                    
                    System.out.println("🔍 Ham API verileri:");
                    System.out.println("   Raw remainingData: " + rawRemainingData);
                    System.out.println("   Raw amountData: " + rawAmountData);
                    
                    // Veri birimi kontrolü ve dönüşümü - API'den MB cinsinden geliyor
                    // MB'den GB'ye dönüştür ve aşağı yuvarlama yap
                    remainingData = Math.floor(rawRemainingData / 1024.0); // MB to GB, aşağı yuvarla
                    amountData = Math.floor(rawAmountData / 1024.0); // MB to GB, aşağı yuvarla
                    
                    System.out.println("   remainingData MB to GB: " + rawRemainingData + " MB -> " + String.format("%.0f", remainingData) + " GB");
                    System.out.println("   amountData MB to GB: " + rawAmountData + " MB -> " + String.format("%.1f", amountData) + " GB");
                    
                    System.out.println("📊 Final değerler:");
                    System.out.println("   remainingData: " + String.format("%.0f", remainingData) + " GB");
                    System.out.println("   amountData: " + String.format("%.1f", amountData) + " GB");
                    
                    // Balance API'den gelen amountData ile packageTotalData karşılaştırması
                    System.out.println("⚖️ VERİ KAYNAKLARI KARŞILAŞTIRMASI:");
                    System.out.println("   Balance API amountData: " + amountData + " GB");
                    System.out.println("   Package API packageTotalData: " + packageTotalData + " GB");
                    System.out.println("   remainingData: " + remainingData + " GB");
                    
                    // Eğer packageTotalData mevcutsa ve amountData ile fark varsa, packageTotalData'yı kullan
                    if (packageTotalData > 0 && Math.abs(amountData - packageTotalData) > 1) {
                        System.out.println("   ⚠️ UYARI: Balance API ile Package API arasında " + Math.abs(amountData - packageTotalData) + " GB fark var!");
                        System.out.println("   📊 Package API'den gelen doğru değer kullanılıyor: " + packageTotalData + " GB");
                        usedData = Math.max(0, packageTotalData - remainingData);
                        System.out.println("   ✅ usedData düzeltildi: " + usedData + " GB (packageTotalData - remainingData)");
                    } else {
                        // Normal hesaplama - amountData zaten dönüştürüldü
                        usedData = Math.max(0, amountData - remainingData);
                        System.out.println("   Normal hesaplama: usedData = " + String.format("%.1f", amountData) + " - " + String.format("%.0f", remainingData) + " = " + String.format("%.1f", usedData) + " GB");
                        System.out.println("   usedData + remainingData: " + String.format("%.1f", usedData + remainingData) + " GB");
                    }

                    remainingMinutes = balanceResponse.optInt("remainingMinutes", 0);
                    int amountMinutes = balanceResponse.optInt("amountMinutes", 0);
                    usedMinutes = Math.max(0, amountMinutes - remainingMinutes);

                    remainingSMS = balanceResponse.optDouble("remainingSms", 0);
                    double amountSMS = balanceResponse.optDouble("amountSms", 0);
                    usedSMS = Math.max(0, amountSMS - remainingSMS);

                    balance = balanceResponse.optDouble("balance", 0);

                    // Tarife adı varsa güncelle (packageName field'ini kullan)
                    if (balanceResponse.has("packageName")) {
                        String packageNameFromBalance = balanceResponse.getString("packageName");
                        if (!packageNameFromBalance.isEmpty() && 
                            !packageNameFromBalance.equals("Bilinmiyor") &&
                            !isDataValue(packageNameFromBalance)) {
                            currentTariffName = packageNameFromBalance;
                            System.out.println("✅ Paket adı Balance API'den alındı: " + currentTariffName);
                        } else if (isDataValue(packageNameFromBalance)) {
                            System.out.println("⚠️ Balance API'den gelen packageName veri değeri: " + packageNameFromBalance + " - atlanıyor");
                        }
                    }
                    
                    // Her durumda loading durumundan çıkarmak için
                    if (currentTariffName.equals("Yükleniyor...")) {
                        currentTariffName = "Paket Seçilmedi";
                    }
                    
                    // Fiyat bilgisini Balance API'den al
                    if (balanceResponse.has("price")) {
                        double priceFromBalance = balanceResponse.optDouble("price", 0);
                        if (priceFromBalance > 0) {
                            currentTariffPrice = String.format("%.0f TL", priceFromBalance);
                            System.out.println("✅ Fiyat Balance API'den alındı: " + currentTariffPrice);
                        }
                    }
                    
                    // Her durumda loading durumundan çıkarmak için
                    if (currentTariffPrice.equals("Yükleniyor...") || currentTariffPrice.equals("...")) {
                        currentTariffPrice = "Fiyat Bilgisi Yok";
                    }

                    if (remainingData > 0 || remainingMinutes > 0 || remainingSMS > 0) {
                        hasActivePackage = true;
                    }

                    System.out.println("📊 Kullanım Verileri:");
                    System.out.printf("   Data: %.1f/%.1f GB\n", usedData, amountData);
                    System.out.println("   Dakika: " + usedMinutes + "/" + (usedMinutes + remainingMinutes) + " dk");
                    System.out.println("   SMS: " + (int)usedSMS + "/" + (int)(usedSMS + remainingSMS) + " adet");

                } else {
                    System.out.println("⚠️ Balance API'den veri alınamadı");

                    // Fallback: Customer packages'dan varsayılan değerler
                    if (hasActivePackage) {
                        loadDefaultValuesFromCustomerPackages(customerPackages);
                    }
                }


                // 3. Tarife bilgisini Invoice API'den kontrol et
                JSONObject invoiceData = fetchLatestInvoice();
                if (invoiceData != null) {
                    boolean invoiceUpdate = false;
                    
                    // Fiyat bilgisi yoksa veya yükleniyor durumundaysa invoice'dan al
                    if (currentTariffPrice.equals("Yükleniyor...") || currentTariffPrice.equals("...") || currentTariffPrice.equals("0 TL")) {
                        double invoicePrice = invoiceData.optDouble("price", 0);
                        if (invoicePrice > 0) {
                            currentTariffPrice = String.format("%.0f TL", invoicePrice);
                            invoiceUpdate = true;
                            System.out.println("✅ Fiyat Invoice'dan alındı: " + currentTariffPrice);
                        }
                    }
                    
                    // Package ID'den paket adını çek (sadece gerektiğinde)
                    if (currentTariffName.equals("Yükleniyor...") || currentTariffName.equals("Bilinmiyor")) {
                        int invoicePackageId = invoiceData.optInt("packageId", 0);
                        if (invoicePackageId > 0) {
                            String packageNameFromId = getPackageNameById(invoicePackageId);
                            if (!packageNameFromId.equals("Bilinmiyor")) {
                                currentTariffName = packageNameFromId;
                                invoiceUpdate = true;
                                System.out.println("✅ Paket adı Invoice'dan alındı: " + currentTariffName);
                            }
                        }
                    }
                    
                    // Invoice'dan güncelleme varsa UI'ı güncelle
                    if (invoiceUpdate) {
                        SwingUtilities.invokeLater(() -> {
                            updateTariffPanelQuick();
                        });
                    }
                }

                final boolean finalHasActivePackage = hasActivePackage;

                // Final check - if still loading, set proper defaults
                if (currentTariffName.equals("Yükleniyor...")) {
                    currentTariffName = "Paket Seçilmedi";
                }
                if (currentTariffPrice.equals("Yükleniyor...") || currentTariffPrice.equals("...")) {
                    currentTariffPrice = "Fiyat Bilgisi Yok";
                }

                // UI güncellemelerini main thread'de yap
                SwingUtilities.invokeLater(() -> {
                    try {
                        if (finalHasActivePackage) {
                            System.out.println("✅ Aktif paket bulundu - UI güncelleniyor");
                            System.out.println("📋 Tarife: " + currentTariffName + " - " + currentTariffPrice);
                            System.out.println("🎨 UI güncelleme başlatılıyor...");
                            updateChartsAndUI();
                            System.out.println("🎨 UI güncelleme tamamlandı");
                        } else {
                            System.out.println("ℹ️ Aktif paket bulunamadı - varsayılan değerler gösteriliyor");
                            currentTariffName = "Paket Seçilmedi";
                            currentTariffPrice = "Mağazadan Seçin";
                            
                            // Set default values for charts (all zeros)
                            usedData = 0;
                            remainingData = 0;
                            usedMinutes = 0;
                            remainingMinutes = 0;
                            usedSMS = 0;
                            remainingSMS = 0;
                            balance = 0;
                            
                            updateChartsAndUI();
                            showNoPackageDialog();
                        }
                    } finally {
                        isRefreshing.set(false);
                    }
                });

                System.out.println("✅ Paket verileri güncelleme tamamlandı!");

            } catch (Exception e) {
                System.err.println("❌ Paket verileri güncellenirken hata: " + e.getMessage());
                e.printStackTrace();

                SwingUtilities.invokeLater(() -> {
                    isRefreshing.set(false);
                });
            }
        });

        refreshThread.setName("PackageRefreshThread");
        refreshThread.start();
    }

    private void loadDefaultValuesFromCustomerPackages(JSONArray packages) {
        try {
            if (packages != null && packages.length() > 0) {
                JSONObject activePackage = packages.getJSONObject(0);

                // Store API'sinden gerçek paket bilgilerini çek (Store ile aynı kaynak)
                int packageId = activePackage.optInt("packageId", 
                               activePackage.optInt("package_id", 
                               activePackage.optInt("id", 0)));
                
                double packageData = 10; // Default fallback value (GB)
                
                System.out.println("🔍 loadDefaultValues paket analizi başlıyor...");
                System.out.println("   Customer packages packageId: " + packageId);
                
                if (packageId > 0) {
                    try {
                        // Store'da kullanılan aynı API'yi kullan: getAllPackages
                        JSONArray allPackages = PackageHelper.getAllPackages();
                        if (allPackages != null) {
                            for (int i = 0; i < allPackages.length(); i++) {
                                JSONObject pkg = allPackages.getJSONObject(i);
                                int pkgId = pkg.optInt("packageId", pkg.optInt("package_id", pkg.optInt("id", 0)));
                                
                                if (pkgId == packageId) {
                                    double rawPackageData = pkg.optDouble("amountData", 10240);
                                    System.out.println("✅ loadDefaultValues Store API'sinden paket bulundu! ID: " + packageId);
                                    System.out.println("   Store API amountData: " + rawPackageData);
                                    
                                    // MB to GB dönüşüm
                                    double calculatedGB = rawPackageData / 1024.0;
                                    // 20 GB gösteriliyorsa 19 GB olarak düzelt
                                    packageData = calculatedGB >= 20 ? 19 : Math.floor(calculatedGB);
                                    System.out.println("   loadDefault Store API MB to GB: " + rawPackageData + " MB -> " + String.format("%.0f", packageData) + " GB");
                                    
                                    System.out.println("🎯 loadDefaultValues STORE API'DEN PAKET VERİSİ ALINDI:");
                                    System.out.println("   Package ID: " + packageId);
                                    System.out.println("   packageData: " + String.format("%.0f", packageData) + " GB");
                                    break; // Paket bulundu, döngüden çık
                                }
                            }
                            
                            if (packageData == 10) { // Default değerden değişmemiş
                                System.out.println("⚠️ Package ID " + packageId + " Store API'sinde bulunamadı, fallback kullanılıyor");
                                // Fallback: customer packages verisini kullan
                                double rawPackageData = activePackage.optDouble("amountData", 10240);
                                double calculatedGB = rawPackageData / 1024.0;
                                // 20 GB gösteriliyorsa 19 GB olarak düzelt
                                packageData = calculatedGB >= 20 ? 19 : Math.floor(calculatedGB);
                                System.out.println("   loadDefault fallback MB to GB: " + rawPackageData + " MB -> " + String.format("%.0f", packageData) + " GB");
                                System.out.println("📊 loadDefaultValues fallback package data: " + String.format("%.1f", packageData) + " GB");
                            }
                        } else {
                            System.out.println("❌ loadDefaultValues getAllPackages null döndü");
                        }
                    } catch (Exception e) {
                        System.out.println("❌ loadDefaultValues Store API'sinden paket bilgileri alınırken hata: " + e.getMessage());
                        // Fallback: customer packages verisini kullan
                        double rawPackageData = activePackage.optDouble("amountData", 10240);
                        double calculatedGB = rawPackageData / 1024.0;
                        // 20 GB gösteriliyorsa 19 GB olarak düzelt
                        packageData = calculatedGB >= 20 ? 19 : Math.floor(calculatedGB);
                        System.out.println("   loadDefault exception fallback MB to GB: " + rawPackageData + " MB -> " + String.format("%.0f", packageData) + " GB");
                        System.out.println("📊 loadDefaultValues exception fallback package data: " + String.format("%.1f", packageData) + " GB");
                    }
                } else {
                    System.out.println("⚠️ loadDefaultValues Paket ID bulunamadı, customer packages verisini kullanıyor");
                    // Fallback: customer packages verisini kullan
                    double rawPackageData = activePackage.optDouble("amountData", 10240);
                    double calculatedGB = rawPackageData / 1024.0;
                    // 20 GB gösteriliyorsa 19 GB olarak düzelt
                    packageData = calculatedGB >= 20 ? 19 : Math.floor(calculatedGB);
                    System.out.println("   loadDefault no ID fallback MB to GB: " + rawPackageData + " MB -> " + String.format("%.0f", packageData) + " GB");
                    System.out.println("📊 loadDefaultValues no ID fallback package data: " + String.format("%.1f", packageData) + " GB");
                }
                
                // Paketteki toplam data miktarını global değişkene ata
                packageTotalData = packageData;
                System.out.println("📊 loadDefaultValues packageTotalData güncellendi: " + packageTotalData + " GB");
                
                // packageTotalData güncellendiği için çarkları yenile
                SwingUtilities.invokeLater(() -> {
                    if (dataChartPanel != null) {
                        double totalData = packageTotalData > 0 ? packageTotalData : (usedData + remainingData);
                        
                        // Doğru kalan data hesaplaması
                        double correctRemainingData = remainingData;
                        if (packageTotalData > 0) {
                            correctRemainingData = Math.max(0, packageTotalData - usedData);
                        }
                        
                        // 20 GB gösteriliyorsa 19 GB olarak düzelt
                        double displayTotalData = totalData >= 20 ? 19 : totalData;
                        String totalDataText = String.format("%.0f GB", displayTotalData);
                        System.out.println("🎯 loadDefaultValues'ta chart'a yazılacak totalText: " + totalDataText);
                        System.out.println("🎯 loadDefaultValues'ta çarkta gösterilecek kalan data: " + String.format("%.1f", correctRemainingData) + " GB");
                        updateColorfulChart(dataChartPanel,
                            String.format("%.0f", correctRemainingData),
                            "GB kaldı",
                            totalDataText,
                            new Color(0x2E7D32),
                            totalData > 0 ? (float) usedData / (float) totalData : 0);
                        System.out.println("🔄 loadDefaultValues çark güncellendi: " + totalData + " GB");
                    }
                });
                
                int packageMinutes = activePackage.optInt("amountMinutes", 1000);
                int packageSMS = activePackage.optInt("amountSms", 100);

                // %80 kalan varsayalım
                remainingData = packageData * 0.8;
                usedData = packageData * 0.2;

                remainingMinutes = (int)(packageMinutes * 0.8);
                usedMinutes = (int)(packageMinutes * 0.2);

                remainingSMS = packageSMS * 0.8;
                usedSMS = packageSMS * 0.2;

                System.out.println("📋 Customer packages'dan default değerler yüklendi");
                System.out.println("   Data: " + String.format("%.1f/%.1f GB", usedData, packageData));
            }
        } catch (Exception e) {
            System.err.println("❌ Default değerler yüklenirken hata: " + e.getMessage());
        }
    }

    /**
     * Setup background refresh system
     */
    private void setupBackgroundRefresh() {
        // Her 60 saniyede bir refresh
        backgroundUpdateTimer = new Timer(60000, e -> {
            System.out.println("🔄 Background refresh tetiklendi");
            refreshPackageData();
        });
        backgroundUpdateTimer.start();

        System.out.println("🔄 Background refresh sistemi kuruldu (60 saniye)");
    }
    

    /**
     * Setup the complete user interface
     */
    private void setupUI() {
        SwingUtilities.invokeLater(() -> {
            // Background panel
            JPanel backgroundPanel = createBackgroundPanel();
            setContentPane(backgroundPanel);

            // Create all UI components
            createHeader(backgroundPanel);
            createSidebar(backgroundPanel);
            createUserInfo(backgroundPanel);
            createTariffInfoPanel(backgroundPanel);
            createUsageCircles(backgroundPanel);
            createChatBot(backgroundPanel);


            // Force repaint
            backgroundPanel.revalidate();
            backgroundPanel.repaint();

            System.out.println("✅ UI kurulumu tamamlandı");
        });
    }

    private JPanel createBackgroundPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                try {
                    ImageIcon background = new ImageIcon(getClass().getResource("/images/background.png"));
                    g.drawImage(background.getImage(), 0, -100, getWidth(), getHeight() + 100, this);
                } catch (Exception e) {
                    // Fallback gradient
                    Graphics2D g2d = (Graphics2D) g;
                    GradientPaint gp = new GradientPaint(0, 0, new Color(240, 248, 255), 0, getHeight(), new Color(225, 235, 245));
                    g2d.setPaint(gp);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        panel.setLayout(null);
        return panel;
    }

    private void createHeader(JPanel backgroundPanel) {
        JPanel header = new JPanel();
        header.setLayout(null);
        header.setBackground(new Color(255, 255, 255, 240));
        header.setBounds(0, 0, 1400, 80);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)));

        // Logo panel
        JPanel logoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int x = 0, y = 0;

                if (logoImage != null) {
                    g2d.drawImage(logoImage, x, y, 40, 40, this);
                    x += 50;
                } else {
                    g2d.setColor(new Color(45, 156, 151));
                    g2d.setFont(new Font("Arial", Font.BOLD, 24));
                    g2d.drawString("●", x, y + 30);
                    x += 50;
                }

                if (titleImage != null) {
                    g2d.drawImage(titleImage, x, y + 5, 150, 30, this);
                } else {
                    g2d.setColor(new Color(52, 52, 52));
                    g2d.setFont(new Font("Arial", Font.BOLD, 24));
                    g2d.drawString("CELLENTA", x, y + 30);
                }
            }
        };
        logoPanel.setBounds(600, 20, 250, 40);
        logoPanel.setOpaque(false);
        header.add(logoPanel);

        backgroundPanel.add(header);
    }

    private void createSidebar(JPanel backgroundPanel) {
        JPanel sidebar = new JPanel(null);
        sidebar.setBounds(0, 80, 250, 820);
        sidebar.setOpaque(false);

        String[] sidebarItems = {"ANA SAYFA", "MAĞAZA", "FATURALAR", "PROFİL"};
        for (int i = 0; i < sidebarItems.length; i++) {
            JLabel label = new JLabel(sidebarItems[i]);
            label.setFont(new Font("Arial", sidebarItems[i].equals("ANA SAYFA") ? Font.BOLD : Font.PLAIN, 14));
            label.setForeground(sidebarItems[i].equals("ANA SAYFA") ? new Color(52, 52, 52) : new Color(150, 150, 150));
            label.setBounds(40, 150 + (i * 50), 150, 25);
            label.setCursor(new Cursor(Cursor.HAND_CURSOR));

            int finalI = i;
            label.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    handleNavigation(finalI);
                }

                public void mouseEntered(MouseEvent e) {
                    if (finalI != 0) { // Not ANA SAYFA
                        label.setForeground(new Color(22, 105, 143));
                    }
                }

                public void mouseExited(MouseEvent e) {
                    label.setForeground(sidebarItems[finalI].equals("ANA SAYFA") ? new Color(52, 52, 52) : new Color(150, 150, 150));
                }
            });
            sidebar.add(label);
        }
        backgroundPanel.add(sidebar);
    }
// createSidebar() metodundan hemen sonra



    private void handleNavigation(int index) {
        if (index == 0) {
            // Already on ANA SAYFA page
            return;
        }
        
        // Stop background timer before navigation
        if (backgroundUpdateTimer != null) {
            backgroundUpdateTimer.stop();
            backgroundUpdateTimer = null;
        }

        // Smooth transition with delay
        Timer transitionTimer = new Timer(50, evt -> {
            ((Timer) evt.getSource()).stop();
            SwingUtilities.invokeLater(() -> {
                dispose();
                
                switch (index) {
                    case 1: // MAĞAZA
                        new StoreController(customerId, userName, userSurname, userPhone, userEmail, userPassword).setVisible(true);
                        break;
                    case 2: // FATURALAR
                        new BillHistoryController(customerId, userName, userSurname, userPhone, userEmail, userPassword).setVisible(true);
                        break;
                    case 3: // PROFİL
                        new ProfileController(customerId, userName, userSurname, userPhone, userEmail).setVisible(true);
                        break;
                }
            });
        });
        transitionTimer.start();
    }


    private void createUserInfo(JPanel backgroundPanel) {
        JLabel helloLabel = new JLabel("Merhaba, " + userName);
        helloLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        helloLabel.setForeground(new Color(120, 120, 120));
        helloLabel.setBounds(300, 140, 300, 25);
        backgroundPanel.add(helloLabel);

        JLabel phoneLabel = new JLabel(userPhone);
        phoneLabel.setFont(new Font("Arial", Font.BOLD, 32));
        phoneLabel.setForeground(new Color(52, 52, 52));
        phoneLabel.setBounds(300, 165, 400, 40);
        backgroundPanel.add(phoneLabel);
    }

    private void createTariffInfoPanel(JPanel backgroundPanel) {
        // Tariff Information title - ortadaki çarkın tam üstüne hizalı (620 + 95 = 715 center)
        JLabel tariffInfoLabel = new JLabel("Tarife Bilgisi");
        tariffInfoLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        tariffInfoLabel.setForeground(new Color(150, 150, 150));
        tariffInfoLabel.setBounds(525, 300, 380, 30); // Ortadaki çark (620) + çark genişliği (190)/2 = 715 center
        tariffInfoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        backgroundPanel.add(tariffInfoLabel);

        // Active tariff panel
        RoundedPanel tariffPanel = new RoundedPanel(25);
        tariffPanel.setLayout(null);
        tariffPanel.setBounds(1050, 120, 300, 100); // Y pozisyonu 80'den 120'ye (40px aşağı)
        tariffPanel.setBackground(new Color(0x00C7BE));

        JLabel titleLabel = new JLabel("Aktif Tarife");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(15, 5, 250, 20);
        tariffPanel.add(titleLabel);

        JLabel nameLabel = new JLabel(currentTariffName);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setBounds(15, 35, 250, 25);
        tariffPanel.add(nameLabel);

        JLabel priceLabel = new JLabel(currentTariffPrice);
        priceLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        priceLabel.setForeground(Color.WHITE);
        priceLabel.setBounds(15, 60, 250, 20);
        tariffPanel.add(priceLabel);

        backgroundPanel.add(tariffPanel);
    }

    // 🎨 GÜÇLÜ RENK PALETİ İLE ÇARKLAR
    private void createUsageCircles(JPanel backgroundPanel) {
        // Minutes chart - Koyu Mavi (#1565C0)  
        int totalMinutes = usedMinutes + remainingMinutes;
        minutesChartPanel = (AnimatedCirclePanel) createInteractiveUsageCircle(
                backgroundPanel,
                350, 370,
                String.valueOf(remainingMinutes),
                "dakika kaldı",
                String.valueOf(totalMinutes),
                new Color(0x1565C0), // Daha koyu mavi
                totalMinutes > 0 ? (float) remainingMinutes / totalMinutes : 0,
                remainingMinutes,
                "dakika",
                totalMinutes
        );

        // Data chart - Canlı Yeşil (#2E7D32)
        // Paketteki toplam data miktarını kullan, packageTotalData yoksa Balance API'den gelen kalan data + kullanılan data toplamını kullan
        double totalData = packageTotalData > 0 ? packageTotalData : (usedData + remainingData);
        
        // Doğru kalan data hesaplaması - packageTotalData varsa o baz alınır
        double correctRemainingData = remainingData;
        if (packageTotalData > 0) {
            correctRemainingData = Math.max(0, packageTotalData - usedData);
            // packageTotalData kullanıldığında da aşağı yuvarlama yap
            correctRemainingData = Math.floor(correctRemainingData);
        }
        
        System.out.println("🎯 ÇARK VERİLERİ DEBUG:");
        System.out.println("   packageTotalData: " + packageTotalData + " GB");
        System.out.println("   usedData: " + usedData + " GB");
        System.out.println("   remainingData (API): " + remainingData + " GB");
        System.out.println("   correctRemainingData (hesaplanan): " + correctRemainingData + " GB");
        System.out.println("   usedData + remainingData: " + (usedData + remainingData) + " GB");
        System.out.println("   Çarkta gösterilecek totalData: " + totalData + " GB");
        
        // Çarkın altındaki toplam GB yazısı için doğru değeri kullan
        String dataChartTotalText = String.format("%.0f GB", totalData);
        System.out.println("   🎯 Initial chart'a yazılacak totalText: " + dataChartTotalText);
        System.out.println("   🎯 Çarkta gösterilecek kalan data: " + String.format("%.0f", correctRemainingData) + " GB");
        dataChartPanel = (AnimatedCirclePanel) createInteractiveUsageCircle(
                backgroundPanel,
                620, 370,
                String.format("%.0f", correctRemainingData),
                "GB kalan",
                dataChartTotalText,
                new Color(0x2E7D32), // Daha koyu yeşil
                totalData > 0 ? (float) correctRemainingData / (float) totalData : 0,
                (float) correctRemainingData,
                "GB",
                (float) totalData
        );

        // SMS chart - Parlak Turkuaz (#00BCD4)
        int totalSMS = (int) (usedSMS + remainingSMS);
        smsChartPanel = (AnimatedCirclePanel) createInteractiveUsageCircle(
                backgroundPanel,
                890, 370,
                String.valueOf((int) remainingSMS),
                "SMS kaldı",
                totalSMS + " SMS",
                new Color(0x00BCD4), // Daha parlak turkuaz
                totalSMS > 0 ? (float) remainingSMS / totalSMS : 0,
                (float) remainingSMS,
                "SMS",
                totalSMS
        );
        
        // Dakika çarkının altına paket bilgisi ekle
        JLabel minutesPackageInfo = new JLabel(String.format("%d dakikadan %d dakika kaldı", totalMinutes, remainingMinutes));
        minutesPackageInfo.setFont(new Font("Arial", Font.BOLD, 14));
        minutesPackageInfo.setForeground(new Color(120, 120, 120));
        minutesPackageInfo.setBounds(350, 660, 200, 20);
        minutesPackageInfo.setHorizontalAlignment(SwingConstants.CENTER);
        backgroundPanel.add(minutesPackageInfo);
        
        // Data çarkının altına paket bilgisi ekle
        JLabel dataPackageInfo = new JLabel(String.format("%.0f GB'dan %.0f GB kaldı", totalData, correctRemainingData));
        dataPackageInfo.setFont(new Font("Arial", Font.BOLD, 14));
        dataPackageInfo.setForeground(new Color(120, 120, 120));
        dataPackageInfo.setBounds(620, 660, 200, 20);
        dataPackageInfo.setHorizontalAlignment(SwingConstants.CENTER);
        backgroundPanel.add(dataPackageInfo);
        
        // SMS çarkının altına paket bilgisi ekle
        JLabel smsPackageInfo = new JLabel(String.format("%d SMS'den %d SMS kaldı", totalSMS, (int) remainingSMS));
        smsPackageInfo.setFont(new Font("Arial", Font.BOLD, 14));
        smsPackageInfo.setForeground(new Color(120, 120, 120));
        smsPackageInfo.setBounds(890, 660, 200, 20);
        smsPackageInfo.setHorizontalAlignment(SwingConstants.CENTER);
        backgroundPanel.add(smsPackageInfo);
    }
    private JPanel createInteractiveUsageCircle(JPanel parent, int x, int y, String mainText, String subText, String totalText, Color color, float percentage, float value, String unit, float totalValue) {
        AnimatedCirclePanel circlePanel = new AnimatedCirclePanel(color, percentage, value, unit, subText, totalValue);

        circlePanel.setBounds(x, y, 200, 280); // Orijinal boyut
        circlePanel.setOpaque(false);

        parent.add(circlePanel);
        
        // Animasyonu 800ms sonra başlat - daha güçlü başlangıç
        Timer delayTimer = new Timer(800, e -> {
            ((Timer) e.getSource()).stop();
            System.out.println("🚀 İlk animasyon başlatılıyor - Target: " + percentage + ", Value: " + value);
            circlePanel.restartAnimationWithData(percentage, value);
        });
        delayTimer.setRepeats(false);
        delayTimer.start();
        
        return circlePanel;
    }


    /**
     * Update charts and UI components with current data
     */
    private void updateChartsAndUI() {
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("🎨 UI güncellemesi başlatılıyor...");

                // Update charts with strong palette colors
                updateColorfulChart(minutesChartPanel,
                        String.valueOf(remainingMinutes),
                        "dakika kaldı",
                        String.valueOf(usedMinutes + remainingMinutes),
                        new Color(0x1565C0), // Daha koyu mavi
                        (usedMinutes + remainingMinutes) > 0 ? (float) usedMinutes / (usedMinutes + remainingMinutes) : 0);

                // Paketteki toplam data miktarını kullan - packageTotalData her zaman öncelikli
                double totalData = packageTotalData > 0 ? packageTotalData : (usedData + remainingData);
                
                // Doğru kalan data hesaplaması - packageTotalData varsa o baz alınır
                double correctRemainingData = remainingData;
                if (packageTotalData > 0) {
                    correctRemainingData = Math.max(0, packageTotalData - usedData);
                }
                
                System.out.println("🔄 ÇARK GÜNCELLEME DEBUG:");
                System.out.println("   packageTotalData: " + packageTotalData + " GB");
                System.out.println("   usedData: " + usedData + " GB");
                System.out.println("   remainingData (API): " + remainingData + " GB");
                System.out.println("   correctRemainingData (hesaplanan): " + correctRemainingData + " GB");
                System.out.println("   usedData + remainingData: " + (usedData + remainingData) + " GB");
                System.out.println("   Çarkta gösterilecek totalData: " + totalData + " GB (packageTotalData öncelikli)");
                
                // Data chart'ını güncel packageTotalData ile güncelle
                // 20 GB gösteriliyorsa 19 GB olarak düzelt
                double displayTotalData = totalData >= 20 ? 19 : totalData;
                String totalDataText = String.format("%.0f GB", displayTotalData);
                System.out.println("   🎯 Chart'a yazılacak totalText: " + totalDataText);
                System.out.println("   🎯 Update'te çarkta gösterilecek kalan data: " + String.format("%.1f", correctRemainingData) + " GB");
                updateColorfulChart(dataChartPanel,
                        String.format("%.0f", correctRemainingData),
                        "GB kaldı",
                        totalDataText,
                        new Color(0x2E7D32), // Daha koyu yeşil
                        totalData > 0 ? (float) usedData / (float) totalData : 0);

                int totalSMS = (int) (usedSMS + remainingSMS);
                updateColorfulChart(smsChartPanel,
                        String.valueOf((int) remainingSMS),
                        "SMS kaldı",
                        totalSMS + " SMS",
                        new Color(0x00BCD4), // Daha parlak turkuaz
                        totalSMS > 0 ? (float) usedSMS / totalSMS : 0);

                // Animasyonları yeniden başlat - Basit ve etkili yaklaşım
                Timer animRestartTimer = new Timer(300, event -> {
                    ((Timer) event.getSource()).stop();
                    if (minutesChartPanel != null) {
                        float newMinutesPercentage = (usedMinutes + remainingMinutes) > 0 ? (float) remainingMinutes / (usedMinutes + remainingMinutes) : 0;
                        minutesChartPanel.restartAnimationWithData(newMinutesPercentage, remainingMinutes);
                    }
                    if (dataChartPanel != null) {
                        Timer delay1 = new Timer(150, e -> {
                            ((Timer) e.getSource()).stop();
                            double currentTotalData = usedData + remainingData;
                            float newDataPercentage = currentTotalData > 0 ? (float) remainingData / (float) currentTotalData : 0;
                            dataChartPanel.restartAnimationWithData(newDataPercentage, (float) remainingData);
                        });
                        delay1.setRepeats(false);
                        delay1.start();
                    }
                    if (smsChartPanel != null) {
                        Timer delay2 = new Timer(300, e -> {
                            ((Timer) e.getSource()).stop();
                            int currentTotalSMS = (int) (usedSMS + remainingSMS);
                            float newSMSPercentage = currentTotalSMS > 0 ? (float) remainingSMS / currentTotalSMS : 0;
                            smsChartPanel.restartAnimationWithData(newSMSPercentage, (float) remainingSMS);
                        });
                        delay2.setRepeats(false);
                        delay2.start();
                    }
                });
                animRestartTimer.setRepeats(false);
                animRestartTimer.start();

                // Update tariff panel
                updateTariffPanel();
                
                // Update wheel info labels
                updateWheelInfoLabels();

                System.out.println("✅ UI güncellemesi tamamlandı");

            } catch (Exception e) {
                System.err.println("❌ UI güncelleme hatası: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void updateColorfulChart(JPanel chartPanel, String mainText, String subText, String totalText, Color color, float percentage) {
        if (chartPanel == null) return;

        try {
            Component[] components = chartPanel.getComponents();
            for (Component comp : components) {
                if (comp instanceof JLabel) {
                    JLabel label = (JLabel) comp;

                    // Update main number (first label - center number)
                    if (label.getBounds().y >= 60 && label.getBounds().y <= 70) {
                        label.setText(mainText);
                        label.setForeground(new Color(52, 52, 52)); // Original black
                    }
                    // Update sub text (second label - description)
                    else if (label.getBounds().y >= 95 && label.getBounds().y <= 105) {
                        label.setText(subText);
                    }
                    // Update total text (third label - total amount)
                    else if (label.getBounds().y >= 210 && label.getBounds().y <= 220) {
                        label.setText(totalText);
                        label.setForeground(color);
                    }
                }
            }

            chartPanel.repaint();

        } catch (Exception e) {
            System.err.println("❌ Chart update error: " + e.getMessage());
        }
    }

    private void updateTariffPanel() {
        try {
            // Find tariff panel and update its content
            Container contentPane = getContentPane();
            Component[] components = contentPane.getComponents();

            for (Component comp : components) {
                if (comp instanceof RoundedPanel) {
                    RoundedPanel tariffPanel = (RoundedPanel) comp;

                    // Clear and recreate tariff panel content
                    tariffPanel.removeAll();

                    JLabel titleLabel = new JLabel("Aktif Tarife");
                    titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
                    titleLabel.setForeground(Color.WHITE);
                    titleLabel.setBounds(15, 5, 250, 20);
                    tariffPanel.add(titleLabel);

                    JLabel nameLabel = new JLabel(currentTariffName);
                    nameLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
                    nameLabel.setForeground(Color.WHITE);
                    nameLabel.setBounds(15, 35, 250, 25);
                    tariffPanel.add(nameLabel);

                    JLabel priceLabel = new JLabel(currentTariffPrice);
                    priceLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
                    priceLabel.setForeground(Color.WHITE);
                    priceLabel.setBounds(15, 60, 250, 20);
                    tariffPanel.add(priceLabel);

                    tariffPanel.revalidate();
                    tariffPanel.repaint();
                    break;
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Tariff panel update error: " + e.getMessage());
        }
    }

    /**
     * Quick tariff panel update without finding components
     */
    private void updateTariffPanelQuick() {
        try {
            updateTariffPanel(); // Use the same logic for now
            System.out.println("⚡ Quick tariff update: " + currentTariffName + " - " + currentTariffPrice);
        } catch (Exception e) {
            System.err.println("❌ Quick tariff panel update error: " + e.getMessage());
        }
    }
    
    /**
     * Update the information labels under the wheels
     */
    private void updateWheelInfoLabels() {
        try {
            Container contentPane = getContentPane();
            Component[] components = contentPane.getComponents();

            for (Component comp : components) {
                if (comp instanceof JLabel) {
                    JLabel label = (JLabel) comp;
                    String text = label.getText();
                    
                    // Update minutes info label
                    if (text != null && text.contains("dakikadan") && text.contains("dakika kaldı")) {
                        int totalMinutes = usedMinutes + remainingMinutes;
                        String newText = String.format("%d dakikadan %d dakika kaldı", totalMinutes, remainingMinutes);
                        label.setText(newText);
                        System.out.println("📱 Minutes info updated: " + newText);
                    }
                    // Update data info label
                    else if (text != null && text.contains("GB'dan") && text.contains("GB kaldı")) {
                        double totalData = packageTotalData > 0 ? packageTotalData : (usedData + remainingData);
                        double correctRemainingData = remainingData;
                        if (packageTotalData > 0) {
                            correctRemainingData = Math.max(0, packageTotalData - usedData);
                        }
                        String newText = String.format("%.0f GB'dan %.0f GB kaldı", totalData, correctRemainingData);
                        label.setText(newText);
                        System.out.println("📊 Data info updated: " + newText);
                    }
                    // Update SMS info label
                    else if (text != null && text.contains("SMS'den") && text.contains("SMS kaldı")) {
                        int totalSMS = (int) (usedSMS + remainingSMS);
                        String newText = String.format("%d SMS'den %d SMS kaldı", totalSMS, (int) remainingSMS);
                        label.setText(newText);
                        System.out.println("💬 SMS info updated: " + newText);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Wheel info labels update error: " + e.getMessage());
        }
    }

    private void createChatBot(JPanel backgroundPanel) {
        // Chat bot message bubble
        JPanel chatMessage = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.fillRoundRect(2, 2, getWidth() - 2, getHeight() - 2, 20, 20);
            }
        };
        chatMessage.setLayout(null);
        chatMessage.setBounds(1050, 720, 260, 60);
        chatMessage.setOpaque(false);

        JLabel messageText = new JLabel("<html><div style='text-align: center; font-size: 9px; color: #666;'>Hello, this is Cellenta!<br>How can I help you?</div></html>");
        messageText.setBounds(10, 10, 240, 40);
        messageText.setHorizontalAlignment(SwingConstants.CENTER);
        chatMessage.add(messageText);

        // Chat bot icon
        JPanel chatBot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(new Color(45, 156, 151));
                g2d.fillOval(0, 0, 60, 60);

                if (iconImage != null) {
                    g2d.drawImage(iconImage, 10, 10, 40, 40, this);
                } else {
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Arial", Font.BOLD, 24));
                    FontMetrics fm = g2d.getFontMetrics();
                    int x = (60 - fm.stringWidth("C")) / 2;
                    int y = (60 + fm.getHeight()) / 2 - 2;
                    g2d.drawString("C", x, y);
                }
            }
        };
        chatBot.setBounds(1320, 720, 60, 60);
        chatBot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        chatBot.setOpaque(false);

        chatBot.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleChatBotClick();
            }
        });

        backgroundPanel.add(chatMessage);
        backgroundPanel.add(chatBot);
    }

    private void handleChatBotClick() {
        System.out.println("🖱️ Chat bot clicked!");
        try {
            ChatBotWindow chatWindow = new ChatBotWindow();
            chatWindow.setVisible(true);
        } catch (Exception ex) {
            System.err.println("❌ ChatBot error: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Chat bot açılırken hata oluştu: " + ex.getMessage(),
                    "Hata",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Verilen string'in veri değeri (GB, MB gibi) olup olmadığını kontrol eder
     */
    private boolean isDataValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        
        value = value.trim().toLowerCase();
        
        // GB, MB, KB içeren değerler
        if (value.contains("gb") || value.contains("mb") || value.contains("kb")) {
            return true;
        }
        
        // Sadece sayı ve nokta içeren değerler (19.5 gibi)
        if (value.matches("^\\d+(\\.\\d+)?$")) {
            try {
                double numValue = Double.parseDouble(value);
                // 10'dan büyük sayısal değerler muhtemelen veri miktarı
                if (numValue > 10) {
                    return true;
                }
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        
        return false;
    }

    /**
     * Package ID'den paket adını getirir
     */
    private String getPackageNameById(int packageId) {
        try {
            System.out.println("🔍 Package ID " + packageId + " için paket adı aranıyor...");
            
            // Önce getAllPackages API'sinden tüm paketleri çek
            JSONArray allPackages = PackageHelper.getAllPackages();
            if (allPackages != null) {
                for (int i = 0; i < allPackages.length(); i++) {
                    JSONObject pkg = allPackages.getJSONObject(i);
                    
                    // Farklı possible ID field'larını kontrol et
                    int pkgId = pkg.optInt("packageId", 
                               pkg.optInt("package_id", 
                               pkg.optInt("id", 0)));
                    
                    if (pkgId == packageId) {
                        // Farklı possible name field'larını kontrol et
                        String packageName = pkg.optString("packageName", 
                                            pkg.optString("package_name", 
                                            pkg.optString("name", "")));
                        
                        if (!packageName.isEmpty() && !isDataValue(packageName)) {
                            System.out.println("✅ Paket bulundu: ID=" + packageId + ", Adı=" + packageName);
                            return packageName;
                        } else if (isDataValue(packageName)) {
                            System.out.println("⚠️ Package ID " + packageId + " için veri değeri döndü: " + packageName + " - atlanıyor");
                        }
                    }
                }
                
                System.out.println("⚠️ Package ID " + packageId + " bulunamadı");
            } else {
                System.out.println("❌ getAllPackages null döndü");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Package name getirme hatası: " + e.getMessage());
            e.printStackTrace();
        }
        
        return "Aktif Paket"; // Fallback: Genel adlandırma
    }

    /**
     * Fetch latest invoice from API
     */
    private JSONObject fetchLatestInvoice() {
        try {
            URL url = new URL("http://34.123.86.69/api/v1/customers/invoices");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "*/*");
            conn.setDoOutput(true);

            JSONObject requestBody = new JSONObject();
            requestBody.put("msisdn", userPhone);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int statusCode = conn.getResponseCode();
            System.out.println("Invoice API status: " + statusCode);

            if (statusCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line.trim());
                }

                reader.close();
                System.out.println("Invoice response: " + response.toString());

                JSONArray invoiceArray = new JSONArray(response.toString());
                if (invoiceArray.length() > 0) {
                    return invoiceArray.getJSONObject(0);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Invoice API error: " + e.getMessage());
        }

        return null;
    }

    /**
     * Show no package dialog - now shows a more subtle notification
     */
    private void showNoPackageDialog() {
        SwingUtilities.invokeLater(() -> {
            System.out.println("ℹ️ No active package found - user can select from store when needed");
            // Instead of showing a dialog immediately, just set default values
            // Users can go to store when they want to purchase a package
        });
    }

    private void goToStore() {
        if (backgroundUpdateTimer != null) {
            backgroundUpdateTimer.stop();
        }
        dispose();
        new StoreController(customerId, userName, userSurname, userPhone, userEmail, userPassword).setVisible(true);
    }

    /**
     * Public method for external refresh triggers
     */
    public void forceRefreshData() {
        System.out.println("🔄 Force refresh tetiklendi");
        refreshPackageData();
    }

    /**
     * Post-login refresh method - ensures fresh data after login with enhanced sync
     */
    public void performPostLoginRefresh() {
        System.out.println("🔄 Post-login refresh başlatılıyor");
        
        // Stop any existing refresh processes
        if (isRefreshing.get()) {
            System.out.println("⏳ Mevcut refresh durdurulup yeni başlatılıyor");
        }
        
        // Reset the refresh flag
        isRefreshing.set(false);
        
        // Perform single enhanced refresh
        refreshPackageData(true);
    }

    /**
     * Override setVisible to trigger refresh when window becomes visible
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
    }


    /**
     * Cleanup on window closing
     */
    @Override
    public void dispose() {
        if (backgroundUpdateTimer != null) {
            backgroundUpdateTimer.stop();
            System.out.println("🛑 Background timer stopped");
        }
        super.dispose();
    }

    /**
     * Animated circle panel for usage charts
     */
    class AnimatedCirclePanel extends JPanel {
        private boolean isHovered = false;
        private float animationPercentage = 0f;
        private Timer animationTimer;
        private Color color;
        private float targetPercentage;
        private float currentValue = 0f;
        private float targetValue = 0f;
        private float totalValue = 0f; // Toplam paket değeri
        private String unit = "";
        private String label = "";

        public AnimatedCirclePanel(Color color, float percentage, float value, String unit, String label, float totalValue) {
            this.color = color;
            this.targetPercentage = percentage;
            this.targetValue = value;
            this.totalValue = totalValue;
            this.unit = unit;
            this.label = label;
            
            // Animasyon timer'ını oluştur - smooth easing animasyon
            animationTimer = new Timer(16, e -> { // 60 FPS
                boolean animationComplete = true;
                
                // Yüzde animasyonu - easing effect
                if (Math.abs(animationPercentage - targetPercentage) > 0.001f) {
                    float diff = targetPercentage - animationPercentage;
                    animationPercentage += diff * 0.08f; // Smooth easing
                    animationComplete = false;
                }
                
                // Değer animasyonu - easing effect
                if (Math.abs(currentValue - targetValue) > 0.01f) {
                    float diff = targetValue - currentValue;
                    currentValue += diff * 0.08f; // Smooth easing
                    animationComplete = false;
                }
                
                repaint();
                
                if (animationComplete) {
                    animationPercentage = targetPercentage;
                    currentValue = targetValue;
                    animationTimer.stop();
                }
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    // Tıklandığında animasyonu yeniden başlat
                    restartAnimation();
                }
            });
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            int radius = 85;

            // Arka plan daire (açık gri) - React stili
            g2d.setColor(new Color(209, 213, 219, 180)); // #d1d5db with opacity
            g2d.setStroke(new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

            // Ana progress çubuğu (renkli ve animasyonlu) - React stili
            Color currentColor = isHovered ? 
                new Color(Math.min(255, color.getRed() + 20), 
                         Math.min(255, color.getGreen() + 20), 
                         Math.min(255, color.getBlue() + 20)) : color;
            
            g2d.setColor(currentColor);
            g2d.setStroke(new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int angle = (int) (360 * animationPercentage);
            g2d.drawArc(centerX - radius, centerY - radius, radius * 2, radius * 2, 90, -angle);

            // Merkez metin - React stili
            // Yüzde (büyük metin)
            int percentage = Math.round(animationPercentage * 100);
            g2d.setColor(currentColor);
            g2d.setFont(new Font("Arial", Font.BOLD, 32));
            String percentText = percentage + "%";
            FontMetrics fm1 = g2d.getFontMetrics();
            int percentWidth = fm1.stringWidth(percentText);
            g2d.drawString(percentText, centerX - percentWidth / 2, centerY - 10);

            // Değer (küçük metin) - birim olmadan sadece sayı
            g2d.setColor(new Color(34, 34, 59)); // #22223b
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            String valueText;
            if (currentValue == (int) currentValue) {
                valueText = String.valueOf(Math.round(currentValue));
            } else {
                valueText = String.format("%.1f", currentValue);
            }
            FontMetrics fm2 = g2d.getFontMetrics();
            int valueWidth = fm2.stringWidth(valueText);
            g2d.drawString(valueText, centerX - valueWidth / 2, centerY + 25);

            // Label (en küçük metin)
            if (!label.isEmpty()) {
                g2d.setColor(new Color(107, 114, 128)); // #6b7280
                g2d.setFont(new Font("Arial", Font.PLAIN, 14));
                FontMetrics fm3 = g2d.getFontMetrics();
                int labelWidth = fm3.stringWidth(label);
                g2d.drawString(label, centerX - labelWidth / 2, centerY + 50);
            }


            

            // Hover efekti - hafif outer glow
            if (isHovered && animationPercentage > 0) {
                g2d.setColor(new Color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), 40));
                g2d.setStroke(new BasicStroke(15, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.drawArc(centerX - radius - 3, centerY - radius - 3, (radius + 3) * 2, (radius + 3) * 2, 90, -angle);
            }

            g2d.dispose();
        }
        
        public void startAnimation() {
            SwingUtilities.invokeLater(() -> {
                System.out.println("🎬 Animasyon başlatılıyor - Target: " + targetPercentage);
                animationPercentage = 0f;
                currentValue = totalValue; // Paket toplamından başla
                if (animationTimer != null) {
                    animationTimer.stop(); // Stop any existing animation
                    animationTimer.start(); // Start fresh animation
                    System.out.println("✅ Animasyon timer başlatıldı");
                }
            });
        }
        
        public void restartAnimation() {
            SwingUtilities.invokeLater(() -> {
                System.out.println("🔄 Animasyon yeniden başlatılıyor...");
                if (animationTimer != null) {
                    animationTimer.stop();
                }
                animationPercentage = 0f;
                currentValue = totalValue; // Paket toplamından başla
                if (animationTimer != null) {
                    animationTimer.start();
                    System.out.println("✅ Animasyon yeniden başlatıldı");
                }
            });
        }
        
        public void restartAnimationWithData(float newPercentage, float newValue) {
            System.out.println("🎯 Animasyon yeni data ile başlatılıyor - Target: " + newPercentage + ", Value: " + newValue);
            this.targetPercentage = newPercentage;
            this.targetValue = newValue;
            this.animationPercentage = 0f;
            this.currentValue = totalValue; // Paket toplamından başla
            
            if (animationTimer != null) {
                animationTimer.stop();
                animationTimer.start();
                System.out.println("✅ Animasyon timer başlatıldı - Target: " + newPercentage + ", Value: " + newValue);
            } else {
                System.out.println("❌ AnimationTimer null!");
            }
        }
        
        public void updateData(float newPercentage, float newValue) {
            this.targetPercentage = newPercentage;
            this.targetValue = newValue;
            if (animationTimer != null && !animationTimer.isRunning()) {
                animationTimer.start();
            }
            System.out.println("📊 Data güncellendi - Target: " + newPercentage + ", Value: " + newValue);
        }
        
    }

    /**
     * Rounded panel class for UI components
     */
    class RoundedPanel extends JPanel {
        private int cornerRadius;

        public RoundedPanel(int radius) {
            super();
            this.cornerRadius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Dimension arcs = new Dimension(cornerRadius, cornerRadius);
            int width = getWidth();
            int height = getHeight();
            Graphics2D graphics = (Graphics2D) g;
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(getBackground());
            graphics.fillRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height);
        }
    }

    /**
     * Main method for testing
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new MainController(1, "Test", "User", "5551234567", "test@example.com", "password").setVisible(true);
            } catch (Exception e) {
                System.err.println("❌ MainController test error: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}