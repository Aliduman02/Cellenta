import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.TestInfo;
import java.util.stream.Collectors;



public class SignUpValidationTest {
    // This method stays inside the class
    private List<String> getAllErrorMessages() {
        // Hata mesajları genellikle kırmızı renkli div'lerde olur
        List<WebElement> errorDivs = driver.findElements(
                By.xpath("//div[contains(@style,'color: rgb(239, 68, 68)')]")
        );
        return errorDivs.stream()
                .filter(WebElement::isDisplayed)
                .map(WebElement::getText)
                .filter(text -> !text.trim().isEmpty())
                .collect(Collectors.toList());
    }


    private static final Logger logger = LoggerFactory.getLogger(SignUpValidationTest.class);
    WebDriver driver;
    WebDriverWait wait;
    String baseUrl = "http://35.187.54.5";

    @BeforeEach
    public void setUp() {
        logger.info("Setting up WebDriver for test: " + TestInfo.class.getName());
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.manage().window().maximize();
        logger.info("WebDriver setup completed");
    }

    // Helper method to navigate to signup page
    private void navigateToSignupPage() throws InterruptedException {
        logger.info("Navigating to signup page");
        driver.get(baseUrl + "/login");
        Thread.sleep(2000);

        WebElement createAccountLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[@class='signup-prompt']//a[contains(text(),'Hesap oluşturun')]")));
        createAccountLink.click();
        logger.info("Clicked on 'Hesap oluşturun' link");
        Thread.sleep(2000);
    }

    // Helper method to fill form with valid data except specified field
    private void fillFormWithValidData() {
        String randomPhone = "5" + (long)(Math.random() * 1000000000L);
        logger.info("Filling form with valid data - Phone: " + randomPhone);

        driver.findElement(By.id("firstName")).sendKeys("Elif");
        driver.findElement(By.id("lastName")).sendKeys("Test");
        driver.findElement(By.id("phone")).sendKeys(randomPhone);
        driver.findElement(By.id("email")).sendKeys("test" + randomPhone + "@mail.com");
    }

    // Helper method to check for error message
    private boolean isErrorMessagePresent(String expectedError) {
        logger.debug("Checking for error message containing: " + expectedError);
        try {
            List<WebElement> errorElements = driver.findElements(By.className("error-message"));
            for (WebElement error : errorElements) {
                if (error.getText().contains(expectedError)) {
                    logger.info("Found error message: " + error.getText());
                    return true;
                }
            }
            // Also check for any visible error text
            List<WebElement> allErrors = driver.findElements(By.xpath("//*[contains(@class,'error') or contains(@class,'invalid')]"));
            for (WebElement error : allErrors) {
                if (error.isDisplayed() && error.getText().contains(expectedError)) {
                    logger.info("Found error message: " + error.getText());
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("Error while checking for error message: " + e.getMessage());
            return false;
        }
        logger.debug("No error message found containing: " + expectedError);
        return false;
    }

    @Test
    @DisplayName("Ad alanı rakam içerdiğinde hata vermeli")
    public void testFirstNameWithNumbers() throws InterruptedException {
        navigateToSignupPage();

        driver.findElement(By.id("firstName")).sendKeys("Elif123");
        driver.findElement(By.id("lastName")).sendKeys("Test");
        driver.findElement(By.id("phone")).sendKeys("5123456789");
        driver.findElement(By.id("email")).sendKeys("test@mail.com");

        driver.findElement(By.cssSelector("button.login-button")).click();
        Thread.sleep(1000);

        List<String> errors = getAllErrorMessages();
        boolean hasExpectedError = errors.stream()
                .anyMatch(msg -> msg.contains("Ad en az 2 karakter olmalı") || msg.contains("yalnızca harf içermeli"));

        Assertions.assertTrue(hasExpectedError, "Ad alanı rakam içerdiğinde hata vermeli");
    }

    @Test
    @DisplayName("Türkçe karakterli isimler kabul edilmeli - Işıl")
    public void testTurkishCharactersFirstName() throws InterruptedException {
        logger.info("Starting test: Türkçe karakterli isimler kabul edilmeli - Işıl");
        navigateToSignupPage();

        String randomPhone = "5" + (long)(Math.random() * 1000000000L);
        driver.findElement(By.id("firstName")).sendKeys("Işıl");
        driver.findElement(By.id("lastName")).sendKeys("Test");
        driver.findElement(By.id("phone")).sendKeys(randomPhone);
        driver.findElement(By.id("email")).sendKeys("test" + randomPhone + "@mail.com");

        WebElement signupButton = driver.findElement(By.cssSelector("button.login-button"));
        signupButton.click();
        logger.info("Clicked signup button with Turkish character name");
        Thread.sleep(2000);

        // Should proceed to next step
        boolean success = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("password"))) != null;
        logger.info("Test result - Proceeded to password step: " + success);
        Assertions.assertTrue(success, "Türkçe karakterli isim (Işıl) kabul edilmeli");
    }

