package org.example;

import org.junit.jupiter.api.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.ConsoleHandler;
import java.util.logging.SimpleFormatter;

import static org.junit.jupiter.api.Assertions.*;

public class ChatWindowTest {

    private static final Logger logger = Logger.getLogger(ChatWindowTest.class.getName());
    private ChatWindow chatWindow;
    private JTextArea messageField;
    private JButton sendButton;
    private JPanel messagePanel;

    @BeforeAll
    static void setupLogger() {
        // Configure logger
        logger.setLevel(Level.ALL);
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        consoleHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(consoleHandler);
        logger.setUseParentHandlers(false);
    }

    @BeforeEach
    void setUp() throws Exception {
        logger.info("Setting up test environment");

        // Create UI components on EDT
        SwingUtilities.invokeAndWait(() -> {
            chatWindow = new ChatWindow();
            chatWindow.setVisible(true);

            // Find components by name
            messageField = (JTextArea) findComponentByName(chatWindow, "messageField");
            sendButton = (JButton) findComponentByName(chatWindow, "gonderBtn");
            messagePanel = (JPanel) findComponentByName(chatWindow, "messagePanel");

            logger.info("ChatWindow created and components found");
        });

        // Allow UI to stabilize
        Thread.sleep(100);
    }

    @AfterEach
    void tearDown() throws Exception {
        logger.info("Tearing down test environment");
        SwingUtilities.invokeAndWait(() -> {
            if (chatWindow != null) {
                chatWindow.dispose();
            }
        });
    }

    @Test
    @DisplayName("Test UI components load correctly")
    void testUIComponentsLoadCorrectly() throws Exception {
        logger.info("Testing UI components loading");

        SwingUtilities.invokeAndWait(() -> {
            assertNotNull(messageField, "Message field should not be null");
            assertNotNull(sendButton, "Send button should not be null");
            assertNotNull(messagePanel, "Message panel should not be null");

            assertEquals("Gönder", sendButton.getText(), "Send button text should be 'Gönder'");
            assertTrue(chatWindow.isVisible(), "Chat window should be visible");

            logger.info("All UI components loaded successfully");
        });
    }

