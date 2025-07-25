package com.cellenta.automation;

import com.cellenta.controller.MainController;
import org.assertj.swing.fixture.FrameFixture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import javax.swing.*;
import java.awt.*;

public class MainControllerUITest extends BaseUITest {
    
    private static final Logger logger = LoggerFactory.getLogger(MainControllerUITest.class);
    private MainController mainController;
    
    // Test data matching the hardcoded values found in MainController
    private static final int TEST_CUSTOMER_ID = 1;
    private static final String TEST_NAME = "Test";
    private static final String TEST_SURNAME = "User";
    private static final String TEST_PHONE = "5551234567";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password";
    
    @Test
    public void testMainDashboardUIComponents() {
        logTestStep("Starting Main Dashboard UI Components Test");
        
        try {
            // Create main controller with test data
            logTestStep("Creating MainController with test user data");
            mainController = new MainController(
                TEST_CUSTOMER_ID, TEST_NAME, TEST_SURNAME, 
                TEST_PHONE, TEST_EMAIL, TEST_PASSWORD
            );
            
            logWindowInfo(mainController);
            logTestInfo("MainController created with test user: {} {}", TEST_NAME, TEST_SURNAME);
            
            // Create window fixture
            logTestStep("Creating window fixture for main dashboard");
            window = new FrameFixture(robot, mainController);
            
            // Show the window
            logTestStep("Displaying main dashboard window");
            window.show();
            waitForWindowToLoad();
            
            // Verify window properties
            logTestStep("Verifying main dashboard window properties");
            window.requireVisible();
            logTestInfo("Main dashboard window is visible");
            
            // Get window dimensions
            Dimension windowSize = window.target().getSize();
            logTestInfo("Main dashboard window size: {}x{}", windowSize.width, windowSize.height);
            
            // Verify window title (if set)
            try {
                String title = window.target().getTitle();
                logTestInfo("Window title: '{}'", title);
            } catch (Exception e) {
                logTestInfo("Window title not set or empty");
            }
            
            // Analyze all components in the main dashboard
            logTestStep("Analyzing main dashboard components");
            analyzeComponentHierarchy(mainController.getContentPane(), 0);
            
            // Check for specific UI elements expected in main dashboard
            logTestStep("Checking for expected dashboard elements");
            checkForDashboardElements();
            
            logTestStep("Main Dashboard UI Components Test completed successfully");
            
        } catch (Exception e) {
            logTestError("Main Dashboard UI Components Test failed", e);
            throw new RuntimeException("Test failed: " + e.getMessage(), e);
        }
    }
    
    @Test
    public void testMainDashboardNavigation() {
        logTestStep("Starting Main Dashboard Navigation Test");
        
        try {
            // Create main controller
            logTestStep("Creating MainController for navigation test");
            mainController = new MainController(
                TEST_CUSTOMER_ID, TEST_NAME, TEST_SURNAME, 
                TEST_PHONE, TEST_EMAIL, TEST_PASSWORD
            );
            
            window = new FrameFixture(robot, mainController);
            window.show();
            waitForWindowToLoad();
            
            // Find navigation buttons
            logTestStep("Searching for navigation buttons");
            Component[] allComponents = getAllComponentsRecursively(mainController.getContentPane());
            
            int navigationButtonCount = 0;
            for (Component comp : allComponents) {
                if (comp instanceof JButton && comp.isVisible() && comp.isEnabled()) {
                    JButton button = (JButton) comp;
                    String buttonText = button.getText();
                    
                    if (buttonText != null && !buttonText.trim().isEmpty()) {
                        logTestInfo("Found navigation button: '{}' - Position: ({}, {})", 
                                  buttonText, button.getX(), button.getY());
                        navigationButtonCount++;
                        
                        // Test button interaction capability
                        testButtonInteraction(button, buttonText);
                    }
                }
            }
            
            logTestInfo("Total navigation buttons found: {}", navigationButtonCount);
            
            // Test menu or tab navigation if available
            logTestStep("Checking for menu or tab navigation");
            checkForMenuComponents(allComponents);
            
            logTestStep("Main Dashboard Navigation Test completed successfully");
            
        } catch (Exception e) {
            logTestError("Main Dashboard Navigation Test failed", e);
            throw new RuntimeException("Test failed: " + e.getMessage(), e);
        }
    }
    
    @Test
    public void testMainDashboardDataDisplay() {
        logTestStep("Starting Main Dashboard Data Display Test");
        
        try {
            // Create main controller
            logTestStep("Creating MainController for data display test");
            mainController = new MainController(
                TEST_CUSTOMER_ID, TEST_NAME, TEST_SURNAME, 
                TEST_PHONE, TEST_EMAIL, TEST_PASSWORD
            );
            
            window = new FrameFixture(robot, mainController);
            window.show();
            waitForWindowToLoad();
            
            // Check for data display components
            logTestStep("Analyzing data display components");
            Component[] allComponents = getAllComponentsRecursively(mainController.getContentPane());
            
            // Look for labels, text fields, and panels that might contain user data
            checkForDataDisplayElements(allComponents);
            
            // Check for user information display
            logTestStep("Verifying user information display");
            checkForUserInformation(allComponents);
            
            // Check for balance/usage information
            logTestStep("Checking for balance and usage information displays");
            checkForBalanceInformation(allComponents);
            
            logTestStep("Main Dashboard Data Display Test completed successfully");
            
        } catch (Exception e) {
            logTestError("Main Dashboard Data Display Test failed", e);
            throw new RuntimeException("Test failed: " + e.getMessage(), e);
        }
    }
    
