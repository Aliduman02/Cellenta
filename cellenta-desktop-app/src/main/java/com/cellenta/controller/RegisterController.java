package com.cellenta.controller;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.net.HttpURLConnection;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.URL;

public class RegisterController extends JFrame {
    private JPanel stepOnePanel, stepTwoPanel;
    private JTextField firstNameField, lastNameField, phoneField, emailField;
    private JPasswordField passwordField, confirmPasswordField;
    private JLabel warningLabel;
    private BufferedImage backgroundImage;
    private BufferedImage titleImage;
    private BufferedImage iconImage;

    public RegisterController() {
        setTitle("Register");
        setSize(1400, 800);
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
        backgroundPanel.setBounds(0, 0, 1400, 800);
        backgroundPanel.setLayout(null);
        setContentPane(backgroundPanel);

        // Sol taraf - Logo ve slogan
        createLeftSection(backgroundPanel);

        // Sağ taraf - Form
        createRightSection(backgroundPanel);
    }

    private void createLeftSection(JPanel parent) {
        // Sol Panel (Şeffaf arka plan)
        JPanel leftPanel = new JPanel() {
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
        leftPanel.setBounds(0, 0, 700, 800);
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
        logoPanel.setBounds(180, 200, 350, 80);
        logoPanel.setOpaque(false);
        leftPanel.add(logoPanel);

        // Slogan
        JLabel slogan = new JLabel("<html><div style='font-size:20px; font-weight:300; margin-bottom:15px;'>Building the Future...</div><br>"
                + "<div style='font-size:12px; line-height:1.4; opacity:0.9;'>Lorem ipsum dolor sit amet, consectetur<br>adipiscing elit, sed do eiusmod tempor<br>incididunt ut labore et dolore magna aliqua.</div></html>");
        slogan.setForeground(Color.WHITE);
        slogan.setBounds(180, 320, 350, 140);
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
        progressPanel.setBounds(180, 500, 150, 10);
        progressPanel.setOpaque(false);
        leftPanel.add(progressPanel);
    }

    private void createRightSection(JPanel parent) {
        // Form container with rounded corners
        JPanel formContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            }
        };
        formContainer.setBounds(800, 100, 450, 600);
        formContainer.setLayout(null);
        formContainer.setOpaque(false);
        parent.add(formContainer);