    @Test
    @DisplayName("Test sending valid message with 'kalan' keyword")
    void testSendingValidMessage() throws Exception {
        logger.info("Testing sending valid message with 'kalan' keyword");

        CountDownLatch latch = new CountDownLatch(1);

        SwingUtilities.invokeLater(() -> {
            // Type message
            messageField.setText("5321234567 kalan");
            logger.info("Typed message: '5321234567 kalan'");

            // Click send button
            sendButton.doClick();
            logger.info("Clicked send button");

            // Check if message field is cleared
            assertEquals("", messageField.getText(), "Message field should be cleared after sending");

            // Check if user message is added
            Component[] components = messagePanel.getComponents();
            assertTrue(components.length > 0, "Message panel should have at least one message");

            MessageBubble userBubble = findMessageBubble(components, true);
            assertNotNull(userBubble, "User message bubble should exist");
            assertEquals("5321234567 kalan", userBubble.getText(), "User message should match input");

            logger.info("User message added successfully");

            // Wait for response (1 second timer)
            Timer responseTimer = new Timer(1500, e -> {
                try {
                    Component[] updatedComponents = messagePanel.getComponents();
                    MessageBubble responseBubble = findMessageBubble(updatedComponents, false);

                    assertNotNull(responseBubble, "Response message bubble should exist");
                    String responseText = responseBubble.getText();

                    // Log the actual response for debugging
                    logger.info("Actual response: " + responseText);

                    // Check for any of the possible responses
                    boolean isValidResponse =
                            responseText.contains("Lütfen numarayı doğru formatta giriniz") ||
                                    responseText.contains("Tarife Bilgileri") ||
                                    responseText.contains("Numara sisteme kayıtlı değil") ||
                                    responseText.contains("Bir hata oluştu") ||
                                    responseText.contains("Tarife bilgileri eksik");

                    assertTrue(isValidResponse,
                            "Response should contain a valid message. Actual: " + responseText);

                    logger.info("Response received successfully");
                } catch (Exception ex) {
                    logger.severe("Error in response timer: " + ex.getMessage());
                    ex.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
            responseTimer.setRepeats(false);
            responseTimer.start();
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS), "Response should be received within 3 seconds");
    }

    @Test
    @DisplayName("Test blocking empty message submission")
    void testBlockingEmptyMessage() throws Exception {
        logger.info("Testing blocking empty message submission");

        SwingUtilities.invokeAndWait(() -> {
            // Set empty message
            messageField.setText("");
            int initialMessageCount = messagePanel.getComponentCount();

            // Click send button
            sendButton.doClick();
            logger.info("Clicked send button with empty message");

            // Check that no message was added
            assertEquals(initialMessageCount, messagePanel.getComponentCount(),
                    "No message should be added for empty input");

            logger.info("Empty message successfully blocked");
        });
    }

    @Test
    @DisplayName("Test error message for invalid format")
    void testInvalidMessageFormat() throws Exception {
        logger.info("Testing error message for invalid format");

        CountDownLatch latch = new CountDownLatch(1);

        SwingUtilities.invokeLater(() -> {
            // Send message without 'kalan' keyword
            messageField.setText("5321234567");
            logger.info("Typed message without 'kalan': '5321234567'");

            sendButton.doClick();
            logger.info("Clicked send button");

            // Wait for error response
            Timer responseTimer = new Timer(1500, e -> {
                try {
                    Component[] components = messagePanel.getComponents();
                    MessageBubble errorBubble = findMessageBubble(components, false);

                    assertNotNull(errorBubble, "Error message bubble should exist");
                    assertEquals("Geçersiz komut. Numara ile birlikte 'kalan' yazmalısınız.",
                            errorBubble.getText(), "Error message should match expected text");

                    logger.info("Error message displayed correctly");
                } catch (Exception ex) {
                    logger.severe("Error in response timer: " + ex.getMessage());
                } finally {
                    latch.countDown();
                }
            });
            responseTimer.setRepeats(false);
            responseTimer.start();
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS), "Error response should be received within 3 seconds");
    }

    @Test
    @DisplayName("Test warning for short phone number")
    void testShortPhoneNumberWarning() throws Exception {
        logger.info("Testing warning for short phone number");

        CountDownLatch latch = new CountDownLatch(1);

        SwingUtilities.invokeLater(() -> {
            // Send message with short number
            messageField.setText("532 kalan");
            logger.info("Typed message with short number: '532 kalan'");

            sendButton.doClick();
            logger.info("Clicked send button");

            // Wait for warning response
            Timer responseTimer = new Timer(1500, e -> {
                try {
                    Component[] components = messagePanel.getComponents();
                    MessageBubble warningBubble = findMessageBubble(components, false);

                    assertNotNull(warningBubble, "Warning message bubble should exist");
                    assertTrue(warningBubble.getText().contains("Lütfen numarayı doğru formatta giriniz"),
                            "Warning message should contain format instruction");

                    logger.info("Warning message displayed correctly");
                } catch (Exception ex) {
                    logger.severe("Error in response timer: " + ex.getMessage());
                } finally {
                    latch.countDown();
                }
            });
            responseTimer.setRepeats(false);
            responseTimer.start();
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS), "Warning response should be received within 3 seconds");
    }

    @Test
    @DisplayName("Test message panel scrolling")
    void testMessagePanelScrolling() throws Exception {
        logger.info("Testing message panel scrolling functionality");

        SwingUtilities.invokeAndWait(() -> {
            // Add multiple messages to trigger scrolling
            for (int i = 1; i <= 10; i++) {
                chatWindow.addMessage("Test message " + i, i % 2 == 0);
                logger.info("Added message " + i);
            }

            // Verify messages were added
            Component[] components = messagePanel.getComponents();
            int messageCount = 0;
            for (Component comp : components) {
                if (comp instanceof MessageBubble) {
                    messageCount++;
                }
            }

            assertEquals(10, messageCount, "Should have 10 message bubbles");
            logger.info("All messages added successfully, scrolling should be active");
        });
    }

    @Test
    @DisplayName("Test window properties")
    void testWindowProperties() throws Exception {
        logger.info("Testing window properties");

        SwingUtilities.invokeAndWait(() -> {
            assertEquals("Cellenta", chatWindow.getTitle(), "Window title should be 'Cellenta'");
            assertEquals(400, chatWindow.getWidth(), "Window width should be 400");
            assertEquals(700, chatWindow.getHeight(), "Window height should be 700");
            assertEquals(JFrame.EXIT_ON_CLOSE, chatWindow.getDefaultCloseOperation(),
                    "Default close operation should be EXIT_ON_CLOSE");

            logger.info("Window properties verified successfully");
        });
    }

    @Test
    @DisplayName("Test valid phone number with 'kalan' keyword")
    void testValidPhoneNumberWithKalan() throws Exception {
        logger.info("Testing valid phone number with 'kalan' keyword");

        CountDownLatch latch = new CountDownLatch(1);

        SwingUtilities.invokeLater(() -> {
            // Type a valid 10-digit number with kalan
            messageField.setText("5551234567 kalan");
            logger.info("Typed valid message: '5551234567 kalan'");

            sendButton.doClick();
            logger.info("Clicked send button");

            // Wait for response
            Timer responseTimer = new Timer(1500, e -> {
                try {
                    Component[] components = messagePanel.getComponents();

                    // Check user message was added
                    MessageBubble userBubble = findMessageBubble(components, true);
                    assertNotNull(userBubble, "User message should be added");
                    assertEquals("5551234567 kalan", userBubble.getText());

                    // Check response was added
                    MessageBubble responseBubble = findMessageBubble(components, false);
                    assertNotNull(responseBubble, "Response message should be added");

                    logger.info("Valid phone number processed successfully");
                } catch (Exception ex) {
                    logger.severe("Error in response timer: " + ex.getMessage());
                } finally {
                    latch.countDown();
                }
            });
            responseTimer.setRepeats(false);
            responseTimer.start();
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS), "Response should be received within 3 seconds");
    }

    // Helper method to find component by name
    private Component findComponentByName(Container container, String name) {
        for (Component component : container.getComponents()) {
            if (name.equals(component.getName())) {
                return component;
            }
            if (component instanceof Container) {
                Component found = findComponentByName((Container) component, name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    // Helper method to find message bubble
    private MessageBubble findMessageBubble(Component[] components, boolean isSender) {
        for (int i = components.length - 1; i >= 0; i--) {
            if (components[i] instanceof MessageBubble) {
                MessageBubble bubble = (MessageBubble) components[i];
                if (bubble.isSentByUser() == isSender) {
                    return bubble;
                }
            }
        }
        return null;
    }
}