package com.cellenta.controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Random;

public class EmailTestController extends JFrame {
    private JTextField emailField;
    private JTextField codeField;
    private JSpinner minutesSpinner;
    private JButton sendButton;
    private JTextArea responseArea;
    private JLabel statusLabel;

    public EmailTestController() {
        setTitle("Email Test - Forgot Password");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        createComponents();
        setupUI();
    }

    private void createComponents() {
        // Ana panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Başlık
        JLabel titleLabel = new JLabel("📧 E-mail Test Panel");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        mainPanel.add(titleLabel);

        // E-mail input
        JPanel emailPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        emailPanel.add(new JLabel("E-mail:"));
        emailField = new JTextField(20);
        emailField.setText("test@gmail.com"); // Varsayılan test e-mail
        emailPanel.add(emailField);
        mainPanel.add(emailPanel);

        // Code input
        JPanel codePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        codePanel.add(new JLabel("Code:"));
        codeField = new JTextField(10);
        // 6 haneli rastgele kod oluştur
        String randomCode = String.format("%06d", new Random().nextInt(999999));
        codeField.setText(randomCode);
        codePanel.add(codeField);
        mainPanel.add(codePanel);

        // Minutes input
        JPanel minutesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        minutesPanel.add(new JLabel("Minutes:"));
        minutesSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 60, 1));
        minutesPanel.add(minutesSpinner);
        mainPanel.add(minutesPanel);

        // Send button
        sendButton = new JButton("📤 Send Test Email");
        sendButton.setFont(new Font("Arial", Font.BOLD, 14));
        sendButton.setBackground(new Color(22, 105, 143));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.addActionListener(this::sendTestEmail);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(sendButton);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(buttonPanel);

        // Status label
        statusLabel = new JLabel("Ready to test...");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        mainPanel.add(statusLabel);

        // Response area
        JLabel responseLabel = new JLabel("📋 Response:");
        responseLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        mainPanel.add(responseLabel);

        responseArea = new JTextArea(10, 50);
        responseArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        responseArea.setEditable(false);
        responseArea.setBackground(new Color(248, 248, 248));
        responseArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(responseArea);
        scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
        mainPanel.add(scrollPane);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void setupUI() {
        // Instruction panel
        JPanel instructionPanel = new JPanel();
        instructionPanel.setBackground(new Color(230, 245, 255));
        instructionPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel instruction = new JLabel("<html><b>📋 Test Talimatları:</b><br>" +
                "1. Gerçek e-posta adresinizi girin<br>" +
                "2. Rastgele 6 haneli kod otomatik oluşturulur<br>" +
                "3. 'Test E-postası Gönder' butonuna tıklayın<br>" +
                "4. E-posta gelen kutunuzu kontrol edin (spam klasörü dahil)<br>" +
                "5. Kodlu e-postanın gelip gelmediğini doğrulayın</html>");
        instruction.setFont(new Font("Arial", Font.PLAIN, 12));
        instructionPanel.add(instruction);

        add(instructionPanel, BorderLayout.SOUTH);
    }

    private void sendTestEmail(ActionEvent e) {
        String email = emailField.getText().trim();
        String code = codeField.getText().trim();
        int minutes = (Integer) minutesSpinner.getValue();

        // Validation
        if (email.isEmpty()) {
            showError("Lütfen bir e-posta adresi girin!");
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            showError("Lütfen geçerli bir e-posta adresi girin!");
            return;
        }

        if (code.isEmpty()) {
            showError("Lütfen bir kod girin!");
            return;
        }

        // UI güncellemeleri
        sendButton.setEnabled(false);
        sendButton.setText("📤 Sending...");
        statusLabel.setText("🔄 Sending email...");
        statusLabel.setForeground(new Color(255, 140, 0)); // Orange
        responseArea.setText("Sending request...\n");

        // API isteğini arka planda gönder
        new Thread(() -> {
            try {
                sendEmailRequest(email, code, minutes);
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    showError("Request failed: " + ex.getMessage());
                    resetUI();
                });
            }
        }).start();
    }

    private void sendEmailRequest(String email, String code, int minutes) throws Exception {
        URL url = new URL("http://34.123.86.69/api/v1/deneme-test-code");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // Request ayarları
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(100000); //   timeout
        conn.setReadTimeout(80000); //  read timeout

        // JSON body oluştur
        String jsonInput = String.format(
                "{\"email\":\"%s\",\"code\":\"%s\",\"minutes\":%d}",
                email, code, minutes
        );

        SwingUtilities.invokeLater(() -> {
            responseArea.append("📤 Request URL: " + url.toString() + "\n");
            responseArea.append("📤 Request Body: " + jsonInput + "\n");
            responseArea.append("📤 Sending request...\n\n");
        });

        // Request gönder
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInput.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Response al
        int responseCode = conn.getResponseCode();

        SwingUtilities.invokeLater(() -> {
            responseArea.append("📥 Response Code: " + responseCode + "\n");
        });

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

            final String responseBody = response.toString();
            SwingUtilities.invokeLater(() -> {
                responseArea.append("📥 Response Body: " + responseBody + "\n\n");

                if ("true".equals(responseBody.trim())) {
                    showSuccess("✅ E-posta başarıyla gönderildi! Gelen kutunuzu kontrol edin (spam klasörü dahil).");
                    responseArea.append("✅ SUCCESS: Email should be sent to " + email + "\n");
                    responseArea.append("🔍 Expected code in email: " + code + "\n");
                    responseArea.append("⏰ Code valid for: " + minutes + " minutes\n");
                } else {
                    showWarning("⚠️ Unexpected response: " + responseBody);
                }
                resetUI();
            });

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

            final String errorBody = errorResponse.toString();
            SwingUtilities.invokeLater(() -> {
                responseArea.append("❌ Error Response: " + errorBody + "\n");
                showError("❌ Failed to send email. Response code: " + responseCode);
                resetUI();
            });
        }
    }

    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(new Color(0, 150, 0)); // Green
    }

    private void showWarning(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(new Color(255, 140, 0)); // Orange
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(new Color(220, 20, 20)); // Red
    }

    private void resetUI() {
        sendButton.setEnabled(true);
        sendButton.setText("📤 Send Test Email");

        // Yeni kod oluştur
        String newCode = String.format("%06d", new Random().nextInt(999999));
        codeField.setText(newCode);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Nimbus look and feel kullan (daha uyumlu)
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception e) {
                // Default look and feel kullan
            }

            new EmailTestController().setVisible(true);
        });
    }
}