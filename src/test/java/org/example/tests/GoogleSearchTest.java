package org.example.tests;

import Utils.MyConfig;
import io.github.bonigarcia.wdm.WebDriverManager;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.*;

import Services.ReportService;
import Services.TestFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;

public class GoogleSearchTest {
    WebDriver driver;

    @BeforeClass
    public void setup() {
        // WebDriverManager.chromedriver().setup();
        ReportService.generateReport();
        System.setProperty("webdriver.chrome.driver", "LocalDriver\\chromedriver" + TestFactory.getChromeVersion()+ ".exe");

        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.addArguments("--start-maximized");
        options.addArguments(Arrays.asList("--no-sandbox","--ignore-certificate-errors","--homepage=about:blank","--no-first-run"));
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        driver = new ChromeDriver(options);
        MyConfig.driver = driver;
    }

    @Test
    public void searchTest() throws InterruptedException, IOException {

        driver.get("http://172.17.22.62:9080/spk/web/login.xhtml");
        Thread.sleep(1000);
        int size = driver.findElements(By.xpath("(//tr[@role='row' and @data-ri])")).size();
        int j = 1;

        while (j<=size) {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            WebElement visibleElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//button[span[text()='Ditolak']]")));
            size = driver.findElements(By.xpath("(//tr[@role='row' and @data-ri])")).size();
            if (size==25) {
                j = 1;
                driver.findElement(By.xpath("(//tr[@role='row' and @data-ri])["+j+"]")).click();
                Thread.sleep(500);
                driver.findElement(By.xpath("//span[text()='Disetujui']")).click();
                visibleElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//button[span[text()='Batal']]")));
                driver.findElement(By.xpath("span[text()='Disetujui']")).click();
            } else {
                j++;
            }


            //span[text()='Disetujui']
            //button[span[text()='Disetujui']]
            //button[span[text()='Batal']]

        }
        
        Thread.sleep(1000);
        // driver.get("https://google.com");
        // driver.findElement(By.xpath("//input[@name='TXT_USERNAME']")).sendKeys("BTN9781");
        // driver.findElement(By.xpath("//input[@name='TXT_PASSWORD']")).sendKeys("Batara1234");
        // ScreenshotService.screenshot_full_whole();
        // Thread.sleep(1000);
        // driver.findElement(By.xpath("//input[@value='Login' and @type='button']")).click();

        // driver.switchTo().frame(driver.findElement(By.xpath("//frame[@name='main']")));
        // WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        // WebElement visibleElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//td[@id='menu_DXI0_T']")));
        
        // driver.findElement(By.xpath("//td[@id='menu_DXI0_T']")).click();
        // driver.findElement(By.xpath("//td[@id='menu_DXI0i3_T']")).click();

        // driver.switchTo().frame(driver.findElement(By.xpath("//iframe[@id='framex']")));
        // visibleElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@id='mainPanel_BARCODE_P_txtID']")));

        // driver.switchTo().parentFrame();
        // ScreenshotService.screenshot_full_whole();
    }

    @AfterClass
    public void teardown() {
        driver.quit();
    }
}
