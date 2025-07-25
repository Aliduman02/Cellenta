package com.cellenta.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LoginController Test Component")
public class LoginControllerTest {

    private LoginController loginController;

    @BeforeEach
    void setUp() {
        // Note: GUI tests might require headless mode
        System.setProperty("java.awt.headless", "true");
    }

    @AfterEach
    void tearDown() {
        if (loginController != null) {
            loginController.dispose();
        }
    }

    @Test
    @DisplayName("Login Controller Class Exists Test")
    void testLoginControllerClassExists() {
        assertNotNull(LoginController.class);
    }

    @Test
    @DisplayName("Login Controller Inherits From JFrame Test")
    void testLoginControllerInheritsFromJFrame() {
        assertTrue(javax.swing.JFrame.class.isAssignableFrom(LoginController.class));
    }

    @Test
    @DisplayName("Login Controller Has Constructor Test")
    void testLoginControllerHasConstructor() throws NoSuchMethodException {
        assertNotNull(LoginController.class.getConstructor());
    }
}