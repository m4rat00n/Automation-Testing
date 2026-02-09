package Services;
import Utils.ExcelReader;
import Utils.Execute;

import Utils.MyConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import static Utils.MyConfig.driver;
import static Utils.MyConfig.mapSaveData;
import static Utils.MyConfig.tempIntDataNo;
import static Utils.Execute.getValue;
import static Utils.Execute.setValue;
import static Utils.Execute.getObjectName;

import java.security.Key;
import java.time.Duration;
import java.util.Arrays;

public class BaseFunction {

    public void start_chrome_driver() {
        // WebDriverManager.chromedriver().setup(); //automatically download chromedriver

        System.setProperty("webdriver.chrome.driver", "LocalDriver\\chromedriver" + TestFactory.getChromeVersion()+ ".exe");

        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.addArguments("--start-maximized");
        options.addArguments(Arrays.asList("--no-sandbox","--ignore-certificate-errors","--homepage=about:blank","--no-first-run"));
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        MyConfig.driver = new ChromeDriver(options);
        System.out.println("Opening browser...");
    }

    public void go_to_url() {
        driver.get(getValue());
        System.out.println("Navigating to: " + getValue());
    }

    public void close_browser() {
        driver.close();
    }

    // CLICK METHODS
    public void click() {
        driver.findElement(By.xpath(TestFactory.getXpath(getObjectName()))).click();
    }

    public void click_js() {
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        WebElement element = driver.findElement(By.xpath(TestFactory.getXpath(getObjectName())));
        jsExecutor.executeScript("arguments[0].click();", element);
    }

    public void click_replace() {
        if (getValue().startsWith("[") && getValue().endsWith("]")) {
            setValue(mapSaveData.get(getValue()));
        }
        driver.findElement(By.xpath(TestFactory.getXpath(getObjectName()).replace("%", getValue()))).click();
    }

    // SET TEXT METHODS
    public void set_text() {
        if (!getValue().equalsIgnoreCase("")) {
            if (getValue().startsWith("[") && getValue().endsWith("]")) {
                setValue(mapSaveData.get(getValue()));
            }
            WebElement element = driver.findElement(By.xpath(TestFactory.getXpath(getObjectName())));
            try {
                element.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
            } catch (Exception e){
                element.clear();
            }
            element.sendKeys(getValue());
        }
    }

    public void select(){
        WebElement element = driver.findElement(By.xpath(TestFactory.getXpath(getObjectName())));
        Select select = new Select(element);
        select.selectByValue(getValue());
    }

    // SCREENSHOT METHODS
    public void screenshot_full_whole(){
        ScreenshotService.screenshot_full_whole();        
    }

    public void screenshot_full_page(){
        try{
            ScreenshotService.screenshot_full_page();
        } catch (Exception e){
            ScreenshotService.screenshot_full_whole();  
        }    
    }

    public void screenshot() {
        ScreenshotService.screenshot();
    }

    // WAIT METHODS
    public void wait_until_element_exist() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(MyConfig.waitDuration));
        WebElement visibleElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(TestFactory.getXpath(getObjectName()))));
    }

    public void wait_for_seconds() {
        try {
            Thread.sleep(Integer.parseInt(getValue())*1000);
        } catch (NumberFormatException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // OTHER METHODS
    public void switch_frame() {
        if (Execute.getValue().equalsIgnoreCase("PARENT FRAME")) {
            driver.switchTo().parentFrame();
        } else if (Execute.getValue().equalsIgnoreCase("NO FRAME")) {
            driver.switchTo().defaultContent();
        } else {
            driver.switchTo().frame(driver.findElement(By.xpath(TestFactory.getXpath(getObjectName()))));
        }
    }

    public void go_to_value() throws Exception {
        MyConfig.intCurrentRow = (ExcelReader.get_anchor_row_from_currentRow(MyConfig.datatableFile, getValue())-1);
        System.out.println("go_to_value -> "+getValue());
    }

    public void anchor_go_to_value() {
        System.out.println("anchor_go_to_value -> "+getValue());
    }

    public void save_data(){
        mapSaveData.put(getObjectName(), getValue());
    }

    public void send_key(){
        Keys key = Keys.valueOf(getValue());
        driver.findElement(By.xpath(TestFactory.getXpath(getObjectName()))).sendKeys(key);
    }

    public void for_data_by_sheet(){
        // [0] -> Sheet Name ; [1] -> Anchor Name
        String[] parts = getValue().split(";");
        tempIntDataNo = MyConfig.intCurrentDataNo;
        //TODO : baca jumlah data dari sheet dengan nomor data yang sama dan baca start no data pertama yang sesuai

    }
}
