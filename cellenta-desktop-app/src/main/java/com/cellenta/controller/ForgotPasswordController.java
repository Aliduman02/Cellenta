package com.cellenta.controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

// Backend bağlantısı için ekstra import'lar
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ForgotPasswordController extends JFrame {
    private JTextField emailField;
    private JLabel warningLabel;
    private JPanel backgroundPanel;
    private Timer resendTimer;
    private int timeLeft = 180;
    private JButton resendButton;
    private BufferedImage backgroundImage;
    private BufferedImage titleImage;
    private BufferedImage logoImage;
    private String userEmail;

    // Email verification components
    private JTextField[] codeFields;
    private JPanel verificationPanel;
    private JPanel emailPanel;
    private JPanel mainPanel;

    // Backend bağlantısı için
    private String sentVerificationCode;

    public ForgotPasswordController() {
        setTitle("Forgot Password - Cellenta");
        setSize(885, 716);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Load background image
        try {
            backgroundImage = ImageIO.read(getClass().getResourceAsStream("/images/background_mavi.jpg"));
            if (backgroundImage == null) {
                System.out.println("Background image not found at /images/background_mavi.jpg");
            }
        } catch (Exception e) {
            System.out.println("Error loading background image: " + e.getMessage());
            backgroundImage = null;
        }

        // Load title image
        try {
            titleImage = ImageIO.read(getClass().getResourceAsStream("/images/title-white.png"));
            if (titleImage == null) {
                System.out.println("Title image not found at /images/title-white.png");
            }
        } catch (Exception e) {
            System.out.println("Error loading title image: " + e.getMessage());
            titleImage = null;
        }

        // Load logo image
        try {
            logoImage = ImageIO.read(getClass().getResourceAsStream("/images/icon-white.png"));
            if (logoImage == null) {
                System.out.println("Logo image not found at /images/icon-white.png");
            }
        } catch (Exception e) {
            System.out.println("Error loading logo image: " + e.getMessage());
            logoImage = null;
        }

        // Main panel with background image
        this.mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (backgroundImage != null) {
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    // Fallback gradient if image not found
                    GradientPaint gp1 = new GradientPaint(0, 0, new Color(0x4ECDC4),
                            getWidth(), getHeight() / 2, new Color(0x44A08D));
                    g2d.setPaint(gp1);
                    g2d.fillRect(0, 0, getWidth(), getHeight() / 2);

                    GradientPaint gp2 = new GradientPaint(0, getHeight() / 2, new Color(0x44A08D),
                            getWidth(), getHeight(), new Color(0x093637));
                    g2d.setPaint(gp2);
                    g2d.fillRect(0, getHeight() / 2, getWidth(), getHeight() / 2);
                }
            }
        };
        this.mainPanel.setLayout(new BorderLayout());

        createLeftPanel(this.mainPanel);
        createEmailInputPanel(this.mainPanel);

        add(this.mainPanel);
    }

    private void createLeftPanel(JPanel mainPanel) {
        // Left Panel
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Decorative shapes
                g2d.setColor(new Color(255, 255, 255, 20));
                g2d.fillOval(-50, -50, 200, 200);
                g2d.fillOval(getWidth() - 100, getHeight() - 100, 150, 150);
            }
        };
        leftPanel.setPreferredSize(new Dimension(450, 716));
        leftPanel.setLayout(null);
        leftPanel.setOpaque(false);

        // Logo and Title Panel
        JPanel logoPanel = new JPanel();
        logoPanel.setOpaque(false);
        logoPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
        logoPanel.setBounds(80, 90, 350, 80);

        // Logo image
        if (logoImage != null) {
            JLabel logoLabel = new JLabel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                    // Draw logo image scaled to fit
                    g2d.drawImage(logoImage, 0, 0, 60, 60, this);
                }
            };
            logoLabel.setPreferredSize(new Dimension(60, 60));
            logoPanel.add(logoLabel);
        } else {
            // Fallback logo if image not found
            JPanel logoIcon = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // Circular gradient
                    RadialGradientPaint rgp = new RadialGradientPaint(
                            new Point(30, 30), 30,
                            new float[]{0f, 1f},
                            new Color[]{Color.WHITE, new Color(255, 255, 255, 100)}
                    );
                    g2d.setPaint(rgp);
                    g2d.fillOval(0, 0, 60, 60);

                    // Inner circle
                    g2d.setColor(Color.WHITE);
                    g2d.fillOval(10, 10, 40, 40);

                    // Logo letter C
                    g2d.setColor(new Color(0x4ECDC4));
                    g2d.setFont(new Font("SansSerif", Font.BOLD, 24));
                    g2d.drawString("C", 22, 37);
                }
            };
            logoIcon.setPreferredSize(new Dimension(60, 60));
            logoIcon.setOpaque(false);
            logoPanel.add(logoIcon);
        }

        leftPanel.add(logoPanel);

        // Title image
        if (titleImage != null) {
            JLabel titleLabel = new JLabel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                    // Calculate scaling to fit within bounds while maintaining aspect ratio
                    int imgWidth = titleImage.getWidth();
                    int imgHeight = titleImage.getHeight();
                    double scale = Math.min((double)getWidth() / imgWidth, (double)getHeight() / imgHeight);
                    int scaledWidth = (int)(imgWidth * scale);
                    int scaledHeight = (int)(imgHeight * scale);

                    // Center the image
                    int x = (getWidth() - scaledWidth) / 2;
                    int y = (getHeight() - scaledHeight) / 2;

                    g2d.drawImage(titleImage, x, y, scaledWidth, scaledHeight, this);
                }
            };
            titleLabel.setBounds(160, 95, 250, 60);
            leftPanel.add(titleLabel);
        } else {
            // Fallback text if image not found
            JLabel brandLabel = new JLabel("CELLENTA");
            brandLabel.setFont(new Font("SansSerif", Font.BOLD, 42));
            brandLabel.setForeground(Color.WHITE);
            brandLabel.setBounds(160, 105, 250, 50);
            leftPanel.add(brandLabel);
        }

        // Slogan
        JLabel slogan = new JLabel("<html><div style='font-size:20px; font-weight:300; margin-bottom:15px;'>Building the Future...</div><br>"
                + "<div style='font-size:12px; line-height:1.4; opacity:0.9;'>Lorem ipsum dolor sit amet, consectetur<br>adipiscing elit, sed do eiusmod tempor<br>incididunt ut labore et dolore magna aliqua.</div></html>");
        slogan.setForeground(Color.WHITE);
        slogan.setBounds(80, 230, 350, 140);
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
        progressPanel.setBounds(80, 420, 150, 10);
        progressPanel.setOpaque(false);
        leftPanel.add(progressPanel);

        mainPanel.add(leftPanel, BorderLayout.WEST);
    }

    private void createEmailInputPanel(JPanel mainPanel) {
        // Right Panel (Form)
        JPanel rightPanel = new JPanel(null);
        rightPanel.setBackground(new Color(248, 250, 252, 200));
        rightPanel.setPreferredSize(new Dimension(435, 716));
        rightPanel.setOpaque(false);

        // Form panel - rounded corners with shadow effect
        emailPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Shadow effect
                g2d.setColor(new Color(0, 0, 0, 10));
                g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 20, 20);

                // Main background
                g2d.setColor(new Color(255, 255, 255, 240));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        emailPanel.setBounds(40, 120, 350, 480);
        emailPanel.setOpaque(false);

        // Title
        JLabel title = new JLabel("Forgot password?", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(17, 24, 39));
        title.setBounds(0, 30, 350, 30);
        emailPanel.add(title);

        // Subtitle
        JLabel subtitle = new JLabel("<html><div style='text-align: center; color:#6B7280;'>Don't worry! It happens. Please enter the<br>email associated with your account.</div></html>");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(102, 102, 102));
        subtitle.setBounds(30, 70, 290, 60);
        emailPanel.add(subtitle);

        // Email label
        JLabel emailLabel = new JLabel("Email");
        emailLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        emailLabel.setForeground(new Color(75, 85, 99));
        emailLabel.setBounds(30, 150, 280, 20);
        emailPanel.add(emailLabel);

        // Email input field
        emailField = new JTextField("Enter your email") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                super.paintComponent(g);
            }
        };
        emailField.setBounds(30, 175, 290, 50);
        emailField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        emailField.setBackground(new Color(249, 250, 251));
        emailField.setForeground(new Color(153, 153, 153));
        emailField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        emailField.setOpaque(false);

        emailField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent evt) {
                if (emailField.getText().equals("Enter your email")) {
                    emailField.setText("");
                    emailField.setForeground(Color.BLACK);
                }
                emailField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0x4ECDC4), 2),
                        BorderFactory.createEmptyBorder(12, 16, 12, 16)
                ));
            }

            @Override
            public void focusLost(FocusEvent evt) {
                if (emailField.getText().isEmpty()) {
                    emailField.setText("Enter your email");
                    emailField.setForeground(new Color(153, 153, 153));
                }
                emailField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                        BorderFactory.createEmptyBorder(12, 16, 12, 16)
                ));
            }
        });
        emailPanel.add(emailField);

        // Send code button
        JButton sendButton = new JButton("Send code") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient background
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x4ECDC4),
                        getWidth(), getHeight(), new Color(0x44A08D));
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                // Text
                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(getText());
                int textHeight = fm.getHeight();
                g2d.drawString(getText(), (getWidth() - textWidth) / 2,
                        (getHeight() + textHeight) / 2 - 3);
            }
        };
        sendButton.setBounds(30, 245, 290, 50);
        sendButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        sendButton.setFocusPainted(false);
        sendButton.setBorderPainted(false);
        sendButton.setContentAreaFilled(false);
        sendButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sendButton.addActionListener(this::handleSend);
        emailPanel.add(sendButton);

        // Warning label
        warningLabel = new JLabel();
        warningLabel.setBounds(30, 310, 290, 25);
        warningLabel.setForeground(new Color(239, 68, 68));
        warningLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        warningLabel.setVisible(false);
        emailPanel.add(warningLabel);

        // "Remember password? Log in" link
        JLabel loginLink = new JLabel("<html><div style='text-align:center; color:#6B7280;'>Remember password? <span style='color:#4ECDC4; font-weight:600;'>Log in</span></div></html>");
        loginLink.setFont(new Font("SansSerif", Font.PLAIN, 14));
        loginLink.setBounds(30, 380, 290, 30);
        loginLink.setHorizontalAlignment(SwingConstants.CENTER);
        loginLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new LoginController().setVisible(true);
                dispose();
            }
        });
        emailPanel.add(loginLink);

        rightPanel.add(emailPanel);
        mainPanel.add(rightPanel, BorderLayout.EAST);
    }

    // ============= BACKEND BAĞLANTISI İLE GÜNCELLENEN METODLAR =============

    private void handleSend(ActionEvent e) {
        String email = emailField.getText().trim();

        if (email.isEmpty() || email.equals("Enter your email")) {
            warningLabel.setText("Please enter your email address.");
            warningLabel.setVisible(true);
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            warningLabel.setText("Please enter a valid email address.");
            warningLabel.setVisible(true);
            return;
        }

        userEmail = email;
        warningLabel.setVisible(false);

        // Send button'ı disable et ve loading göster
        JButton sendButton = (JButton) e.getSource();
        sendButton.setEnabled(false);
        sendButton.setText("Sending...");

        // Backend'e email gönder (background thread'de)
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return sendEmailToBackend(email);
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    sendButton.setEnabled(true);
                    sendButton.setText("Send code");

                    if (success) {
                        // Email başarıyla gönderildi, verification paneline geç
                        createVerificationPanel((JPanel) getContentPane().getComponent(0));
                        revalidate();
                        repaint();
                        SwingUtilities.invokeLater(() -> codeFields[0].requestFocus());
                    } else {
                        // Hata durumu
                        warningLabel.setText("Failed to send email. Please try again.");
                        warningLabel.setVisible(true);
                    }
                } catch (Exception ex) {
                    sendButton.setEnabled(true);
                    sendButton.setText("Send code");
                    warningLabel.setText("Network error. Please check your connection.");
                    warningLabel.setVisible(true);
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    // Backend'e email gönderme metodu
    private boolean sendEmailToBackend(String email) {
        try {
            // 6 haneli rastgele kod oluştur
            sentVerificationCode = String.format("%06d", new java.util.Random().nextInt(999999));

            URL url = new URL("http://34.123.86.69/api/v1/deneme-test-code");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Request ayarları
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000); // 10 saniye timeout
            conn.setReadTimeout(8000); // 8 saniye read timeout

            // JSON body oluştur
            String jsonInput = String.format(
                    "{\"email\":\"%s\",\"code\":\"%s\",\"minutes\":%d}",
                    email, sentVerificationCode, 10 // 10 dakika geçerli
            );

            System.out.println("📤 Sending email to: " + email);
            System.out.println("📤 Generated code: " + sentVerificationCode);

            // Request gönder
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInput.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Response al
            int responseCode = conn.getResponseCode();
            System.out.println("📥 Response Code: " + responseCode);

            if (responseCode == 200) {
                // Başarılı response
                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                }

                String responseBody = response.toString();
                System.out.println("📥 Response: " + responseBody);

                if ("true".equals(responseBody.trim())) {
                    System.out.println("✅ Email sent successfully!");
                    return true;
                } else {
                    System.err.println("⚠️ Unexpected response: " + responseBody);
                    return false;
                }
            } else {
                // Hata response
                StringBuilder errorResponse = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), "utf-8"))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        errorResponse.append(responseLine.trim());
                    }
                }

                System.err.println("❌ Error Response: " + errorResponse.toString());
                System.err.println("❌ Failed to send email. Response code: " + responseCode);
                return false;
            }

        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void createVerificationPanel(JPanel mainPanel) {
        // Clear right panel
        Component[] components = mainPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel && ((JPanel) comp).getPreferredSize().width == 435) {
                mainPanel.remove(comp);
                break;
            }
        }

        // New right panel
        JPanel rightPanel = new JPanel(null);
        rightPanel.setBackground(new Color(248, 250, 252, 200));
        rightPanel.setPreferredSize(new Dimension(435, 716));
        rightPanel.setOpaque(false);

        verificationPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Shadow effect
                g2d.setColor(new Color(0, 0, 0, 10));
                g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 20, 20);

                // Main background
                g2d.setColor(new Color(255, 255, 255, 240));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        verificationPanel.setBounds(40, 120, 350, 480);
        verificationPanel.setOpaque(false);

        // Title
        JLabel title = new JLabel("Please, check your mail", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(17, 24, 39));
        title.setBounds(0, 30, 350, 30);
        verificationPanel.add(title);

        // Subtitle
        JLabel subtitle = new JLabel("We've sent a code to your mail", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(102, 102, 102));
        subtitle.setBounds(0, 70, 350, 30);
        verificationPanel.add(subtitle);

        // Create 6 code input fields
        codeFields = new JTextField[6];
        int startX = 30;
        int fieldWidth = 45;
        int fieldHeight = 50;
        int gap = 10;

        for (int i = 0; i < 6; i++) {
            codeFields[i] = new JTextField() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // Background
                    g2d.setColor(getBackground());
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                    super.paintComponent(g);
                }
            };
            codeFields[i].setBounds(startX + (fieldWidth + gap) * i, 120, fieldWidth, fieldHeight);
            codeFields[i].setFont(new Font("SansSerif", Font.BOLD, 20));
            codeFields[i].setHorizontalAlignment(JTextField.CENTER);
            codeFields[i].setBackground(new Color(249, 250, 251));
            codeFields[i].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                    BorderFactory.createEmptyBorder(5, 7, 5, 5)
            ));
            codeFields[i].setOpaque(false);

            final int index = i;
            codeFields[i].addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    codeFields[index].setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(0x4ECDC4), 2),
                            BorderFactory.createEmptyBorder(5, 5, 5, 5)
                    ));
                }

                @Override
                public void focusLost(FocusEvent e) {
                    codeFields[index].setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                            BorderFactory.createEmptyBorder(5, 5, 5, 5)
                    ));
                }
            });

            codeFields[i].addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {
                    char c = e.getKeyChar();
                    if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE) {
                        e.consume();
                    }
                    if (Character.isDigit(c) && codeFields[index].getText().length() >= 1) {
                        e.consume();
                    }
                }

                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && codeFields[index].getText().isEmpty() && index > 0) {
                        codeFields[index - 1].requestFocus();
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    if (Character.isDigit(e.getKeyChar()) && index < 5) {
                        codeFields[index + 1].requestFocus();
                    }
                }
            });

            verificationPanel.add(codeFields[i]);
        }

        // Verify button
        JButton verifyButton = new JButton("Verify") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient background
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x4ECDC4),
                        getWidth(), getHeight(), new Color(0x44A08D));
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                // Text
                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(getText());
                int textHeight = fm.getHeight();
                g2d.drawString(getText(), (getWidth() - textWidth) / 2,
                        (getHeight() + textHeight) / 2 - 3);
            }
        };
        verifyButton.setBounds(30, 200, 290, 50);
        verifyButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        verifyButton.setFocusPainted(false);
        verifyButton.setBorderPainted(false);
        verifyButton.setContentAreaFilled(false);
        verifyButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        verifyButton.addActionListener(this::handleVerify);
        verificationPanel.add(verifyButton);

        // Resend button
        resendButton = new JButton("Send code again 00:20");
        resendButton.setBounds(30, 270, 290, 30);
        resendButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        resendButton.setForeground(new Color(102, 102, 102));
        resendButton.setContentAreaFilled(false);
        resendButton.setBorder(null);
        resendButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        resendButton.setEnabled(false);
        verificationPanel.add(resendButton);

        rightPanel.add(verificationPanel);
        mainPanel.add(rightPanel, BorderLayout.EAST);

        startResendTimer();
    }

    // Güncellenmiş handleVerify metodu
    private void handleVerify(ActionEvent e) {
        StringBuilder code = new StringBuilder();
        for (JTextField field : codeFields) {
            code.append(field.getText());
        }

        if (code.length() != 6) {
            JOptionPane.showMessageDialog(this, "Please enter the complete 6-digit code.",
                    "Incomplete Code", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String enteredCode = code.toString();

        // Girilen kod ile gönderilen kodu karşılaştır
        if (sentVerificationCode != null && sentVerificationCode.equals(enteredCode)) {
            // Kod doğru, password reset paneline geç
            createResetPasswordPanel();
            revalidate();
            repaint();

            if (resendTimer != null && resendTimer.isRunning()) {
                resendTimer.stop();
            }
        } else {
            // Yanlış kod
            JOptionPane.showMessageDialog(this,
                    "Invalid verification code. Please check your email and try again.",
                    "Invalid Code", JOptionPane.ERROR_MESSAGE);

            // Code alanlarını temizle
            for (JTextField field : codeFields) {
                field.setText("");
            }
            SwingUtilities.invokeLater(() -> codeFields[0].requestFocus());
        }
    }

    private void createPasswordChangedPanel() {
        // Clear right panel
        Component[] components = mainPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel && ((JPanel) comp).getPreferredSize().width == 435) {
                mainPanel.remove(comp);
                break;
            }
        }

        // New right panel
        JPanel rightPanel = new JPanel(null);
        rightPanel.setBackground(new Color(248, 250, 252, 200));
        rightPanel.setPreferredSize(new Dimension(435, 716));
        rightPanel.setOpaque(false);

        JPanel successPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Shadow effect
                g2d.setColor(new Color(0, 0, 0, 10));
                g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 20, 20);

                // Main background
                g2d.setColor(new Color(255, 255, 255, 240));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        successPanel.setBounds(40, 120, 350, 480);
        successPanel.setOpaque(false);

        // Title
        JLabel title = new JLabel("Password changed", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(17, 24, 39));
        title.setBounds(0, 120, 350, 30);
        successPanel.add(title);

        // Subtitle
        JLabel subtitle = new JLabel("<html><div style='text-align: center; color:#6B7280;'>Your password has been changed<br>successfully</div></html>");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(102, 102, 102));
        subtitle.setBounds(65, 140, 350, 60);
        successPanel.add(subtitle);

        // Back to login button
        JButton backButton = new JButton("Back to login") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient background
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x4ECDC4),
                        getWidth(), getHeight(), new Color(0x44A08D));
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                // Text
                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(getText());
                int textHeight = fm.getHeight();
                g2d.drawString(getText(), (getWidth() - textWidth) / 2,
                        (getHeight() + textHeight) / 2 - 3);
            }
        };
        backButton.setBounds(30, 260, 290, 50);
        backButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        backButton.setFocusPainted(false);
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> {
            dispose();
            new LoginController().setVisible(true);
        });
        successPanel.add(backButton);

        rightPanel.add(successPanel);
        mainPanel.add(rightPanel, BorderLayout.EAST);
    }

    private void createResetPasswordPanel() {
        // Clear right panel
        Component[] components = mainPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel && ((JPanel) comp).getPreferredSize().width == 435) {
                mainPanel.remove(comp);
                break;
            }
        }

        // New right panel
        JPanel rightPanel = new JPanel(null);
        rightPanel.setBackground(new Color(248, 250, 252, 200));
        rightPanel.setPreferredSize(new Dimension(435, 716));
        rightPanel.setOpaque(false);

        JPanel resetPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Shadow effect
                g2d.setColor(new Color(0, 0, 0, 10));
                g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 20, 20);

                // Main background
                g2d.setColor(new Color(255, 255, 255, 240));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        resetPanel.setBounds(40, 120, 350, 480);
        resetPanel.setOpaque(false);

        // Title
        JLabel title = new JLabel("Reset password", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(17, 24, 39));
        title.setBounds(0, 30, 350, 30);
        resetPanel.add(title);

        // Subtitle
        JLabel subtitle = new JLabel("<html><div style='text-align: center; color:#6B7280;'>Set the new password for your account<br>so you can login and access all features.</div></html>");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(102, 102, 102));
        subtitle.setBounds(30, 70, 290, 60);
        resetPanel.add(subtitle);

        // New Password label
        JLabel newPasswordLabel = new JLabel("New Password");
        newPasswordLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        newPasswordLabel.setForeground(new Color(75, 85, 99));
        newPasswordLabel.setBounds(30, 150, 280, 20);
        resetPanel.add(newPasswordLabel);

        // New Password field
        JPasswordField newPasswordField = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        newPasswordField.setBounds(30, 175, 290, 45);
        newPasswordField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        newPasswordField.setBackground(new Color(249, 250, 251));
        newPasswordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        newPasswordField.setOpaque(false);
        resetPanel.add(newPasswordField);

        // Confirm Password label
        JLabel confirmPasswordLabel = new JLabel("Confirm Password");
        confirmPasswordLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        confirmPasswordLabel.setForeground(new Color(75, 85, 99));
        confirmPasswordLabel.setBounds(30, 240, 280, 20);
        resetPanel.add(confirmPasswordLabel);

        // Confirm Password field
        JPasswordField confirmPasswordField = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        confirmPasswordField.setBounds(30, 265, 290, 45);
        confirmPasswordField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        confirmPasswordField.setBackground(new Color(249, 250, 251));
        confirmPasswordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        confirmPasswordField.setOpaque(false);
        resetPanel.add(confirmPasswordField);

        // Reset Password button
        JButton resetButton = new JButton("Reset Password") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient background
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x4ECDC4),
                        getWidth(), getHeight(), new Color(0x44A08D));
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                // Text
                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(getText());
                int textHeight = fm.getHeight();
                g2d.drawString(getText(), (getWidth() - textWidth) / 2,
                        (getHeight() + textHeight) / 2 - 3);
            }
        };
        resetButton.setBounds(30, 335, 290, 50);
        resetButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        resetButton.setFocusPainted(false);
        resetButton.setBorderPainted(false);
        resetButton.setContentAreaFilled(false);
        resetButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        resetButton.addActionListener(e -> handleResetPassword(newPasswordField, confirmPasswordField));
        resetPanel.add(resetButton);

        rightPanel.add(resetPanel);
        mainPanel.add(rightPanel, BorderLayout.EAST);
    }

    private void startResendTimer() {
        timeLeft = 180;
        resendTimer = new Timer(1000, e -> {
            timeLeft--;
            if (timeLeft <= 0) {
                resendButton.setText("Send code again");
                resendButton.setEnabled(true);
                resendButton.setForeground(new Color(0x4ECDC4));
                resendButton.addActionListener(this::handleResend);
                resendTimer.stop();
            } else {
                resendButton.setText(String.format("Send code again %02d:%02d", timeLeft / 60, timeLeft % 60));
            }
        });
        resendTimer.start();
    }

    private void handleResend(ActionEvent e) {
        // Tekrar email gönder
        if (userEmail != null) {
            JButton resendBtn = (JButton) e.getSource();
            resendBtn.setEnabled(false);
            resendBtn.setText("Sending...");

            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return sendEmailToBackend(userEmail);
                }

                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        if (success) {
                            JOptionPane.showMessageDialog(ForgotPasswordController.this,
                                    "Verification code sent again!", "Code Sent", JOptionPane.INFORMATION_MESSAGE);

                            // Clear code fields
                            for (JTextField field : codeFields) {
                                field.setText("");
                            }

                            // Restart timer
                            resendBtn.setForeground(new Color(102, 102, 102));
                            startResendTimer();

                            // Focus on first field
                            SwingUtilities.invokeLater(() -> codeFields[0].requestFocus());
                        } else {
                            JOptionPane.showMessageDialog(ForgotPasswordController.this,
                                    "Failed to send email. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                            resendBtn.setEnabled(true);
                            resendBtn.setText("Send code again");
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(ForgotPasswordController.this,
                                "Network error. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                        resendBtn.setEnabled(true);
                        resendBtn.setText("Send code again");
                        ex.printStackTrace();
                    }
                }
            };
            worker.execute();
        }
    }

    private void handleResetPassword(JPasswordField newPasswordField, JPasswordField confirmPasswordField) {
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // Validations
        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in both password fields.",
                    "Empty Fields", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (newPassword.length() < 8) {
            JLabel label = new JLabel("⚠ Password must be at least 8 characters long.");
            label.setFont(new Font("SansSerif", Font.BOLD, 17));
            label.setForeground(new Color(0x00C7BE));

            JOptionPane.showMessageDialog(this, label, "Invalid Password", JOptionPane.PLAIN_MESSAGE);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.",
                    "Password Mismatch", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Şu an için sadece success paneline geç (gerçek password reset API'si eklenebilir)
        createPasswordChangedPanel();
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getLookAndFeel());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new ForgotPasswordController().setVisible(true);
        });
    }
}