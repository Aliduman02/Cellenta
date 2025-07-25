package com.cellenta.controller;

import com.cellenta.BalanceHelper;
import com.cellenta.LoginHelper;
import com.cellenta.PhoneNumberStorageHelper;
import com.cellenta.PhoneAutoCompletePopup;

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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import org.json.JSONObject;

public class LoginController extends JFrame {
    private JTextField phoneField;
    private JPasswordField passwordField;
    private JTextField warningField;
    private BufferedImage backgroundImage;
    private BufferedImage titleImage;
    private BufferedImage iconImage;
    private int customerId;

    // Panel referansları responsive layout için
    private JPanel leftPanel;
    private JPanel rightPanel;
    private JPanel formPanel;
    
    private PhoneAutoCompletePopup autoCompletePopup;
    private boolean isRememberMeEnabled = true;

    public LoginController() {
        setTitle("Giriş Yap");
        setSize(885, 716);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Arka plan resmini yükle
        try {
            backgroundImage = ImageIO.read(getClass().getResourceAsStream("/images/background_mavi.jpg"));
            if (backgroundImage == null) {
                backgroundImage = ImageIO.read(getClass().getResourceAsStream("/resources/images/background_mavi.jpg"));
            }
        } catch (Exception e) {
            System.out.println("Background image not found, using default gradient.");
            backgroundImage = null;
        }

        // Title ve icon resimlerini yükle
        try {
            titleImage = ImageIO.read(getClass().getResourceAsStream("/images/title-white.png"));
            if (titleImage == null) {
                titleImage = ImageIO.read(getClass().getResourceAsStream("/resources/images/title-white.png"));
            }
        } catch (Exception e) {
            System.out.println("Title image not found: " + e.getMessage());
            titleImage = null;
        }

        try {
            iconImage = ImageIO.read(getClass().getResourceAsStream("/images/icon-white.png"));
            if (iconImage == null) {
                iconImage = ImageIO.read(getClass().getResourceAsStream("/resources/images/icon-white.png"));
            }
        } catch (Exception e) {
            System.out.println("Icon image not found: " + e.getMessage());
            iconImage = null;
        }

        // Ana panel - arka plan resmi ile
        JPanel mainPanel = new JPanel() {
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
        mainPanel.setLayout(null); // Absolute layout kullanıyoruz responsive için

        createLeftPanel(mainPanel);
        createRightPanel(mainPanel);

        add(mainPanel);

        // Responsive layout için component listener ekle
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateLayout();
            }
        });

        // İlk layout'u ayarla
        SwingUtilities.invokeLater(() -> updateLayout());
    }

    private void createLeftPanel(JPanel parent) {
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
                g2d.fillOval(getWidth() - 100, getHeight() - 100, 150, 150);
            }
        };
        leftPanel.setLayout(null);
        leftPanel.setOpaque(false);

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
                }

                // Title resmi çiz
                if (titleImage != null) {
                    g2d.drawImage(titleImage, x, y + 10, 200, 40, this);
                }
            }
        };
        logoPanel.setBounds(80, 90, 350, 80);
        logoPanel.setOpaque(false);
        leftPanel.add(logoPanel);

        // Slogan - pozisyonu ve boyutu ayarlandı
        JLabel slogan = new JLabel("<html><div style='font-size:15px; font-weight:bold; margin-bottom:15px;'>Sizi Yarının Dünyasına Bağlıyoruz</div><br>"
                + "<div style='font-size:12px; line-height:1.4; opacity:0.9;'>Kesintisiz iletişim, güvenilir kapsama ve<br>gerçekten bağlantılı bir dünya için<br>yenilikçi çözümler deneyimleyin.</div></html>");
        slogan.setForeground(Color.WHITE);
        slogan.setBounds(80, 220, 350, 180);
        leftPanel.add(slogan);

        // Progress indicator - pozisyonu aşağı indirildi
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

        parent.add(leftPanel);
    }

    private void createRightPanel(JPanel parent) {
        // Sağ Panel (Form) - Daha modern tasarım
        rightPanel = new JPanel(null);
        rightPanel.setBackground(new Color(248, 250, 252, 200));
        rightPanel.setOpaque(false);

        // Form paneli - rounded corners ve shadow effect
        formPanel = new JPanel(null) {
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
        formPanel.setOpaque(false);

        JLabel welcome = new JLabel("TEKRAR HOŞGELDİNİZ", SwingConstants.CENTER);
        welcome.setFont(new Font("SansSerif", Font.PLAIN, 12));
        welcome.setForeground(new Color(107, 114, 128));
        welcome.setBounds(0, 30, 350, 20);
        formPanel.add(welcome);

        JLabel title = new JLabel("HESABINIZA GİRİŞ YAPIN", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(17, 24, 39));
        title.setBounds(0, 55, 350, 30);
        formPanel.add(title);

        JLabel phoneLabel = new JLabel("Telefon Numarası");
        phoneLabel.setBounds(30, 110, 280, 20);
        phoneLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        phoneLabel.setForeground(new Color(75, 85, 99));
        formPanel.add(phoneLabel);

        // Phone field with modern styling and number formatting
        phoneField = new JTextField() {
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

        phoneField = new JTextField();

        // Sadece rakam girişi için DocumentFilter
        PlainDocument phoneDoc = (PlainDocument) phoneField.getDocument();
        phoneDoc.setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string == null) return;

                String newStr = string.replaceAll("[^0-9]", ""); // Sadece rakamlar

                // Toplam 10 karakter sınırı
                if (fb.getDocument().getLength() + newStr.length() <= 10) {
                    super.insertString(fb, offset, newStr, attr);
                    // Hide warning if input is valid
                    if (warningField != null) {
                        warningField.setVisible(false);
                    }
                } else {
                    // Show warning when trying to enter more than 10 digits
                    if (warningField != null) {
                        warningField.setText("Telefon numarası en fazla 10 hane olabilir.");
                        warningField.setVisible(true);
                    }
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text == null) return;

                String newStr = text.replaceAll("[^0-9]", "");

                if (fb.getDocument().getLength() - length + newStr.length() <= 10) {
                    super.replace(fb, offset, length, newStr, attrs);
                    // Hide warning if input is valid
                    if (warningField != null) {
                        warningField.setVisible(false);
                    }
                } else {
                    // Show warning when trying to enter more than 10 digits
                    if (warningField != null) {
                        warningField.setText("Telefon numarası en fazla 10 hane olabilir.");
                        warningField.setVisible(true);
                    }
                }
            }

            @Override
            public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                super.remove(fb, offset, length);
                // Hide warning when removing characters
                if (warningField != null) {
                    warningField.setVisible(false);
                }
            }
        });



        phoneField.setDocument(phoneDoc);

        phoneField.setBounds(30, 135, 250, 45);
        phoneField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        phoneField.setBackground(new Color(249, 250, 251));
        phoneField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        phoneField.setOpaque(false);
        
        autoCompletePopup = new PhoneAutoCompletePopup(selectedPhone -> {
            phoneField.setText(selectedPhone);
            phoneField.setCaretPosition(selectedPhone.length());
        });
        
        phoneField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (autoCompletePopup.isVisible()) {
                        autoCompletePopup.selectNext();
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    if (autoCompletePopup.isVisible()) {
                        autoCompletePopup.selectPrevious();
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (autoCompletePopup.isVisible() && autoCompletePopup.hasSuggestions()) {
                        String selected = autoCompletePopup.getSelectedSuggestion();
                        if (selected != null) {
                            phoneField.setText(selected);
                            phoneField.setCaretPosition(selected.length());
                            autoCompletePopup.setVisible(false);
                        }
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    autoCompletePopup.setVisible(false);
                } else {
                    updateAutoComplete();
                }
            }
            
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP) {
                    e.consume();
                }
            }
        });
        
        phoneField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                Timer timer = new Timer(150, evt -> {
                    autoCompletePopup.setVisible(false);
                    ((Timer) evt.getSource()).stop();
                });
                timer.start();
            }
            
            @Override
            public void focusGained(FocusEvent e) {
                if (!phoneField.getText().isEmpty()) {
                    updateAutoComplete();
                }
            }
        });
        
        formPanel.add(phoneField);

        // Check icon for phone field
        JLabel phoneCheck = new JLabel("✓") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Circle background
                g2d.setColor(new Color(34, 197, 94));
                g2d.fillOval(5, 5, 25, 25);

                // Check mark
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
                g2d.drawString("✓", 12, 22);
            }
        };
        phoneCheck.setBounds(285, 140, 35, 35);
        phoneCheck.setOpaque(false);
        formPanel.add(phoneCheck);

        JLabel passLabel = new JLabel("Şİfre");
        passLabel.setBounds(30, 195, 280, 20);
        passLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        passLabel.setForeground(new Color(75, 85, 99));
        formPanel.add(passLabel);

        // Password field - boş başlangıç
        passwordField = new JPasswordField() {
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
        passwordField.setBounds(30, 220, 250, 45);
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        passwordField.setBackground(new Color(249, 250, 251));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        passwordField.setOpaque(false);
        formPanel.add(passwordField);

        // Eye icon for password field
        JLabel eyeIcon = new JLabel("👁") {
            private boolean isPasswordVisible = false;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background
                g2d.setColor(new Color(243, 244, 246));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);

                // Eye icon
                g2d.setColor(new Color(107, 114, 128));
                g2d.setFont(new Font("SansSerif", Font.PLAIN, 16));
                String eyeText = isPasswordVisible ? "🙈" : "👁";
                g2d.drawString(eyeText, 8, 22);
            }

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        isPasswordVisible = !isPasswordVisible;
                        if (isPasswordVisible) {
                            passwordField.setEchoChar((char) 0); // Şifreyi göster
                        } else {
                            passwordField.setEchoChar('•'); // Şifreyi gizle
                        }
                        repaint();
                    }
                });
            }
        };
        eyeIcon.setBounds(285, 225, 35, 35);
        eyeIcon.setOpaque(false);
        eyeIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        formPanel.add(eyeIcon);

        // Remember me with toggle switch
        JPanel rememberPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        rememberPanel.setBounds(30, 285, 150, 30);
        rememberPanel.setOpaque(false);

        JPanel toggleSwitch = new JPanel() {
            private boolean isOn = true;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Switch background
                g2d.setColor(isOn ? new Color(34, 197, 94) : new Color(229, 231, 235));
                g2d.fillRoundRect(0, 5, 40, 20, 20, 20);

                // Switch knob
                g2d.setColor(Color.WHITE);
                int knobX = isOn ? 22 : 2;
                g2d.fillOval(knobX, 7, 16, 16);
            }

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        isOn = !isOn;
                        isRememberMeEnabled = isOn;
                        repaint();
                    }
                });
            }
        };
        toggleSwitch.setPreferredSize(new Dimension(40, 30));
        toggleSwitch.setOpaque(false);
        toggleSwitch.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel rememberLabel = new JLabel("Beni Hatırla");
        rememberLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        rememberLabel.setForeground(new Color(75, 85, 99));
        rememberLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        rememberPanel.add(toggleSwitch);
        rememberPanel.add(rememberLabel);
        formPanel.add(rememberPanel);

        JButton forgotBtn = new JButton("Şifrenizi mi unuttunuz?");
        forgotBtn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        forgotBtn.setForeground(new Color(59, 130, 246));
        forgotBtn.setContentAreaFilled(false);
        forgotBtn.setBorderPainted(false);
        forgotBtn.setFocusPainted(false);
        forgotBtn.setBounds(180, 290, 160, 20);
        forgotBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgotBtn.addActionListener(e -> switchToForgotPassword());
        formPanel.add(forgotBtn);

        // Modern gradient button
        JButton loginBtn = new JButton("Giriş Yap") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient background
                GradientPaint gp = new GradientPaint(0, 0, new Color(34, 197, 94),
                        getWidth(), getHeight(), new Color(21, 128, 61));
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
        loginBtn.setBounds(30, 340, 290, 50);
        loginBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        loginBtn.setFocusPainted(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setContentAreaFilled(false);
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginBtn.addActionListener(this::handleLogin);
        formPanel.add(loginBtn);

        JLabel signupLink = new JLabel("<html><div style='text-align:center; color:#6B7280;'>Hesabınız yok mu? <span style='color:#3B82F6; font-weight:600;'>Kayıt Ol</span></div></html>");
        signupLink.setFont(new Font("SansSerif", Font.PLAIN, 14));
        signupLink.setBounds(30, 410, 290, 30);
        signupLink.setHorizontalAlignment(SwingConstants.CENTER);
        signupLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signupLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                switchToRegister();
            }
        });
        formPanel.add(signupLink);

        warningField = new JTextField();
        warningField.setVisible(false);
        warningField.setEditable(false);
        warningField.setBounds(30, 450, 290, 25);
        warningField.setForeground(new Color(239, 68, 68));
        warningField.setBorder(null);
        warningField.setFont(new Font("SansSerif", Font.PLAIN, 12));
        warningField.setBackground(Color.WHITE);
        formPanel.add(warningField);

        rightPanel.add(formPanel);
        parent.add(rightPanel);
    }

    private void updateLayout() {
        int frameWidth = getWidth();
        int frameHeight = getHeight();

        // Minimum boyutlar
        int minLeftWidth = 400;
        int minRightWidth = 450;
        int totalMinWidth = minLeftWidth + minRightWidth;
        int formWidth = 350;

        if (frameWidth <= totalMinWidth) {
            // Küçük ekranlarda sabit boyutlar
            leftPanel.setBounds(0, 0, minLeftWidth, frameHeight);
            rightPanel.setBounds(minLeftWidth, 0, minRightWidth, frameHeight);

            // Form paneli sağ panelin ortasında
            int formX = (minRightWidth - formWidth) / 2;
            formPanel.setBounds(formX, 120, formWidth, 480);
        } else {
            // Büyük ekranlarda responsive
            int extraSpace = frameWidth - totalMinWidth;
            int leftWidth = minLeftWidth + (extraSpace / 2);
            int rightWidth = minRightWidth + (extraSpace / 2);

            // Panellerin ortada buluşması için
            leftPanel.setBounds(0, 0, leftWidth, frameHeight);
            rightPanel.setBounds(leftWidth, 0, rightWidth, frameHeight);

            // Form paneli sağ panelin ortasında
            int formX = (rightWidth - formWidth) / 2;
            formPanel.setBounds(formX, 120, formWidth, 480);
        }

        revalidate();
        repaint();
    }

    private void handleLogin(ActionEvent e) {
        String phone = phoneField.getText().trim();
        String pass = String.valueOf(passwordField.getPassword()).trim();

        if (phone.isEmpty() || pass.isEmpty()) {
            warningField.setText("Lütfen tüm alanları doldurun.");
            warningField.setVisible(true);
            return;
        }

        if (phone.length() != 10) {
            warningField.setText("Telefon numarası tam 10 hane olmalıdır.");
            warningField.setVisible(true);
            return;
        }

        if (pass.length() < 8) {
            warningField.setText("Şifre en az 8 karakter içermelidir.");
            warningField.setVisible(true);
            return;
        }

        // 📤 Giriş yap ve JSON dönen veriyi al
        JSONObject loginData = LoginHelper.login(phone, pass);

        if (loginData != null) {
            try {
                int customerId = loginData.getInt("cust_id");
                String name = loginData.getString("name");
                String surname = loginData.getString("surname");
                String email = loginData.getString("email");

                if (isRememberMeEnabled) {
                    PhoneNumberStorageHelper.savePhoneNumber(phone);
                }

                // 🎯 Giriş başarılı, ana sayfaya geçiş yap
                SwingUtilities.invokeLater(() -> {
                    // Simple smooth transition with delay
                    Timer transitionTimer = new Timer(100, evt -> {
                        ((Timer) evt.getSource()).stop();
                        dispose();
                        new MainController(customerId, name, surname, phone, email, pass).setVisible(true);
                    });
                    transitionTimer.start();
                });

            } catch (Exception ex) {
                ex.printStackTrace();
                warningField.setText("Giriş yanıtı ayrıştırma hatası!");
                warningField.setVisible(true);
            }
        } else {
            warningField.setText("Giriş başarısız!");
            warningField.setVisible(true);
        }
    }

    private void switchToForgotPassword() {
        // Clear all content
        getContentPane().removeAll();
        
        // Create and show forgot password controller content in the same window
        ForgotPasswordController forgotPasswordController = new ForgotPasswordController(this);
        forgotPasswordController.setVisible(false); // Don't show as separate window
        
        // Copy forgot password content to current window
        getContentPane().add(forgotPasswordController.getContentPane());
        setTitle("Forgot Password - Cellenta");
        
        // Pencere boyutunu yeniden ayarla ve ortala
        setSize(885, 716);
        setLocationRelativeTo(null);
        
        revalidate();
        repaint();
        
        // Clean up the temporary forgot password controller
        forgotPasswordController.dispose();
    }

    private void switchToRegister() {
        // Clear all content
        getContentPane().removeAll();
        
        // Create and show register controller content in the same window
        RegisterController registerController = new RegisterController();
        registerController.setVisible(false); // Don't show as separate window
        
        // Copy register content to current window
        getContentPane().add(registerController.getContentPane());
        setTitle("Register - Cellenta");
        
        // Pencere boyutunu yeniden ayarla ve ortala
        setSize(885, 716);
        setLocationRelativeTo(null);
        
        revalidate();
        repaint();
        
        // Clean up the temporary register controller
        registerController.dispose();
    }

    private void updateAutoComplete() {
        String currentText = phoneField.getText();
        java.util.List<String> suggestions = PhoneNumberStorageHelper.getSuggestionsForInput(currentText);
        
        if (!suggestions.isEmpty()) {
            Point fieldLocation = phoneField.getLocationOnScreen();
            autoCompletePopup.setLocation(fieldLocation.x, fieldLocation.y + phoneField.getHeight());
            autoCompletePopup.updateSuggestions(suggestions);
        } else {
            autoCompletePopup.setVisible(false);
        }
    }

    /**
     * Override setVisible method
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            SwingUtilities.invokeLater(() -> {
                // Keep phone field empty on startup - suggestions will show when user types
                phoneField.setText("");
            });
        }
    }
}