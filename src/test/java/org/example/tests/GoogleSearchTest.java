package org.example.tests;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import Services.TestFactory;
import Utils.MyConfig;

public class GoogleSearchTest {
    WebDriver driver;

    @BeforeClass
    public void setup() {
        // WebDriverManager.chromedriver().setup();
        // ReportService.generateReport();
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

        driver.get("https://www.google.com");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("q")));
        searchBox.sendKeys("Selenium WebDriver");
        searchBox.submit();

        // Wait for the results to load and display the results
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("search")));
        System.out.println("Search completed successfully.");
        //==================== HALO ========================= //
    }

    @AfterClass
    public void teardown() {
        driver.quit();
    }
}