    @Test
    @DisplayName("Türkçe karakterli soyisimler kabul edilmeli - Yılmaz")
    public void testTurkishCharactersLastName() throws InterruptedException {
        logger.info("Starting test: Türkçe karakterli soyisimler kabul edilmeli - Yılmaz");
        navigateToSignupPage();

        String randomPhone = "5" + (long)(Math.random() * 1000000000L);
        driver.findElement(By.id("firstName")).sendKeys("Test");
        driver.findElement(By.id("lastName")).sendKeys("Yılmaz");
        driver.findElement(By.id("phone")).sendKeys(randomPhone);
        driver.findElement(By.id("email")).sendKeys("test" + randomPhone + "@mail.com");

        WebElement signupButton = driver.findElement(By.cssSelector("button.login-button"));
        signupButton.click();
        logger.info("Clicked signup button with Turkish character surname");
        Thread.sleep(2000);

        // Should proceed to next step
        boolean success = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("password"))) != null;
        logger.info("Test result - Proceeded to password step: " + success);
        Assertions.assertTrue(success, "Türkçe karakterli soyisim (Yılmaz) kabul edilmeli");
    }

    @Test
    @DisplayName("Telefon numarası 5 ile başlamalı")
    public void testPhoneStartsWith5() throws InterruptedException {
        navigateToSignupPage();

        driver.findElement(By.id("firstName")).sendKeys("Elif");
        driver.findElement(By.id("lastName")).sendKeys("Test");
        driver.findElement(By.id("phone")).sendKeys("4123456789"); // 5 ile başlamıyor
        driver.findElement(By.id("email")).sendKeys("test@mail.com");

        driver.findElement(By.cssSelector("button.login-button")).click();
        Thread.sleep(1000);

        List<String> errors = getAllErrorMessages();
        boolean hasExpectedError = errors.stream().anyMatch(msg ->
                msg.contains("5 ile başlamalı") ||
                        msg.contains("Telefon numarası") ||
                        msg.contains("geçersiz")
        );

        Assertions.assertTrue(hasExpectedError, "Telefon numarası 5 ile başlamadığında hata vermeli");
    }

    @Test
    @DisplayName("Telefon numarası 10 karakter olmalı")
    public void testPhoneLength() throws InterruptedException {
        logger.info("Starting test: Telefon numarası 10 karakter olmalı");
        navigateToSignupPage();

        driver.findElement(By.id("firstName")).sendKeys("Elif");
        driver.findElement(By.id("lastName")).sendKeys("Test");
        driver.findElement(By.id("phone")).sendKeys("512345"); // Çok kısa
        driver.findElement(By.id("email")).sendKeys("test@mail.com");

        driver.findElement(By.cssSelector("button.login-button")).click();
        logger.info("Clicked signup button with short phone number");
        Thread.sleep(1000);

        List<String> errors = getAllErrorMessages();
        boolean hasExpectedError = errors.stream().anyMatch(msg ->
                msg.contains("10 haneli olmalı") ||
                        msg.contains("10 karakter") ||
                        msg.contains("Telefon numarası") ||
                        msg.contains("geçersiz")
        );

        logger.info("Test result - Error detected: " + hasExpectedError);
        Assertions.assertTrue(hasExpectedError, "Telefon numarası 10 karakterden az olduğunda hata vermeli");
    }
    @Test
    @DisplayName("E-posta formatı doğru olmalı")
    public void testInvalidEmailFormat() throws InterruptedException {
        logger.info("Starting test: E-posta formatı doğru olmalı");
        navigateToSignupPage();

        driver.findElement(By.id("firstName")).sendKeys("Elif");
        driver.findElement(By.id("lastName")).sendKeys("Test");
        driver.findElement(By.id("phone")).sendKeys("5123456789");
        driver.findElement(By.id("email")).sendKeys("invalidemail"); // @ yok

        driver.findElement(By.cssSelector("button.login-button")).click();
        logger.info("Clicked signup button with invalid email format");
        Thread.sleep(1000);

        List<String> errors = getAllErrorMessages();
        boolean hasExpectedError = errors.stream().anyMatch(msg ->
                msg.toLowerCase().contains("geçerli") ||
                        msg.toLowerCase().contains("e-posta") ||
                        msg.toLowerCase().contains("email")
        );

        logger.info("Test result - Error detected: " + hasExpectedError);
        Assertions.assertTrue(hasExpectedError, "Geçersiz e-posta formatında hata vermeli");
    }

    @Test
    @DisplayName("Boş alanlar hata vermeli")
    public void testEmptyFields() throws InterruptedException {
        logger.info("Starting test: Boş alanlar hata vermeli");
        navigateToSignupPage();

        // Alanları boş bırakıp gönder
        driver.findElement(By.cssSelector("button.login-button")).click();
        logger.info("Clicked signup button with empty fields");
        Thread.sleep(1000);

        // Hata mesajlarını çek
        List<String> errors = getAllErrorMessages();

        // En az bir hata mesajı bekleniyor
        boolean hasAnyError = !errors.isEmpty();

        // Hata mesajı içeriği kontrolü (isteğe bağlı, örnek)
        boolean hasRequiredFieldError = errors.stream().anyMatch(msg ->
                msg.toLowerCase().contains("zorunlu") ||
                        msg.toLowerCase().contains("gerekli") ||
                        msg.toLowerCase().contains("boş") ||
                        msg.toLowerCase().contains("doldur")
        );

        // Hem sayfada kalmalı hem de hata mesajı olmalı
        boolean stillOnSignup = driver.getCurrentUrl().contains("/signup");

        logger.info("Test result - Still on signup page: " + stillOnSignup + ", Error shown: " + hasAnyError);
        Assertions.assertTrue(stillOnSignup, "Boş alanlarla form gönderilmemeli");
        Assertions.assertTrue(hasAnyError, "Boş alanlar için en az bir hata mesajı gösterilmeli");
        // Eğer spesifik hata mesajı kontrolü istersen:
        // Assertions.assertTrue(hasRequiredFieldError, "Boş alanlar için uygun hata mesajı gösterilmeli");
    }

    @Test
    @DisplayName("Şifre en az 8 karakter olmalı")
    public void testPasswordMinLength() throws InterruptedException {
        logger.info("Starting test: Şifre en az 8 karakter olmalı");
        navigateToSignupPage();
        fillFormWithValidData();

        driver.findElement(By.cssSelector("button.login-button")).click();
        Thread.sleep(2000);

        // Şifre alanına 8 karakterden kısa bir şifre gir
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password"))).sendKeys("Test1!");
        driver.findElement(By.id("confirmPassword")).sendKeys("Test1!");
        logger.info("Entered short password");

        // Devam et
        driver.findElement(By.cssSelector("button.login-button")).click();
        Thread.sleep(1000);

        List<String> errors = getAllErrorMessages();
        boolean hasExpectedError = errors.stream().anyMatch(msg ->
                msg.contains("8 karakter") ||
                        msg.contains("en az") ||
                        msg.toLowerCase().contains("şifre")
        );

        logger.info("Test result - Error detected: " + hasExpectedError);
        Assertions.assertTrue(hasExpectedError, "Şifre 8 karakterden az olduğunda hata vermeli");
    }


    @Test
    @DisplayName("Şifre en az 1 küçük harf içermeli")
    public void testPasswordLowercase() throws InterruptedException {
        logger.info("Starting test: Şifre en az 1 küçük harf içermeli");
        navigateToSignupPage();
        fillFormWithValidData();

        // İlk adımı geç
        driver.findElement(By.cssSelector("button.login-button")).click();
        Thread.sleep(2000);

        // Sadece büyük harf, rakam ve özel karakter içeren şifre gir
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password"))).sendKeys("TEST1234!");
        driver.findElement(By.id("confirmPassword")).sendKeys("TEST1234!");
        logger.info("Entered password without lowercase");

        // Devam et
        driver.findElement(By.cssSelector("button.login-button")).click();
        Thread.sleep(1000);

        // Hata mesajlarını çek
        List<String> errors = getAllErrorMessages();
        boolean hasExpectedError = errors.stream().anyMatch(msg ->
                msg.toLowerCase().contains("küçük harf") ||
                        msg.toLowerCase().contains("lowercase")
        );

        logger.info("Test result - Error detected: " + hasExpectedError);
        Assertions.assertTrue(hasExpectedError, "Şifre küçük harf içermediğinde hata vermeli");
    }
    @Test
    @DisplayName("Şifre en az 1 rakam içermeli")
    public void testPasswordNumberTest() throws InterruptedException {
        logger.info("Starting test: Şifre en az 1 rakam içermeli");
        navigateToSignupPage();
        fillFormWithValidData();

        // İlk adımı geç
        driver.findElement(By.cssSelector("button.login-button")).click();
        Thread.sleep(2000);

        // Rakam içermeyen şifre gir
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password"))).sendKeys("TestTest!");
        driver.findElement(By.id("confirmPassword")).sendKeys("TestTest!");
        logger.info("Rakam içermeyen şifre girildi");

        // Devam et
        driver.findElement(By.cssSelector("button.login-button")).click();
        Thread.sleep(1000);

        // Hata mesajlarını çek
        List<String> errors = getAllErrorMessages();
        boolean hasExpectedError = errors.stream().anyMatch(msg ->
                msg.toLowerCase().contains("rakam") ||
                        msg.toLowerCase().contains("sayı") ||
                        msg.toLowerCase().contains("number")
        );

        logger.info("Test sonucu - Hata tespit edildi: " + hasExpectedError);
        Assertions.assertTrue(hasExpectedError, "Şifre rakam içermediğinde hata vermeli");
    }



    @Test
    @DisplayName("Şifre en az 1 özel karakter içermeli")
    public void testPasswordSpecialChar() throws InterruptedException {
        logger.info("Starting test: Şifre en az 1 özel karakter içermeli");
        navigateToSignupPage();
        fillFormWithValidData();

        // İlk adımı geç
        driver.findElement(By.cssSelector("button.login-button")).click();
        Thread.sleep(2000);

        // Şifre ve tekrar şifre alanlarına özel karakter içermeyen şifre gir
        WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
        passwordField.clear();
        passwordField.sendKeys("Test1234");

        WebElement confirmPasswordField = driver.findElement(By.id("confirmPassword"));
        confirmPasswordField.clear();
        confirmPasswordField.sendKeys("Test1234");
        logger.info("Entered password without special character");

        // Devam et
        driver.findElement(By.cssSelector("button.login-button")).click();
        Thread.sleep(1000);

        // Hata mesajlarını çek
        List<String> errors = getAllErrorMessages();
        boolean hasExpectedError = errors.stream().anyMatch(msg ->
                msg.toLowerCase().contains("özel karakter") ||
                        msg.toLowerCase().contains("special character")
        );

        logger.info("Test result - Special character error detected: " + hasExpectedError);
        Assertions.assertTrue(hasExpectedError, "Şifre özel karakter içermediğinde hata vermeli");
    }
    @Test
    @DisplayName("Şifreler eşleşmeli")
    public void testPasswordMismatch() throws InterruptedException {
        logger.info("Starting test: Şifreler eşleşmeli");
        navigateToSignupPage();
        fillFormWithValidData();

        // İlk adımı geç
        driver.findElement(By.cssSelector("button.login-button")).click();
        Thread.sleep(2000);

        // Farklı şifreler gir
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password"))).sendKeys("Test1234!");
        driver.findElement(By.id("confirmPassword")).sendKeys("Test1234@"); // Farklı şifre
        logger.info("Entered mismatched passwords");

        // Devam et
        driver.findElement(By.cssSelector("button.login-button")).click();
        Thread.sleep(1000);

        // Hata mesajlarını çek
        List<String> errors = getAllErrorMessages();
        boolean hasExpectedError = errors.stream().anyMatch(msg ->
                msg.toLowerCase().contains("eşleşm") ||
                        msg.toLowerCase().contains("match") ||
                        msg.toLowerCase().contains("aynı")
        );

        logger.info("Test result - Error detected: " + hasExpectedError);
        Assertions.assertTrue(hasExpectedError, "Şifreler eşleşmediğinde hata vermeli");
    }

    @Test
    @DisplayName("Paket seçimi yapılmalı")
    public void testPackageSelectionRequired() throws InterruptedException {
        logger.info("Starting test: Paket seçimi yapılmalı");
        navigateToSignupPage();
        fillFormWithValidData();

        WebElement signupButton = driver.findElement(By.cssSelector("button.login-button"));
        signupButton.click();
        Thread.sleep(2000);

        // Fill passwords
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password"))).sendKeys("Test1234!");
        driver.findElement(By.id("confirmPassword")).sendKeys("Test1234!");
        logger.info("Filled passwords");

        // Try to submit without selecting package
        WebElement finalSignupButton = driver.findElement(By.cssSelector("button.login-button"));
        finalSignupButton.click();
        logger.info("Clicked signup without selecting package");
        Thread.sleep(1000);

        // Should show error or stay on same page
        boolean hasError = driver.getCurrentUrl().contains("/signup") ||
                isErrorMessagePresent("paket") ||
                isErrorMessagePresent("seçiniz");
        logger.info("Test result - Error detected: " + hasError);
        Assertions.assertTrue(hasError, "Paket seçimi yapılmadan form gönderilmemeli");
    }



    // Edge case testleri
    @Test
    @DisplayName("Çok uzun isim girişi")
    public void testVeryLongName() throws InterruptedException {
        logger.info("Starting test: Çok uzun isim girişi");
        navigateToSignupPage();

        String veryLongName = "A".repeat(100);
        driver.findElement(By.id("firstName")).sendKeys(veryLongName);
        driver.findElement(By.id("lastName")).sendKeys("Test");
        driver.findElement(By.id("phone")).sendKeys("5123456789");
        driver.findElement(By.id("email")).sendKeys("test@mail.com");

        WebElement signupButton = driver.findElement(By.cssSelector("button.login-button"));
        signupButton.click();
        logger.info("Clicked signup with very long name");
        Thread.sleep(1000);

        // Check if error or truncation occurs
        WebElement firstNameField = driver.findElement(By.id("firstName"));
        String actualValue = firstNameField.getAttribute("value");
        logger.info("Actual name field value length: " + actualValue.length());

        boolean hasLengthLimit = actualValue.length() <= 50 || isErrorMessagePresent("uzun");
        logger.info("Test result - Length limited or error shown: " + hasLengthLimit);
        Assertions.assertTrue(hasLengthLimit, "Çok uzun isim için sınırlama olmalı");
    }

    @Test
    @DisplayName("SQL Injection denemesi")
    public void testSQLInjectionAttempt() throws InterruptedException {
        logger.info("Starting test: SQL Injection denemesi");
        navigateToSignupPage();

        String sqlInjection = "Test'; DROP TABLE users; --";
        driver.findElement(By.id("firstName")).sendKeys(sqlInjection);
        driver.findElement(By.id("lastName")).sendKeys("Test");
        driver.findElement(By.id("phone")).sendKeys("5123456789");
        driver.findElement(By.id("email")).sendKeys("test@mail.com");

        driver.findElement(By.cssSelector("button.login-button")).click();
        logger.info("Clicked signup with SQL injection attempt: " + sqlInjection);
        Thread.sleep(1000);

        // Hata mesajlarını çek
        List<String> errors = getAllErrorMessages();
        boolean blocked = errors.stream().anyMatch(msg ->
                msg.toLowerCase().contains("geçersiz") ||
                        msg.toLowerCase().contains("karakter") ||
                        msg.toLowerCase().contains("uygun değil")
        ) || driver.getCurrentUrl().contains("/signup");

        logger.info("Test result - SQL injection blocked: " + blocked);
        Assertions.assertTrue(blocked, "SQL injection karakterleri kabul edilmemeli");
    }
    @Test
    @DisplayName("XSS denemesi")
    public void testXSSAttempt() throws InterruptedException {
        logger.info("Starting test: XSS denemesi");
        navigateToSignupPage();

        String xssAttempt = "<script>alert('XSS')</script>";
        driver.findElement(By.id("firstName")).sendKeys(xssAttempt);
        driver.findElement(By.id("lastName")).sendKeys("Test");
        driver.findElement(By.id("phone")).sendKeys("5123456789");
        driver.findElement(By.id("email")).sendKeys("test@mail.com");

        driver.findElement(By.cssSelector("button.login-button")).click();
        logger.info("Clicked signup with XSS attempt: " + xssAttempt);
        Thread.sleep(1000);

        // Sayfada alert açılmadığından emin ol
        try {
            driver.switchTo().alert();
            logger.error("XSS attack was successful - alert appeared!");
            Assertions.fail("XSS saldırısı başarılı olmamalı");
        } catch (NoAlertPresentException e) {
            logger.info("No alert appeared - XSS blocked successfully");
        }

        // Hata mesajlarını çek
        List<String> errors = getAllErrorMessages();
        boolean blocked = errors.stream().anyMatch(msg ->
                msg.toLowerCase().contains("geçersiz") ||
                        msg.toLowerCase().contains("uygun değil") ||
                        msg.toLowerCase().contains("karakter")
        ) || driver.getCurrentUrl().contains("/signup");

        logger.info("Test result - XSS blocked: " + blocked);
        Assertions.assertTrue(blocked, "Script tagları kabul edilmemeli");
    }
    @Test
    @DisplayName("Aynı telefon numarası ile tekrar kayıt denemesi")
    public void testDuplicatePhoneNumber() throws InterruptedException {
        logger.info("Starting test: Aynı telefon numarası ile tekrar kayıt denemesi");
        String fixedPhone = "5999999999";
        String validPassword = "Test1234!";

        // İlk kayıt
        navigateToSignupPage();
        driver.findElement(By.id("firstName")).sendKeys("Test");
        driver.findElement(By.id("lastName")).sendKeys("User");
        driver.findElement(By.id("phone")).sendKeys(fixedPhone);
        driver.findElement(By.id("email")).sendKeys("first@mail.com");

        driver.findElement(By.cssSelector("button.login-button")).click();
        logger.info("First signup - step 1 completed");
        Thread.sleep(2000);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password"))).sendKeys(validPassword);
        driver.findElement(By.id("confirmPassword")).sendKeys(validPassword);

        WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".package-dropdown")));
        dropdown.click();
        Thread.sleep(1000);

        List<WebElement> packageOptions = driver.findElements(By.xpath("//div[contains(@class,'package-option')]"));
        if (!packageOptions.isEmpty()) {
            packageOptions.get(0).click();
        }

        driver.findElement(By.cssSelector("button.login-button")).click();
        logger.info("First signup completed");
        Thread.sleep(3000);

        // Aynı telefon ile ikinci kayıt denemesi
        navigateToSignupPage();
        driver.findElement(By.id("firstName")).sendKeys("Another");
        driver.findElement(By.id("lastName")).sendKeys("User");
        driver.findElement(By.id("phone")).sendKeys(fixedPhone);
        driver.findElement(By.id("email")).sendKeys("second@mail.com");

        driver.findElement(By.cssSelector("button.login-button")).click();
        logger.info("Attempting second signup with same phone: " + fixedPhone);
        Thread.sleep(1000);

        List<String> errors = getAllErrorMessages();
        boolean blocked = errors.stream().anyMatch(msg ->
                msg.toLowerCase().contains("kullanılıyor") ||
                        msg.toLowerCase().contains("mevcut") ||
                        msg.toLowerCase().contains("exists")
        );

        logger.info("Test result - Duplicate phone blocked: " + blocked);
        Assertions.assertTrue(blocked, "Aynı telefon numarası ile tekrar kayıt yapılamamalı");
    }
    @Test
    @DisplayName("Özel karakterli e-posta adresi")
    public void testSpecialCharEmail() throws InterruptedException {
        logger.info("Starting test: Özel karakterli e-posta adresi");
        navigateToSignupPage();

        String randomPhone = "5" + (long)(Math.random() * 1000000000L);
        String specialEmail = "test+tag@mail.com";
        driver.findElement(By.id("firstName")).sendKeys("Test");
        driver.findElement(By.id("lastName")).sendKeys("User");
        driver.findElement(By.id("phone")).sendKeys(randomPhone);
        driver.findElement(By.id("email")).sendKeys(specialEmail);

        driver.findElement(By.cssSelector("button.login-button")).click();
        logger.info("Clicked signup with special character email: " + specialEmail);
        Thread.sleep(2000);

        // Şifre adımına geçilebildi mi kontrol et
        boolean success = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("password"))) != null;
        logger.info("Test result - Special character email accepted: " + success);
        Assertions.assertTrue(success, "Geçerli özel karakterli e-posta adresleri kabul edilmeli");
    }
    @Test
    @DisplayName("Başarılı kayıt sonrası Dashboard ve Faturalar kontrolü")
    public void testSignupAndPackageVisibleOnBills() throws InterruptedException {
        logger.info("Starting test: Başarılı kayıt ve Faturalar görünümü");

        navigateToSignupPage();

        String randomPhone = "5" + (long)(Math.random() * 1000000000L);
        String validPassword = "Test1234!";
        String email = "test" + randomPhone + "@mail.com";

        driver.findElement(By.id("firstName")).sendKeys("Elif");
        driver.findElement(By.id("lastName")).sendKeys("Yılmaz");
        driver.findElement(By.id("phone")).sendKeys(randomPhone);
        driver.findElement(By.id("email")).sendKeys(email);

        driver.findElement(By.cssSelector("button.login-button")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password"))).sendKeys(validPassword);
        driver.findElement(By.id("confirmPassword")).sendKeys(validPassword);

        WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".package-dropdown")));
        dropdown.click();

        List<WebElement> packageOptions = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.xpath("//div[@style[contains(.,'overflow-y: auto')]]/div")));
        if (packageOptions.isEmpty()) {
            packageOptions = driver.findElements(By.xpath("//div[contains(@class,'package-option')]"));
        }

        String selectedPackageName = "";
        if (!packageOptions.isEmpty()) {
            WebElement selectedPackage = packageOptions.get(0);
            selectedPackageName = selectedPackage.getText();
            selectedPackage.click();
            logger.info("Selected first package option: " + selectedPackageName);
        }

        WebElement hesapOlusturButton = driver.findElement(By.xpath("//button[contains(text(),'Hesap Oluştur')]"));
        hesapOlusturButton.click();

        // Dashboard'a yönlendirmeyi bekle
        boolean redirected = false;
        try {
            redirected = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.urlContains("/dashboard"));
        } catch (TimeoutException e) {
            logger.error("10 saniye içinde /dashboard yönlendirmesi gerçekleşmedi.");
        }

        List<WebElement> errors = driver.findElements(By.cssSelector(".error-message"));
        for (WebElement error : errors) {
            logger.warn("Hata mesajı: " + error.getText());
        }

        String currentUrl = driver.getCurrentUrl();
        logger.info("Current URL after signup: " + currentUrl);
        Assertions.assertTrue(redirected, "Dashboard'a yönlendirilmedi.");

        // Faturalar linkini bul, tıkla ve URL kontrolü yap
        WebElement billsLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/bills']")));
        billsLink.click();

        boolean onBillsPage = false;
        try {
            onBillsPage = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.urlToBe("http://35.187.54.5/bills"));
        } catch (TimeoutException e) {
            logger.error("10 saniye içinde /bills sayfasına yönlendirme gerçekleşmedi.");
        }
        Assertions.assertTrue(onBillsPage, "Faturalar sayfasına yönlendirilmedi.");
        logger.info("Navigated to Bills page, URL is correct.");

        // Paket bilgisinin faturalar sayfasında görünür olduğunu doğrula
        List<WebElement> billItems = driver.findElements(By.xpath("//*[contains(text(),'" + selectedPackageName + "')]"));
        Assertions.assertFalse(billItems.isEmpty(), "Seçilen paket faturalar sayfasında görünmüyor: " + selectedPackageName);
    }




    @AfterEach
        public void tearDown() {
            logger.info("Tearing down test");
            if (driver != null) {
                driver.quit();
                logger.info("WebDriver closed");
            }
        }
    }