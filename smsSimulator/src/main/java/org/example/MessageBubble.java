package org.example;

import javax.swing.*;
import java.awt.*;

public class MessageBubble extends JPanel {
    public MessageBubble(String htmlMessage, boolean isSender, int parentWidth) {
        setOpaque(false);
        setLayout(new FlowLayout(isSender ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));

        int bubbleAreaWidth = parentWidth / 2;

        // HTML içeriğe stil ekle
        String styledHtml;
        if (isSender) {
            // Giden mesajlar için: font size 18pt ve kalın (bold)
            styledHtml = "<html><body style='font-size:18pt; font-weight:bold; color:white;'>" + htmlMessage + "</body></html>";
        } else {
            // Gelen mesajlar için: font size 18pt, normal, siyah renk
            styledHtml = "<html><body style='font-size:18pt; font-weight:normal; color:black;'>" + htmlMessage + "</body></html>";
        }

        JEditorPane textPane = new JEditorPane("text/html", styledHtml);
        textPane.setEditable(false);
        textPane.setOpaque(false);
        textPane.setBorder(null);

        // Burada font ayarı yapmaya gerek yok, çünkü HTML stil kullanıyoruz

        // Genişliği ayarla, yüksekliği içeriğe göre olsun
        textPane.setSize(new Dimension(bubbleAreaWidth - 20, Integer.MAX_VALUE));
        Dimension preferred = textPane.getPreferredSize();
        textPane.setPreferredSize(new Dimension(bubbleAreaWidth - 20, preferred.height));

        JPanel bubblePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bgColor = isSender
                        ? new Color(0, 199, 190, 204)
                        : new Color(211, 215, 226);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        bubblePanel.setLayout(new BorderLayout());
        bubblePanel.setOpaque(false);
        bubblePanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        bubblePanel.setPreferredSize(new Dimension(bubbleAreaWidth, preferred.height + 16));
        bubblePanel.add(textPane, BorderLayout.CENTER);

        add(bubblePanel);
        setMaximumSize(new Dimension(parentWidth, preferred.height + 30));
    }
}
