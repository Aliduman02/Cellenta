package com.cellenta.controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import org.json.JSONArray;
import org.json.JSONObject;
import com.cellenta.PackageHelper;
import com.cellenta.controller.ChatBotWindow;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Timer;

public class StoreController extends JFrame {

    // UI Components
    private JPanel expandedPanel = null;
    private boolean isExpanded = false;
    private String currentExpandedName = "";
    private String currentExpandedDesc = "";
    private int currentExpandedPackageId = -1;
    private JPanel packageContainer;

    // Images
    private BufferedImage titleImage;
    private BufferedImage iconImage;
    private BufferedImage logoImage;

    // User Data
    private int customerId;
    private String userName;
    private String userSurname;
    private String userPhone;
    private String userEmail;
    private String userPassword;

    // Processing flag
    private AtomicBoolean isProcessing = new AtomicBoolean(false);

    public StoreController(int customerId, String name, String surname, String phone, String email, String password) {
        this.customerId = customerId;
        this.userName = name;
        this.userSurname = surname;
        this.userPhone = phone;
        this.userEmail = email;
        this.userPassword = password;

        System.out.println("🏪 StoreController başlatılıyor...");
        System.out.println("👤 Customer ID: " + customerId);
        System.out.println("📝 User: " + name + " " + surname);

        initializeFrame();
        loadImages();
        createUserInterface();

        // API bağlantısını test et
        testApiConnection();
    }

    private void initializeFrame() {
        setTitle("Cellenta Store");
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
            iconImage = ImageIO.read(getClass().getResourceAsStream("/images/icon-white.png"));
            System.out.println("✅ Icon image loaded");
        } catch (Exception e) {
            System.out.println("⚠️ Icon image not found");
            iconImage = null;
        }

