import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;

public class LogOutTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private final String baseUrl = "http://35.187.54.5";

    @BeforeEach
    public void setUp() {
        driver = new ChromeDriver();
        driver.manage().window().setSize(new Dimension(1280, 1024)); // Mobil moddan kaç
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("Signup → Dashboard → Profile → Logout (Debug)")
    public void testSignupDashboardProfileLogout() throws InterruptedException {
        navigateToSignupPage();

        String randomPhone = "5" + (long) (Math.random() * 1000000000L);
        String validPassword = "Test1234!";
        String email = "test" + randomPhone + "@mail.com";

        driver.findElement(By.id("firstName")).sendKeys("Elif");
        driver.findElement(By.id("lastName")).sendKeys("Test");
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

        if (!packageOptions.isEmpty()) {
            packageOptions.get(0).click();
        }

        driver.findElement(By.xpath("//button[contains(text(),'Hesap Oluştur')]")).click();

        // === Dashboard geldik mi? ===
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/dashboard"), "Dashboard'a yönlendirilmedi!");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".dashboard-layout")));
        System.out.println("✔ Dashboard layout yüklendi.");

        // === Sidebar'da 'Profil' yazısı DOM'da mı? ===
        String pageHtml = driver.getPageSource();
        if (pageHtml.contains("Profil")) {
            System.out.println("✔ DOM içinde 'Profil' yazısı bulundu.");
        } else {
            System.out.println("❌ DOM içinde 'Profil' yazısı YOK! Sidebar render edilmemiş olabilir.");
        }

        // === Profil linkini bulmayı dene ===
        By profileLinkLocator = By.xpath("//a[@href='/profile' and contains(.,'Profil')]");
        WebElement profileLink;
        try {
            profileLink = wait.until(ExpectedConditions.presenceOfElementLocated(profileLinkLocator));
            System.out.println("✔ Profil linki bulundu.");
        } catch (TimeoutException e) {
            System.out.println("❌ Profil linki bulunamadı. XPath hata verdi.");
            System.out.println("➡ Sayfa kaynağı (HTML):");
            System.out.println(driver.getPageSource());
            throw e; // test patlasın
        }

        // === Link gerçekten tıklanabilir mi? ===
        System.out.println("Display: " + profileLink.isDisplayed());
        System.out.println("Enabled: " + profileLink.isEnabled());
        System.out.println("Visibility: " + profileLink.getCssValue("visibility"));
        System.out.println("Opacity: " + profileLink.getCssValue("opacity"));

        // === Tıkla (JS zorlaması) ===
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click();", profileLink);

        // === Profil sayfası açıldı mı? ===
        wait.until(ExpectedConditions.urlContains("/profile"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/profile"), "Profil sayfasına gidilemedi!");
        System.out.println("✔ Profil sayfasına başarıyla geçildi.");

        // === Çıkış Yap ===
        By logoutBtnLocator = By.xpath("//button[contains(text(),'Çıkış Yap')]");
        WebElement logoutBtn = wait.until(ExpectedConditions.elementToBeClickable(logoutBtnLocator));
        logoutBtn.click();

        wait.until(ExpectedConditions.urlContains("/login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"), "Çıkış sonrası login sayfasına yönlendirilmedi!");
        System.out.println("✔ Başarıyla çıkış yapıldı.");
    }

    private void navigateToSignupPage() {
        driver.get(baseUrl + "/login");
        WebElement signupLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(),'Hesap oluşturun')]")));
        signupLink.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("firstName")));
    }
}