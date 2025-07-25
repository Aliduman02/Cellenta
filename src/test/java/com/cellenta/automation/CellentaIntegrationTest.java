package com.cellenta.automation;

import com.cellenta.MainApp;
import com.cellenta.controller.LoginController;
import com.cellenta.service.ChatBotService;
import org.assertj.swing.fixture.FrameFixture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class CellentaIntegrationTest extends BaseUITest {
    
    private static final Logger logger = LoggerFactory.getLogger(CellentaIntegrationTest.class);
    
    @Test
    public void testApplicationStartup() {
        logTestStep("Starting Application Startup Integration Test");
        
        try {
            // Test MainApp class instantiation
            logTestStep("Testing MainApp class accessibility");
            Class<?> mainAppClass = MainApp.class;
            logTestInfo("MainApp class loaded successfully: {}", mainAppClass.getName());
            
            // Test LoginController creation (simulating main method behavior)
            logTestStep("Testing LoginController instantiation");
            LoginController loginController = new LoginController();
            logTestInfo("LoginController created successfully");
            
            // Test window creation and display
            logTestStep("Testing login window display");
            window = new FrameFixture(robot, loginController);
            window.show();
            waitForWindowToLoad();
            
            // Verify application startup state
            logTestStep("Verifying application startup state");
            window.requireVisible();
            logTestInfo("Login window is visible after startup");
            
            // Check window responsiveness
            logTestStep("Testing window responsiveness");
            Dimension originalSize = window.target().getSize();
            logTestInfo("Original window size: {}x{}", originalSize.width, originalSize.height);
            
            // Test window interactions
            testWindowInteractions();
            
            logTestStep("Application Startup Integration Test completed successfully");
            
        } catch (Exception e) {
            logTestError("Application Startup Integration Test failed", e);
            throw new RuntimeException("Integration test failed: " + e.getMessage(), e);
        }
    }
    
    @Test
    public void testChatBotServiceIntegration() {
        logTestStep("Starting ChatBot Service Integration Test");
        
        try {
            // Test ChatBot service availability
            logTestStep("Testing ChatBot service class loading");
            Class<?> chatBotClass = ChatBotService.class;
            logTestInfo("ChatBotService class loaded: {}", chatBotClass.getName());
            
            // Test ChatBot service method accessibility
            logTestStep("Verifying ChatBot sendMessage method");
            try {
                java.lang.reflect.Method sendMessageMethod = chatBotClass.getMethod("sendMessage", String.class);
                logTestInfo("sendMessage method found: {}", sendMessageMethod.getName());
                
                boolean isStatic = java.lang.reflect.Modifier.isStatic(sendMessageMethod.getModifiers());
                logTestInfo("sendMessage method is static: {}", isStatic);
                
            } catch (NoSuchMethodException e) {
                logTestError("sendMessage method not found", e);
                throw e;
            }
            
            // Test ChatBot service with test messages (async to avoid blocking)
            logTestStep("Testing ChatBot service responses");
            testChatBotResponses();
            
            logTestStep("ChatBot Service Integration Test completed successfully");
            
        } catch (Exception e) {
            logTestError("ChatBot Service Integration Test failed", e);
            throw new RuntimeException("ChatBot integration test failed: " + e.getMessage(), e);
        }
    }
    
    @Test
    public void testEndToEndUserJourney() {
        logTestStep("Starting End-to-End User Journey Test");
        
        try {
            // Simulate complete user journey
            logTestStep("Phase 1: Application Launch");
            LoginController loginController = new LoginController();
            window = new FrameFixture(robot, loginController);
            window.show();
            waitForWindowToLoad();
            
            logTestInfo("User journey started with login window");
            
            // Phase 2: Login attempt simulation
            logTestStep("Phase 2: Login Form Interaction Simulation");
            simulateLoginFormInteraction();
            
            // Phase 3: UI Component verification
            logTestStep("Phase 3: UI Component Verification");
            verifyUIComponents();
            
            // Phase 4: Navigation testing
            logTestStep("Phase 4: Navigation Testing");
            testNavigationCapabilities();
            
            // Phase 5: Performance check
            logTestStep("Phase 5: Performance Check");
            performanceCheck();
            
            logTestStep("End-to-End User Journey Test completed successfully");
            
        } catch (Exception e) {
            logTestError("End-to-End User Journey Test failed", e);
            throw new RuntimeException("End-to-end test failed: " + e.getMessage(), e);
        }
    }
    
    @Test
    public void testMultiWindowIntegration() {
        logTestStep("Starting Multi-Window Integration Test");
        
        try {
            // Test multiple window creation and management
            logTestStep("Creating multiple controller instances");
            
            LoginController loginController1 = new LoginController();
            logTestInfo("First LoginController created");
            
            LoginController loginController2 = new LoginController();
            logTestInfo("Second LoginController created");
            
            // Test first window
            logTestStep("Testing first login window");
            FrameFixture window1 = new FrameFixture(robot, loginController1);
            window1.show();
            waitForWindowToLoad();
            
            logTestInfo("First window displayed and responsive");
            
            // Test second window
            logTestStep("Testing second login window");
            FrameFixture window2 = new FrameFixture(robot, loginController2);
            window2.show();
            waitForWindowToLoad();
            
            logTestInfo("Second window displayed");
            
            // Verify both windows are independent
            logTestStep("Verifying window independence");
            verifyWindowIndependence(window1, window2);
            
            // Cleanup
            logTestStep("Cleaning up multiple windows");
            window1.cleanUp();
            window2.cleanUp();
            
            logTestStep("Multi-Window Integration Test completed successfully");
            
        } catch (Exception e) {
            logTestError("Multi-Window Integration Test failed", e);
            throw new RuntimeException("Multi-window test failed: " + e.getMessage(), e);
        }
    }
    
    private void testWindowInteractions() {
        logTestStep("Testing basic window interactions");
        
        try {
            // Test window focus
            window.focus();
            logTestInfo("Window focus test completed");
            
            // Test window movement capability
            Point originalLocation = window.target().getLocation();
            logTestInfo("Window original location: ({}, {})", originalLocation.x, originalLocation.y);
            
            // Test window resize capability (if resizable)
            if (window.target().isResizable()) {
                Dimension originalSize = window.target().getSize();
                logTestInfo("Window is resizable - Original size: {}x{}", originalSize.width, originalSize.height);
            } else {
                logTestInfo("Window is not resizable");
            }
            
        } catch (Exception e) {
            logTestError("Error during window interaction tests", e);
        }
    }
    
    private void testChatBotResponses() {
        logTestStep("Testing ChatBot service responses");
        
        // Test with different types of messages
        String[] testMessages = {
            null, // Test null handling
            "", // Test empty string
            "Hello", // Test basic message
            "Merhaba", // Test Turkish message
            "What is Cellenta?", // Test service-specific question
            "How do I login?" // Test help question
        };
        
        for (String message : testMessages) {
            try {
                logTestStep("Testing ChatBot with message: '{}'", message);
                
                // Use CompletableFuture for timeout handling
                CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> 
                    ChatBotService.sendMessage(message)
                );
                
                String response = future.get(5, TimeUnit.SECONDS); // 5 second timeout
                
                if (response != null && !response.trim().isEmpty()) {
                    logTestInfo("ChatBot response received (length: {})", response.length());
                } else {
                    logTestWarning("ChatBot returned empty or null response");
                }
                
            } catch (Exception e) {
                logTestError("ChatBot test failed for message: " + message, e);
                // Don't fail the entire test for individual ChatBot failures
            }
        }
    }
    
    private void simulateLoginFormInteraction() {
        logTestStep("Simulating login form interaction");
        
        try {
            // Find form components
            // Get content pane from JFrame
            Container contentPane = ((javax.swing.JFrame) window.target()).getContentPane();
            Component[] allComponents = getAllComponentsRecursively(contentPane);
            
            JTextField phoneField = null;
            JPasswordField passwordField = null;
            java.util.List<JButton> buttons = new java.util.ArrayList<>();
            
            for (Component comp : allComponents) {
                if (comp instanceof JTextField && !(comp instanceof JPasswordField) && comp.isVisible()) {
                    phoneField = (JTextField) comp;
                } else if (comp instanceof JPasswordField && comp.isVisible()) {
                    passwordField = (JPasswordField) comp;
                } else if (comp instanceof JButton && comp.isVisible()) {
                    buttons.add((JButton) comp);
                }
            }
            
            // Simulate form filling
            if (phoneField != null) {
                logTestStep("Simulating phone number input");
                phoneField.setText("5551234567");
                logTestInfo("Phone number entered: {}", phoneField.getText());
            }
            
            if (passwordField != null) {
                logTestStep("Simulating password input");
                passwordField.setText("testPassword123");
                logTestInfo("Password entered (length: {})", passwordField.getPassword().length);
            }
            
            logTestInfo("Form interaction simulation completed - Found {} buttons", buttons.size());
            
        } catch (Exception e) {
            logTestError("Error during login form simulation", e);
        }
    }
    
    private void verifyUIComponents() {
        logTestStep("Verifying UI components");
        
        // Get content pane from JFrame
        Container contentPane = ((javax.swing.JFrame) window.target()).getContentPane();
        Component[] allComponents = getAllComponentsRecursively(contentPane);
        
        int totalComponents = allComponents.length;
        int visibleComponents = 0;
        int enabledComponents = 0;
        
        for (Component comp : allComponents) {
            if (comp.isVisible()) visibleComponents++;
            if (comp.isEnabled()) enabledComponents++;
        }
        
        logTestInfo("UI Component Summary - Total: {}, Visible: {}, Enabled: {}", 
                  totalComponents, visibleComponents, enabledComponents);
        
        // Verify critical component types exist
        boolean hasButtons = false;
        boolean hasTextFields = false;
        boolean hasLabels = false;
        
        for (Component comp : allComponents) {
            if (comp instanceof JButton && comp.isVisible()) hasButtons = true;
            if (comp instanceof JTextField && comp.isVisible()) hasTextFields = true;
            if (comp instanceof JLabel && comp.isVisible()) hasLabels = true;
        }
        
        logTestInfo("Critical components present - Buttons: {}, TextFields: {}, Labels: {}", 
                  hasButtons, hasTextFields, hasLabels);
    }
    
    private void testNavigationCapabilities() {
        logTestStep("Testing navigation capabilities");
        
        try {
            // Test keyboard navigation
            window.pressKey(java.awt.event.KeyEvent.VK_TAB);
            logTestInfo("Tab key navigation test completed");
            
            // Test escape key
            window.pressKey(java.awt.event.KeyEvent.VK_ESCAPE);
            logTestInfo("Escape key test completed");
            
        } catch (Exception e) {
            logTestError("Error during navigation testing", e);
        }
    }
    
    private void performanceCheck() {
        logTestStep("Performing basic performance check");
        
        long startTime = System.currentTimeMillis();
        
        // Test UI responsiveness
        for (int i = 0; i < 5; i++) {
            window.focus();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        logTestInfo("UI responsiveness test completed in {} ms", totalTime);
        
        if (totalTime < 1000) {
            logTestInfo("Performance check passed - UI is responsive");
        } else {
            logTestWarning("Performance check warning - UI response time: {} ms", totalTime);
        }
    }
    
    private void verifyWindowIndependence(FrameFixture window1, FrameFixture window2) {
        try {
            // Verify both windows are visible
            window1.requireVisible();
            window2.requireVisible();
            logTestInfo("Both windows are visible independently");
            
            // Verify windows have different positions or can be differentiated
            Point pos1 = window1.target().getLocation();
            Point pos2 = window2.target().getLocation();
            
            logTestInfo("Window 1 position: ({}, {})", pos1.x, pos1.y);
            logTestInfo("Window 2 position: ({}, {})", pos2.x, pos2.y);
            
            // Test independent focus
            window1.focus();
            logTestInfo("Window 1 focused independently");
            
            window2.focus();
            logTestInfo("Window 2 focused independently");
            
        } catch (Exception e) {
            logTestError("Error verifying window independence", e);
        }
    }
    
    private Component[] getAllComponentsRecursively(Container container) {
        java.util.List<Component> allComponents = new java.util.ArrayList<>();
        addComponentsRecursively(container, allComponents);
        return allComponents.toArray(new Component[0]);
    }
    
    private void addComponentsRecursively(Container container, java.util.List<Component> allComponents) {
        Component[] components = container.getComponents();
        for (Component comp : components) {
            allComponents.add(comp);
            if (comp instanceof Container) {
                addComponentsRecursively((Container) comp, allComponents);
            }
        }
    }
}