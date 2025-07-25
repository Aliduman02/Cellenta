package com.cellenta.controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import org.json.JSONObject;
import org.json.JSONArray;
import com.cellenta.controller.ChatBotWindow;

public class BillHistoryController extends JFrame {
    private static BufferedImage backgroundImage;
    private static boolean backgroundLoaded = false;
    private BufferedImage titleImage;
    private BufferedImage logoImage;
    private BufferedImage iconImage;
    private int customerId;
    private String userPhone;
    private String userName;
    private String userSurname;
    private String userEmail;
    private String userPassword;
    private List<JSONObject> invoiceList = new ArrayList<>();
    private JPanel listPanel;

    public BillHistoryController(int customerId, String name, String surname, String phone, String email, String password) {
        this.customerId = customerId;
        this.userName = name;
        this.userSurname = surname;
        this.userPhone = phone;
        this.userEmail = email;
        this.userPassword = password;
        setTitle("Bills");
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        // Arka plan resmini sadece bir kez yükle
        loadBackgroundImage();

        // Title ve logo resimlerini yükle
        try {
            titleImage = ImageIO.read(getClass().getResourceAsStream("/images/title.png"));
        } catch (Exception e) {
            System.out.println("Title image not found: " + e.getMessage());
            titleImage = null;
        }

        try {
            logoImage = ImageIO.read(getClass().getResourceAsStream("/images/logo.png"));
        } catch (Exception e) {
            System.out.println("Logo image not found: " + e.getMessage());
            logoImage = null;
        }

        try {
            iconImage = ImageIO.read(getClass().getResourceAsStream("/images/icon-white.png"));
        } catch (Exception e) {
            System.out.println("Icon image not found: " + e.getMessage());
            iconImage = null;
        }

        // Ana arka plan paneli
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                if (backgroundImage != null) {
                    // Arka plan resmini tam ekrana yayılacak şekilde çiz
                    // Aspect ratio'yu korumak için resmi ölçekle
                    int panelWidth = getWidth();
                    int panelHeight = getHeight();
                    int imageWidth = backgroundImage.getWidth();
                    int imageHeight = backgroundImage.getHeight();

                    // Resmin aspect ratio'sunu hesapla
                    double imageAspect = (double) imageWidth / imageHeight;
                    double panelAspect = (double) panelWidth / panelHeight;

                    int drawWidth, drawHeight;
                    int drawX = 0, drawY = 0;

                    if (imageAspect > panelAspect) {
                        // Resim panelden daha geniş - yüksekliği panele sığdır
                        drawHeight = panelHeight;
                        drawWidth = (int) (drawHeight * imageAspect);
                        drawX = (panelWidth - drawWidth) / 2;
                    } else {
                        // Resim panelden daha dar - genişliği panele sığdır
                        drawWidth = panelWidth;
                        drawHeight = (int) (drawWidth / imageAspect);
                        drawY = (panelHeight - drawHeight) / 2;
                    }

                    // Resmi çiz
                    g2d.drawImage(backgroundImage, drawX, drawY, drawWidth, drawHeight, this);

                    // Eğer resim tam ekranı kaplamıyorsa, boş kısımları gradient ile doldur
                    if (drawX > 0 || drawY > 0 || drawWidth < panelWidth || drawHeight < panelHeight) {
                        // Gradient overlay
                        GradientPaint gradient = new GradientPaint(
                                0, 0, new Color(248, 248, 248, 100),
                                0, panelHeight, new Color(235, 235, 235, 100)
                        );
                        g2d.setPaint(gradient);
                        g2d.fillRect(0, 0, panelWidth, panelHeight);
                    }
                } else {
                    // Gradient arka plan (resim yüklenemediğinde)
                    GradientPaint gradient = new GradientPaint(
                            0, 0, new Color(248, 248, 248),
                            0, getHeight(), new Color(235, 235, 235)
                    );
                    g2d.setPaint(gradient);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        backgroundPanel.setLayout(null);
        backgroundPanel.setBounds(0, 0, 1400, 900);
        setContentPane(backgroundPanel);

        // Üst header - dinamik genişlik
        JPanel header = new JPanel(null) {
            @Override
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(x, y, BillHistoryController.this.getWidth(), height);
            }
        };
        header.setBackground(new Color(255, 255, 255, 240)); // Slight transparency
        header.setBounds(0, 0, getWidth(), 80);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)));

        // Logo paneli - Logo ve Title resimleri ile
        JPanel logoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                int x = 0;
                int y = 0;

                // Logo.png resmi çiz
                if (logoImage != null) {
                    g2d.drawImage(logoImage, x, y, 40, 40, this);
                    x += 50; // Logo'dan sonra title için boşluk
                } else {
                    // Logo yoksa varsayılan logo
                    g2d.setColor(new Color(22, 105, 143));
                    g2d.fillOval(x, y, 40, 40);
                    g2d.setColor(Color.WHITE);
                    g2d.setStroke(new BasicStroke(3));
                    g2d.drawArc(x + 8, y + 8, 24, 24, 0, 270);
                    g2d.fillOval(x + 20, y + 12, 8, 8);
                    x += 50;
                }

                // Title resmi çiz
                if (titleImage != null) {
                    g2d.drawImage(titleImage, x, y + 5, 150, 30, this);
                } else {
                    // Title yoksa varsayılan CELLENTA yazısı
                    g2d.setColor(Color.BLACK);
                    g2d.setFont(new Font("Arial", Font.BOLD, 24));
                    g2d.drawString("CELLENTA", x, y + 30);
                }
            }
        };
        logoPanel.setBounds(600, 20, 250, 40);
        logoPanel.setOpaque(false);
        header.add(logoPanel);

        backgroundPanel.add(header);

        // Pencere boyutu değiştiğinde header'ı güncelle
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                header.setBounds(0, 0, getWidth(), 80);
            }
        });

        // Sol sidebar
        JPanel sidebar = new JPanel(null);
        sidebar.setOpaque(false); // Arka plan transparan, resim aynı kalır
        sidebar.setBounds(0, 80, 250, 820);

        String[] sidebarItems = {"ANA SAYFA", "MAĞAZA", "FATURALAR", "PROFİL"};
        for (int i = 0; i < sidebarItems.length; i++) {
            JLabel sideLabel = new JLabel(sidebarItems[i]);
            sideLabel.setFont(new Font("Arial", sidebarItems[i].equals("FATURALAR") ? Font.BOLD : Font.PLAIN, 14));
            sideLabel.setForeground(sidebarItems[i].equals("FATURALAR") ? new Color(52, 52, 52) : new Color(150, 150, 150));
            sideLabel.setBounds(50, 150 + (i * 50), 150, 25);
            sideLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

            int finalI = i;
            sideLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (finalI == 2) {
                        // Already on BILLS page
                        return;
                    }
                    
                    // Smooth transition with delay
                    Timer transitionTimer = new Timer(50, evt -> {
                        ((Timer) evt.getSource()).stop();
                        SwingUtilities.invokeLater(() -> {
                            setVisible(false);
                            
                            switch (finalI) {
                                case 0 -> SwingUtilities.invokeLater(() -> 
                                    new MainController( customerId,name, surname, phone, email, password).setVisible(true));
                                case 1 -> SwingUtilities.invokeLater(() -> 
                                    new StoreController(customerId,name, surname, phone, email, password).setVisible(true));
                                case 3 -> SwingUtilities.invokeLater(() -> 
                                    new ProfileController( customerId,name, surname, phone, email).setVisible(true));
                            }
                        });
                    });
                    transitionTimer.start();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    sideLabel.setForeground(new Color(22, 105, 143));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!sidebarItems[finalI].equals("FATURALAR")) {
                        sideLabel.setForeground(new Color(150, 150, 150));
                    } else {
                        sideLabel.setForeground(new Color(52, 52, 52));
                    }
                }
            });

            sidebar.add(sideLabel);
        }
        backgroundPanel.add(sidebar);

        // Ana content area
        JPanel mainContent = new JPanel(null);
        mainContent.setOpaque(false);
        mainContent.setBounds(250, 80, 1150, 820);

        // Sayfa başlığı
        JLabel pageTitle = new JLabel("← Son Ödemeler");
        pageTitle.setFont(new Font("Arial", Font.BOLD, 28));
        pageTitle.setForeground(new Color(52, 52, 52));
        pageTitle.setBounds(80, 50, 300, 35);
        pageTitle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pageTitle.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Timer transitionTimer = new Timer(50, evt -> {
                    ((Timer) evt.getSource()).stop();
                    SwingUtilities.invokeLater(() -> {
                        setVisible(false);
                        SwingUtilities.invokeLater(() -> 
                            new MainController(customerId,name, surname, phone, email, password).setVisible(true)
                        );
                    });
                });
                transitionTimer.start();
            }
        });
        mainContent.add(pageTitle);

        // Sağ üst fatura bilgisi ve ödeme butonu
        JPanel billPaymentPanel = new JPanel(null);
        billPaymentPanel.setOpaque(false);
        billPaymentPanel.setBounds(700, 30, 400, 80);

        JLabel billAmount = new JLabel("799 TL");
        billAmount.setFont(new Font("Arial", Font.BOLD, 28));
        billAmount.setForeground(new Color(52, 52, 52));
        billAmount.setBounds(0, 0, 120, 35);
        billPaymentPanel.add(billAmount);

        JLabel billText = new JLabel("Faturayı öde");
        billText.setFont(new Font("Arial", Font.PLAIN, 14));
        billText.setForeground(new Color(150, 150, 150));
        billText.setBounds(0, 35, 120, 20);
        billPaymentPanel.add(billText);

        // Ödeme butonu - daha büyük ve modern
        JButton payButton = new JButton("→") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(22, 105, 143));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 20));
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth("→")) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                g2d.drawString("→", x, y);
            }
        };
        payButton.setBounds(280, 10, 60, 45);
        payButton.setBorder(null);
        payButton.setFocusPainted(false);
        payButton.setContentAreaFilled(false);
        payButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        payButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Fatura ödeme sayfasına yönlendiriliyorsunuz...");
        });
        billPaymentPanel.add(payButton);

        mainContent.add(billPaymentPanel);

        // Fatura listesi kartı - daha modern ve temiz
        JPanel billCard = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 250));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

                // Subtle shadow effect
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.fillRoundRect(2, 2, getWidth()-2, getHeight()-2, 25, 25);

                g2d.setColor(new Color(255, 255, 255, 250));
                g2d.fillRoundRect(0, 0, getWidth()-2, getHeight()-2, 25, 25);
            }
        };
        billCard.setOpaque(false);
        billCard.setBounds(80, 120, 950, 480);

        // Fatura listesi - API'den gelecek veriler için hazırlık
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);
        listPanel.setBounds(0, 0, 950, 400);
        
        // Başlangıçta loading mesajı göster
        JLabel loadingLabel = new JLabel("Faturalar yükleniyor...");
        loadingLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        loadingLabel.setForeground(new Color(120, 120, 120));
        loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loadingLabel.setPreferredSize(new Dimension(880, 100));
        listPanel.add(loadingLabel);

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBounds(25, 25, 900, 450);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Performans optimizasyonu
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setBlockIncrement(64);

        billCard.add(scrollPane);
        mainContent.add(billCard);
        backgroundPanel.add(mainContent);

        // Chat bot mesaj balonu
        JPanel chatMessage = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Mesaj balonu arka planı
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Gölge efekti
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.fillRoundRect(2, 2, getWidth()-2, getHeight()-2, 20, 20);
            }
        };
        chatMessage.setLayout(null);
        chatMessage.setBounds(1050, 720, 260, 60);
        chatMessage.setOpaque(false);

        JLabel messageText = new JLabel("<html><div style='text-align: center; font-size: 9px; color: #666;'>Merhaba, ben Cellenta!<br>Size nasıl yardımcı olabilirim?</div></html>");
        messageText.setBounds(10, 10, 240, 40);
        messageText.setHorizontalAlignment(SwingConstants.CENTER);
        chatMessage.add(messageText);

        // Sağ alt köşe chat botu
        JPanel chatBot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                // Yeşil daire arka plan
                g2d.setColor(new Color(45, 156, 151));
                g2d.fillOval(0, 0, 60, 60);

                // Icon resmi çiz
                if (iconImage != null) {
                    g2d.drawImage(iconImage, 10, 10, 40, 40, this);
                } else {
                    // Icon yoksa varsayılan logo
                    g2d.setColor(Color.WHITE);
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawArc(15, 15, 30, 30, 0, 270);
                    g2d.fillOval(25, 20, 10, 10);
                }
            }
        };
        chatBot.setOpaque(false);
        chatBot.setBounds(1320, 720, 60, 60);
        chatBot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chatBot.setLayout(null);

        chatBot.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("🖱️ Bills - Chat bot ikonuna tıklandı!");

                try {
                    System.out.println("💬 ChatBotWindow oluşturuluyor...");
                    ChatBotWindow chatWindow = new ChatBotWindow();
                    System.out.println("✅ ChatBotWindow oluşturuldu!");

                    chatWindow.setVisible(true);
                    System.out.println("🎯 ChatBotWindow görünür yapıldı!");

                } catch (Exception ex) {
                    System.err.println("❌ ChatBot açılırken hata: " + ex.getMessage());
                    ex.printStackTrace();

                    // Kullanıcıya hata mesajı göster
                    JOptionPane.showMessageDialog(BillHistoryController.this,
                            "Chat bot açılırken hata oluştu: " + ex.getMessage(),
                            "Hata",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                chatBot.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                chatBot.repaint();
            }
        });
        backgroundPanel.add(chatMessage);
        backgroundPanel.add(chatBot);
        
        // Fatura verilerini API'den çek
        loadInvoiceData();
    }

    /**
     * Override setVisible method
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
    }

    /**
     * API'den fatura verilerini çeker
     */
    private void loadInvoiceData() {
        SwingUtilities.invokeLater(() -> {
            System.out.println("📋 Faturalar yükleniyor...");
            
            Thread invoiceThread = new Thread(() -> {
                try {
                    JSONArray invoices = fetchInvoicesFromAPI();
                    if (invoices != null && invoices.length() > 0) {
                        invoiceList.clear();
                        for (int i = 0; i < invoices.length(); i++) {
                            invoiceList.add(invoices.getJSONObject(i));
                        }
                        
                        SwingUtilities.invokeLater(() -> {
                            updateBillList();
                            updateBillPaymentInfo();
                        });
                        
                        System.out.println("✅ " + invoices.length() + " fatura yüklendi");
                    } else {
                        System.out.println("⚠️ Hiç fatura bulunamadı");
                        SwingUtilities.invokeLater(() -> {
                            showNoInvoicesMessage();
                        });
                    }
                } catch (Exception e) {
                    System.err.println("❌ Fatura yükleme hatası: " + e.getMessage());
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        showErrorMessage("Faturalar yüklenirken hata oluştu: " + e.getMessage());
                    });
                }
            });
            
            invoiceThread.setName("InvoiceLoadThread");
            invoiceThread.start();
        });
    }

    /**
     * API'den fatura verilerini çeker
     */
    private JSONArray fetchInvoicesFromAPI() {
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
            System.out.println("📡 Invoice API status: " + statusCode);

            if (statusCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line.trim());
                }

                reader.close();
                System.out.println("📥 Invoice response: " + response.toString());

                return new JSONArray(response.toString());
            } else {
                System.err.println("❌ API call failed with status: " + statusCode);
            }

        } catch (Exception e) {
            System.err.println("❌ Invoice API error: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Fatura listesini günceller
     */
    private void updateBillList() {
        if (listPanel == null) return;
        
        listPanel.removeAll();
        
        for (JSONObject invoice : invoiceList) {
            String startDate = invoice.optString("startDate", "");
            String endDate = invoice.optString("endDate", "");
            double price = invoice.optDouble("price", 0);
            String paymentStatus = invoice.optString("paymentStatus", "UNKNOWN");
            String isActive = invoice.optString("isActive", "N");
            String daysLeft = invoice.optString("daysLeft", "0");
            
            // Tarih formatını düzenle
            String dateRange = formatDateRange(startDate, endDate);
            
            // Status'u düzenle
            String status = formatPaymentStatus(paymentStatus, isActive, daysLeft);
            String statusType = getStatusType(paymentStatus, isActive);
            
            // Fiyat formatı
            String amount = String.format("%.0f TL", price);
            
            listPanel.add(createBillItem(dateRange, status, amount, statusType));
        }
        
        listPanel.revalidate();
        listPanel.repaint();
    }

    /**
     * Ödeme bilgilerini günceller
     */
    private void updateBillPaymentInfo() {
        // Ödenmemiş faturaları bul
        double totalUnpaid = 0;
        for (JSONObject invoice : invoiceList) {
            String paymentStatus = invoice.optString("paymentStatus", "PAID");
            if ("UNPAID".equals(paymentStatus)) {
                totalUnpaid += invoice.optDouble("price", 0);
            }
        }
        
        // Final variable for lambda
        final double finalTotalUnpaid = totalUnpaid;
        
        // UI'daki tutar labelını güncelle
        SwingUtilities.invokeLater(() -> {
            Component[] components = getContentPane().getComponents();
            updatePaymentAmount(components, finalTotalUnpaid);
        });
    }

    private void updatePaymentAmount(Component[] components, double amount) {
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                updatePaymentAmountInPanel((JPanel) comp, amount);
            }
        }
    }

    private void updatePaymentAmountInPanel(JPanel panel, double amount) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JPanel) {
                updatePaymentAmountInPanel((JPanel) comp, amount);
            } else if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                if (label.getFont().getSize() == 28 && label.getText().contains("TL")) {
                    label.setText(String.format("%.0f TL", amount));
                }
            }
        }
    }

    /**
     * Tarih aralığını formatlar
     */
    private String formatDateRange(String startDate, String endDate) {
        try {
            if (startDate.isEmpty() || endDate.isEmpty()) {
                return "Tarih bilgisi yok";
            }
            
            // API'den gelen format: "13/07/25" -> "13 Tem - 12 Ağu 2025" 
            String[] months = {"Oca", "Şub", "Mar", "Nis", "May", "Haz", 
                              "Tem", "Ağu", "Eyl", "Eki", "Kas", "Ara"};
            
            String[] startParts = startDate.split("/");
            String[] endParts = endDate.split("/");
            
            if (startParts.length == 3 && endParts.length == 3) {
                int startMonth = Integer.parseInt(startParts[1]);
                int endMonth = Integer.parseInt(endParts[1]);
                
                String startMonthName = months[startMonth - 1];
                String endMonthName = months[endMonth - 1];
                
                return startParts[0] + " " + startMonthName + " - " + 
                       endParts[0] + " " + endMonthName + " 20" + startParts[2];
            }
            
        } catch (Exception e) {
            System.err.println("⚠️ Tarih formatı hatası: " + e.getMessage());
        }
        
        return startDate + " - " + endDate;
    }

    /**
     * Ödeme durumunu formatlar
     */
    private String formatPaymentStatus(String paymentStatus, String isActive, String daysLeft) {
        if ("PAID".equals(paymentStatus)) {
            return "✅ Ödendi";
        } else if ("UNPAID".equals(paymentStatus)) {
            if ("Y".equals(isActive) && !daysLeft.equals("0")) {
                try {
                    int days = Integer.parseInt(daysLeft);
                    if (days > 0) {
                        return "⏰ " + days + " gün kaldı";
                    } else {
                        return "⚠️ Süresi doldu";
                    }
                } catch (NumberFormatException e) {
                    return "⏳ Ödeme bekleniyor";
                }
            } else {
                return "⏳ Ödeme bekleniyor";
            }
        }
        return "❓ Bilinmiyor";
    }

    /**
     * Status tipini belirler (CSS class için)
     */
    private String getStatusType(String paymentStatus, String isActive) {
        if ("PAID".equals(paymentStatus)) {
            return "success";
        } else if ("UNPAID".equals(paymentStatus)) {
            return "warning";
        }
        return "default";
    }

    /**
     * Fatura bulunamadığında mesaj gösterir
     */
    private void showNoInvoicesMessage() {
        if (listPanel != null) {
            listPanel.removeAll();
            
            JLabel noInvoiceLabel = new JLabel("Henüz faturanız bulunmuyor");
            noInvoiceLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            noInvoiceLabel.setForeground(new Color(120, 120, 120));
            noInvoiceLabel.setHorizontalAlignment(SwingConstants.CENTER);
            noInvoiceLabel.setPreferredSize(new Dimension(880, 100));
            
            listPanel.add(noInvoiceLabel);
            listPanel.revalidate();
            listPanel.repaint();
        }
    }

    /**
     * Hata mesajı gösterir
     */
    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Hata", JOptionPane.ERROR_MESSAGE);
    }

    private static void loadBackgroundImage() {
        if (!backgroundLoaded) {
            try {
                // Farklı dosya yollarını dene
                File bgFile = new File("resources/images/background.png");
                if (!bgFile.exists()) {
                    bgFile = new File("src/main/resources/images/background.png");
                }
                if (!bgFile.exists()) {
                    bgFile = new File("images/background.png");
                }
                if (!bgFile.exists()) {
                    // ClassLoader ile kaynak dosyasını yükle
                    var resource = BillHistoryController.class.getClassLoader().getResource("images/background.png");
                    if (resource != null) {
                        backgroundImage = ImageIO.read(resource);
                    } else {
                        System.out.println("Background image not found, using default background");
                    }
                } else {
                    backgroundImage = ImageIO.read(bgFile);
                }
                backgroundLoaded = true;
            } catch (IOException e) {
                System.out.println("Could not load background image: " + e.getMessage());
                backgroundImage = null;
                backgroundLoaded = true;
            }
        }
    }

    private JPanel createBillItem(String date, String status, String amount, String type) {
        JPanel item = new JPanel(null);
        item.setPreferredSize(new Dimension(880, 90)); // Daha yüksek
        item.setMaximumSize(new Dimension(880, 90));
        item.setOpaque(false);
        item.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)));

        // Status ikonu - daha büyük ve modern
        JPanel statusIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (type.equals("warning")) {
                    g2d.setColor(new Color(255, 142, 60));
                } else if (type.equals("success")) {
                    g2d.setColor(new Color(53, 219, 149));
                } else {
                    g2d.setColor(new Color(120, 120, 120));
                }
                g2d.fillOval(0, 0, 35, 35);

                // İkon çizimi
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2.5f));
                if (type.equals("success")) {
                    // Checkmark
                    g2d.drawLine(10, 18, 15, 23);
                    g2d.drawLine(15, 23, 25, 12);
                } else if (type.equals("warning")) {
                    // Clock icon
                    g2d.drawLine(17, 10, 17, 18);
                    g2d.drawLine(17, 18, 22, 23);
                } else {
                    // Question mark
                    g2d.drawArc(12, 8, 10, 8, 0, 180);
                    g2d.fillOval(16, 22, 3, 3);
                }
            }
        };
        statusIcon.setBounds(40, 25, 35, 35);
        statusIcon.setOpaque(false);
        item.add(statusIcon);

        // Fatura dönemi (tarih) - üst satır
        JLabel dateLabel = new JLabel(date);
        dateLabel.setFont(new Font("Arial", Font.BOLD, 16));
        dateLabel.setForeground(new Color(52, 52, 52));
        dateLabel.setBounds(90, 15, 300, 25);
        item.add(dateLabel);

        // Fatura tutarı - büyük ve vurgulu
        String paymentStatusText = type.equals("success") ? " - Ödendi" : type.equals("warning") ? " - Ödenmedi" : "";
        JLabel amountLabel = new JLabel(amount + paymentStatusText);
        amountLabel.setFont(new Font("Arial", Font.BOLD, 20));
        amountLabel.setForeground(Color.BLACK);
        amountLabel.setBounds(90, 40, 300, 30);
        item.add(amountLabel);

        // Ödeme durumu - detaylı bilgi ile
        JLabel statusLabel = new JLabel(status);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        Color statusColor = type.equals("warning") ? new Color(255, 142, 60) : 
                           type.equals("success") ? new Color(53, 219, 149) : 
                           new Color(120, 120, 120);
        statusLabel.setForeground(statusColor);
        statusLabel.setBounds(450, 35, 200, 25);
        item.add(statusLabel);

        // Durum açıklaması - daha büyük ve belirgin
        String statusDescription = getStatusDescription(type, status);
        if (!statusDescription.isEmpty()) {
            JLabel descLabel = new JLabel(statusDescription);
            descLabel.setFont(new Font("Arial", Font.BOLD, 18));
            // Durum tipine göre daha belirgin renk
            Color descColor = type.equals("warning") ? new Color(220, 53, 69) : 
                             type.equals("success") ? new Color(40, 167, 69) : 
                             new Color(52, 52, 52);
            descLabel.setForeground(descColor);
            descLabel.setBounds(650, 35, 120, 30);
            item.add(descLabel);
        }

        // Fatura ID veya ek bilgi (sağ tarafta)
        JLabel extraInfoLabel = new JLabel("Fatura Detayı →");
        extraInfoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        extraInfoLabel.setForeground(new Color(120, 120, 120));
        extraInfoLabel.setBounds(700, 45, 120, 20);
        extraInfoLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        item.add(extraInfoLabel);

        return item;
    }

    /**
     * Status açıklaması döndürür
     */
    private String getStatusDescription(String type, String status) {
        if (type.equals("success")) {
            return "Ödendi";
        } else if (type.equals("warning")) {
            return "Ödenmedi";
        }
        return "";
    }
}