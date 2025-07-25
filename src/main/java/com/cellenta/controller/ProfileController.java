package com.cellenta.controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class ProfileController extends JFrame {
    private BufferedImage titleImage;
    private BufferedImage iconImage;
    private int customerId;
    public ProfileController(int customerId,String name, String surname, String phone, String email) {
        this.customerId=customerId;
        setTitle("Profil");
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Title ve icon resimlerini yükle
        try {
            titleImage = ImageIO.read(getClass().getResourceAsStream("/images/title.png"));
        } catch (Exception e) {
            System.out.println("Title image not found: " + e.getMessage());
            titleImage = null;
        }

        try {
            iconImage = ImageIO.read(getClass().getResourceAsStream("/images/icon.png"));
        } catch (Exception e) {
            System.out.println("Icon image not found: " + e.getMessage());
            iconImage = null;
        }

        // ✅ Arka plan olarak background.png ayarlanıyor
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon img = new ImageIcon(getClass().getResource("/images/background.png"));
                g.drawImage(img.getImage(), 0, -100, getWidth(), getHeight() + 100, this);
            }
        };
        backgroundPanel.setLayout(null);
        setContentPane(backgroundPanel);

        // HEADER
        JPanel header = new JPanel(null);
        header.setBounds(0, 0, 1400, 80);
        header.setOpaque(false); // şeffaf
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)));

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
                    g2d.drawImage(iconImage, x, y, 40, 40, this);
                    x += 50; // Icon'dan sonra title için boşluk
                } else {
                    // Icon yoksa varsayılan logo
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
                    g2d.drawString("CELLENTA", x, y + 25);
                }
            }
        };
        logoPanel.setBounds(575, 20, 250, 40);
        logoPanel.setOpaque(false);
        header.add(logoPanel);

        getContentPane().add(header);

        // SIDEBAR
        JPanel sidebar = new JPanel(null);
        sidebar.setBounds(0, 80, 250, 820);
        sidebar.setOpaque(false);

        String[] sidebarItems = {"ANA SAYFA", "MAĞAZA", "FATURALAR", "PROFİL"};
        for (int i = 0; i < sidebarItems.length; i++) {
            JLabel label = new JLabel(sidebarItems[i]);
            label.setFont(new Font("Arial", sidebarItems[i].equals("PROFİL") ? Font.BOLD : Font.PLAIN, 14));
            label.setForeground(sidebarItems[i].equals("PROFİL") ? new Color(52, 52, 52) : new Color(150, 150, 150));
            label.setBounds(50, 150 + (i * 50), 150, 25);
            label.setCursor(new Cursor(Cursor.HAND_CURSOR));

            int finalI = i;
            label.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (finalI == 3) {
                        // Already on PROFİL page
                        return;
                    }
                    
                    // Smooth transition with delay
                    Timer transitionTimer = new Timer(50, evt -> {
                        ((Timer) evt.getSource()).stop();
                        SwingUtilities.invokeLater(() -> {
                            setVisible(false);
                            
                            switch (finalI) {
                                case 0 -> SwingUtilities.invokeLater(() -> 
                                    new MainController(customerId,name, surname, phone, email, "").setVisible(true));
                                case 1 -> SwingUtilities.invokeLater(() -> 
                                    new StoreController(customerId,name, surname, phone, email, "").setVisible(true));
                                case 2 -> SwingUtilities.invokeLater(() -> 
                                    new BillHistoryController(customerId,name, surname, phone, email, "").setVisible(true));
                            }
                        });
                    });
                    transitionTimer.start();
                }

                public void mouseEntered(MouseEvent e) {
                    label.setForeground(new Color(22, 105, 143));
                }

                public void mouseExited(MouseEvent e) {
                    label.setForeground(sidebarItems[finalI].equals("PROFİL") ? new Color(52, 52, 52) : new Color(150, 150, 150));
                }
            });
            sidebar.add(label);
        }
        getContentPane().add(sidebar);

        // MAIN CONTENT
        JPanel mainContent = new JPanel(null);
        mainContent.setBounds(250, 80, 1150, 820);
        mainContent.setOpaque(false);

        JLabel pageTitle = new JLabel("← Profil");
        pageTitle.setFont(new Font("Arial", Font.BOLD, 28));
        pageTitle.setForeground(new Color(52, 52, 52));
        pageTitle.setBounds(50, 50, 200, 35);
        pageTitle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pageTitle.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                Timer transitionTimer = new Timer(50, evt -> {
                    ((Timer) evt.getSource()).stop();
                    SwingUtilities.invokeLater(() -> {
                        setVisible(false);
                        SwingUtilities.invokeLater(() -> 
                            new MainController(customerId,name, surname, phone, email, "").setVisible(true)
                        );
                    });
                });
                transitionTimer.start();
            }
        });
        mainContent.add(pageTitle);

        JButton logoutBtn = createRoundButton("Çıkış Yap", new Color(255, 71, 71), Color.WHITE);
        logoutBtn.setBounds(950, 50, 120, 40);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> switchToLogin());
        mainContent.add(logoutBtn);

        // PROFILE CARD
        JPanel profileCard = new JPanel(null) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.setColor(new Color(240, 240, 240));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            }
        };
        profileCard.setBounds(50, 120, 1050, 550);
        profileCard.setOpaque(false);

        JLabel cardTitle = new JLabel("Kişisel Bilgiler");
        cardTitle.setFont(new Font("Arial", Font.BOLD, 24));
        cardTitle.setForeground(new Color(52, 52, 52));
        cardTitle.setBounds(50, 50, 400, 30);
        profileCard.add(cardTitle);

        JPanel profileImage = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(22, 105, 143));
                g2d.fillOval(0, 0, 120, 120);
            }
        };
        profileImage.setBounds(850, 120, 120, 120);
        profileImage.setOpaque(false);
        profileImage.setLayout(new GridBagLayout());

        String initial = (name != null && !name.isEmpty()) ? name.substring(0, 1).toUpperCase() : "?";
        JLabel initialLabel = new JLabel(initial);
        initialLabel.setFont(new Font("Arial", Font.BOLD, 48));
        initialLabel.setForeground(Color.WHITE);
        profileImage.add(initialLabel);
        profileCard.add(profileImage);

        int y = 120;
        addProfileField(profileCard, "Ad", name, y); y += 80;
        addProfileField(profileCard, "Soyad", surname, y); y += 80;
        addProfileField(profileCard, "Telefon Numarası", phone, y); y += 80;
        addProfileField(profileCard, "E-posta", email, y);

        mainContent.add(profileCard);
        getContentPane().add(mainContent);

        // CHAT BOT
        JPanel chatBot = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(22, 105, 143));
                g2d.fillOval(0, 0, 60, 60);
            }
        };
        chatBot.setBounds(1320, 800, 60, 60);
        chatBot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chatBot.setLayout(null);
        chatBot.setOpaque(false);

        JPanel chatLogo = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawArc(5, 5, 20, 20, 0, 270);
                g2d.fillOval(15, 8, 6, 6);
            }
        };
        chatLogo.setBounds(15, 15, 30, 30);
        chatLogo.setOpaque(false);
        chatBot.add(chatLogo);

        JLabel chatMessage = new JLabel("<html><div style='text-align: center; font-size: 10px; color: #666;'>Merhaba, ben Cellenta!<br>Size nasıl yardımcı olabilirim?</div></html>");
        chatMessage.setOpaque(true);
        chatMessage.setBackground(Color.WHITE);
        chatMessage.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        chatMessage.setBounds(1150, 750, 160, 40);
        chatMessage.setVisible(false);

        chatBot.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                chatMessage.setVisible(!chatMessage.isVisible());
            }
        });

        getContentPane().add(chatBot);
        getContentPane().add(chatMessage);
    }

    private JButton createRoundButton(String text, Color bgColor, Color textColor) {
        JButton button = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                super.paintComponent(g);
            }
        };
        button.setBackground(bgColor);
        button.setForeground(textColor);
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.setBorder(null);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        return button;
    }

    private void addProfileField(JPanel panel, String labelText, String value, int y) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        label.setForeground(new Color(150, 150, 150));
        label.setBounds(50, y, 200, 20);
        panel.add(label);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 16));
        valueLabel.setForeground(new Color(52, 52, 52));
        valueLabel.setBounds(50, y + 25, 400, 30);
        panel.add(valueLabel);
    }

    private void switchToLogin() {
        // Close current window and open login window with smooth transition
        Timer transitionTimer = new Timer(50, evt -> {
            ((Timer) evt.getSource()).stop();
            SwingUtilities.invokeLater(() -> {
                dispose(); // Close current window
                new LoginController().setVisible(true); // Open new login window
            });
        });
        transitionTimer.start();
    }

    /**
     * Override setVisible method
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
    }
}