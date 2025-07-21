package org.example;

import javax.swing.*;
import java.awt.*;

public class MessageBubble extends JPanel {

    public MessageBubble(String message, boolean isSender) {
        setOpaque(false);

        JTextArea textArea = new JTextArea(message);
        textArea.setFont(new Font("Arial", Font.PLAIN, 14));
        textArea.setForeground(isSender ? Color.WHITE : Color.BLACK);
        textArea.setBackground(new Color(0, 0, 0, 0));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setFocusable(false);
        textArea.setBorder(null);
        textArea.setMaximumSize(new Dimension(300, Short.MAX_VALUE));

        JPanel bubblePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bgColor = isSender
                        ? new Color(0, 199, 190, 204)   // Turkuaz yarı saydam
                        : new Color(211, 215, 226);     // Lavanta grisi
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        bubblePanel.setLayout(new BorderLayout());
        bubblePanel.setOpaque(false);
        bubblePanel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        bubblePanel.setMaximumSize(new Dimension(320, Integer.MAX_VALUE));
        bubblePanel.add(textArea, BorderLayout.CENTER);

        setLayout(new FlowLayout(isSender ? FlowLayout.RIGHT : FlowLayout.LEFT, 2, 2));
        add(bubblePanel);
    }
}
