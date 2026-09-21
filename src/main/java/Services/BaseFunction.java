package Services;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import Utils.ExcelReader;
import Utils.Execute;
import static Utils.Execute.getObjectName;
import static Utils.Execute.getValue;
import static Utils.Execute.setValue;
import Utils.MyConfig;
import static Utils.MyConfig.driver;
import static Utils.MyConfig.intCurrentDataNo;
import static Utils.MyConfig.intCurrentRow;
import static Utils.MyConfig.intLoopCounter;
import static Utils.MyConfig.intTotalLoopCounter;
import static Utils.MyConfig.mapSaveData;
import static Utils.MyConfig.tempCurrentRow;
import static Utils.MyConfig.tempIntDataNo;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

public class BaseFunction {

    private static final Set<String> VALID_SWIPE_DIRECTIONS =
        Set.of("up", "down", "left", "right");

    private void sleep(int seconds){
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException ignored) {}
    }
    private void into_view(){
        if (MyConfig.driverType.equalsIgnoreCase("web") || (MyConfig.driverType.equalsIgnoreCase("mobile") && TestFactory.getProperties("browser/native").equalsIgnoreCase("browser"))) {
            WebElement element = driver.findElement(By.xpath(TestFactory.getXpath(getObjectName())));
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].scrollIntoView({block:'center'});", element);
            sleep(1);
        }
    }

    private boolean isMobile() {
        return driver instanceof io.appium.java_client.AppiumDriver;
    }

    private boolean isAndroid() {
        return driver instanceof io.appium.java_client.android.AndroidDriver;
    }
    
    private void hideKeyboardSafe() {
        try {
            if (driver instanceof AndroidDriver) {
                ((AndroidDriver) driver).hideKeyboard();
            } else if (driver instanceof IOSDriver) {
                ((IOSDriver) driver).executeScript("mobile: hideKeyboard");
            }
        } catch (Exception ignored) {}
    }

    //Methodnya mulai sini yaa
    public void debug(){
        sleep(1);
    }

    public void start_new_driver() {
        TestFactory.start_driver();
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
        sleep(1);
        into_view();
        new WebDriverWait(driver, Duration.ofSeconds(10))
            .ignoring(StaleElementReferenceException.class)
            .until(webDriver -> {
                WebElement el = webDriver.findElement(By.xpath(TestFactory.getXpath(getObjectName())));
                el.click();
                return true;
            });
    }

    public void click_js() {
        sleep(1);
        into_view();
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        WebElement element = driver.findElement(By.xpath(TestFactory.getXpath(getObjectName())));
        jsExecutor.executeScript("arguments[0].click();", element);
    }

    public void click_replace() {
        if (getValue().startsWith("[") && getValue().endsWith("]")) {
            setValue(mapSaveData.get(getValue()));
        }
        sleep(1);
        new WebDriverWait(driver, Duration.ofSeconds(10))
            .ignoring(StaleElementReferenceException.class)
            .until(webDriver -> {
                WebElement el = webDriver.findElement(By.xpath(TestFactory.getXpath(getObjectName()).replace("%", getValue())));
                el.click();
                return true;
            });
    }

    // SET TEXT METHODS
    public void set_text() {
        if (!getValue().equalsIgnoreCase("")) {
            sleep(1);
            into_view();
            if (getValue().startsWith("[") && getValue().endsWith("]")) {
                setValue(mapSaveData.get(getValue()));
            }

            By element = By.xpath(TestFactory.getXpath(getObjectName()));
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(dver -> {
                try {
                    WebElement ele = dver.findElement(element);
                    if (!isMobile()) {
                        ele.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
                    } else if (isAndroid()) {
                        ele.click();
                        try {
                            ((AppiumDriver) driver).executeScript(
                                "mobile: clear",
                                Map.of("elementId", ((RemoteWebElement) ele).getId())
                            );
                        } catch (Exception e) {
                            ele.clear();
                        }
                    } else {
                        ele.click();
                        ele.clear();
                    }

                    ele.sendKeys(getValue());
                    hideKeyboardSafe();
                    return true;
                } catch (StaleElementReferenceException e) {
                    return false;
                }
            });
        }
    }

    public void set_text_hidden() {
        if (!getValue().equalsIgnoreCase("")) {
            if (getValue().startsWith("[") && getValue().endsWith("]")) {
                setValue(mapSaveData.get(getValue()));
            }
            JavascriptExecutor js = (JavascriptExecutor) driver;
            WebElement element = driver.findElement(By.xpath(TestFactory.getXpath(getObjectName())));
            
            js.executeScript(
                "arguments[0].removeAttribute('hidden');" +
                "arguments[0].style.display='block';" +
                "arguments[0].style.visibility='visible';" +
                "arguments[0].style.opacity=1;",
                element
            );

            new WebDriverWait(driver, Duration.ofSeconds(10))
                .ignoring(StaleElementReferenceException.class)
                .until(webDriver -> {
                    WebElement el = webDriver.findElement(By.xpath(TestFactory.getXpath(getObjectName())));
                    try {
                        el.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
                    } catch (Exception e){
                        el.clear();
                    }
                    el.sendKeys(getValue());
                    return true;
                });
        }
    }

    public void select(){
        into_view();
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
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(TestFactory.getXpath(getObjectName()))));
    }

    public void wait_until_element_gone() {
        if (driver.findElements(By.xpath(TestFactory.getXpath(getObjectName()))).size() > 0) {
            wait_until_element_gone();
        }
    }

    public void wait_for_seconds() {
        try {
            Thread.sleep(Integer.parseInt(getValue())*1000);
        } catch (NumberFormatException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // OTHER METHODS
    public void get_text() {
        String text = driver.findElement(By.xpath(TestFactory.getXpath(getObjectName()))).getText();
        MyConfig.mapSaveData.put(getValue(), text);
    }

    public void switch_frame() {
        if (Execute.getValue().equalsIgnoreCase("PARENT FRAME")) {
            driver.switchTo().parentFrame();
        } else if (Execute.getValue().equalsIgnoreCase("NO FRAME")) {
            driver.switchTo().defaultContent();
        } else {
            driver.switchTo().frame(driver.findElement(By.xpath(TestFactory.getXpath(getObjectName()))));
        }
    }

    public void go_to_value() {
        try {
            intCurrentRow = (ExcelReader.get_anchor_row_from_currentRow(MyConfig.datatableFile, "anchor_go_to_value", getValue())-1);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getCause());
        }
        System.out.println("go_to_value -> "+getValue());
    }

    public void anchor_go_to_value() {
        System.out.println("anchor_go_to_value -> "+getValue());
    }

    public void if_element_exist_go_to_value() {
        if (driver.findElements(By.xpath(TestFactory.getXpath(getObjectName()))).isEmpty()) {
            intCurrentRow = (ExcelReader.get_anchor_row_from_currentRow(MyConfig.datatableFile, "anchor_go_to_value", (getValue() + " NOT EXIST"))-1);
        }
    }

    public void if_null_go_to_value() {
        if (getValue() == null || getValue().isEmpty()) {
            intCurrentRow = (ExcelReader.get_anchor_row_from_currentRow(MyConfig.datatableFile, "anchor_go_to_value", ("NULL VALUE"))-1);
        }
    }

    public void save_data(){
        mapSaveData.put(getObjectName(), getValue());
    }

    public void send_key(){
        Keys key = Keys.valueOf(getValue());
        driver.findElement(By.xpath(TestFactory.getXpath(getObjectName()))).sendKeys(key);
    }

    public void input_text_robot(){
        try {
            Robot robot = new Robot();
            for (char c : getValue().toCharArray()) {
                int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
                if (KeyEvent.CHAR_UNDEFINED == keyCode) {
                    throw new RuntimeException(
                            "Key code not found for character '" + c + "'");
                }
                robot.keyPress(keyCode);
                robot.keyRelease(keyCode);
            }
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);
        } catch (AWTException | RuntimeException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getCause());
        }
    }

    public void for_data_by_sheet(){
        // [0] -> Sheet Name ; [1] -> Anchor Name
        ArrayList<Integer> arrData = new ArrayList<>();
        if (intTotalLoopCounter == 0) {
            arrData = ExcelReader.get_dataForSheet(MyConfig.intCurrentDataNo, getValue());
        } else {
            arrData = ExcelReader.get_dataForSheet(MyConfig.tempIntDataNo, getValue());
        }

        intTotalLoopCounter = arrData.size();
        String[] strSplit = getValue().split(";");
        try {
            if (intTotalLoopCounter == 0 || intLoopCounter == intTotalLoopCounter) {
                MyConfig.intCurrentRow = (ExcelReader.get_anchor_row_from_currentRow(MyConfig.datatableFile, "end_for_data", strSplit[1])-1);
            } else {
                if (intLoopCounter == 0) {
                    tempIntDataNo = MyConfig.intCurrentDataNo;
                    tempCurrentRow = intCurrentRow - 1;
                }
                intCurrentDataNo = arrData.get(intLoopCounter);
                System.out.println("Looping Data ke : "+ (intLoopCounter + 1));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getCause());
        }
    }

    public void end_for_data(){
        if (intLoopCounter < intTotalLoopCounter) {
            intLoopCounter += 1;
            MyConfig.intCurrentRow = tempCurrentRow;
        } else {
            MyConfig.intCurrentDataNo = tempIntDataNo;
            tempIntDataNo = 0;
            intTotalLoopCounter = 0;
            intLoopCounter = 0;
        }    
    }

    //mobile
    public void swipe_until_exist(){
        int maxSwipe = 10;
        int swipeCount = 0;
        while (driver.findElements(By.xpath(TestFactory.getXpath(getObjectName()))).isEmpty() && driver.findElement(By.xpath(TestFactory.getXpath(getObjectName()))).isDisplayed() && swipeCount < maxSwipe) {
            swipe("up");
            swipeCount++;
        }
    }

    public void screenshot_full_mobile(){
        String pageSource;
        int intScroll = 0;
        do {
            pageSource = driver.getPageSource();
            screenshot();
            swipe("up");
            sleep(5);
            intScroll++;
        } while (!pageSource.equals(driver.getPageSource()) && intScroll < 30);
    }

    private void swipe(String direction) {
        if (!isMobile()) {
            return;
        }

        String normalizedDirection
                = direction.toLowerCase(Locale.ROOT).trim();

        if (!VALID_SWIPE_DIRECTIONS.contains(normalizedDirection)) {
            throw new IllegalArgumentException(
                    "Direction harus: up, down, left, atau right. "
                    + "Direction diterima: " + direction
            );
        }

        String platformName = String.valueOf(
                TestFactory.getProperties("platformName")
        ).toLowerCase(Locale.ROOT);

        if (platformName.contains("android")) {
            swipeAndroid(normalizedDirection);
        } else if (platformName.contains("ios")) {
            swipeIOS(normalizedDirection);
        } else {
            throw new UnsupportedOperationException(
                    "Platform tidak didukung untuk swipe: " + platformName
            );
        }
    }

    private void swipeAndroid(String direction) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Dimension screenSize = driver.manage().window().getSize();

        int left = (int) (screenSize.width * 0.10);
        int top = (int) (screenSize.height * 0.20);
        int width = (int) (screenSize.width * 0.80);
        int height = (int) (screenSize.height * 0.60);

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("left", left);
        arguments.put("top", top);
        arguments.put("width", width);
        arguments.put("height", height);
        arguments.put("direction", direction);
        arguments.put("percent", 0.75);

        js.executeScript(
                "mobile: swipeGesture",
                arguments
        );
    }

    private void swipeIOS(String direction) {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("direction", direction);
        arguments.put("velocity", 1500);

        js.executeScript(
                "mobile: swipe",
                arguments
        );
    }

    // private void swipe(String direction) {
    //     if (isMobile()) {
    //         JavascriptExecutor js = (JavascriptExecutor) driver;
    //         Dimension size = driver.manage().window().getSize();
    //         // WebElement container = driver.findElement(By.xpath(TestFactory.getXpath(getObjectName())));
    //         // js.executeScript("mobile: swipeGesture", Map.of( 
    //         //     "elementId", ((RemoteWebElement) container).getId(), 
    //         //     "direction", "up", 
    //         //     "percent", 0.75 ));
    //         js.executeScript("mobile: swipeGesture", Map.of(
    //             "left", size.width * 0.5,
    //             "top", size.height * 0.75,
    //             "width", 1,
    //             "height", size.height * 0.25,
    //             "direction", direction,
    //             "percent", 0.75
    //         ));
    //     }
    // }


}