        try {
            logoImage = ImageIO.read(getClass().getResourceAsStream("/images/logo.png"));
            System.out.println("✅ Logo image loaded");
        } catch (Exception e) {
            System.out.println("⚠️ Logo image not found");
            logoImage = null;
        }
    }

    private void createUserInterface() {
        // Ana background panel oluştur
        JPanel backgroundPanel = createBackgroundPanel();
        backgroundPanel.setLayout(null); // Absolute layout kullan
        setContentPane(backgroundPanel);

        // Header ekle
        JPanel header = createHeader();
        backgroundPanel.add(header);

        // Sidebar ekle
        JPanel sidebar = createSidebar();
        backgroundPanel.add(sidebar);

        // Main content ekle
        JPanel mainContent = createMainContent();
        backgroundPanel.add(mainContent);

        // Chat bot ekle
        JPanel chatBot = createChatBot();
        backgroundPanel.add(chatBot);

        // Tüm componentleri yenile
        backgroundPanel.revalidate();
        backgroundPanel.repaint();
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
                    // Resim yoksa gradient arka plan
                    Graphics2D g2d = (Graphics2D) g;
                    GradientPaint gp = new GradientPaint(0, 0, new Color(240, 248, 255), 0, getHeight(), new Color(230, 240, 250));
                    g2d.setPaint(gp);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        panel.setLayout(null);
        return panel;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setLayout(null);
        header.setBackground(Color.WHITE);
        header.setBounds(0, 0, 1400, 100);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

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
                    g2d.setFont(new Font("Arial", Font.BOLD, 30));
                    g2d.drawString("●", x, y + 30);
                    x += 50;
                }

                if (titleImage != null) {
                    g2d.drawImage(titleImage, x, y + 5, 150, 30, this);
                } else {
                    g2d.setColor(Color.BLACK);
                    g2d.setFont(new Font("Arial", Font.BOLD, 28));
                    g2d.drawString("CELLENTA", x, y + 30);
                }
            }
        };
        logoPanel.setBounds(600, 25, 250, 50);
        logoPanel.setOpaque(false);
        header.add(logoPanel);

        return header;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(null);
        sidebar.setBounds(0, 100, 250, 800);
        sidebar.setOpaque(false);

        String[] items = {"HOME", "STORE", "BILLS", "PROFILE"};

        for (int i = 0; i < items.length; i++) {
            JLabel label = new JLabel(items[i]);
            label.setFont(new Font("Arial", items[i].equals("STORE") ? Font.BOLD : Font.PLAIN, 14));
            label.setForeground(items[i].equals("STORE") ? new Color(52, 52, 52) : new Color(150, 150, 150));
            label.setBounds(50, 150 + (i * 50), 150, 25);
            label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            final int index = i;
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleNavigation(index);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!items[index].equals("STORE")) {
                        label.setForeground(new Color(22, 105, 143));
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    label.setForeground(items[index].equals("STORE") ? new Color(52, 52, 52) : new Color(150, 150, 150));
                }
            });

            sidebar.add(label);
        }

        return sidebar;
    }

    private void handleNavigation(int index) {
        if (index == 1) {
            // Already on STORE page
            return;
        }
        
        SwingUtilities.invokeLater(() -> {
            setVisible(false);
            
            switch (index) {
                case 0:
                    SwingUtilities.invokeLater(() -> 
                        new MainController(customerId, userName, userSurname, userPhone, userEmail, userPassword).setVisible(true));
                    break;
                case 2:
                    SwingUtilities.invokeLater(() -> 
                        new BillHistoryController(customerId, userName, userSurname, userPhone, userEmail, userPassword).setVisible(true));
                    break;
                case 3:
                    SwingUtilities.invokeLater(() -> 
                        new ProfileController(customerId, userName, userSurname, userPhone, userEmail).setVisible(true));
                    break;
            }
        });
    }

    private JPanel createMainContent() {
        JPanel mainContent = new JPanel();
        mainContent.setLayout(null);
        mainContent.setOpaque(false);
        mainContent.setBounds(250, 100, 1150, 800);

        // Back button
        JLabel backButton = new JLabel("< Store");
        backButton.setFont(new Font("Arial", Font.PLAIN, 20));
        backButton.setForeground(new Color(120, 120, 120));
        backButton.setBounds(80, 40, 150, 30);
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        backButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SwingUtilities.invokeLater(() -> {
                    setVisible(false);
                    SwingUtilities.invokeLater(() -> 
                        new MainController(customerId, userName, userSurname, userPhone, userEmail, userPassword).setVisible(true)
                    );
                });
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                backButton.setForeground(new Color(22, 105, 143));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                backButton.setForeground(new Color(120, 120, 120));
            }
        });
        mainContent.add(backButton);

        // Package container - beyaz arka planlı kutu
        JPanel packageMainContainer = new JPanel();
        packageMainContainer.setLayout(null);
        packageMainContainer.setBackground(Color.WHITE);
        packageMainContainer.setBounds(80, 100, 900, 500);
        packageMainContainer.setBorder(createRoundedBorder(new Color(230, 230, 230), 1, 20));
        mainContent.add(packageMainContainer);

        // Loading message
        JLabel loadingLabel = new JLabel("📦 Paketler yükleniyor...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        loadingLabel.setForeground(new Color(108, 117, 125));
        loadingLabel.setBounds(0, 200, 900, 30);
        packageMainContainer.add(loadingLabel);

        // Load packages in background
        loadPackagesFromAPI(packageMainContainer, loadingLabel);

        return mainContent;
    }

    private void loadPackagesFromAPI(JPanel container, JLabel loadingLabel) {
        System.out.println("📦 Starting package loading...");

        Timer loadTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((Timer) e.getSource()).stop();

                Thread loadThread = new Thread(() -> {
                    try {
                        System.out.println("📡 Fetching packages from API...");
                        JSONArray packages = PackageHelper.getAllPackages();

                        SwingUtilities.invokeLater(() -> {
                            container.remove(loadingLabel);
                            displayPackages(container, packages);
                            container.revalidate();
                            container.repaint();
                        });

                    } catch (Exception ex) {
                        System.err.println("❌ Package loading error: " + ex.getMessage());
                        ex.printStackTrace();

                        SwingUtilities.invokeLater(() -> {
                            container.remove(loadingLabel);
                            showErrorMessage(container, "❌ Paketler yüklenemedi: " + ex.getMessage());
                            container.revalidate();
                            container.repaint();
                        });
                    }
                });

                loadThread.setName("PackageLoadingThread");
                loadThread.start();
            }
        });
        loadTimer.start();
    }

    private void displayPackages(JPanel container, JSONArray packages) {
        System.out.println("🎨 Displaying packages in UI...");

        packageContainer = new JPanel();
        packageContainer.setLayout(new BoxLayout(packageContainer, BoxLayout.Y_AXIS));
        packageContainer.setBackground(Color.WHITE);

        if (packages != null && packages.length() > 0) {
            System.out.println("✅ Creating " + packages.length() + " package panels");

            for (int i = 0; i < packages.length(); i++) {
                try {
                    JSONObject pkg = packages.getJSONObject(i);

                    String name = pkg.optString("packageName", "Package " + (i + 1));
                    int minutes = pkg.optInt("amountMinutes", 0);
                    int sms = pkg.optInt("amountSms", 0);
                    int dataGB = pkg.optInt("amountData", 0) / 1024;

                    int packageId = getPackageId(pkg, i);
                    String description = minutes + " dk " + sms + " sms " + dataGB + " GB";

                    System.out.println("📦 Creating panel for: " + name);
                    JPanel packagePanel = createPackageItem(name, description, packageId);
                    packageContainer.add(packagePanel);

                    if (i < packages.length() - 1) {
                        packageContainer.add(Box.createRigidArea(new Dimension(0, 15)));
                    }

                } catch (Exception e) {
                    System.err.println("❌ Error processing package " + i + ": " + e.getMessage());
                }
            }
        } else {
            System.out.println("⚠️ No packages found, showing error");
            JLabel errorLabel = new JLabel("❌ Hiç paket bulunamadı", SwingConstants.CENTER);
            errorLabel.setFont(new Font("Arial", Font.ITALIC, 16));
            errorLabel.setForeground(Color.RED);
            errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            packageContainer.add(errorLabel);
        }

        // Scroll pane oluştur
        JScrollPane scrollPane = new JScrollPane(packageContainer);
        scrollPane.setBounds(20, 20, 860, 460);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        container.add(scrollPane);

        System.out.println("✅ Packages displayed successfully");
    }

    private int getPackageId(JSONObject pkg, int index) {
        if (pkg.has("package_id")) return pkg.getInt("package_id");
        if (pkg.has("packageId")) return pkg.getInt("packageId");
        if (pkg.has("id")) return pkg.getInt("id");
        return index + 1; // fallback
    }

    private void showErrorMessage(JPanel container, String message) {
        JLabel errorLabel = new JLabel(message, SwingConstants.CENTER);
        errorLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        errorLabel.setForeground(Color.RED);
        errorLabel.setBounds(0, 200, 900, 30);
        container.add(errorLabel);
    }

    private JPanel createPackageItem(String name, String description, int packageId) {
        System.out.println("🔧 Creating package item: " + name);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(840, 80));
        panel.setMaximumSize(new Dimension(840, 80));
        panel.setMinimumSize(new Dimension(840, 80));
        panel.setBackground(Color.WHITE);
        panel.setBorder(createRoundedBorder(new Color(230, 230, 230), 1, 15));

        // Left side - package info
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel(name);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        descLabel.setForeground(new Color(120, 120, 120));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftPanel.add(titleLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        leftPanel.add(descLabel);

        // Right side - expand button
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 25));

        JButton expandButton = createExpandButton();
        expandButton.addActionListener(e -> handlePackageExpansion(panel, name, description, packageId));

        rightPanel.add(expandButton, BorderLayout.CENTER);

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    private JButton createExpandButton() {
        JButton button = new JButton(">");
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setForeground(new Color(153, 102, 255));
        button.setBackground(new Color(245, 240, 255));
        button.setPreferredSize(new Dimension(40, 40));
        button.setBorder(createRoundedBorder(new Color(245, 240, 255), 0, 20));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(235, 230, 255));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(245, 240, 255));
            }
        });

        return button;
    }

    private void handlePackageExpansion(JPanel panel, String name, String description, int packageId) {
        if (expandedPanel != null && expandedPanel != panel) {
            collapseCurrentPanel();
        }

        if (!isExpanded || expandedPanel != panel) {
            expandPackagePanel(panel, name, description, packageId);
        } else {
            collapseCurrentPanel();
        }
    }

    private void expandPackagePanel(JPanel panel, String name, String description, int packageId) {
        if (expandedPanel != null) {
            collapseCurrentPanel();
        }

        currentExpandedName = name;
        currentExpandedDesc = description;
        currentExpandedPackageId = packageId;

        // Resize panel
        panel.setPreferredSize(new Dimension(840, 280));
        panel.setMaximumSize(new Dimension(840, 280));
        panel.setMinimumSize(new Dimension(840, 280));

        // Clear and rebuild
        panel.removeAll();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(createRoundedBorder(new Color(230, 230, 230), 1, 15));

        // Top section
        JPanel topPanel = createExpandedTopSection(name);

        // Middle section
        JPanel middlePanel = createExpandedMiddleSection(description);

        // Bottom section
        JPanel bottomPanel = createExpandedBottomSection(name, packageId);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(middlePanel, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        expandedPanel = panel;
        isExpanded = true;

        refreshPanelDisplay(panel);
    }

    private JPanel createExpandedTopSection(String name) {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 0, 25));

        JLabel title = new JLabel(name);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(Color.BLACK);

        JButton closeButton = new JButton("×");
        closeButton.setFont(new Font("Arial", Font.BOLD, 24));
        closeButton.setForeground(new Color(153, 102, 255));
        closeButton.setBackground(Color.WHITE);
        closeButton.setPreferredSize(new Dimension(30, 30));
        closeButton.setBorder(null);
        closeButton.setFocusPainted(false);
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> collapseCurrentPanel());

        closeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                closeButton.setForeground(new Color(133, 82, 235));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                closeButton.setForeground(new Color(153, 102, 255));
            }
        });

        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(closeButton, BorderLayout.EAST);

        return topPanel;
    }

    private JPanel createExpandedMiddleSection(String description) {
        JPanel middlePanel = new JPanel(null); // Absolute layout for better control
        middlePanel.setBackground(Color.WHITE);
        middlePanel.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));

        // Parse package description more intelligently
        String[] parts = description.split(" ");
        
        // Create feature rows with icons and better layout
        int yPos = 0;
        
        if (parts.length >= 6) {
            // Minutes/Calls
            yPos += addFeatureRow(middlePanel, 0, yPos, "📞", "Konuşma", parts[0] + " " + parts[1], new Color(22, 105, 143));
            
            // SMS
            yPos += addFeatureRow(middlePanel, 0, yPos, "💬", "SMS", parts[2] + " " + parts[3], new Color(53, 219, 149));
            
            // Internet
            String internetText = parts.length > 6 ? String.join(" ", java.util.Arrays.copyOfRange(parts, 4, parts.length)) 
                                                  : parts[4] + " " + parts[5];
            yPos += addFeatureRow(middlePanel, 0, yPos, "🌐", "İnternet", internetText, new Color(255, 142, 60));
        } else {
            // Fallback for unknown format
            yPos += addFeatureRow(middlePanel, 0, yPos, "📋", "Paket İçeriği", description, new Color(120, 120, 120));
        }

        // Add some spacing at the bottom and set preferred height
        yPos += 15; // Extra bottom margin
        middlePanel.setPreferredSize(new Dimension(780, yPos));
        
        return middlePanel;
    }

    /**
     * Creates a feature row with icon, title, and value
     * Returns the height used for positioning next element
     */
    private int addFeatureRow(JPanel parent, int x, int y, String iconText, String title, String value, Color accentColor) {
        final int ROW_HEIGHT = 50;
        final int ICON_SIZE = 35;
        
        // Icon circle background
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw circular background
                g2d.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 20));
                g2d.fillOval(0, 0, ICON_SIZE, ICON_SIZE);
                
                // Draw custom icon based on type
                g2d.setColor(accentColor);
                g2d.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                
                int centerX = ICON_SIZE / 2;
                int centerY = ICON_SIZE / 2;
                
                switch (iconText) {
                    case "📞": // Phone icon
                        g2d.drawRoundRect(centerX - 6, centerY - 8, 12, 16, 4, 4);
                        g2d.drawLine(centerX - 3, centerY - 5, centerX + 3, centerY - 5);
                        g2d.drawLine(centerX - 3, centerY - 2, centerX + 3, centerY - 2);
                        g2d.drawLine(centerX - 3, centerY + 1, centerX + 3, centerY + 1);
                        break;
                    case "💬": // SMS/Message icon
                        g2d.drawRoundRect(centerX - 8, centerY - 6, 16, 10, 4, 4);
                        int[] xPoints = {centerX - 2, centerX + 2, centerX};
                        int[] yPoints = {centerY + 4, centerY + 4, centerY + 8};
                        g2d.drawPolyline(xPoints, yPoints, 3);
                        g2d.fillOval(centerX - 5, centerY - 3, 2, 2);
                        g2d.fillOval(centerX - 1, centerY - 3, 2, 2);
                        g2d.fillOval(centerX + 3, centerY - 3, 2, 2);
                        break;
                    case "🌐": // Internet/Globe icon
                        g2d.drawOval(centerX - 8, centerY - 8, 16, 16);
                        g2d.drawLine(centerX - 8, centerY, centerX + 8, centerY);
                        g2d.drawArc(centerX - 5, centerY - 8, 10, 16, 0, 180);
                        g2d.drawArc(centerX - 5, centerY - 8, 10, 16, 180, 180);
                        break;
                    default: // Fallback to emoji
                        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
                        FontMetrics fm = g2d.getFontMetrics();
                        int textX = (ICON_SIZE - fm.stringWidth(iconText)) / 2;
                        int textY = (ICON_SIZE + fm.getAscent()) / 2 - 2;
                        g2d.drawString(iconText, textX, textY);
                        break;
                }
            }
        };
        iconPanel.setBounds(x, y + 7, ICON_SIZE, ICON_SIZE);
        iconPanel.setOpaque(false);
        parent.add(iconPanel);
        
        // Title label
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(52, 52, 52));
        titleLabel.setBounds(x + ICON_SIZE + 15, y, 120, 25);
        parent.add(titleLabel);
        
        // Value label
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 18));
        valueLabel.setForeground(accentColor);
        valueLabel.setBounds(x + ICON_SIZE + 15, y + 25, 400, 25);
        parent.add(valueLabel);
        
        return ROW_HEIGHT;
    }

    private JPanel createExpandedBottomSection(String name, int packageId) {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 30, 30));

        JButton selectButton = new JButton("Paketi Seç");
        selectButton.setFont(new Font("Arial", Font.BOLD, 16));
        selectButton.setForeground(Color.WHITE);
        selectButton.setBackground(new Color(40, 167, 69));
        selectButton.setPreferredSize(new Dimension(800, 50));
        selectButton.setBorder(createRoundedBorder(new Color(40, 167, 69), 0, 25));
        selectButton.setFocusPainted(false);
        selectButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        selectButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                selectButton.setBackground(new Color(33, 136, 56));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                selectButton.setBackground(new Color(40, 167, 69));
            }
        });

        selectButton.addActionListener(e -> handlePackageSelection(name, packageId));

        bottomPanel.add(selectButton, BorderLayout.CENTER);
        return bottomPanel;
    }

    private void handlePackageSelection(String packageName, int packageId) {
        if (isProcessing.get()) {
            System.out.println("⚠️ Already processing a package selection");
            return;
        }

        System.out.println("🎯 PACKAGE SELECTION STARTED");
        System.out.println("👤 Customer ID: " + customerId);
        System.out.println("📦 Package: " + packageName + " (ID: " + packageId + ")");

        if (packageId <= 0 || customerId <= 0) {
            showErrorDialog("❌ Invalid IDs - Customer: " + customerId + ", Package: " + packageId);
            return;
        }

        isProcessing.set(true);
        updateSelectionStatus("⏳ Paket ekleniyor...", new Color(0, 123, 255));

        Thread selectionThread = new Thread(() -> {
            try {
                System.out.println("📤 Calling PackageHelper.addPackageToCustomer...");
                PackageHelper.PackageResponse response = PackageHelper.addPackageToCustomer(customerId, packageId);

                System.out.println("📥 PackageHelper response: " + response);

                SwingUtilities.invokeLater(() -> {
                    isProcessing.set(false);
                    processPackageResponse(response, packageName);
                });

            } catch (Exception e) {
                System.err.println("💥 Selection thread error: " + e.getMessage());
                e.printStackTrace();

                SwingUtilities.invokeLater(() -> {
                    isProcessing.set(false);
                    updateSelectionStatus("❌ Hata oluştu!", Color.RED);

                    Timer errorTimer = new Timer(2000, event -> {
                        showErrorDialog("Beklenmeyen hata: " + e.getMessage());
                        resetExpandedPanel();
                    });
                    errorTimer.setRepeats(false);
                    errorTimer.start();
                });
            }
        });

        selectionThread.setName("PackageSelectionThread-" + packageId);
        selectionThread.start();
    }

    private void updateSelectionStatus(String text, Color color) {
        if (expandedPanel != null) {
            Component bottomComponent = ((BorderLayout) expandedPanel.getLayout()).getLayoutComponent(BorderLayout.SOUTH);
            if (bottomComponent instanceof JPanel) {
                JPanel bottomPanel = (JPanel) bottomComponent;
                bottomPanel.removeAll();

                JLabel statusLabel = new JLabel(text, SwingConstants.CENTER);
                statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
                statusLabel.setForeground(color);

                bottomPanel.add(statusLabel, BorderLayout.CENTER);
                bottomPanel.revalidate();
                bottomPanel.repaint();
            }
        }
    }

    private void processPackageResponse(PackageHelper.PackageResponse response, String packageName) {
        System.out.println("🎉 Processing response: " + response.isSuccess());

        try {
            if (response.isSuccess()) {
                System.out.println("✅ PACKAGE SUCCESSFULLY ADDED!");

                updateSelectionStatus("✅ Başarılı!", new Color(0, 128, 0));

                Timer successTimer = new Timer(1000, e -> {
                    ((Timer) e.getSource()).stop();
                    showSuccessDialog(packageName);
                });
                successTimer.start();

            } else {
                System.out.println("❌ PACKAGE ADDITION FAILED: " + response.getMessage());

                updateSelectionStatus("❌ " + response.getMessage(), Color.RED);

                if (response.getMessage().contains("zaten mevcut")) {
                    updateSelectionStatus("⚠️ " + response.getMessage(), new Color(255, 165, 0));

                    Timer warningTimer = new Timer(2000, e -> {
                        ((Timer) e.getSource()).stop();
                        showWarningDialog(response.getMessage());
                        collapseCurrentPanel();
                    });
                    warningTimer.start();

                } else {
                    Timer errorTimer = new Timer(2000, e -> {
                        ((Timer) e.getSource()).stop();
                        showErrorDialog(response.getMessage());
                        resetExpandedPanel();
                    });
                    errorTimer.start();
                }
            }

        } catch (Exception e) {
            System.err.println("💥 Response processing error: " + e.getMessage());
            e.printStackTrace();

            updateSelectionStatus("❌ İşleme hatası!", Color.RED);

            Timer errorTimer = new Timer(2000, event -> {
                showErrorDialog("Response processing error: " + e.getMessage());
                resetExpandedPanel();
            });
            errorTimer.setRepeats(false);
            errorTimer.start();
        }
    }
