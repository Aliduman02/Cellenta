package com.cellenta.controller;

import com.cellenta.service.ChatBotService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;

public class ChatBotWindow extends JFrame {
    private JPanel chatPanel;
    private JScrollPane scrollPane;
    private JTextField messageField;
    private JButton sendButton;
    private BufferedImage iconImage;
    private boolean isWaitingForResponse = false;
    private List<ChatMessage> messages = new ArrayList<>();

    public ChatBotWindow() {
        initializeComponents();
        addWelcomeMessage();
    }

    private void initializeComponents() {
        setTitle("Cellenta Assistant");
        setSize(450, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Icon yükle
        try {
            iconImage = ImageIO.read(getClass().getResourceAsStream("/images/icon-white.png"));
            if (iconImage != null) {
                setIconImage(iconImage);
            }
        } catch (Exception e) {
            System.out.println("Icon could not be loaded: " + e.getMessage());
        }

        // Ana panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(248, 249, 250));
        setContentPane(mainPanel);

        // Header
        createHeader(mainPanel);

        // Chat alanı
        createChatArea(mainPanel);

        // Input alanı
        createInputArea(mainPanel);
    }

    private void createHeader(JPanel parent) {
        JPanel header = new JPanel();
        header.setBackground(new Color(22, 105, 143)); // Turkuaz
        header.setPreferredSize(new Dimension(450, 70));
        header.setLayout(new BorderLayout());
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Sol panel - Bot ikonu ve bilgileri
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setOpaque(false);

        // Bot ikonu
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Beyaz daire arka plan
                g2d.setColor(Color.WHITE);
                g2d.fillOval(0, 0, 40, 40);

                // Icon çiz - Siyah logo
                g2d.setColor(new Color(22, 105, 143));
                g2d.setFont(new Font("Arial", Font.BOLD, 16));
                FontMetrics fm = g2d.getFontMetrics();
                int x = (40 - fm.stringWidth("C")) / 2;
                int y = (40 + fm.getHeight()) / 2 - 2;
                g2d.drawString("C", x, y);
            }
        };
        iconPanel.setPreferredSize(new Dimension(40, 40));
        iconPanel.setOpaque(false);

