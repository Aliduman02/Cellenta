package com.cellenta.automation;

import org.assertj.swing.core.Robot;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;

import javax.swing.JFrame;
import java.awt.Dimension;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class BaseUITest {
    
    protected static final Logger logger = LoggerFactory.getLogger(BaseUITest.class);
    protected FrameFixture window;
    protected Robot robot;
    
    @BeforeClass
    public void setUpClass() {
        logger.info("========================================");
        logger.info("STARTING UI AUTOMATION TEST SUITE");
        logger.info("Test Class: {}", this.getClass().getSimpleName());
        logger.info("Start Time: {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        logger.info("========================================");
        
        // Set headless mode for CI/CD environments
        System.setProperty("java.awt.headless", "false");
        
        // Set robot delay for more stable tests
        System.setProperty("assertj.swing.robot.delay", "100");
        
        logger.info("UI Test environment configured successfully");
    }
    
    @BeforeMethod
    public void setUpMethod() {
        String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        logger.info("------------------------------------------");
        logger.info("STARTING TEST METHOD: {}", methodName);
        logger.info("Time: {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")));
        logger.info("------------------------------------------");
        
        try {
            robot = org.assertj.swing.core.BasicRobot.robotWithCurrentAwtHierarchy();
            robot.settings().delayBetweenEvents(50);
            robot.settings().eventPostingDelay(100);
            
            logger.info("Robot initialized with delay settings");
            
        } catch (Exception e) {
            logger.error("Failed to initialize robot: {}", e.getMessage());
            throw new RuntimeException("Test setup failed", e);
        }
    }
    
    @AfterMethod
    public void tearDownMethod() {
        String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        
        try {
            if (window != null) {
                logger.info("Cleaning up window for test: {}", methodName);
                window.cleanUp();
                window = null;
            }
            
            if (robot != null) {
                logger.info("Cleaning up robot for test: {}", methodName);
                robot.cleanUp();
                robot = null;
            }
            
            logger.info("TEST METHOD COMPLETED: {}", methodName);
            logger.info("------------------------------------------");
            
        } catch (Exception e) {
            logger.error("Error during test cleanup: {}", e.getMessage());
        }
    }
    
    @AfterClass
    public void tearDownClass() {
        logger.info("========================================");
        logger.info("UI AUTOMATION TEST SUITE COMPLETED");
        logger.info("End Time: {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        logger.info("========================================");
    }
    
    protected void logTestStep(String step, Object... args) {
        logger.info("TEST STEP: " + step, args);
    }
    
    protected void logTestInfo(String info, Object... args) {
        logger.info("INFO: " + info, args);
    }
    
    protected void logTestWarning(String warning, Object... args) {
        logger.warn("WARNING: " + warning, args);
    }
    
    protected void logTestError(String error, Exception e) {
        logger.error("ERROR: {} - Exception: {}", error, e.getMessage());
    }
    
    protected void waitForWindowToLoad() {
        logTestStep("Waiting for window to load completely");
        try {
            Thread.sleep(1000); // Wait for window components to initialize
            logger.info("Window load wait completed");
        } catch (InterruptedException e) {
            logger.warn("Window load wait interrupted: {}", e.getMessage());
        }
    }
    
    protected void takeScreenshotOnFailure(String testName) {
        // This method can be extended to take screenshots on test failures
        logger.info("Screenshot capability available for test: {}", testName);
    }
    
    protected void logWindowInfo(JFrame frame) {
        if (frame != null) {
            Dimension size = frame.getSize();
            logger.info("Window Info - Title: '{}', Size: {}x{}, Visible: {}", 
                       frame.getTitle(), size.width, size.height, frame.isVisible());
        } else {
            logger.warn("Window is null - cannot log window info");
        }
    }
}