// StoreController.java içindeki showSuccessDialog metodunu bu şekilde değiştirin:

    // StoreController.java içindeki showSuccessDialog metodunu bu şekilde değiştirin:
// StoreController.java içindeki showSuccessDialog metodunu bu şekilde değiştirin:

// StoreController.java içindeki showSuccessDialog metodunu bu şekilde değiştirin:

    private void showSuccessDialog(String packageName) {
        try {
            int choice = JOptionPane.showConfirmDialog(this,
                    "🎉 BAŞARILI!\n\n" +
                            "'" + packageName + "' paketi hesabınıza eklendi!\n" +
                            "Paket aktif duruma geçti.\n\n" +
                            "Ana sayfaya gitmek istiyor musunuz?",
                    "Paket Eklendi",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);

            collapseCurrentPanel();

            if (choice == JOptionPane.YES_OPTION) {
                System.out.println("🏠 Ana sayfaya yönlendiriliyor...");

                // Simple direct redirect with small delay
                Timer simpleTimer = new Timer(800, e -> {
                    try {
                        ((Timer) e.getSource()).stop();

                        System.out.println("✅ StoreController kapatılıyor...");
                        dispose();

                        System.out.println("🏠 MainController oluşturuluyor...");
                        MainController mainController = new MainController(customerId, userName, userSurname, userPhone, userEmail, userPassword);
                        mainController.setVisible(true);

                        System.out.println("✅ MainController başarıyla gösteriliyor!");

                    } catch (Exception ex) {
                        System.err.println("❌ Redirect error: " + ex.getMessage());
                        ex.printStackTrace();

                        // Emergency fallback
                        try {
                            dispose();
                            new MainController(customerId, userName, userSurname, userPhone, userEmail, userPassword).setVisible(true);
                        } catch (Exception ex2) {
                            System.err.println("❌ Emergency redirect failed: " + ex2.getMessage());
                        }
                    }
                });

                simpleTimer.setRepeats(false);
                simpleTimer.start();

                System.out.println("⏰ Simple redirect timer started (0.8 seconds)");
            }

        } catch (Exception e) {
            System.err.println("💥 Success dialog error: " + e.getMessage());
            e.printStackTrace();
        }
    }

