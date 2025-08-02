import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;

public class LoginTest {

    WebDriver driver;
    WebDriverWait wait;
    String baseUrl = "http://35.187.54.5";

    @BeforeEach
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.manage().window().maximize();
    }

    @Test
    public void testLoginSuccess() throws InterruptedException {
        driver.get(baseUrl + "/login");

        // Telefon numarası alanını doldur
        WebElement phoneInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("phone")));
        phoneInput.clear();
        phoneInput.sendKeys("5347824380");

        // Şifre alanını doldur
        WebElement passwordInput = driver.findElement(By.id("password"));
        passwordInput.clear();
        passwordInput.sendKeys("test123?A");

        // Giriş Yap butonuna tıkla
        WebElement loginButton = driver.findElement(By.cssSelector("button.login-button"));
        loginButton.click();

        // Dashboard'a yönlendirilmiş mi kontrol et
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/dashboard"),
                "Login sonrası URL /dashboard içermiyor! Giriş başarısız.");
    }
    @Test
    public void testInvalidPasswordShowsErrorMessage() throws InterruptedException {
        driver.get(baseUrl + "/login");

        WebElement phoneInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("phone")));
        phoneInput.clear();
        phoneInput.sendKeys("5347824380");

        WebElement passwordInput = driver.findElement(By.id("password"));
        passwordInput.clear();
        passwordInput.sendKeys("yanlisSifre123");

        WebElement loginButton = driver.findElement(By.cssSelector("button.login-button"));
        loginButton.click();

        // Hata mesajı div'i sayfada görünüyor mu?
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'şifre') or contains(text(),'geçersiz') or contains(text(),'hatalı')]")));

        Assertions.assertTrue(errorMessage.isDisplayed(), "Hata mesajı görünmüyor!");
    }
    @Test
    public void testSQLInjectionLogin() throws InterruptedException {
        driver.get(baseUrl + "/login");

        WebElement phoneInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("phone")));
        phoneInput.clear();
        phoneInput.sendKeys("5347824380");

        WebElement passwordInput = driver.findElement(By.id("password"));
        passwordInput.clear();
        passwordInput.sendKeys("' OR '1'='1");

        WebElement loginButton = driver.findElement(By.cssSelector("button.login-button"));
        loginButton.click();

        // dashboard yönlendirmesi olmamalı
        Thread.sleep(3000); // Beklemeyi artırmak gerekirse süreyi ayarla
        Assertions.assertFalse(driver.getCurrentUrl().contains("/dashboard"),
                "SQL injection ile giriş yapılabildi! Bu bir güvenlik açığıdır.");
    }
    @Test
    public void testXSSInjectionLogin() throws InterruptedException {
        driver.get(baseUrl + "/login");

        WebElement phoneInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("phone")));
        phoneInput.clear();
        phoneInput.sendKeys("5347824380");

        WebElement passwordInput = driver.findElement(By.id("password"));
        passwordInput.clear();
        passwordInput.sendKeys("<script>alert('XSS')</script>");

        WebElement loginButton = driver.findElement(By.cssSelector("button.login-button"));
        loginButton.click();

        Thread.sleep(2000);
        // URL dashboard'a gitmemeli
        Assertions.assertFalse(driver.getCurrentUrl().contains("/dashboard"),
                "XSS saldırısı ile giriş yapılabildi! Güvenlik açığı olabilir.");
    }
    @Test
    public void testUnregisteredShowsErrorMessage() throws InterruptedException {
        driver.get(baseUrl + "/login");

        WebElement phoneInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("phone")));
        phoneInput.clear();
        phoneInput.sendKeys("5333333333");

        WebElement passwordInput = driver.findElement(By.id("password"));
        passwordInput.clear();
        passwordInput.sendKeys("yenikullanici");

        WebElement loginButton = driver.findElement(By.cssSelector("button.login-button"));
        loginButton.click();

        // Hata mesajı div'i sayfada görünüyor mu?
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'şifre') or contains(text(),'geçersiz') or contains(text(),'hatalı')]")));

        Assertions.assertTrue(errorMessage.isDisplayed(), "Hata mesajı görünmüyor!");
    }


    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}