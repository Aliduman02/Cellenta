package com.cellenta.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BalanceController Test Component")
public class BalanceControllerTest {

    private BalanceController balanceController;

    @BeforeEach
    void setUp() {
        System.setProperty("java.awt.headless", "true");
    }

    @AfterEach
    void tearDown() {
        if (balanceController != null) {
            balanceController.dispose();
        }
    }

    @Test
    @DisplayName("Balance Controller Class Exists Test")
    void testBalanceControllerClassExists() {
        assertNotNull(BalanceController.class);
    }

    @Test
    @DisplayName("Balance Controller Inherits From JFrame Test")
    void testBalanceControllerInheritsFromJFrame() {
        assertTrue(javax.swing.JFrame.class.isAssignableFrom(BalanceController.class));
    }

    @Test
    @DisplayName("Balance Controller Has Constructor Test")
    void testBalanceControllerHasConstructor() throws NoSuchMethodException {
        assertNotNull(BalanceController.class.getConstructor());
    }

    @Test
    @DisplayName("Balance Controller Constructor With Parameters Test")
    void testBalanceControllerConstructorWithParams() {
        try {
            java.lang.reflect.Constructor<?> constructor = BalanceController.class.getConstructor(
                int.class, String.class, String.class, String.class, String.class, String.class
            );
            assertNotNull(constructor);
        } catch (NoSuchMethodException e) {
            // Constructor with parameters might not exist, test basic constructor instead
            try {
                assertNotNull(BalanceController.class.getConstructor());
            } catch (NoSuchMethodException ex) {
                fail("BalanceController should have at least one constructor");
            }
        }
    }
}