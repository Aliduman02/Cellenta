import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;

public class MagazaTest {

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

    @AfterEach
    public void tearDown() {
        driver.quit();
    }

    @Test
    public void testStorePackagePurchaseAppearsInBills() throws InterruptedException {
        // 1. Login
        driver.get(baseUrl + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).sendKeys("ad");
        driver.findElement(By.name("password")).sendKeys("sifre"); // doğru şifreyi koy
        driver.findElement(By.tagName("button")).click(); // giriş butonu

        // 2. Mağaza menüsüne tıkla
        WebElement storeMenu = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[@href='/store' and @title='Mağaza']")));
        storeMenu.click();

        // 3. URL kontrolü: Mağaza sayfasına yönlendik mi?
        wait.until(ExpectedConditions.urlContains("/store"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/store"));

        // 4. İlk paketin "Paketi Seç" butonuna tıkla
        WebElement selectButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'Paketi Seç')]")));
        selectButton.click();

        // 5. Popup’ta "✅ Onayla" butonuna tıkla
        WebElement confirmButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'Onayla')]")));
        confirmButton.click();

        // 6. /bills sayfasına yönlenene kadar bekle
        wait.until(ExpectedConditions.urlContains("/bills"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/bills"));

        // 7. Faturalar sayfasında alınan paketin ismini doğrula
        WebElement billItem = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'Aile Paketi') or contains(text(),'Genç Tarife') or contains(text(),'Paket')]")));
        Assertions.assertTrue(billItem.isDisplayed(), "Alınan paket faturalarda görünmeli.");
    }
}