        // Başlık
        JLabel title = new JLabel("LET'S GET YOU STARTED", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.PLAIN, 12));
        title.setForeground(new Color(0x999999));
        title.setBounds(0, 30, 450, 20);
        formContainer.add(title);

        // Alt başlık
        JLabel subtitle = new JLabel("Create an Account", SwingConstants.CENTER);
        subtitle.setFont(new Font("Arial", Font.BOLD, 28));
        subtitle.setForeground(new Color(0x333333));
        subtitle.setBounds(0, 60, 450, 40);
        formContainer.add(subtitle);

        // Step One Panel
        stepOnePanel = new JPanel();
        stepOnePanel.setLayout(null);
        stepOnePanel.setBounds(50, 120, 350, 400);
        stepOnePanel.setOpaque(false);
        formContainer.add(stepOnePanel);

        createStepOneFields();

        // Step Two Panel
        stepTwoPanel = new JPanel();
        stepTwoPanel.setLayout(null);
        stepTwoPanel.setBounds(50, 120, 350, 400);
        stepTwoPanel.setOpaque(false);
        stepTwoPanel.setVisible(false);
        formContainer.add(stepTwoPanel);

        createStepTwoFields();

        // Login link
        JLabel loginLink = new JLabel("<html>Already have an account? <font color='#4A9EBB'><u>Log in</u></font></html>", SwingConstants.CENTER);
        loginLink.setFont(new Font("Arial", Font.PLAIN, 14));
        loginLink.setForeground(new Color(0x666666));
        loginLink.setBounds(0, 560, 450, 20);
        loginLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginLink.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                openLoginPage();
            }
        });
        formContainer.add(loginLink);
    }

    private void createStepOneFields() {
        int yPos = 0;

        // First Name Field
        JPanel firstNameContainer = createFieldContainer("First Name", "Enter your first name", yPos);
        firstNameField = (JTextField) firstNameContainer.getComponent(1);
        stepOnePanel.add(firstNameContainer);
        yPos += 75;

        // Last Name Field
        JPanel lastNameContainer = createFieldContainer("Last Name", "Enter your last name", yPos);
        lastNameField = (JTextField) lastNameContainer.getComponent(1);
        stepOnePanel.add(lastNameContainer);
        yPos += 75;

        // Phone Field with number filtering
        JPanel phoneContainer = createPhoneFieldContainer("Phone Number", "Enter your phone number", yPos);
        phoneField = (JTextField) phoneContainer.getComponent(1);
        stepOnePanel.add(phoneContainer);
        yPos += 75;

        // Email Field
        JPanel emailContainer = createFieldContainer("E-Mail", "Enter your email", yPos);
        emailField = (JTextField) emailContainer.getComponent(1);
        stepOnePanel.add(emailContainer);
        yPos += 85;

        // Continue button
        JButton continueButton = createModernButton("Continue", new Color(0x7DD3C0));
        continueButton.setBounds(0, yPos, 350, 50);
        continueButton.addActionListener(e -> {
            if (validateStepOne()) {
                stepOnePanel.setVisible(false);
                stepTwoPanel.setVisible(true);
            }
        });
        stepOnePanel.add(continueButton);
    }

    private void createStepTwoFields() {
        int yPos = 20;

        // Password Field
        JPanel passwordContainer = createPasswordFieldContainer("Create a password", "Min 8 chars, 1 upper, 1 lower, 1 number, 1 special", yPos);
        passwordField = (JPasswordField) passwordContainer.getComponent(1);
        stepTwoPanel.add(passwordContainer);
        yPos += 75;

        // Confirm Password Field
        JPanel confirmContainer = createPasswordFieldContainer("Confirm password", "repeat password", yPos);
        confirmPasswordField = (JPasswordField) confirmContainer.getComponent(1);
        stepTwoPanel.add(confirmContainer);
        yPos += 75;

        // Sign up button
        JButton signupButton = createModernButton("Sign up", new Color(0x4A9EBB));
        signupButton.setBounds(0, yPos, 350, 50);
        signupButton.addActionListener(this::handleRegister);
        stepTwoPanel.add(signupButton);

        // Warning label
        warningLabel = new JLabel();
        warningLabel.setBounds(0, yPos + 60, 350, 25);
        warningLabel.setForeground(new Color(0xFF4747));
        warningLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        warningLabel.setVisible(false);
        warningLabel.setHorizontalAlignment(SwingConstants.CENTER);
        stepTwoPanel.add(warningLabel);
    }

    private JPanel createFieldContainer(String labelText, String placeholder, int yPos) {
        JPanel container = new JPanel();
        container.setLayout(null);
        container.setBounds(0, yPos, 350, 70);
        container.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        label.setForeground(new Color(0x333333));
        label.setBounds(0, 0, 350, 20);
        container.add(label);

        JTextField field = new JTextField();
        field.setBounds(0, 25, 350, 45);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDDDDDD), 1),
                BorderFactory.createEmptyBorder(0, 15, 0, 15)
        ));
        field.setBackground(Color.WHITE);

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
        container.setBounds(0, yPos, 350, 70);
        container.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        label.setForeground(new Color(0x333333));
        label.setBounds(0, 0, 350, 20);
        container.add(label);

        JTextField field = new JTextField();
        field.setBounds(0, 25, 350, 45);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDDDDDD), 1),
                BorderFactory.createEmptyBorder(0, 15, 0, 15)
        ));
        field.setBackground(Color.WHITE);

        // Phone field'a sadece rakam girişi ve maksimum 11 karakter sınırı
        PlainDocument phoneDoc = new PlainDocument();
        phoneDoc.setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string == null) return;

                String newStr = string.replaceAll("[^0-9]", ""); // Sadece rakamları al
                if (fb.getDocument().getLength() + newStr.length() <= 11) {
                    super.insertString(fb, offset, newStr, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text == null) return;

                String newStr = text.replaceAll("[^0-9]", ""); // Sadece rakamları al
                if (fb.getDocument().getLength() - length + newStr.length() <= 11) {
                    super.replace(fb, offset, length, newStr, attrs);
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
        container.setBounds(0, yPos, 350, 70);
        container.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        label.setForeground(new Color(0x333333));
        label.setBounds(0, 0, 350, 20);
        container.add(label);

        JPasswordField field = new JPasswordField();
        field.setBounds(0, 25, 305, 45);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDDDDDD), 1),
                BorderFactory.createEmptyBorder(0, 15, 0, 15)
        ));
        field.setBackground(Color.WHITE);
        field.setEchoChar((char) 0);
        field.setText(placeholder);
        field.setForeground(new Color(0xAAAAAA));

        // Eye icon button
        JButton eyeButton = new JButton("👁");
        eyeButton.setBounds(310, 25, 40, 45);
        eyeButton.setBorder(BorderFactory.createLineBorder(new Color(0xDDDDDD), 1));
        eyeButton.setBackground(Color.WHITE);
        eyeButton.setFocusPainted(false);
        eyeButton.setFont(new Font("Arial", Font.PLAIN, 16));
        eyeButton.addActionListener(e -> {
            if (field.getEchoChar() == 0) {
                field.setEchoChar('*');
            } else {
                field.setEchoChar((char) 0);
            }
        });

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

        container.add(field);
        container.add(eyeButton);
        return container;
    }

    private JButton createModernButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
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
                isPlaceholderText(phoneField, "Enter your phone number") ||
                isPlaceholderText(emailField, "Enter your email")) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return false;
        }

        // Telefon numarası kontrolü
        String phone = phoneField.getText().trim();
        if (phone.length() < 10 || phone.length() > 11) {
            JOptionPane.showMessageDialog(this, "Phone number must be 10-11 digits.");
            return false;
        }

        if (!phone.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Phone number must contain only numbers.");
            return false;
        }

        // 0 ile başlıyorsa veya 5 ile başlıyorsa kabul et
        if (!phone.startsWith("0") && !phone.startsWith("5")) {
            JOptionPane.showMessageDialog(this, "Phone number must start with 0 or 5.");
            return false;
        }

        return true;
    }

    private boolean isPlaceholderText(JTextField field, String placeholder) {
        return field.getText().trim().isEmpty() || field.getText().equals(placeholder);
    }

    private void handleRegister(ActionEvent e) {
        String pass = new String(passwordField.getPassword()).trim();
        String confirm = new String(confirmPasswordField.getPassword()).trim();

        if (pass.isEmpty() || confirm.isEmpty() ||
                pass.equals("Min 8 chars, 1 upper, 1 lower, 1 number") || confirm.equals("repeat password")) {
            warningLabel.setText("Please fill in all fields.");
            warningLabel.setVisible(true);
            return;
        }

        if (!pass.equals(confirm)) {
            warningLabel.setText("Passwords do not match.");
            warningLabel.setVisible(true);
            return;
        }
        if (!isValidPassword(pass)) {
            warningLabel.setText("Password must contain: 1 uppercase, 1 lowercase, 1 number, 1 special character (min 8 chars)");
            warningLabel.setVisible(true);
            return;
        }
        if (pass.length() < 8) {
            warningLabel.setText("Password must be at least 8 characters.");
            warningLabel.setVisible(true);
            return;
        }

        // Backend'den gelen şifre kuralları kontrolü
        if (!isValidPassword(pass)) {
            warningLabel.setText("Password must contain: 1 uppercase, 1 lowercase, 1 number (min 8 chars)");
            warningLabel.setVisible(true);
            return;
        }

        warningLabel.setVisible(false);

        // Kullanıcı bilgilerini al
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();

        // MSISDN formatını düzelt - 0 ile başlıyorsa kaldır
        String msisdn = phone;
        if (phone.startsWith("0")) {
            msisdn = phone.substring(1); // 0'ı kaldır
        }

        // MSISDN'in 5 ile başladığından emin ol
        if (!msisdn.startsWith("5")) {
            warningLabel.setText("Invalid phone number format.");
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

                JOptionPane.showMessageDialog(this,
                        "✅ Registration successful!\n\nYou can now login and select your package from the store.",
                        "Registration Complete",
                        JOptionPane.INFORMATION_MESSAGE);
                openLoginPage();

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
                String errorMessage = "Registration failed";
                if (response.toString().contains("already exists") || response.toString().contains("duplicate")) {
                    errorMessage = "Phone number or email already registered";
                } else if (response.toString().contains("invalid") || response.toString().contains("validation")) {
                    errorMessage = "Invalid input data";
                } else if (responseCode == 400) {
                    errorMessage = "Invalid request data";
                } else if (responseCode == 500) {
                    errorMessage = "Server error, please try again";
                }

                warningLabel.setText(errorMessage);
                warningLabel.setVisible(true);
            }

        } catch (java.net.ConnectException ce) {
            System.err.println("🌐 Connection Error: " + ce.getMessage());
            warningLabel.setText("Cannot connect to server. Please check your internet connection.");
            warningLabel.setVisible(true);

        } catch (java.net.SocketTimeoutException ste) {
            System.err.println("⏰ Timeout Error: " + ste.getMessage());
            warningLabel.setText("Request timed out. Please try again.");
            warningLabel.setVisible(true);

        } catch (Exception ex) {
            System.err.println("💥 General Error: " + ex.getMessage());
            ex.printStackTrace();
            warningLabel.setText("Error: " + ex.getMessage());
            warningLabel.setVisible(true);
        }
    }

    // Backend'den gelen şifre kurallarını kontrol eden metod

    private boolean isValidPassword(String password) {
        // En az 1 küçük, 1 büyük harf, 1 rakam, 1 özel karakter ve minimum 8 karakter uzunluğunda olmalı
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$";

        return password != null && password.matches(pattern);
    }

    private void openLoginPage() {
        // LoginController sınıfınızın tam adını kullanın
        try {
            // LoginController'ı çağır
            new LoginController().setVisible(true);
            this.dispose(); // Mevcut register penceresini kapat
        } catch (Exception ex) {
            // Eğer LoginController sınıfı bulunamazsa veya hata olursa
            System.err.println("LoginController bulunamadı: " + ex.getMessage());
            // Alternatif olarak bir dialog gösterebilirsiniz
            JOptionPane.showMessageDialog(this, "Login sayfasına yönlendirilemiyor. Lütfen uygulamayı yeniden başlatın.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new RegisterController().setVisible(true);
        });
    }
}