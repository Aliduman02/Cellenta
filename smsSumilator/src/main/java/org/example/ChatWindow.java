package org.example;

import javax.swing.*;
import java.awt.*;

public class ChatWindow extends JFrame {

    private JPanel messagePanel;
    private JScrollPane scrollPane;
    private JTextArea messageField;
    private JButton sendButton;

    public ChatWindow() {
        setTitle("Cellenta");
        setSize(400, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ÜST BAR (ortalanmış logo)
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                int arc = 15;

                Color color1 = new Color(0x00C7BE);
                Color color2 = new Color(0x16698F);

                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(color1.getRed(), color1.getGreen(), color1.getBlue(), 180),
                        0, getHeight(), new Color(color2.getRed(), color2.getGreen(), color2.getBlue(), 180)
                );
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                g2.dispose();
            }
        };
        headerPanel.setOpaque(false);
        headerPanel.setPreferredSize(new Dimension(400, 45));

        headerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 5)); // Ortalanmış logo

        // LOGO
        ImageIcon logoIcon = new ImageIcon(getClass().getResource("/cellenta-logo.png"));
        Image scaledImage = logoIcon.getImage().getScaledInstance(160, 40, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaledImage));

        headerPanel.add(logoLabel);

        add(headerPanel, BorderLayout.NORTH);

        // MESAJ PANELİ
        messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setBackground(Color.decode("#F4F7F8"));

        scrollPane = new JScrollPane(messagePanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        add(scrollPane, BorderLayout.CENTER);

        // MESAJ GİRİŞİ
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        messageField = new JTextArea(1, 20);
        messageField.setLineWrap(true);
        messageField.setWrapStyleWord(true);
        messageField.setFont(new Font("Arial", Font.PLAIN, 14));
        messageField.setBackground(new Color(255, 255, 255, 180));
        messageField.setForeground(Color.BLACK);
        messageField.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        messageField.setMaximumSize(new Dimension(280, 30));

        JScrollPane messageFieldScrollPane = new JScrollPane(messageField);
        messageFieldScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        messageFieldScrollPane.setBorder(null);

        sendButton = new JButton("Gönder") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(0x00C7BE),
                        0, getHeight(), new Color(0x16698F)
                );
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                g2.setColor(new Color(255, 255, 255, 80));
                g2.fillRoundRect(0, 0, getWidth(), getHeight() / 2, 15, 15);

                g2.dispose();

                super.paintComponent(g);
            }
        };
        sendButton.setForeground(Color.WHITE);
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sendButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sendButton.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 14));
        sendButton.setContentAreaFilled(false);
        sendButton.setFocusPainted(false);

        messageField.setBackground(Color.WHITE);
        messageField.setOpaque(true);

        inputPanel.add(messageFieldScrollPane, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        // ALT BAR (hafif şeffaf)
        JPanel footerBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                Color color1 = new Color(0x00C7BE);
                Color color2 = new Color(0x16698F);
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(color1.getRed(), color1.getGreen(), color1.getBlue(), 180),
                        0, getHeight(), new Color(color2.getRed(), color2.getGreen(), color2.getBlue(), 180)
                );
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        footerBar.setOpaque(false);
        footerBar.setPreferredSize(new Dimension(400, 30));

        JPanel bottomGroup = new JPanel();
        bottomGroup.setLayout(new BorderLayout());
        bottomGroup.add(inputPanel, BorderLayout.CENTER);
        bottomGroup.add(footerBar, BorderLayout.SOUTH);

        add(bottomGroup, BorderLayout.SOUTH);

        // GÖNDER BUTONUNA İŞLEV
        sendButton.addActionListener(e -> {
            String msg = messageField.getText().trim();
            if (!msg.isEmpty()) {
                addMessage(msg, true);
                messageField.setText("");

                Timer timer = new Timer(1000, evt -> {
                    String lowerMsg = msg.toLowerCase();
                    if (lowerMsg.contains("kalan")) {
                        String number = msg.replaceAll("[^0-9]", "");
                        if (number.length() == 10 || number.length() == 11) {
                            String result = TariffApiClient.getTariffFromMiddleware(number);
                            addMessage(result, false);
                        } else {
                            addMessage("Lütfen numarayı doğru formatta giriniz. Örnek: 05321112233 kalan", false);
                        }
                    } else {
                        addMessage("Geçersiz komut. Numara ile birlikte 'kalan' yazmalısınız.", false);
                    }
                });
                timer.setRepeats(false);
                timer.start();
            }
        });
    }

    public void addMessage(String text, boolean isSentByUser) {
        MessageBubble bubble = new MessageBubble(text, isSentByUser);
        messagePanel.add(bubble);
        messagePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        messagePanel.revalidate();
        messagePanel.repaint();

        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }
}
