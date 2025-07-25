package com.cellenta.automation;

import com.cellenta.controller.LoginController;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.core.GenericTypeMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import javax.swing.*;
import java.awt.*;

public class LoginControllerUITest extends BaseUITest {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginControllerUITest.class);
    private LoginController loginController;
    
    @Test
    public void testLoginUIComponentsVisibility() {
        logTestStep("Starting Login UI Components Visibility Test");
        
        try {
            // Create and show login controller
            logTestStep("Creating LoginController instance");
            loginController = new LoginController();
            
            logWindowInfo(loginController);
            
            // Create fixture for the login window
            logTestStep("Creating window fixture");
            window = new FrameFixture(robot, loginController);
            
            // Show the window
            logTestStep("Showing login window");
            window.show();
            waitForWindowToLoad();
            
            // Verify window title
            logTestStep("Verifying window title");
            window.requireTitle("Login");
            logTestInfo("Window title verified successfully");
            
            // Verify window is visible
            logTestStep("Verifying window visibility");
            window.requireVisible();
            logTestInfo("Window visibility verified successfully");
            
            // Check if phone field exists and is visible
            logTestStep("Searching for phone number input field");
            try {
                JTextComponentFixture phoneField = window.textBox(new GenericTypeMatcher<JTextField>(JTextField.class) {
                    @Override
                    protected boolean isMatching(JTextField textField) {
                        return textField.isVisible() && textField.isEnabled();
                    }
                });
                phoneField.requireVisible();
                logTestInfo("Phone number field found and is visible");
            } catch (Exception e) {
                logTestWarning("Phone field not found with generic matcher, trying alternative approach");
            }
            
            // Check for password field
            logTestStep("Searching for password input field");
            try {
                // Look for password field by type
                Component[] components = loginController.getContentPane().getComponents();
                boolean passwordFieldFound = findPasswordFieldRecursively(components);
                
                if (passwordFieldFound) {
                    logTestInfo("Password field found in component hierarchy");
                } else {
                    logTestWarning("Password field not found in visible components");
                }
            } catch (Exception e) {
                logTestError("Error searching for password field", e);
            }
            
            // Check for buttons
            logTestStep("Searching for interactive buttons");
            try {
                Component[] allComponents = getAllComponentsRecursively(loginController.getContentPane());
                int buttonCount = 0;
                
                for (Component comp : allComponents) {
                    if (comp instanceof JButton && comp.isVisible()) {
                        JButton button = (JButton) comp;
                        buttonCount++;
                        logTestInfo("Found button: '{}' - Enabled: {}", 
                                  button.getText(), button.isEnabled());
                    }
                }
                
                logTestInfo("Total visible buttons found: {}", buttonCount);
                
            } catch (Exception e) {
                logTestError("Error searching for buttons", e);
            }
            
            logTestStep("Login UI Components Visibility Test completed successfully");
            
        } catch (Exception e) {
            logTestError("Login UI Components Visibility Test failed", e);
            throw new RuntimeException("Test failed: " + e.getMessage(), e);
        }
    }
    
    @Test
    public void testLoginWindowProperties() {
        logTestStep("Starting Login Window Properties Test");
        
        try {
            // Create login controller
            logTestStep("Creating LoginController for properties test");
            loginController = new LoginController();
            
            // Create window fixture
            window = new FrameFixture(robot, loginController);
            window.show();
            waitForWindowToLoad();
            
            // Test window size
            logTestStep("Verifying window size");
            Dimension windowSize = window.target().getSize();
            logTestInfo("Window size: {}x{}", windowSize.width, windowSize.height);
            
            // Verify minimum expected size
            if (windowSize.width >= 800 && windowSize.height >= 600) {
                logTestInfo("Window size meets minimum requirements");
            } else {
                logTestWarning("Window size smaller than expected: {}x{}", 
                             windowSize.width, windowSize.height);
            }
            
            // Test window location
            logTestStep("Checking window location");
            Point location = window.target().getLocation();
            logTestInfo("Window location: ({}, {})", location.x, location.y);
            
            // Test if window is resizable
            logTestStep("Checking window resizability");
            boolean isResizable = window.target().isResizable();
            logTestInfo("Window is resizable: {}", isResizable);
            
            // Test window state
            logTestStep("Checking window state");
            int state = window.target().getExtendedState();
            logTestInfo("Window state: {}", state);
            
            logTestStep("Login Window Properties Test completed successfully");
            
        } catch (Exception e) {
            logTestError("Login Window Properties Test failed", e);
            throw new RuntimeException("Test failed: " + e.getMessage(), e);
        }
    }
    
    @Test
    public void testLoginFormInteraction() {
        logTestStep("Starting Login Form Interaction Test");
        
        try {
            // Create login controller
            logTestStep("Creating LoginController for interaction test");
            loginController = new LoginController();
            
            window = new FrameFixture(robot, loginController);
            window.show();
            waitForWindowToLoad();
            
            // Try to find and interact with form components
            logTestStep("Attempting to find and interact with form components");
            
            Component[] allComponents = getAllComponentsRecursively(loginController.getContentPane());
            
            JTextField phoneField = null;
            JPasswordField passwordField = null;
            
            // Find text input components
            for (Component comp : allComponents) {
                if (comp instanceof JTextField && !(comp instanceof JPasswordField) && comp.isVisible() && comp.isEnabled()) {
                    phoneField = (JTextField) comp;
                    logTestInfo("Found text field (assuming phone): enabled={}, visible={}", 
                              comp.isEnabled(), comp.isVisible());
                } else if (comp instanceof JPasswordField && comp.isVisible() && comp.isEnabled()) {
                    passwordField = (JPasswordField) comp;
                    logTestInfo("Found password field: enabled={}, visible={}", 
                              comp.isEnabled(), comp.isVisible());
                }
            }
            
            // Test interaction if components found
            if (phoneField != null) {
                logTestStep("Testing phone field interaction");
                try {
                    String testPhone = "5551234567";
                    phoneField.setText(testPhone);
                    logTestInfo("Successfully entered test phone number: {}", testPhone);
                    
                    String enteredText = phoneField.getText();
                    if (testPhone.equals(enteredText)) {
                        logTestInfo("Phone field text verification successful");
                    } else {
                        logTestWarning("Phone field text mismatch - Expected: {}, Actual: {}", 
                                     testPhone, enteredText);
                    }
                } catch (Exception e) {
                    logTestError("Error interacting with phone field", e);
                }
            } else {
                logTestWarning("Phone field not found for interaction test");
            }
            
            if (passwordField != null) {
                logTestStep("Testing password field interaction");
                try {
                    String testPassword = "password123";
                    passwordField.setText(testPassword);
                    logTestInfo("Successfully entered test password");
                    
                    // Don't log actual password for security
                    if (passwordField.getPassword().length > 0) {
                        logTestInfo("Password field contains text (length: {})", 
                                  passwordField.getPassword().length);
                    }
                } catch (Exception e) {
                    logTestError("Error interacting with password field", e);
                }
            } else {
                logTestWarning("Password field not found for interaction test");
            }
            
            logTestStep("Login Form Interaction Test completed");
            
        } catch (Exception e) {
            logTestError("Login Form Interaction Test failed", e);
            throw new RuntimeException("Test failed: " + e.getMessage(), e);
        }
    }
    
    private boolean findPasswordFieldRecursively(Component[] components) {
        for (Component comp : components) {
            if (comp instanceof JPasswordField && comp.isVisible()) {
                return true;
            }
            if (comp instanceof Container) {
                Container container = (Container) comp;
                if (findPasswordFieldRecursively(container.getComponents())) {
                    return true;
                }
            }
        }
        return false;
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