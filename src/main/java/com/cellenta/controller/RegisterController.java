package com.cellenta.controller;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.net.HttpURLConnection;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.URL;
import com.cellenta.PackageHelper;
import org.json.JSONArray;
import org.json.JSONObject;

public class RegisterController extends JFrame {
    private JPanel stepOnePanel, stepTwoPanel;
    private JTextField firstNameField, lastNameField, phoneField, emailField;
    private JPasswordField passwordField, confirmPasswordField;
    private JLabel warningLabel;
    private JLabel stepOneWarningLabel;
    private JComboBox<Object> packageComboBox;
    private JSONArray availablePackages;
    private BufferedImage backgroundImage;
    private BufferedImage titleImage;
    private BufferedImage iconImage;
    private JButton backButton;
    
    // Panel referansları responsive layout için
    private JPanel leftPanel;
    private JPanel formContainer;
    
    // Temporary fields for registration data
    private String tempFirstName;
    private String tempLastName;
    private String tempPhone;
    private String tempEmail;
    private String tempPassword;

    public RegisterController() {
        setTitle("Kayıt Ol");
        setSize(885, 716);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        // Arka plan resmini yükle
        try {
            backgroundImage = ImageIO.read(getClass().getResourceAsStream("/images/background_mavi.jpg"));
            if (backgroundImage == null) {
                backgroundImage = ImageIO.read(getClass().getResourceAsStream("/resources/images/background_mavi.jpg"));
            }
            System.out.println("Background image loaded: " + (backgroundImage != null));
        } catch (Exception e) {
            System.out.println("Background image not found, using default gradient: " + e.getMessage());
            backgroundImage = null;
        }

        // Title ve icon resimlerini yükle
        try {
            titleImage = ImageIO.read(getClass().getResourceAsStream("/images/title-white.png"));
            if (titleImage == null) {
                titleImage = ImageIO.read(getClass().getResourceAsStream("/resources/images/title-white.png"));
            }
            System.out.println("Title image loaded: " + (titleImage != null));
        } catch (Exception e) {
            System.out.println("Title image not found: " + e.getMessage());
            titleImage = null;
        }

        try {
            iconImage = ImageIO.read(getClass().getResourceAsStream("/images/icon-white.png"));
            if (iconImage == null) {
                iconImage = ImageIO.read(getClass().getResourceAsStream("/resources/images/icon-white.png"));
            }
            System.out.println("Icon image loaded: " + (iconImage != null));
        } catch (Exception e) {
            System.out.println("Icon image not found: " + e.getMessage());
            iconImage = null;
        }

        // Load available packages
        loadPackages();

        // Ana arkaplan panel - background image veya gradient
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (backgroundImage != null) {
                    // Resim varsa resmi çiz
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    // Resim yoksa varsayılan gradient arka plan
                    GradientPaint gp = new GradientPaint(
                            0, 0, new Color(0x7DD3C0),
                            getWidth(), getHeight(), new Color(0x4A9EBB));
                    g2d.setPaint(gp);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        backgroundPanel.setBounds(0, 0, getWidth(), getHeight());
        backgroundPanel.setLayout(null);
        setContentPane(backgroundPanel);

        // Sol taraf - Logo ve slogan
        createLeftSection(backgroundPanel);

        // Sağ taraf - Form
        createRightSection(backgroundPanel);
        
        // Responsive layout için component listener ekle
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateLayout();
            }
        });
        
        // İlk layout'u ayarla
        SwingUtilities.invokeLater(() -> updateLayout());
        
        // Load existing data if any
        loadStepOneData();
    }

    private void createLeftSection(JPanel parent) {
        // Sol Panel (Şeffaf arka plan)
        leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Şeffaf overlay
                g2d.setColor(new Color(0, 0, 0, 30));
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Dekoratif şekiller
                g2d.setColor(new Color(255, 255, 255, 20));
                g2d.fillOval(-50, -50, 200, 200);
                g2d.fillOval(getWidth()-100, getHeight()-100, 150, 150);
            }
        };
        leftPanel.setBounds(0, 0, 442, 716);
        leftPanel.setLayout(null);
        leftPanel.setOpaque(false);
        parent.add(leftPanel);

        // Logo paneli - Icon ve Title resimleri ile
        JPanel logoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                int x = 0;
                int y = 0;

                // Icon resmi çiz
                if (iconImage != null) {
                    g2d.drawImage(iconImage, x, y, 60, 60, this);
                    x += 70; // Icon'dan sonra title için boşluk
                } else {
                    System.out.println("Icon image is null!");
                }

                // Title resmi çiz
                if (titleImage != null) {
                    g2d.drawImage(titleImage, x, y + 10, 200, 40, this);
                } else {
                    System.out.println("Title image is null!");
                }
            }
        };
        logoPanel.setBounds(80, 120, 350, 80);
        logoPanel.setOpaque(false);
        leftPanel.add(logoPanel);

        // Slogan
        JLabel slogan = new JLabel("<html><div style='font-size:20px; font-weight:bold; margin-bottom:15px;'>Sizi Yarının Dünyasına Bağlıyoruz</div><br>"
                + "<div style='font-size:12px; line-height:1.4; opacity:0.9;'>Kesintisiz iletişim, güvenilir kapsama ve<br>gerçekten bağlantılı bir dünya için<br>yenilikçi çözümler deneyimleyin.</div></html>");
        slogan.setForeground(Color.WHITE);
        slogan.setBounds(80, 210, 300, 220);
        leftPanel.add(slogan);

        // Progress indicator
        JPanel progressPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Progress bars
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, 60, 4, 2, 2);
                g2d.setColor(new Color(255, 255, 255, 100));
                g2d.fillRoundRect(70, 0, 30, 4, 2, 2);
                g2d.fillRoundRect(110, 0, 30, 4, 2, 2);
            }
        };
        progressPanel.setBounds(80, 450, 150, 10);
        progressPanel.setOpaque(false);
        leftPanel.add(progressPanel);
    }

    private void createRightSection(JPanel parent) {
        // Form container with rounded corners
        formContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            }
        };
        formContainer.setBounds(442, 50, 443, 666); // Moved up and increased height
        formContainer.setLayout(null);
        formContainer.setOpaque(false);
        parent.add(formContainer);

        // Başlık
        JLabel title = new JLabel("HADI BAŞLAYALIM", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.PLAIN, 12));
        title.setForeground(new Color(0x999999));
        title.setBounds(0, 30, 450, 20);
        formContainer.add(title);

        // Alt başlık
        JLabel subtitle = new JLabel("Hesap Oluştur", SwingConstants.CENTER);
        subtitle.setFont(new Font("Arial", Font.BOLD, 28));
        subtitle.setForeground(new Color(0x333333));
        subtitle.setBounds(0, 60, 450, 40);
        formContainer.add(subtitle);

        // Step One Panel
        stepOnePanel = new JPanel();
        stepOnePanel.setLayout(null);
        stepOnePanel.setBounds(35, 120, 380, 420);
        stepOnePanel.setOpaque(false);
        formContainer.add(stepOnePanel);

        createStepOneFields();

        // Step Two Panel
        stepTwoPanel = new JPanel();
        stepTwoPanel.setLayout(null);
        stepTwoPanel.setBounds(35, 120, 380, 430); // Further increased height for better spacing
        stepTwoPanel.setOpaque(false);
        stepTwoPanel.setVisible(false);
        formContainer.add(stepTwoPanel);

        createStepTwoFields();

        // Login link
        JLabel loginLink = new JLabel("<html>Zaten hesabınız var mı? <font color='#4A9EBB'><u>Giriş Yap</u></font></html>", SwingConstants.CENTER);
        loginLink.setFont(new Font("Arial", Font.PLAIN, 14));
        loginLink.setForeground(new Color(0x666666));
        loginLink.setBounds(0, 560, 450, 20); // Adjusted position - not too low, not conflicting with sign-up
        loginLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginLink.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                openLoginPage();
            }
        });
        formContainer.add(loginLink);
        
        // Back button (positioned outside the form container, top-left)
        JButton backButton = createModernButton("← Geri Dön", new Color(0xCCCCCC));
        backButton.setBounds(35, 50, 150, 45);
        backButton.setVisible(false); // Initially hidden
        backButton.addActionListener(e -> {
            // Save current step two data
            saveStepTwoData();
            // Go back to step one
            stepTwoPanel.setVisible(false);
            stepOnePanel.setVisible(true);
            backButton.setVisible(false); // Hide back button when on step one
        });
        parent.add(backButton); // Add to main background panel, not form container
        
        // Store reference to back button for later use
        this.backButton = backButton;
    }

    private void createStepOneFields() {
        int yPos = 0;

        // First Name Field
        JPanel firstNameContainer = createFieldContainer("Ad", "Adınızı girin", yPos);
        firstNameField = (JTextField) firstNameContainer.getComponent(1);
        addCharacterLimit(firstNameField, 60);
        stepOnePanel.add(firstNameContainer);
        yPos += 75;

        // Last Name Field
        JPanel lastNameContainer = createFieldContainer("Soyad", "Soyadınızı girin", yPos);
        lastNameField = (JTextField) lastNameContainer.getComponent(1);
        addCharacterLimit(lastNameField, 60);
        stepOnePanel.add(lastNameContainer);
        yPos += 75;

        // Phone Field with number filtering
        JPanel phoneContainer = createPhoneFieldContainer("Telefon Numarası", "Telefon numaranızı girin", yPos);
        phoneField = (JTextField) phoneContainer.getComponent(1);
        stepOnePanel.add(phoneContainer);
        yPos += 75;

        // Email Field
        JPanel emailContainer = createFieldContainer("E-Posta", "E-posta adresinizi girin", yPos);
        emailField = (JTextField) emailContainer.getComponent(1);
        stepOnePanel.add(emailContainer);
        yPos += 75;

        // Warning label for step one
        stepOneWarningLabel = new JLabel();
        stepOneWarningLabel.setBounds(0, yPos, 380, 25);
        stepOneWarningLabel.setForeground(new Color(0xFF4747));
        stepOneWarningLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        stepOneWarningLabel.setVisible(false);
        stepOneWarningLabel.setHorizontalAlignment(SwingConstants.LEFT);
        stepOnePanel.add(stepOneWarningLabel);
        yPos += 40;

        // Continue button
        JButton continueButton = createModernButton("Devam Et", new Color(0x10B981));
        continueButton.setBounds(0, yPos, 380, 50);
        continueButton.addActionListener(e -> {
            if (validateStepOne()) {
                // Save step one data
                saveStepOneData();
                // Load step two data if exists
                loadStepTwoData();
                stepOnePanel.setVisible(false);
                stepTwoPanel.setVisible(true);
                // Show back button when on step two
                if (backButton != null) {
                    backButton.setVisible(true);
                }
            }
        });
        stepOnePanel.add(continueButton);
    }

    private void createStepTwoFields() {
        int yPos = 20;

        // Password Field
        JPanel passwordContainer = createPasswordFieldContainer("Şifre Oluşturun", "Min 8 karakter, 1 büyük, 1 küçük, 1 rakam, 1 özel", yPos);
        passwordField = (JPasswordField) passwordContainer.getComponent(1);
        stepTwoPanel.add(passwordContainer);
        yPos += 75;

        // Confirm Password Field
        JPanel confirmContainer = createPasswordFieldContainer("Şifre Onayı", "şifrenizi tekrarlayın", yPos);
        confirmPasswordField = (JPasswordField) confirmContainer.getComponent(1);
        stepTwoPanel.add(confirmContainer);
        yPos += 75;

        // Warning label - place it right after password fields
        warningLabel = new JLabel();
        warningLabel.setBounds(0, yPos, 380, 25);
        warningLabel.setForeground(new Color(0xFF4747));
        warningLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        warningLabel.setVisible(false);
        warningLabel.setHorizontalAlignment(SwingConstants.LEFT);
        stepTwoPanel.add(warningLabel);
        yPos += 30;

        // Add real-time password confirmation validation
        addPasswordValidationListeners();

        // Package Selection
        JPanel packageContainer = createPackageSelectionContainer("Paket Seçin", yPos);
        stepTwoPanel.add(packageContainer);
    }

    private JPanel createFieldContainer(String labelText, String placeholder, int yPos) {
        JPanel container = new JPanel();
        container.setLayout(null);
        container.setBounds(0, yPos, 380, 70);
        container.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        label.setForeground(new Color(0x333333));
        label.setBounds(0, 0, 380, 20);
        container.add(label);

        JTextField field = new JTextField();
        field.setBounds(0, 25, 380, 45);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5E7EB), 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        field.setBackground(new Color(0xF9FAFB));

        // Placeholder effect
        field.setText(placeholder);
        field.setForeground(new Color(0xAAAAAA));
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(new Color(0x333333));
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(new Color(0xAAAAAA));
                }
            }
        });

        container.add(field);
        return container;
    }

    private JPanel createPhoneFieldContainer(String labelText, String placeholder, int yPos) {
        JPanel container = new JPanel();
        container.setLayout(null);
        container.setBounds(0, yPos, 380, 70);
        container.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        label.setForeground(new Color(0x333333));
        label.setBounds(0, 0, 380, 20);
        container.add(label);

        JTextField field = new JTextField();
        field.setBounds(0, 25, 380, 45);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5E7EB), 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        field.setBackground(new Color(0xF9FAFB));

        // Phone field'a sadece rakam girişi ve maksimum 11 karakter sınırı
        PlainDocument phoneDoc = new PlainDocument();
        phoneDoc.setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string == null) return;

                String newStr = string.replaceAll("[^0-9]", ""); // Sadece rakamları al
                if (fb.getDocument().getLength() + newStr.length() <= 11) {
                    super.insertString(fb, offset, newStr, attr);
                    // Hide warning if input is valid
                    if (stepOneWarningLabel != null) {
                        stepOneWarningLabel.setVisible(false);
                    }
                } else {
                    // Show warning when trying to enter more than 11 digits
                    showPhoneWarning("Telefon numarası en fazla 11 hane olabilir.");
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text == null) return;

                String newStr = text.replaceAll("[^0-9]", ""); // Sadece rakamları al
                if (fb.getDocument().getLength() - length + newStr.length() <= 11) {
                    super.replace(fb, offset, length, newStr, attrs);
                    // Hide warning if input is valid
                    if (stepOneWarningLabel != null) {
                        stepOneWarningLabel.setVisible(false);
                    }
                } else {
                    // Show warning when trying to enter more than 11 digits
                    showPhoneWarning("Telefon numarası en fazla 11 hane olabilir.");
                }
            }

            @Override
            public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                super.remove(fb, offset, length);
                // Hide warning when removing characters
                if (stepOneWarningLabel != null) {
                    stepOneWarningLabel.setVisible(false);
                }
            }
        });
        field.setDocument(phoneDoc);

        // Placeholder effect
        field.setText(placeholder);
        field.setForeground(new Color(0xAAAAAA));
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(new Color(0x333333));
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(new Color(0xAAAAAA));
                }
            }
        });

        container.add(field);
        return container;
    }

    private JPanel createPasswordFieldContainer(String labelText, String placeholder, int yPos) {
        JPanel container = new JPanel();
        container.setLayout(null);
        container.setBounds(0, yPos, 380, 70);
        container.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        label.setForeground(new Color(0x333333));
        label.setBounds(0, 0, 380, 20);
        container.add(label);

        JPasswordField field = new JPasswordField();
        field.setBounds(0, 25, 335, 45);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5E7EB), 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        field.setBackground(new Color(0xF9FAFB));
        field.setEchoChar((char) 0);
        field.setText(placeholder);
        field.setForeground(new Color(0xAAAAAA));

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (new String(field.getPassword()).equals(placeholder)) {
                    field.setText("");
                    field.setForeground(new Color(0x333333));
                    field.setEchoChar('*');
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (new String(field.getPassword()).isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(new Color(0xAAAAAA));
                    field.setEchoChar((char) 0);
                }
            }
        });

        JLabel eyeIcon = new JLabel() {
            private boolean isPasswordVisible = false;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Arka plan
                g2d.setColor(new Color(243, 244, 246));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);

                // Göz ikonu
                g2d.setColor(new Color(107, 114, 128));
                g2d.setFont(new Font("SansSerif", Font.PLAIN, 16));
                String eyeText = isPasswordVisible ? "🙈" : "👁";
                g2d.drawString(eyeText, 8, 22);
            }
        };
        eyeIcon.setBounds(340, 25, 40, 45);
        eyeIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        eyeIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                boolean isVisible = field.getEchoChar() == (char) 0;
                if (isVisible) {
                    field.setEchoChar('*');  // Şifreyi gizle
                } else {
                    field.setEchoChar((char) 0);  // Şifreyi göster
                }

                // Göz ikonunu güncelle
                eyeIcon.repaint();
            }
        });


        container.add(field);
        container.add(eyeIcon);
        return container;
    }


    private JPanel createPackageSelectionContainer(String labelText, int yPos) {
        JPanel container = new JPanel();
        container.setLayout(null);
        container.setBounds(0, yPos, 380, 240); // Adjusted height: label(20) + list(200) + gap(10) + button(50) + margin(15)
        container.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        label.setForeground(new Color(0x333333));
        label.setBounds(0, 0, 380, 20);
        container.add(label);

        // Create package list similar to store
        JPanel packageListPanel = createStoreStylePackageList();
        packageListPanel.setBounds(0, 25, 380, 150); // Reduced height to leave room for button
        container.add(packageListPanel);
        
        // Sign up button after package list (fixed position)
        JButton signupButton = createModernButton("Kayıt Ol", new Color(0x3B82F6));
        signupButton.setBounds(0, 180, 380, 50);
        signupButton.addActionListener(this::handleRegister);
        container.add(signupButton);
        
        return container;
    }

    private JPanel selectedPackageItem = null;

    private JPanel createStoreStylePackageList() {
        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BorderLayout());
        mainContainer.setBackground(Color.WHITE);
        mainContainer.setBorder(BorderFactory.createLineBorder(new Color(0xE5E7EB), 1));

        // Load packages
        loadPackages();
        
        JPanel packagesContainer = new JPanel();
        packagesContainer.setLayout(new BoxLayout(packagesContainer, BoxLayout.Y_AXIS));
        packagesContainer.setBackground(Color.WHITE);

        // Create combo box for form submission (hidden)
        packageComboBox = new JComboBox<Object>();
        packageComboBox.addItem("Paket Seçin");

        if (availablePackages != null && availablePackages.length() > 0) {
            for (int i = 0; i < availablePackages.length(); i++) {
                try {
                    JSONObject pkg = availablePackages.getJSONObject(i);
                    PackageItem packageItem = new PackageItem(pkg);
                    
                    JPanel storeStyleItem = createStoreStylePackageItem(packageItem, i);
                    packagesContainer.add(storeStyleItem);
                    
                    if (i < availablePackages.length() - 1) {
                        packagesContainer.add(Box.createRigidArea(new Dimension(0, 5)));
                    }
                    
                    // Add to hidden combo box
                    packageComboBox.addItem(packageItem);
                } catch (Exception e) {
                    System.err.println("❌ Error creating package item: " + e.getMessage());
                }
            }
        } else {
            JLabel noPackagesLabel = new JLabel("Paket bulunamadı", SwingConstants.CENTER);
            noPackagesLabel.setForeground(new Color(0x666666));
            packagesContainer.add(noPackagesLabel);
        }

        JScrollPane scrollPane = new JScrollPane(packagesContainer);
        scrollPane.setBorder(null);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        mainContainer.add(scrollPane, BorderLayout.CENTER);
        return mainContainer;
    }

    private JPanel createStoreStylePackageItem(PackageItem packageItem, int index) {
        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(new BorderLayout());
        itemPanel.setBackground(Color.WHITE);
        itemPanel.setBorder(createRoundedBorder(new Color(0xDDDDDD), 1, 8));
        itemPanel.setPreferredSize(new Dimension(330, 60));
        itemPanel.setMaximumSize(new Dimension(330, 60));

        // Left side - package info
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));

        JLabel nameLabel = new JLabel(packageItem.getDisplayName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setForeground(new Color(0x333333));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftPanel.add(nameLabel);

        // Right side - expand button and price
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(null);
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setPreferredSize(new Dimension(80, 60));

        JLabel priceLabel = new JLabel(packageItem.getFormattedPrice());
        priceLabel.setFont(new Font("Arial", Font.BOLD, 12));
        priceLabel.setForeground(new Color(0x4A9EBB));
        priceLabel.setBounds(5, 8, 50, 15);
        rightPanel.add(priceLabel);

        JButton expandButton = new JButton("▼");
        expandButton.setFont(new Font("Arial", Font.BOLD, 12));
        expandButton.setForeground(new Color(0x4A9EBB));
        expandButton.setBackground(Color.WHITE);
        expandButton.setBounds(25, 30, 30, 20);
        expandButton.setBorder(BorderFactory.createLineBorder(new Color(0x4A9EBB), 1));
        expandButton.setFocusPainted(false);
        expandButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        rightPanel.add(expandButton);

        // Click handlers
        expandButton.addActionListener(e -> {
            handlePackageItemExpansion(itemPanel, packageItem, expandButton, index);
        });

        // Selection by clicking on item
        itemPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getSource() != expandButton) {
                    selectPackageItem(itemPanel, packageItem);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (selectedPackageItem != itemPanel) {
                    itemPanel.setBackground(new Color(0xF8F9FA));
                    leftPanel.setBackground(new Color(0xF8F9FA));
                    rightPanel.setBackground(new Color(0xF8F9FA));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (selectedPackageItem != itemPanel) {
                    itemPanel.setBackground(Color.WHITE);
                    leftPanel.setBackground(Color.WHITE);
                    rightPanel.setBackground(Color.WHITE);
                }
            }
        });

        itemPanel.add(leftPanel, BorderLayout.CENTER);
        itemPanel.add(rightPanel, BorderLayout.EAST);

        return itemPanel;
    }

    private void selectPackageItem(JPanel itemPanel, PackageItem packageItem) {
        // Reset previous selection
        if (selectedPackageItem != null) {
            selectedPackageItem.setBackground(Color.WHITE);
            // Reset child components background
            resetPanelBackground(selectedPackageItem, Color.WHITE);
        }

        // Set new selection
        selectedPackageItem = itemPanel;
        itemPanel.setBackground(new Color(0xE3F2FD));
        resetPanelBackground(itemPanel, new Color(0xE3F2FD));

        // Update combo box
        packageComboBox.setSelectedItem(packageItem);

        System.out.println("✅ Package selected: " + packageItem.getDisplayName());
    }

    private void resetPanelBackground(JPanel panel, Color color) {
        Component[] components = panel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                comp.setBackground(color);
            }
        }
        panel.repaint();
    }

    private void handlePackageItemExpansion(JPanel itemPanel, PackageItem packageItem, JButton expandButton, int index) {
        // Check if already expanded
        boolean isExpanded = expandButton.getText().equals("▲");
        
        if (isExpanded) {
            // Collapse
            collapsePackageItem(itemPanel, expandButton);
        } else {
            // Expand
            expandPackageItem(itemPanel, packageItem, expandButton);
        }
    }

    private void expandPackageItem(JPanel itemPanel, PackageItem packageItem, JButton expandButton) {
        // Change button
        expandButton.setText("▲");
        
        // Resize panel
        itemPanel.setPreferredSize(new Dimension(330, 160));
        itemPanel.setMaximumSize(new Dimension(330, 160));

        // Add expanded content
        JPanel expandedPanel = createExpandedContent(packageItem);
        itemPanel.add(expandedPanel, BorderLayout.SOUTH);

        // Refresh
        itemPanel.revalidate();
        itemPanel.repaint();
        itemPanel.getParent().revalidate();
        itemPanel.getParent().repaint();
    }

    private void collapsePackageItem(JPanel itemPanel, JButton expandButton) {
        // Change button
        expandButton.setText("▼");
        
        // Remove expanded content
        Component[] components = itemPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel && ((JPanel) comp).getClientProperty("expanded") != null) {
                itemPanel.remove(comp);
                break;
            }
        }

        // Resize panel back
        itemPanel.setPreferredSize(new Dimension(330, 60));
        itemPanel.setMaximumSize(new Dimension(330, 60));

        // Refresh
        itemPanel.revalidate();
        itemPanel.repaint();
        itemPanel.getParent().revalidate();
        itemPanel.getParent().repaint();
    }

    private JPanel createExpandedContent(PackageItem packageItem) {
        JPanel expandedPanel = new JPanel();
        expandedPanel.setLayout(null);
        expandedPanel.setBackground(new Color(0xF8F9FA));
        expandedPanel.setPreferredSize(new Dimension(330, 95)); // Increased height for better icon layout
        expandedPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xEEEEEE)));
        expandedPanel.putClientProperty("expanded", true);

        int yPos = 10;

        // Internet
        if (packageItem.dataAmount > 0) {
            addDetailRow(expandedPanel, "🌐", "İnternet", 
                        String.format("%d GB", packageItem.dataAmount), 10, yPos, new Color(0x4A9EBB));
            yPos += 25;
        }

        // Minutes
        if (packageItem.minutesAmount > 0) {
            addDetailRow(expandedPanel, "📞", "Konuşma", 
                        packageItem.minutesAmount + " dk", 110, 10, new Color(0x2E7D32));
        }

        // SMS
        if (packageItem.smsAmount > 0) {
            addDetailRow(expandedPanel, "💬", "SMS", 
                        packageItem.smsAmount + " adet", 210, 10, new Color(0xFF9800));
        }

        // Select button
        JButton selectButton = new JButton("Bu Paketi Seç");
        selectButton.setFont(new Font("Arial", Font.BOLD, 11));
        selectButton.setForeground(Color.WHITE);
        selectButton.setBackground(new Color(0x10B981));
        selectButton.setBounds(10, 60, 330, 25); // Moved down to accommodate better icon spacing
        selectButton.setBorder(createRoundedBorder(new Color(0x10B981), 0, 4));
        selectButton.setFocusPainted(false);
        selectButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        selectButton.addActionListener(e -> {
            selectPackageItem((JPanel) expandedPanel.getParent(), packageItem);
            collapsePackageItem((JPanel) expandedPanel.getParent(), 
                              findExpandButton((JPanel) expandedPanel.getParent()));
        });

        selectButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                selectButton.setBackground(new Color(0x357A8A));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                selectButton.setBackground(new Color(0x10B981));
            }
        });

        expandedPanel.add(selectButton);
        return expandedPanel;
    }

    private JButton findExpandButton(JPanel itemPanel) {
        Component[] components = itemPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                Component[] subComponents = ((JPanel) comp).getComponents();
                for (Component subComp : subComponents) {
                    if (subComp instanceof JButton && 
                        (((JButton) subComp).getText().equals("▼") || ((JButton) subComp).getText().equals("▲"))) {
                        return (JButton) subComp;
                    }
                }
            }
        }
        return null;
    }
    private void addDetailRow(JPanel parent, String icon, String title, String value, int x, int y, Color color) {
        JPanel rowPanel = new JPanel(null); // null layout, ama içeride hizalama yapacağız
        rowPanel.setBounds(x, y, 100, 60);  // genişliği ortalama için yeterli tut
        rowPanel.setOpaque(false);

        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        iconLabel.setBounds(0, 0, 100, 20); // tam ortada

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        titleLabel.setForeground(new Color(0x666666));
        titleLabel.setBounds(0, 20, 100, 15); // ortalanmış

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 11));
        valueLabel.setForeground(color);
        valueLabel.setBounds(0, 35, 100, 15); // ortalanmış

        rowPanel.add(iconLabel);
        rowPanel.add(titleLabel);
        rowPanel.add(valueLabel);

        parent.add(rowPanel);
    }


    private javax.swing.border.Border createRoundedBorder(Color color, int thickness, int radius) {
        return new javax.swing.border.AbstractBorder() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                if (thickness > 0) {
                    g2d.setStroke(new BasicStroke(thickness));
                    g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
                }
                g2d.dispose();
            }

            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(thickness, thickness, thickness, thickness);
            }
        };
    }


    private void loadPackages() {
        try {
            System.out.println("📦 Loading available packages...");
            availablePackages = PackageHelper.getAllPackages();
            
            if (availablePackages != null && availablePackages.length() > 0) {
                System.out.println("✅ " + availablePackages.length() + " packages loaded");
            } else {
                System.out.println("⚠️ No packages available");
                availablePackages = new JSONArray(); // Empty array as fallback
            }
        } catch (Exception e) {
            System.err.println("❌ Error loading packages: " + e.getMessage());
            e.printStackTrace();
            availablePackages = new JSONArray(); // Empty array as fallback
        }
    }



    // PackageItem class to hold package details
    private class PackageItem {
        private final JSONObject packageData;
        private final String packageName;
        private final double price;
        private final int packageId;
        private final int dataAmount;
        private final int minutesAmount;
        private final int smsAmount;
        
        public PackageItem(JSONObject pkg) {
            this.packageData = pkg;
            this.packageName = pkg.optString("packageName", "Paket");
            this.price = pkg.optDouble("price", 0);
            this.packageId = pkg.optInt("packageId", 
                           pkg.optInt("package_id", 
                           pkg.optInt("id", 0)));
            // Store sayfasındaki ile tamamen aynı format
            this.minutesAmount = pkg.optInt("amountMinutes", 0);
            this.smsAmount = pkg.optInt("amountSms", 0);
            this.dataAmount = pkg.optInt("amountData", 0) / 1024; // Store'daki gibi integer bölme
        }
        
        public String getDisplayName() {
            return packageName;
        }
        
        public double getPrice() {
            return price;
        }
        
        public int getPackageId() {
            return packageId;
        }
        
        public String getFormattedPrice() {
            return price > 0 ? String.format("%.0f TL", price) : "Fiyat Bilgisi Yok";
        }
        
        public String getDetailedDescription() {
            StringBuilder desc = new StringBuilder();
            desc.append("<html><div style='padding: 5px;'>");
            desc.append("<b>").append(packageName).append("</b><br>");
            desc.append("<span style='color: #4A9EBB; font-size: 14px; font-weight: bold;'>").append(getFormattedPrice()).append("</span><br>");
            desc.append("<div style='margin-top: 8px; font-size: 11px; color: #666;'>");
            
            if (dataAmount > 0) {
                desc.append("🌐 ").append(String.format("%d GB", dataAmount)).append(" İnternet<br>");
            }
            if (minutesAmount > 0) {
                desc.append("📞 ").append(minutesAmount).append(" dk<br>");
            }
            if (smsAmount > 0) {
                desc.append("💬 ").append(smsAmount).append(" adet<br>");
            }
            
            desc.append("</div></div></html>");
            return desc.toString();
        }
        
        @Override
        public String toString() {
            return packageName + " - " + getFormattedPrice();
        }
        
        public JSONObject getPackageData() {
            return packageData;
        }
    }


    private JButton createModernButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private boolean validateStepOne() {
        if (isPlaceholderText(firstNameField, "Enter your first name") ||
                isPlaceholderText(lastNameField, "Enter your last name") ||
                isPlaceholderText(phoneField, "Telefon numaranızı girin") ||
                isPlaceholderText(emailField, "E-posta adresinizi girin")) {
            JOptionPane.showMessageDialog(this, "Lütfen tüm alanları doldurun.");
            return false;
        }

        // E-posta doğrulama kontrolü
        String email = emailField.getText().trim();
        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Lütfen geçerli bir e-posta adresi girin. Sadece yaygın e-posta sağlayıcıları kabul edilir.");
            return false;
        }

        // Telefon numarası kontrolü
        String phone = phoneField.getText().trim();
        if (phone.length() < 10 || phone.length() > 11) {
            JOptionPane.showMessageDialog(this, "Telefon numarası 10-11 haneli olmalıdır.");
            return false;
        }

        if (!phone.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Telefon numarası sadece rakam içermelidir.");
            return false;
        }

        // 0 ile başlıyorsa veya 5 ile başlıyorsa kabul et
        if (!phone.startsWith("0") && !phone.startsWith("5")) {
            JOptionPane.showMessageDialog(this, "Telefon numarası 0 veya 5 ile başlamalıdır.");
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        email = email.toLowerCase().trim();
        
        // Temel e-posta formatı kontrolü
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            return false;
        }
        
        // Kabul edilen e-posta sağlayıcıları
        String[] allowedDomains = {
            "@gmail.com", "@hotmail.com", "@outlook.com", "@yahoo.com", 
            "@yandex.com", "@mail.ru", "@icloud.com", "@live.com",
            "@msn.com", "@aol.com", "@protonmail.com", "@tutanota.com",
            "@zoho.com", "@fastmail.com", "@gmx.com", "@web.de",
            "@yandex.ru", "@yandex.tr", "@mynet.com", "@superonline.com",
            "@turk.net", "@ttnet.net.tr", "@hotmail.com.tr"
        };
        
        for (String domain : allowedDomains) {
            if (email.endsWith(domain)) {
                return true;
            }
        }
        
        return false;
    }

    private boolean isPlaceholderText(JTextField field, String placeholder) {
        return field.getText().trim().isEmpty() || field.getText().equals(placeholder);
    }

    private void addPasswordValidationListeners() {
        javax.swing.event.DocumentListener passwordListener = new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                validatePasswordConfirmation();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                validatePasswordConfirmation();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                validatePasswordConfirmation();
            }
        };

        passwordField.getDocument().addDocumentListener(passwordListener);
        confirmPasswordField.getDocument().addDocumentListener(passwordListener);
    }

    private void showPhoneWarning(String message) {
        if (stepOneWarningLabel != null) {
            stepOneWarningLabel.setText(message);
            stepOneWarningLabel.setVisible(true);
            
            // Hide warning after 3 seconds
            Timer timer = new Timer(3000, e -> stepOneWarningLabel.setVisible(false));
            timer.setRepeats(false);
            timer.start();
        }
    }

    private void addCharacterLimit(JTextField field, int maxLength) {
        PlainDocument doc = new PlainDocument();
        doc.setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string == null) return;

                if (fb.getDocument().getLength() + string.length() <= maxLength) {
                    super.insertString(fb, offset, string, attr);
                } else {
                    // Show warning when trying to enter more than max characters
                    showNameWarning("En fazla " + maxLength + " karakter girebilirsiniz.");
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text == null) return;

                if (fb.getDocument().getLength() - length + text.length() <= maxLength) {
                    super.replace(fb, offset, length, text, attrs);
                } else {
                    // Show warning when trying to enter more than max characters
                    showNameWarning("En fazla " + maxLength + " karakter girebilirsiniz.");
                }
            }
        });
        field.setDocument(doc);
    }

    private void showNameWarning(String message) {
        if (stepOneWarningLabel != null) {
            stepOneWarningLabel.setText(message);
            stepOneWarningLabel.setVisible(true);
            
            // Hide warning after 3 seconds
            Timer timer = new Timer(3000, e -> stepOneWarningLabel.setVisible(false));
            timer.setRepeats(false);
            timer.start();
        }
    }

    private void validatePasswordConfirmation() {
        if (passwordField == null || confirmPasswordField == null || warningLabel == null) {
            return;
        }
        
        String password = new String(passwordField.getPassword()).trim();
        String confirmPassword = new String(confirmPasswordField.getPassword()).trim();
        
        System.out.println("Debug - Password: '" + password + "', Confirm: '" + confirmPassword + "'");

        // Don't validate if fields are empty or contain placeholder text
        if (password.isEmpty() || confirmPassword.isEmpty() ||
            password.equals("Min 8 karakter, 1 büyük, 1 küçük, 1 rakam, 1 özel") ||
            confirmPassword.equals("şifrenizi tekrarlayın")) {
            warningLabel.setVisible(false);
            System.out.println("Debug - Fields empty or placeholder, hiding warning");
            return;
        }

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            warningLabel.setText("Şifreler eşleşmiyor.");
            warningLabel.setVisible(true);
            System.out.println("Debug - Passwords don't match, showing warning");
        } else {
            warningLabel.setVisible(false);
            System.out.println("Debug - Passwords match, hiding warning");
        }
    }

    private void handleRegister(ActionEvent e) {
        final String pass = new String(passwordField.getPassword()).trim();
        String confirm = new String(confirmPasswordField.getPassword()).trim();

        if (pass.isEmpty() || confirm.isEmpty() ||
                pass.equals("Min 8 karakter, 1 büyük, 1 küçük, 1 rakam, 1 özel") || confirm.equals("Şifrenizi tekrar girin")) {
            warningLabel.setText("Lütfen tüm alanları doldurun.");
            warningLabel.setVisible(true);
            return;
        }

        if (!pass.equals(confirm)) {
            warningLabel.setText("Şifreler eşleşmiyor. Lütfen şifrenizi tekrar kontrol edin.");
            warningLabel.setVisible(true);
            return;
        }

        if (!isValidPassword(pass)) {
            warningLabel.setText("Şifre minimum 8 karakter, 1 büyük harf, 1 küçük harf, 1 rakam ve 1 özel karakter içermelidir.");
            warningLabel.setVisible(true);
            return;
        }

        warningLabel.setVisible(false);

        // Kullanıcı bilgilerini al ve instance variables'a ata
        final String firstName = firstNameField.getText().trim();
        final String lastName = lastNameField.getText().trim();
        final String phone = phoneField.getText().trim();
        final String email = emailField.getText().trim();
        
        // Store in instance variables for lambda access
        this.tempFirstName = firstName;
        this.tempLastName = lastName;
        this.tempPhone = phone;
        this.tempEmail = email;
        this.tempPassword = pass;

        // MSISDN formatını düzelt - 0 ile başlıyorsa kaldır
        final String msisdn = phone.startsWith("0") ? phone.substring(1) : phone;

        // MSISDN'in 5 ile başladığından emin ol
        if (!msisdn.startsWith("5")) {
            warningLabel.setText("Geçersiz telefon numarası formatı.");
            warningLabel.setVisible(true);
            return;
        }

        System.out.println("📱 Original phone: " + phone);
        System.out.println("📱 Formatted MSISDN: " + msisdn);

        // JSON oluştur ve istek gönder
        try {
            URL url = new URL("http://34.123.86.69/api/v1/auth/register");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            String jsonInputString = String.format(
                    "{\"msisdn\":\"%s\",\"password\":\"%s\",\"name\":\"%s\",\"surname\":\"%s\",\"email\":\"%s\"}",
                    msisdn, pass, firstName, lastName, email
            );
            System.out.println("📤 Gönderilen JSON:");
            System.out.println(jsonInputString);
            System.out.println("🌐 Endpoint: " + url.toString());

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            System.out.println("📥 Response Code: " + responseCode);

            if (responseCode == 200 || responseCode == 201) {
                // Başarılı yanıt - Response body'yi oku
                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                }
                System.out.println("✅ Registration Success: " + response.toString());

                // Extract customer ID from response for package assignment
                int customerId = extractCustomerIdFromResponse(response.toString());
                
                // If customer ID not found in response, try to get it by phone number
                if (customerId <= 0) {
                    System.out.println("🔍 Customer ID not found in response, trying to find by phone number...");
                    customerId = findCustomerIdByPhone(msisdn);
                }
                
                // Check if user selected a package
                if (packageComboBox.getSelectedIndex() > 0) {
                    if (customerId > 0) {
                        System.out.println("✅ Customer ID found: " + customerId + ", proceeding with package assignment");
                        assignSelectedPackageToCustomer(customerId, phone, firstName, lastName, email, pass);
                    } else {
                        System.out.println("❌ Customer ID could not be determined, skipping package assignment");
                        JOptionPane.showMessageDialog(this,
                                "✅ Registration successful!\n\n⚠️ Could not assign package automatically. Please select your package from the store after login.",
                                "Kayıt Tamamlandı",
                                JOptionPane.WARNING_MESSAGE);
                        openLoginPage();
                    }
                } else {
                    System.out.println("ℹ️ No package selected during registration");
                    
                    // Direct redirect without dialog for faster experience
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this,
                                "✅ Registration successful!\n\nYou can select your package from the store after login.",
                                "Kayıt Tamamlandı",
                                JOptionPane.INFORMATION_MESSAGE);
                        openLoginPage();
                    });
                }

            } else {
                // Hata yanıtını oku
                InputStream errorStream = conn.getErrorStream();
                StringBuilder response = new StringBuilder();
                if (errorStream != null) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(errorStream, "utf-8"))) {
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                    }
                }

                System.out.println("❌ Error Response: " + response.toString());

                // Hata mesajını kullanıcı dostu hale getir
                String errorMessage = "Kayıt başarısız";
                if (response.toString().contains("already exists") || response.toString().contains("duplicate")) {
                    errorMessage = "Telefon numarası veya e-posta zaten kayıtlı";
                } else if (response.toString().contains("invalid") || response.toString().contains("validation")) {
                    errorMessage = "Geçersiz veri girişi";
                } else if (responseCode == 400) {
                    errorMessage = "Geçersiz istek verisi";
                } else if (responseCode == 500) {
                    errorMessage = "Sunucu hatası, lütfen tekrar deneyin";
                }

                warningLabel.setText(errorMessage);
                warningLabel.setVisible(true);
            }

        } catch (java.net.ConnectException ce) {
            System.err.println("🌐 Connection Error: " + ce.getMessage());
            warningLabel.setText("Sunucuya bağlanılamıyor. Lütfen internet bağlantınızı kontrol edin.");
            warningLabel.setVisible(true);

        } catch (java.net.SocketTimeoutException ste) {
            System.err.println("⏰ Timeout Error: " + ste.getMessage());
            warningLabel.setText("İstek zaman aşımına uğradı. Lütfen tekrar deneyin.");
            warningLabel.setVisible(true);

        } catch (Exception ex) {
            System.err.println("💥 General Error: " + ex.getMessage());
            ex.printStackTrace();
            warningLabel.setText("Hata: " + ex.getMessage());
            warningLabel.setVisible(true);
        }
    }

    // Backend'den gelen şifre kurallarını kontrol eden metod

    private boolean isValidPassword(String password) {
        // En az 1 küçük, 1 büyük harf, 1 rakam, 1 özel karakter ve minimum 8 karakter uzunluğunda olmalı
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$";

        return password != null && password.matches(pattern);
    }

    private int extractCustomerIdFromResponse(String responseBody) {
        try {
            System.out.println("🔍 Extracting customer ID from response: " + responseBody);
            
            // Quick regex check first for cust_id
            if (responseBody.contains("\"cust_id\":")) {
                System.out.println("🎯 Found cust_id pattern in response, extracting...");
                java.util.regex.Pattern quickPattern = java.util.regex.Pattern.compile("\"cust_id\"\\s*:\\s*(\\d+)");
                java.util.regex.Matcher quickMatcher = quickPattern.matcher(responseBody);
                if (quickMatcher.find()) {
                    int quickId = Integer.parseInt(quickMatcher.group(1));
                    System.out.println("⚡ Quick regex extraction successful: " + quickId);
                    return quickId;
                }
            }
            
            // First try to parse as JSON object
            try {
                System.out.println("🔍 Attempting to parse JSON object from: " + responseBody);
                JSONObject responseJson = new JSONObject(responseBody);
                System.out.println("✅ JSON parsing successful");
                
                // Try different possible field names for customer ID
                System.out.println("🔍 Looking for cust_id field in JSON...");
                int customerId = responseJson.optInt("cust_id", 0);
                System.out.println("🔍 cust_id value: " + customerId);
                
                if (customerId == 0) {
                    customerId = responseJson.optInt("customerId", 
                                   responseJson.optInt("customer_id", 
                                   responseJson.optInt("id", 0)));
                    System.out.println("🔍 Alternative ID search result: " + customerId);
                }
                
                if (customerId > 0) {
                    System.out.println("✅ Customer ID found in JSON object: " + customerId);
                    return customerId;
                }
                
                // If not found, try nested objects
                if (responseJson.has("data")) {
                    JSONObject data = responseJson.getJSONObject("data");
                    customerId = data.optInt("cust_id", 
                               data.optInt("customerId", 
                               data.optInt("customer_id", 
                               data.optInt("id", 0))));
                    
                    if (customerId > 0) {
                        System.out.println("✅ Customer ID found in data object: " + customerId);
                        return customerId;
                    }
                }
                
                if (responseJson.has("user")) {
                    JSONObject user = responseJson.getJSONObject("user");
                    customerId = user.optInt("cust_id", 
                               user.optInt("customerId", 
                               user.optInt("customer_id", 
                               user.optInt("id", 0))));
                    
                    if (customerId > 0) {
                        System.out.println("✅ Customer ID found in user object: " + customerId);
                        return customerId;
                    }
                }
                
            } catch (Exception jsonException) {
                System.out.println("⚠️ JSON object parsing failed: " + jsonException.getMessage());
                System.out.println("⚠️ Response is not JSON object, trying as array...");
                
                // Try parsing as JSON array (some APIs return array)
                try {
                    JSONArray responseArray = new JSONArray(responseBody);
                    if (responseArray.length() > 0) {
                        JSONObject firstItem = responseArray.getJSONObject(0);
                        int customerId = firstItem.optInt("cust_id", 
                                       firstItem.optInt("customerId", 
                                       firstItem.optInt("customer_id", 
                                       firstItem.optInt("id", 0))));
                        
                        if (customerId > 0) {
                            System.out.println("✅ Customer ID found in array response: " + customerId);
                            return customerId;
                        }
                    }
                } catch (Exception arrayException) {
                    System.out.println("⚠️ Response is not JSON array either");
                }
            }
            
            // If JSON parsing failed, try to extract ID using regex
            System.out.println("🔍 Trying regex extraction from response...");
            String[] patterns = {
                "\"cust_id\"\\s*:\\s*(\\d+)",
                "\"customerId\"\\s*:\\s*(\\d+)",
                "\"customer_id\"\\s*:\\s*(\\d+)",
                "\"id\"\\s*:\\s*(\\d+)",
                "cust_id=(\\d+)",
                "customerId=(\\d+)",
                "customer_id=(\\d+)",
                "id=(\\d+)"
            };
            
            for (String pattern : patterns) {
                java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
                java.util.regex.Matcher m = p.matcher(responseBody);
                if (m.find()) {
                    int customerId = Integer.parseInt(m.group(1));
                    System.out.println("✅ Customer ID found with regex: " + customerId);
                    return customerId;
                }
            }
            
            System.out.println("❌ No customer ID found in response");
            return 0;
            
        } catch (Exception e) {
            System.err.println("❌ Error extracting customer ID: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    private void assignSelectedPackageToCustomer(int customerId, String userPhone, String firstName, String lastName, String email, String password) {
        try {
            System.out.println("🎯 Starting package assignment process...");
            System.out.println("👤 Customer ID: " + customerId);
            
            int selectedIndex = packageComboBox.getSelectedIndex();
            System.out.println("📋 Selected combo box index: " + selectedIndex);
            System.out.println("📦 Available packages count: " + (availablePackages != null ? availablePackages.length() : "null"));
            
            Object selectedItem = packageComboBox.getSelectedItem();
            
            if (selectedIndex <= 0 || !(selectedItem instanceof PackageItem)) {
                System.out.println("⚠️ No valid package selected - selectedIndex: " + selectedIndex);
                openLoginPageWithMessage("Kayıt başarılı! Lütfen mağazadan paket seçin.");
                return;
            }
            
            PackageItem selectedPackageItem = (PackageItem) selectedItem;
            JSONObject selectedPackage = selectedPackageItem.getPackageData();
            System.out.println("📄 Selected package JSON: " + selectedPackage.toString(2));
            
            int packageId = selectedPackageItem.getPackageId();
            
            System.out.println("🆔 Extracted package ID: " + packageId);
            
            if (packageId <= 0) {
                System.out.println("❌ Invalid package ID - package: " + selectedPackage.toString());
                openLoginPageWithMessage("Kayıt başarılı! Lütfen mağazadan paket seçin.");
                return;
            }
            
            String packageName = selectedPackageItem.getDisplayName();
            
            System.out.println("📦 Assigning package " + packageId + " (" + packageName + ") to customer " + customerId);
            
            // Show loading message
            warningLabel.setText("Adding package to your account...");
            warningLabel.setForeground(new Color(0x4A9EBB));
            warningLabel.setVisible(true);
            
            // Assign package in background thread to avoid blocking UI
            Thread packageAssignThread = new Thread(() -> {
                System.out.println("🎯 Starting PackageHelper.addPackageToCustomer with customerId=" + customerId + ", packageId=" + packageId);
                PackageHelper.PackageResponse result = PackageHelper.addPackageToCustomer(customerId, packageId);
                System.out.println("📤 PackageHelper.addPackageToCustomer result: Success=" + result.isSuccess() + ", Message=" + result.getMessage());
                
                // Wait to ensure the assignment is processed on backend
                try {
                    Thread.sleep(4000); // Increased to 4 seconds for better reliability
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                SwingUtilities.invokeLater(() -> {
                    warningLabel.setVisible(false);
                    
                    if (result.isSuccess()) {
                        System.out.println("✅ Package assigned successfully");
                        System.out.println("🔍 Package assignment details - CustomerID: " + customerId + ", PackageID: " + packageId);
                        
                        // Skip verification for faster user experience - just redirect to login with success message
                        JOptionPane.showMessageDialog(this,
                                "✅ Kayıt başarılı!\n\n📦 Seçtiğiniz paket hesabınıza eklendi.\n\nLütfen giriş yapın ve paket verilerinizin yüklenmesi için birkaç saniye bekleyin.",
                                "Kayıt Tamamlandı",
                                JOptionPane.INFORMATION_MESSAGE);
                        
                        System.out.println("📝 Redirecting to login page...");
                        openLoginPage();
                        
                    } else {
                        System.out.println("⚠️ Package assignment failed: " + result.getMessage());
                        JOptionPane.showMessageDialog(this,
                                "✅ Kayıt başarılı!\n\n⚠️ Paket ataması başarısız: " + result.getMessage() + 
                                "\n\nGiriş yaptıktan sonra mağazadan paket seçebilirsiniz.",
                                "Kayıt Tamamlandı",
                                JOptionPane.WARNING_MESSAGE);
                        openLoginPage();
                    }
                });
            });
            
            packageAssignThread.setName("PackageAssignThread");
            packageAssignThread.start();
            
        } catch (Exception e) {
            System.err.println("❌ Error assigning package: " + e.getMessage());
            e.printStackTrace();
            warningLabel.setVisible(false);
            openLoginPageWithMessage("Registration successful! Please select a package from the store.");
        }
    }

    private void verifyPackageAssignmentMultiple(int customerId, int packageId, Runnable onSuccess) {
        verifyPackageAssignmentWithRetry(customerId, packageId, onSuccess, 0, 2); // Reduced from 3 to 2 attempts for faster completion
    }
    
    private void verifyPackageAssignmentWithRetry(int customerId, int packageId, Runnable onSuccess, int attempt, int maxAttempts) {
        Thread verifyThread = new Thread(() -> {
            try {
                System.out.println("🔍 Verifying package assignment attempt " + (attempt + 1) + "/" + maxAttempts + " for customer " + customerId);
                
                // Check customer packages to verify assignment
                JSONArray customerPackages = PackageHelper.getCustomerPackages(customerId);
                boolean packageFound = false;
                
                if (customerPackages != null && customerPackages.length() > 0) {
                    for (int i = 0; i < customerPackages.length(); i++) {
                        JSONObject pkg = customerPackages.getJSONObject(i);
                        int pkgId = pkg.optInt("packageId", 
                                   pkg.optInt("package_id", 
                                   pkg.optInt("id", 0)));
                        
                        if (pkgId == packageId) {
                            packageFound = true;
                            System.out.println("✅ Package verification successful - Package " + packageId + " found in customer packages");
                            break;
                        }
                    }
                }
                
                if (packageFound) {
                    SwingUtilities.invokeLater(onSuccess);
                } else if (attempt < maxAttempts - 1) {
                    System.out.println("⚠️ Package not found yet, retrying in 2 seconds... (attempt " + (attempt + 1) + "/" + maxAttempts + ")");
                    Thread.sleep(2000); // Reduced retry wait time from 5 to 2 seconds
                    verifyPackageAssignmentWithRetry(customerId, packageId, onSuccess, attempt + 1, maxAttempts);
                } else {
                    System.out.println("❌ Package verification failed after " + maxAttempts + " attempts");
                    SwingUtilities.invokeLater(() -> {
                        // Show brief success message and auto-redirect
                        JLabel successMessage = new JLabel("<html><div style='text-align: center;'>" +
                                "✅ Registration successful!<br><br>" +
                                "⚠️ Package is being processed and will appear shortly.<br><br>" +
                                "📝 Redirecting to login page...</div></html>");
                        successMessage.setFont(new Font("Arial", Font.PLAIN, 14));
                        successMessage.setHorizontalAlignment(SwingConstants.CENTER);
                        
                        JDialog successDialog = new JDialog(this, "Kayıt Tamamlandı", true);
                        successDialog.setLayout(new BorderLayout());
                        successDialog.add(successMessage, BorderLayout.CENTER);
                        successDialog.setSize(400, 200);
                        successDialog.setLocationRelativeTo(this);
                        successDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                        
                        // Auto-close after 2 seconds and redirect to login
                        Timer redirectTimer = new Timer(1500, event -> { // Faster redirect - 1.5 seconds
                            successDialog.dispose();
                            openLoginPage();
                        });
                        redirectTimer.setRepeats(false);
                        redirectTimer.start();
                        
                        successDialog.setVisible(true);
                    });
                }
                
            } catch (Exception e) {
                System.err.println("❌ Error verifying package assignment: " + e.getMessage());
                e.printStackTrace();
                if (attempt < maxAttempts - 1) {
                    try {
                        Thread.sleep(2000); // Reduced from 5 to 2 seconds
                        verifyPackageAssignmentWithRetry(customerId, packageId, onSuccess, attempt + 1, maxAttempts);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    SwingUtilities.invokeLater(() -> {
                        // Show brief success message and auto-redirect
                        JLabel successMessage = new JLabel("<html><div style='text-align: center;'>" +
                                "✅ Registration successful!<br><br>" +
                                "⚠️ Package assignment may take a few moments to appear.<br><br>" +
                                "📝 Redirecting to login page...</div></html>");
                        successMessage.setFont(new Font("Arial", Font.PLAIN, 14));
                        successMessage.setHorizontalAlignment(SwingConstants.CENTER);
                        
                        JDialog successDialog = new JDialog(this, "Kayıt Tamamlandı", true);
                        successDialog.setLayout(new BorderLayout());
                        successDialog.add(successMessage, BorderLayout.CENTER);
                        successDialog.setSize(400, 200);
                        successDialog.setLocationRelativeTo(this);
                        successDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                        
                        // Auto-close after 2 seconds and redirect to login
                        Timer redirectTimer = new Timer(1500, event -> { // Faster redirect - 1.5 seconds
                            successDialog.dispose();
                            openLoginPage();
                        });
                        redirectTimer.setRepeats(false);
                        redirectTimer.start();
                        
                        successDialog.setVisible(true);
                    });
                }
            }
        });
        
        verifyThread.setName("PackageVerifyThread-" + attempt);
        verifyThread.start();
    }

    private void verifyPackageAssignment(int customerId, int packageId, Runnable onSuccess) {
        Thread verifyThread = new Thread(() -> {
            try {
                System.out.println("🔍 Verifying package assignment for customer " + customerId);
                
                // Check customer packages to verify assignment
                JSONArray customerPackages = PackageHelper.getCustomerPackages(customerId);
                boolean packageFound = false;
                
                if (customerPackages != null && customerPackages.length() > 0) {
                    for (int i = 0; i < customerPackages.length(); i++) {
                        JSONObject pkg = customerPackages.getJSONObject(i);
                        int pkgId = pkg.optInt("packageId", 
                                   pkg.optInt("package_id", 
                                   pkg.optInt("id", 0)));
                        
                        if (pkgId == packageId) {
                            packageFound = true;
                            System.out.println("✅ Package verification successful - Package " + packageId + " found in customer packages");
                            break;
                        }
                    }
                }
                
                if (packageFound) {
                    SwingUtilities.invokeLater(onSuccess);
                } else {
                    System.out.println("⚠️ Package verification failed - Package " + packageId + " not found in customer packages");
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this,
                                "✅ Kayıt başarılı!\n\n⚠️ Paket ataması birkaç dakika sürebilir.\n\nLütfen panele erişmek için giriş yapın.",
                                "Kayıt Tamamlandı",
                                JOptionPane.INFORMATION_MESSAGE);
                        
                        // Redirect to login page
                        openLoginPage();
                    });
                }
                
            } catch (Exception e) {
                System.err.println("❌ Error verifying package assignment: " + e.getMessage());
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    // Show brief success message and auto-redirect
                    JLabel successMessage = new JLabel("<html><div style='text-align: center;'>" +
                            "✅ Registration successful!<br><br>" +
                            "⚠️ Package assignment may take a few moments to appear.<br><br>" +
                            "📝 Redirecting to login page...</div></html>");
                    successMessage.setFont(new Font("Arial", Font.PLAIN, 14));
                    successMessage.setHorizontalAlignment(SwingConstants.CENTER);
                    
                    JDialog successDialog = new JDialog(this, "Kayıt Tamamlandı", true);
                    successDialog.setLayout(new BorderLayout());
                    successDialog.add(successMessage, BorderLayout.CENTER);
                    successDialog.setSize(400, 200);
                    successDialog.setLocationRelativeTo(this);
                    successDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                    
                    // Auto-close after 2 seconds and redirect to login
                    Timer redirectTimer = new Timer(1500, event -> { // Faster redirect - 1.5 seconds
                        successDialog.dispose();
                        openLoginPage();
                    });
                    redirectTimer.setRepeats(false);
                    redirectTimer.start();
                    
                    successDialog.setVisible(true);
                });
            }
        });
        
        verifyThread.setName("PackageVerifyThread");
        verifyThread.start();
    }

    private void openLoginPageWithMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Kayıt Tamamlandı", JOptionPane.INFORMATION_MESSAGE);
        openLoginPage();
    }

    private int findCustomerIdByPhone(String msisdn) {
        try {
            System.out.println("🔍 Searching customer ID for phone: " + msisdn);
            
            // Try to get balance data which usually contains customer ID
            JSONObject balanceData = com.cellenta.BalanceHelper.getBalanceJSON(msisdn);
            if (balanceData != null) {
                int customerId = balanceData.optInt("customerId", 
                               balanceData.optInt("customer_id", 
                               balanceData.optInt("id", 0)));
                
                if (customerId > 0) {
                    System.out.println("✅ Customer ID found from balance API: " + customerId);
                    return customerId;
                }
            }
            
            // Try a simple login request to get customer info
            try {
                URL url = new URL("http://34.123.86.69/api/v1/auth/login");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                // Use the same password that was just registered
                String password = new String(passwordField.getPassword()).trim();
                String loginJson = String.format("{\"msisdn\":\"%s\",\"password\":\"%s\"}", msisdn, password);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(loginJson.getBytes("utf-8"));
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    StringBuilder response = new StringBuilder();
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                    }
                    
                    System.out.println("🔍 Login response for customer ID: " + response.toString());
                    int customerId = extractCustomerIdFromResponse(response.toString());
                    if (customerId > 0) {
                        System.out.println("✅ Customer ID found from login API: " + customerId);
                        return customerId;
                    }
                }
            } catch (Exception e) {
                System.err.println("⚠️ Could not get customer ID via login: " + e.getMessage());
            }
            
            System.out.println("❌ Could not find customer ID by phone number");
            return 0;
            
        } catch (Exception e) {
            System.err.println("❌ Error finding customer ID by phone: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    private void openLoginPage() {
        switchToLogin();
    }

    private void switchToLogin() {
        // Close current window and open new login window
        dispose();
        SwingUtilities.invokeLater(() -> {
            new LoginController().setVisible(true);
        });
    }

    /**
     * Save step one data to temporary variables
     */
    private void saveStepOneData() {
        tempFirstName = firstNameField.getText().trim();
        tempLastName = lastNameField.getText().trim();
        tempPhone = phoneField.getText().trim();
        tempEmail = emailField.getText().trim();
        System.out.println("📝 Step one data saved: " + tempFirstName + " " + tempLastName);
    }

    /**
     * Save step two data to temporary variables
     */
    private void saveStepTwoData() {
        tempPassword = new String(passwordField.getPassword());
        System.out.println("📝 Step two data saved (password length: " + tempPassword.length() + ")");
    }

    /**
     * Load step two data from temporary variables
     */
    private void loadStepTwoData() {
        if (tempPassword != null && !tempPassword.isEmpty()) {
            passwordField.setText(tempPassword);
            confirmPasswordField.setText(tempPassword);
            System.out.println("📋 Step two data loaded");
        }
    }

    /**
     * Load step one data from temporary variables (when going back)
     */
    private void updateLayout() {
        int frameWidth = getWidth();
        int frameHeight = getHeight();

        // Minimum boyutlar - LoginController gibi
        int minLeftWidth = 380;
        int minRightWidth = 505;
        int totalMinWidth = minLeftWidth + minRightWidth;
        int formWidth = 450;

        if (frameWidth <= totalMinWidth) {
            // Küçük ekranlarda sabit boyutlar
            leftPanel.setBounds(0, 0, minLeftWidth, frameHeight);
            
            // Form paneli sağ panelin ortasında
            int formX = minLeftWidth + (minRightWidth - formWidth) / 2;
            formContainer.setBounds(formX, 60, formWidth, 620);
        } else {
            // Büyük ekranlarda responsive
            int extraSpace = frameWidth - totalMinWidth;
            int leftWidth = minLeftWidth + (extraSpace / 2);
            int rightWidth = minRightWidth + (extraSpace / 2);

            // Panellerin ortada buluşması için
            leftPanel.setBounds(0, 0, leftWidth, frameHeight);
            
            // Form paneli sağ panelin ortasında
            int formX = leftWidth + (rightWidth - formWidth) / 2;
            formContainer.setBounds(formX, 60, formWidth, 620);
        }

        revalidate();
        repaint();
    }

    private void loadStepOneData() {
        if (tempFirstName != null) firstNameField.setText(tempFirstName);
        if (tempLastName != null) lastNameField.setText(tempLastName);
        if (tempPhone != null) phoneField.setText(tempPhone);
        if (tempEmail != null) emailField.setText(tempEmail);
        System.out.println("📋 Step one data loaded: " + tempFirstName + " " + tempLastName);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new RegisterController().setVisible(true);
        });
    }
}