// createLoadingDialog metodunu tamamen kaldırın - artık kullanılmıyor

    private JDialog createLoadingDialog() {
        JDialog dialog = new JDialog(this, "Yükleniyor...", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(350, 120);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        // Simple loading label
        JLabel loadingLabel = new JLabel("🔄 Ana sayfaya yönlendiriliyor...");
        loadingLabel.setFont(new Font("Arial", Font.BOLD, 16));
        loadingLabel.setForeground(new Color(22, 105, 143));

        panel.add(loadingLabel);
        dialog.add(panel);

        // Auto-close after 3 seconds as failsafe
        Timer failsafeTimer = new Timer(3000, e -> {
            System.out.println("⚠️ Failsafe: Dialog force close");
            dialog.setVisible(false);
            dialog.dispose();
            ((Timer) e.getSource()).stop();
        });
        failsafeTimer.setRepeats(false);
        failsafeTimer.start();

        return dialog;
    }




    private void showWarningDialog(String message) {
        JOptionPane.showMessageDialog(this, "⚠️ " + message, "Uyarı", JOptionPane.WARNING_MESSAGE);
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, "❌ " + message, "Hata", JOptionPane.ERROR_MESSAGE);
    }

    private void resetExpandedPanel() {
        try {
            if (expandedPanel != null) {
                expandPackagePanel(expandedPanel, currentExpandedName, currentExpandedDesc, currentExpandedPackageId);
            }
        } catch (Exception e) {
            System.err.println("💥 Panel reset error: " + e.getMessage());
            collapseCurrentPanel();
        }
    }

    private void collapseCurrentPanel() {
        if (expandedPanel == null) return;

        expandedPanel.setPreferredSize(new Dimension(840, 80));
        expandedPanel.setMaximumSize(new Dimension(840, 80));
        expandedPanel.setMinimumSize(new Dimension(840, 80));

        expandedPanel.removeAll();
        expandedPanel.setLayout(new BorderLayout());
        expandedPanel.setBackground(Color.WHITE);
        expandedPanel.setBorder(createRoundedBorder(new Color(230, 230, 230), 1, 15));

        // Recreate collapsed content
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel title = new JLabel(currentExpandedName);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(Color.BLACK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel details = new JLabel(currentExpandedDesc);
        details.setFont(new Font("Arial", Font.PLAIN, 14));
        details.setForeground(new Color(120, 120, 120));
        details.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftPanel.add(title);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        leftPanel.add(details);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 25));

        JButton expandButton = createExpandButton();
        expandButton.addActionListener(e -> expandPackagePanel(expandedPanel, currentExpandedName, currentExpandedDesc, currentExpandedPackageId));

        rightPanel.add(expandButton, BorderLayout.CENTER);

        expandedPanel.add(leftPanel, BorderLayout.WEST);
        expandedPanel.add(rightPanel, BorderLayout.EAST);

        refreshPanelDisplay(expandedPanel);

        expandedPanel = null;
        isExpanded = false;
        currentExpandedName = "";
        currentExpandedDesc = "";
        currentExpandedPackageId = -1;
    }

    private void refreshPanelDisplay(JPanel panel) {
        panel.revalidate();
        panel.repaint();

        Container parent = panel.getParent();
        if (parent != null) {
            parent.revalidate();
            parent.repaint();
        }
    }

    private JPanel createChatBot() {
        JPanel chatContainer = new JPanel();
        chatContainer.setLayout(null);
        chatContainer.setBounds(1050, 720, 330, 60);
        chatContainer.setOpaque(false);

        // Chat message bubble
        JPanel chatMessage = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.fillRoundRect(2, 2, getWidth()-2, getHeight()-2, 20, 20);
            }
        };
        chatMessage.setLayout(null);
        chatMessage.setBounds(0, 0, 260, 60);
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
        chatBot.setBounds(270, 0, 60, 60);
        chatBot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        chatBot.setOpaque(false);

        chatBot.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleChatBotClick();
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

        chatContainer.add(chatMessage);
        chatContainer.add(chatBot);

        return chatContainer;
    }

    private void handleChatBotClick() {
        System.out.println("🖱️ Chat bot clicked!");

        try {
            System.out.println("💬 Creating ChatBotWindow...");
            ChatBotWindow chatWindow = new ChatBotWindow();
            System.out.println("✅ ChatBotWindow created!");

            chatWindow.setVisible(true);
            System.out.println("🎯 ChatBotWindow made visible!");

        } catch (Exception ex) {
            System.err.println("❌ ChatBot error: " + ex.getMessage());
            ex.printStackTrace();

            JOptionPane.showMessageDialog(this,
                    "Chat bot açılırken hata oluştu: " + ex.getMessage(),
                    "Hata",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void testApiConnection() {
        System.out.println("🧪 Starting API connection test...");

        Thread testThread = new Thread(() -> {
            try {
                boolean connectionOk = PackageHelper.testConnection();
                System.out.println("🧪 API connection test: " + (connectionOk ? "✅ Success" : "❌ Failed"));

                if (customerId > 0) {
                    JSONArray customerPackages = PackageHelper.getCustomerPackages(customerId);
                    if (customerPackages != null) {
                        System.out.println("🧪 Customer package count: " + customerPackages.length());
                    } else {
                        System.out.println("🧪 Could not retrieve customer packages");
                    }
                }

            } catch (Exception e) {
                System.err.println("🧪 Test error: " + e.getMessage());
            }
        });

        testThread.setName("ApiTestThread");
        testThread.start();
    }

    private javax.swing.border.Border createRoundedBorder(Color color, int thickness, int radius) {
        return new javax.swing.border.AbstractBorder() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                g2d.setStroke(new BasicStroke(thickness));
                g2d.draw(new RoundRectangle2D.Float(x, y, width - 1, height - 1, radius, radius));
                g2d.dispose();
            }

            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(thickness, thickness, thickness, thickness);
            }
        };
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Test with sample data
                new StoreController(1, "Test", "User", "5551234567", "test@example.com", "password").setVisible(true);
            } catch (Exception e) {
                System.err.println("❌ StoreController initialization error: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}