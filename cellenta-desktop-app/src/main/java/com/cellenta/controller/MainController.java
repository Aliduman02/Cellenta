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
    private String currentTariffName = "Tarife Yükleniyor...";
    private String currentTariffPrice = "Yükleniyor...";

    // Processing flags
    private AtomicBoolean isRefreshing = new AtomicBoolean(false);
    private Timer backgroundUpdateTimer;

    public MainController(int customerId, String name, String surname, String phone, String email, String password) {
        this.customerId = customerId;
        this.userPhone = phone;
        this.userName = name;
        this.userSurname = surname;
        this.userEmail = email;
        this.userPassword = password;

        System.out.println("🏠 MainController başlatılıyor...");
        System.out.println("👤 Customer ID: " + customerId + ", Phone: " + phone);

        // Initialize frame
        initializeFrame();

        // Load images
        loadImages();

        // Debug API responses
        debugApiResponses();

        // Initial data refresh
        performInitialDataRefresh();

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

        // Immediate start with minimal delay for UI to settle
        Timer initialTimer = new Timer(100, e -> {
            ((Timer) e.getSource()).stop();
            refreshPackageData();
        });
        initialTimer.start();
    }

    /**
     * Enhanced package data refresh with multiple data sources
     */
    private void refreshPackageData() {
        if (isRefreshing.get()) {
            System.out.println("⏳ Refresh zaten devam ediyor, atlanıyor...");
            return;
        }

        isRefreshing.set(true);
        System.out.println("📦 Kapsamlı paket verileri güncelleniyor...");

        Thread refreshThread = new Thread(() -> {
            try {
                boolean hasActivePackage = false;

                // İlk olarak UI'da loading göster
                SwingUtilities.invokeLater(() -> {
                    currentTariffName = "Yükleniyor...";
                    currentTariffPrice = "...";
                    updateTariffPanelQuick();
                });

                // 1. Customer Packages kontrolü
                System.out.println("📋 Customer packages kontrol ediliyor...");
                JSONArray customerPackages = PackageHelper.getCustomerPackages(customerId);

                if (customerPackages != null && customerPackages.length() > 0) {
                    System.out.println("✅ Customer packages bulundu: " + customerPackages.length() + " adet");
                    hasActivePackage = true;

                    // İlk paketten tarife bilgilerini al
                    JSONObject activePackage = customerPackages.getJSONObject(0);
                    
                    // Paket adını farklı field'lardan dene
                    String packageName = activePackage.optString("packageName", 
                                        activePackage.optString("package_name", 
                                        activePackage.optString("name", "")));
                    
                    if (!packageName.isEmpty()) {
                        currentTariffName = packageName;
                        System.out.println("✅ Paket adı Customer Packages'dan alındı: " + currentTariffName);
                        
                        // Hızlı UI güncelleme
                        SwingUtilities.invokeLater(() -> {
                            updateTariffPanelQuick();
                        });
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
                    int packageId = activePackage.optInt("packageId", 0);
                    if (packageId > 0) {
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
                                        if (pkgId == packageId) {
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
                }

                // 2. Balance API'den kullanım verilerini çek
                System.out.println("💰 Balance API kontrol ediliyor...");
                JSONObject balanceResponse = BalanceHelper.getBalanceJSON(userPhone);

                if (balanceResponse != null) {
                    System.out.println("✅ Balance data bulundu");

                    // ✅ GB dönüşümü yapılıyor (MB / 1024)
                    remainingData = balanceResponse.optDouble("remainingData", 0) / 1024.0;
                    double amountData = balanceResponse.optDouble("amountData", 0) / 1024.0;
                    usedData = Math.max(0, amountData - remainingData);

                    remainingMinutes = balanceResponse.optInt("remainingMinutes", 0);
                    int amountMinutes = balanceResponse.optInt("amountMinutes", 0);
                    usedMinutes = Math.max(0, amountMinutes - remainingMinutes);

                    remainingSMS = balanceResponse.optDouble("remainingSms", 0);
                    double amountSMS = balanceResponse.optDouble("amountSms", 0);
                    usedSMS = Math.max(0, amountSMS - remainingSMS);

                    balance = balanceResponse.optDouble("balance", 0);

                    // Tarife adı varsa güncelle (sadece "Bilinmiyor" ise)
                    if (balanceResponse.has("tariffName") && currentTariffName.equals("Bilinmiyor")) {
                        String tariffFromBalance = balanceResponse.getString("tariffName");
                        if (!tariffFromBalance.isEmpty() && !tariffFromBalance.equals("Bilinmiyor")) {
                            currentTariffName = tariffFromBalance;
                            System.out.println("✅ Paket adı Balance API'den alındı: " + currentTariffName);
                        }
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

                // Eğer fiyat hala yüklenmemişse, default değer ata
                if (currentTariffPrice.equals("Yükleniyor...") || currentTariffPrice.equals("...")) {
                    currentTariffPrice = "Fiyat Bilgisi Yok";
                    System.out.println("⚠️ Fiyat bilgisi bulunamadı - default değer atandı");
                }

                // UI güncellemelerini main thread'de yap
                SwingUtilities.invokeLater(() -> {
                    try {
                        if (finalHasActivePackage) {
                            System.out.println("✅ Aktif paket bulundu - UI güncelleniyor");
                            System.out.println("📋 Tarife: " + currentTariffName + " - " + currentTariffPrice);
                            updateChartsAndUI();
                        } else {
                            System.out.println("⚠️ Aktif paket bulunamadı - uyarı gösteriliyor");
                            currentTariffName = "Paket Bulunamadı";
                            currentTariffPrice = "-";
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

                // Default değerler - paket tam dolu varsayalım
                double packageData = activePackage.optDouble("amountData", 10240) / 1024.0; // MB to GB
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

        String[] sidebarItems = {"HOME", "STORE", "BILLS", "PROFILE"};
        for (int i = 0; i < sidebarItems.length; i++) {
            JLabel label = new JLabel(sidebarItems[i]);
            label.setFont(new Font("Arial", sidebarItems[i].equals("HOME") ? Font.BOLD : Font.PLAIN, 14));
            label.setForeground(sidebarItems[i].equals("HOME") ? new Color(52, 52, 52) : new Color(150, 150, 150));
            label.setBounds(40, 150 + (i * 50), 150, 25);
            label.setCursor(new Cursor(Cursor.HAND_CURSOR));

            int finalI = i;
            label.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    handleNavigation(finalI);
                }

                public void mouseEntered(MouseEvent e) {
                    if (finalI != 0) { // Not HOME
                        label.setForeground(new Color(22, 105, 143));
                    }
                }

                public void mouseExited(MouseEvent e) {
                    label.setForeground(sidebarItems[finalI].equals("HOME") ? new Color(52, 52, 52) : new Color(150, 150, 150));
                }
            });
            sidebar.add(label);
        }
        backgroundPanel.add(sidebar);
    }
// createSidebar() metodundan hemen sonra



    private void handleNavigation(int index) {
        if (index == 0) {
            // Already on HOME page
            return;
        }
        
        SwingUtilities.invokeLater(() -> {
            // Stop background timer before navigation
            if (backgroundUpdateTimer != null) {
                backgroundUpdateTimer.stop();
                backgroundUpdateTimer = null;
            }

            // Fast transition
            setVisible(false);
            
            switch (index) {
                case 1: // STORE
                    SwingUtilities.invokeLater(() -> 
                        new StoreController(customerId, userName, userSurname, userPhone, userEmail, userPassword).setVisible(true)
                    );
                    break;
                case 2: // BILLS
                    SwingUtilities.invokeLater(() -> 
                        new BillHistoryController(customerId, userName, userSurname, userPhone, userEmail, userPassword).setVisible(true)
                    );
                    break;
                case 3: // PROFILE
                    SwingUtilities.invokeLater(() -> 
                        new ProfileController(customerId, userName, userSurname, userPhone, userEmail).setVisible(true)
                    );
                    break;
            }
        });
    }

    private void createUserInfo(JPanel backgroundPanel) {
        JLabel helloLabel = new JLabel("Hello, " + userName);
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
                "minutes left",
                totalMinutes + " MIN",
                new Color(0x1565C0), // Daha koyu mavi
                totalMinutes > 0 ? (float) usedMinutes / totalMinutes : 0
        );

        // Data chart - Canlı Yeşil (#2E7D32)
        double totalData = usedData + remainingData;
        dataChartPanel = (AnimatedCirclePanel) createInteractiveUsageCircle(
                backgroundPanel,
                620, 370,
                String.format("%.1f", remainingData),
                "GB left",
                String.format("%.1f GB", totalData),
                new Color(0x2E7D32), // Daha koyu yeşil
                totalData > 0 ? (float) usedData / (float) totalData : 0
        );

        // SMS chart - Parlak Turkuaz (#00BCD4)
        int totalSMS = (int) (usedSMS + remainingSMS);
        smsChartPanel = (AnimatedCirclePanel) createInteractiveUsageCircle(
                backgroundPanel,
                890, 370,
                String.valueOf((int) remainingSMS),
                "SMS left",
                totalSMS + " SMS",
                new Color(0x00BCD4), // Daha parlak turkuaz
                totalSMS > 0 ? (float) usedSMS / totalSMS : 0
        );
    }
    private JPanel createInteractiveUsageCircle(JPanel parent, int x, int y, String mainText, String subText, String totalText, Color color, float percentage) {
        AnimatedCirclePanel circlePanel = new AnimatedCirclePanel(color, percentage);

        circlePanel.setBounds(x, y, 190, 250);
        circlePanel.setOpaque(false);
        circlePanel.setLayout(null);

        // Ana sayı (ortadaki büyük rakam)
        JLabel main = new JLabel(mainText, SwingConstants.CENTER);
        main.setFont(new Font("Arial", Font.BOLD, 32));
        main.setForeground(new Color(52, 52, 52));
        main.setBounds(0, 65, 190, 40);
        circlePanel.add(main);

        // Alt açıklama
        JLabel sub = new JLabel(subText, SwingConstants.CENTER);
        sub.setFont(new Font("Arial", Font.PLAIN, 14));
        sub.setForeground(new Color(120, 120, 120));
        sub.setBounds(0, 100, 190, 18);
        circlePanel.add(sub);

        // Toplam değer (en altta)
        JLabel total = new JLabel(totalText, SwingConstants.CENTER);
        total.setFont(new Font("Arial", Font.BOLD, 18));
        total.setForeground(color);
        total.setBounds(0, 215, 190, 30);
        circlePanel.add(total);

        parent.add(circlePanel);
        
        // Animasyonu 500ms sonra başlat
        Timer delayTimer = new Timer(500, e -> {
            ((Timer) e.getSource()).stop();
            circlePanel.startAnimation();
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
                        "minutes left",
                        (usedMinutes + remainingMinutes) + " MIN",
                        new Color(0x1565C0), // Daha koyu mavi
                        (usedMinutes + remainingMinutes) > 0 ? (float) usedMinutes / (usedMinutes + remainingMinutes) : 0);

                double totalData = usedData + remainingData;
                updateColorfulChart(dataChartPanel,
                        String.format("%.1f", remainingData),
                        "GB left",
                        String.format("%.1f GB", totalData),
                        new Color(0x2E7D32), // Daha koyu yeşil
                        totalData > 0 ? (float) usedData / (float) totalData : 0);

                int totalSMS = (int) (usedSMS + remainingSMS);
                updateColorfulChart(smsChartPanel,
                        String.valueOf((int) remainingSMS),
                        "SMS left",
                        totalSMS + " SMS",
                        new Color(0x00BCD4), // Daha parlak turkuaz
                        totalSMS > 0 ? (float) usedSMS / totalSMS : 0);

                // Animasyonları yeniden başlat
                Timer animRestartTimer = new Timer(200, event -> {
                    ((Timer) event.getSource()).stop();
                    if (minutesChartPanel != null) {
                        minutesChartPanel.updateData((usedMinutes + remainingMinutes) > 0 ? (float) remainingMinutes / (usedMinutes + remainingMinutes) : 0);
                        minutesChartPanel.restartAnimation();
                    }
                    if (dataChartPanel != null) {
                        Timer delay1 = new Timer(100, e -> {
                            ((Timer) e.getSource()).stop();
                            double currentTotalData = usedData + remainingData;
                            dataChartPanel.updateData(currentTotalData > 0 ? (float) remainingData / (float) currentTotalData : 0);
                            dataChartPanel.restartAnimation();
                        });
                        delay1.setRepeats(false);
                        delay1.start();
                    }
                    if (smsChartPanel != null) {
                        Timer delay2 = new Timer(200, e -> {
                            ((Timer) e.getSource()).stop();
                            int currentTotalSMS = (int) (usedSMS + remainingSMS);
                            smsChartPanel.updateData(currentTotalSMS > 0 ? (float) remainingSMS / currentTotalSMS : 0);
                            smsChartPanel.restartAnimation();
                        });
                        delay2.setRepeats(false);
                        delay2.start();
                    }
                });
                animRestartTimer.setRepeats(false);
                animRestartTimer.start();

                // Update tariff panel
                updateTariffPanel();

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
                        
                        if (!packageName.isEmpty()) {
                            System.out.println("✅ Paket bulundu: ID=" + packageId + ", Adı=" + packageName);
                            return packageName;
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
        
        return "Paket #" + packageId; // Fallback: ID ile göster
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
     * Show no package dialog
     */
    private void showNoPackageDialog() {
        SwingUtilities.invokeLater(() -> {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    "⚠️ Aktif paketiniz bulunmuyor!\n\n" +
                            "Şu durumlardan biri söz konusu olabilir:\n" +
                            "• Henüz paket satın almamışsınız\n" +
                            "• Paketinizin süresi dolmuş olabilir\n" +
                            "• Sistem güncellenmesi devam ediyor\n\n" +
                            "Yeni paket satın almak için mağazaya gitmek ister misiniz?",
                    "Paket Durumu",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (result == JOptionPane.YES_OPTION) {
                goToStore();
            }
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
     * Override setVisible to trigger refresh when window becomes visible
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            System.out.println("👁️ MainController visible oldu - refresh tetikleniyor");

            // Delay refresh to allow UI to settle
            Timer visibilityRefreshTimer = new Timer(1000, e -> {
                refreshPackageData();
                ((Timer) e.getSource()).stop();
            });
            visibilityRefreshTimer.start();
        }
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

        public AnimatedCirclePanel(Color color, float percentage) {
            this.color = color;
            this.targetPercentage = percentage;
            
            // Animasyon timer'ını başlat
            animationTimer = new Timer(15, e -> {
                if (animationPercentage < targetPercentage) {
                    animationPercentage += 0.025f; // Daha hızlı animasyon
                    if (animationPercentage > targetPercentage) {
                        animationPercentage = targetPercentage;
                    }
                    repaint();
                } else {
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

            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            int radius = 85;

            // Arka plan daire (açık gri)
            g2d.setColor(new Color(240, 240, 240, 200));
            g2d.setStroke(new BasicStroke(18, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

            // Kullanım çubuğu (renkli ve animasyonlu)
            Color currentColor = isHovered ? 
                new Color(Math.min(255, color.getRed() + 30), 
                         Math.min(255, color.getGreen() + 30), 
                         Math.min(255, color.getBlue() + 30)) : color;
            
            g2d.setColor(currentColor);
            g2d.setStroke(new BasicStroke(isHovered ? 24 : 20, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int angle = (int) (360 * animationPercentage);
            g2d.drawArc(centerX - radius, centerY - radius, radius * 2, radius * 2, 90, -angle);

            // Gradient efekti için iç halka
            if (animationPercentage > 0) {
                Color innerColor = new Color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), 80);
                g2d.setColor(innerColor);
                g2d.setStroke(new BasicStroke(12, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.drawArc(centerX - radius + 6, centerY - radius + 6, (radius - 6) * 2, (radius - 6) * 2, 90, -angle);
            }

            // Hover olduğunda dış parlaklık efekti
            if (isHovered && animationPercentage > 0) {
                g2d.setColor(new Color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), 60));
                g2d.setStroke(new BasicStroke(28, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.drawArc(centerX - radius - 4, centerY - radius - 4, (radius + 4) * 2, (radius + 4) * 2, 90, -angle);
            }

            g2d.dispose();
        }
        
        public void startAnimation() {
            SwingUtilities.invokeLater(() -> {
                System.out.println("🎬 Animasyon başlatılıyor...");
                animationPercentage = 0f;
                if (animationTimer != null && !animationTimer.isRunning()) {
                    animationTimer.start();
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
                if (animationTimer != null) {
                    animationTimer.start();
                    System.out.println("✅ Animasyon yeniden başlatıldı");
                }
            });
        }
        
        public void updateData(float newPercentage) {
            this.targetPercentage = newPercentage;
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