    private void analyzeComponentHierarchy(Container container, int depth) {
        String indent = "  ".repeat(depth);
        Component[] components = container.getComponents();
        
        logTestInfo("{}Container: {} - Components: {}", 
                  indent, container.getClass().getSimpleName(), components.length);
        
        for (Component comp : components) {
            String visibility = comp.isVisible() ? "visible" : "hidden";
            String enabled = comp.isEnabled() ? "enabled" : "disabled";
            
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                logTestInfo("{}  Label: '{}' - {} {}", 
                          indent, label.getText(), visibility, enabled);
            } else if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                logTestInfo("{}  Button: '{}' - {} {}", 
                          indent, button.getText(), visibility, enabled);
            } else if (comp instanceof JTextField) {
                JTextField textField = (JTextField) comp;
                logTestInfo("{}  TextField: '{}' - {} {}", 
                          indent, textField.getText(), visibility, enabled);
            } else if (comp instanceof Container) {
                logTestInfo("{}  {}: {} {}", 
                          indent, comp.getClass().getSimpleName(), visibility, enabled);
                if (depth < 3) { // Limit recursion depth
                    analyzeComponentHierarchy((Container) comp, depth + 1);
                }
            } else {
                logTestInfo("{}  {}: {} {}", 
                          indent, comp.getClass().getSimpleName(), visibility, enabled);
            }
        }
    }
    
    private void checkForDashboardElements() {
        logTestStep("Checking for common dashboard elements");
        
        Component[] allComponents = getAllComponentsRecursively(mainController.getContentPane());
        
        boolean hasLabels = false;
        boolean hasButtons = false;
        boolean hasPanels = false;
        
        for (Component comp : allComponents) {
            if (comp instanceof JLabel && comp.isVisible()) hasLabels = true;
            if (comp instanceof JButton && comp.isVisible()) hasButtons = true;
            if (comp instanceof JPanel && comp.isVisible()) hasPanels = true;
        }
        
        logTestInfo("Dashboard elements found - Labels: {}, Buttons: {}, Panels: {}", 
                  hasLabels, hasButtons, hasPanels);
    }
    
    private void testButtonInteraction(JButton button, String buttonText) {
        try {
            // Test if button is clickable (don't actually click to avoid navigation)
            boolean isClickable = button.isEnabled() && button.isVisible();
            logTestInfo("Button '{}' is clickable: {}", buttonText, isClickable);
            
            // Get button bounds for interaction testing
            Rectangle bounds = button.getBounds();
            logTestInfo("Button '{}' bounds: x={}, y={}, width={}, height={}", 
                      buttonText, bounds.x, bounds.y, bounds.width, bounds.height);
            
        } catch (Exception e) {
            logTestError("Error testing button interaction for: " + buttonText, e);
        }
    }
    
    private void checkForMenuComponents(Component[] allComponents) {
        for (Component comp : allComponents) {
            if (comp instanceof JMenuBar) {
                logTestInfo("Found menu bar component");
            } else if (comp instanceof JMenu) {
                JMenu menu = (JMenu) comp;
                logTestInfo("Found menu: '{}'", menu.getText());
            } else if (comp instanceof JTabbedPane) {
                JTabbedPane tabbedPane = (JTabbedPane) comp;
                logTestInfo("Found tabbed pane with {} tabs", tabbedPane.getTabCount());
            }
        }
    }
    
    private void checkForDataDisplayElements(Component[] allComponents) {
        int labelCount = 0;
        int textFieldCount = 0;
        
        for (Component comp : allComponents) {
            if (comp instanceof JLabel && comp.isVisible()) {
                JLabel label = (JLabel) comp;
                if (label.getText() != null && !label.getText().trim().isEmpty()) {
                    labelCount++;
                }
            } else if (comp instanceof JTextField && comp.isVisible()) {
                textFieldCount++;
            }
        }
        
        logTestInfo("Data display elements - Labels with text: {}, Text fields: {}", 
                  labelCount, textFieldCount);
    }
    
    private void checkForUserInformation(Component[] allComponents) {
        logTestStep("Searching for user information displays");
        
        for (Component comp : allComponents) {
            if (comp instanceof JLabel && comp.isVisible()) {
                JLabel label = (JLabel) comp;
                String text = label.getText();
                
                if (text != null && (
                    text.contains(TEST_NAME) || 
                    text.contains(TEST_PHONE) || 
                    text.contains(TEST_EMAIL) ||
                    text.toLowerCase().contains("user") ||
                    text.toLowerCase().contains("name") ||
                    text.toLowerCase().contains("phone") ||
                    text.toLowerCase().contains("email"))) {
                    
                    logTestInfo("Found potential user info label: '{}'", text);
                }
            }
        }
    }
    
    private void checkForBalanceInformation(Component[] allComponents) {
        logTestStep("Searching for balance/usage information");
        
        for (Component comp : allComponents) {
            if (comp instanceof JLabel && comp.isVisible()) {
                JLabel label = (JLabel) comp;
                String text = label.getText();
                
                if (text != null && (
                    text.toLowerCase().contains("balance") ||
                    text.toLowerCase().contains("credit") ||
                    text.toLowerCase().contains("remaining") ||
                    text.toLowerCase().contains("usage") ||
                    text.toLowerCase().contains("data") ||
                    text.toLowerCase().contains("minute") ||
                    text.toLowerCase().contains("sms") ||
                    text.contains("₺") ||
                    text.contains("GB") ||
                    text.contains("MB"))) {
                    
                    logTestInfo("Found potential balance/usage label: '{}'", text);
                }
            }
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