        // Bot bilgileri
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new EmptyBorder(0, 12, 0, 0));

        JLabel nameLabel = new JLabel("Cellenta Assistant");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setForeground(Color.WHITE);

        JLabel statusLabel = new JLabel("● Online - Size yardımcı olmak için buradayım");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(53, 219, 149)); // Yeşil

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(2));
        infoPanel.add(statusLabel);

        leftPanel.add(iconPanel);
        leftPanel.add(infoPanel);

        header.add(leftPanel, BorderLayout.WEST);
        parent.add(header, BorderLayout.NORTH);
    }

    private void createChatArea(JPanel parent) {
        // Chat paneli
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(new Color(248, 249, 250));
        chatPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Scroll pane
        scrollPane = new JScrollPane(chatPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Scroll bar styling
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(180, 180, 180);
                this.trackColor = new Color(245, 245, 245);
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                return button;
            }
        });

        parent.add(scrollPane, BorderLayout.CENTER);
    }

    private void createInputArea(JPanel parent) {
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(new EmptyBorder(15, 20, 20, 20));

        // Input container
        JPanel inputContainer = new JPanel(new BorderLayout());
        inputContainer.setBackground(Color.WHITE);
        inputContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));

        // Mesaj input alanı
        messageField = new JTextField();
        messageField.setFont(new Font("Arial", Font.PLAIN, 14));
        messageField.setBorder(new EmptyBorder(12, 15, 12, 15));
        messageField.setBackground(Color.WHITE);

        // Placeholder effect
        messageField.setText("Mesajınızı yazın...");
        messageField.setForeground(Color.GRAY);
        messageField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (messageField.getText().equals("Mesajınızı yazın...")) {
                    messageField.setText("");
                    messageField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (messageField.getText().isEmpty()) {
                    messageField.setText("Mesajınızı yazın...");
                    messageField.setForeground(Color.GRAY);
                }
            }
        });

        // Enter tuşu ile gönderme
        messageField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && !isWaitingForResponse) {
                    sendMessage();
                }
            }
        });

        // Gönder butonu - Unicode arrow yerine basit >
        sendButton = new JButton(">");
        sendButton.setBackground(new Color(22, 105, 143));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFont(new Font("Arial", Font.BOLD, 18));
        sendButton.setBorder(new EmptyBorder(12, 15, 12, 15));
        sendButton.setFocusPainted(false);
        sendButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sendButton.setPreferredSize(new Dimension(50, 50));

        // Hover efekti
        sendButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!isWaitingForResponse) {
                    sendButton.setBackground(new Color(18, 85, 115));
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!isWaitingForResponse) {
                    sendButton.setBackground(new Color(22, 105, 143));
                }
            }
        });

        sendButton.addActionListener(e -> {
            if (!isWaitingForResponse) {
                sendMessage();
            }
        });

        inputContainer.add(messageField, BorderLayout.CENTER);
        inputContainer.add(sendButton, BorderLayout.EAST);

        inputPanel.add(inputContainer, BorderLayout.CENTER);
        parent.add(inputPanel, BorderLayout.SOUTH);
    }

    private void addWelcomeMessage() {
        String welcomeMessage = "Merhaba! Ben Cellenta Assistant. Size nasıl yardımcı olabilirim?\n\n" +
                "- Hesap işlemleri (giriş, kayıt, şifre sıfırlama)\n" +
                "- Bakiye ve kullanım sorguları\n" +
                "- Kalan dakika, internet ve SMS bilgileri\n" +
                "- Paket bilgileri\n" +
                "- Fatura işlemleri\n\n" +
                "Sorunuzu yazabilirsiniz...";

        addMessage(welcomeMessage, false);
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (message.isEmpty() || message.equals("Mesajınızı yazın...")) {
            return;
        }

        // Kullanıcı mesajını ekle
        addMessage(message, true);
        messageField.setText("");

        // Bekleme durumunu aktifleştir
        setWaitingState(true);

        // API çağrısını arka planda yap
        new Thread(() -> {
            try {
                String response = ChatBotService.sendMessage(message);
                SwingUtilities.invokeLater(() -> {
                    addMessage(response, false);
                    setWaitingState(false);
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    addMessage("Üzgünüm, şu anda bir sorun yaşıyorum. Lütfen daha sonra tekrar deneyin.", false);
                    setWaitingState(false);
                });
            }
        }).start();
    }

    private void addMessage(String message, boolean isUser) {
        SwingUtilities.invokeLater(() -> {
            JPanel messagePanel = createMessageBubble(message, isUser);
            chatPanel.add(messagePanel);
            chatPanel.add(Box.createVerticalStrut(10));

            chatPanel.revalidate();
            chatPanel.repaint();

            // En alta scroll et
            SwingUtilities.invokeLater(() -> {
                JScrollBar vertical = scrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            });
        });
    }

    private JPanel createMessageBubble(String message, boolean isUser) {
        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.setOpaque(false);
        outerPanel.setBorder(new EmptyBorder(5, 0, 5, 0));

        // Mesaj container
        JPanel messageContainer = new JPanel(new BorderLayout());
        messageContainer.setOpaque(false);

        // Avatar paneli
        JPanel avatarPanel = createAvatarPanel(isUser);

        // Mesaj balonu
        JPanel bubble = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bubbleColor = isUser ? new Color(22, 105, 143) : Color.WHITE;
                g2d.setColor(bubbleColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

                // Gölge efekti (sadece bot mesajları için)
                if (!isUser) {
                    g2d.setColor(new Color(0, 0, 0, 10));
                    g2d.fillRoundRect(2, 2, getWidth()-2, getHeight()-2, 18, 18);
                }

                // Border
                g2d.setColor(isUser ? new Color(22, 105, 143) : new Color(230, 230, 230));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 18, 18);
            }
        };
        bubble.setLayout(new BorderLayout());
        bubble.setBorder(new EmptyBorder(12, 15, 12, 15));

        // Mesaj metni
        JTextArea messageText = new JTextArea(message);
        messageText.setEditable(false);
        messageText.setOpaque(false);
        messageText.setFont(new Font("Arial", Font.PLAIN, 13));
        messageText.setForeground(isUser ? Color.WHITE : new Color(60, 60, 60));
        messageText.setLineWrap(true);
        messageText.setWrapStyleWord(true);

        bubble.add(messageText, BorderLayout.CENTER);

        // Zaman damgası
        String timestamp = java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        JLabel timeLabel = new JLabel(timestamp);
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        timeLabel.setForeground(isUser ? new Color(255, 255, 255, 180) : new Color(120, 120, 120));
        timeLabel.setHorizontalAlignment(isUser ? SwingConstants.RIGHT : SwingConstants.LEFT);
        timeLabel.setBorder(new EmptyBorder(5, 0, 0, 0));
        bubble.add(timeLabel, BorderLayout.SOUTH);

        // Layout arrangement
        if (isUser) {
            messageContainer.add(Box.createHorizontalStrut(50), BorderLayout.WEST);
            messageContainer.add(bubble, BorderLayout.CENTER);
            messageContainer.add(avatarPanel, BorderLayout.EAST);
        } else {
            messageContainer.add(avatarPanel, BorderLayout.WEST);
            messageContainer.add(bubble, BorderLayout.CENTER);
            messageContainer.add(Box.createHorizontalStrut(50), BorderLayout.EAST);
        }

        outerPanel.add(messageContainer, BorderLayout.CENTER);
        return outerPanel;
    }

    private JPanel createAvatarPanel(boolean isUser) {
        JPanel avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (isUser) {
                    // Kullanıcı avatarı - mavi gradient
                    GradientPaint gradient = new GradientPaint(0, 0, new Color(22, 105, 143),
                            35, 35, new Color(53, 219, 149));
                    g2d.setPaint(gradient);
                    g2d.fillOval(0, 0, 35, 35);

                    // Kullanıcı ikonu - U harfi
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Arial", Font.BOLD, 16));
                    FontMetrics fm = g2d.getFontMetrics();
                    int x = (35 - fm.stringWidth("U")) / 2;
                    int y = (35 + fm.getHeight()) / 2 - 2;
                    g2d.drawString("U", x, y);
                } else {
                    // Bot avatarı
                    g2d.setColor(new Color(22, 105, 143));
                    g2d.fillOval(0, 0, 35, 35);

                    // Bot ikonu - C harfi
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Arial", Font.BOLD, 14));
                    FontMetrics fm = g2d.getFontMetrics();
                    int x = (35 - fm.stringWidth("C")) / 2;
                    int y = (35 + fm.getHeight()) / 2 - 2;
                    g2d.drawString("C", x, y);
                }
            }
        };
        avatarPanel.setPreferredSize(new Dimension(35, 35));
        avatarPanel.setOpaque(false);
        avatarPanel.setBorder(new EmptyBorder(0, 8, 0, 8));
        return avatarPanel;
    }

    private void setWaitingState(boolean waiting) {
        isWaitingForResponse = waiting;
        sendButton.setEnabled(!waiting);
        messageField.setEnabled(!waiting);

        if (waiting) {
            sendButton.setText("...");
            sendButton.setBackground(new Color(150, 150, 150));
        } else {
            sendButton.setText(">");
            sendButton.setBackground(new Color(22, 105, 143));
        }
    }

    // ChatMessage sınıfı (inner class)
    private static class ChatMessage {
        String message;
        boolean isUser;
        long timestamp;

        ChatMessage(String message, boolean isUser) {
            this.message = message;
            this.isUser = isUser;
            this.timestamp = System.currentTimeMillis();
        }
    }
}