package Services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import Utils.MyConfig;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

import java.net.URL;

public class TestFactory {
    public static void teardown() {
        MyConfig.driver.quit();
    }

    public static String getXpath(String key){
        /** JSON reading code, currently not in use as we are using HashMap to store the xpath values from Excel
        JSONObject root = new JSONObject();
        String json;
        try {
            json = new String(Files.readAllBytes(Paths.get("src\\main\\java\\Application\\"+Execute.getApplication()+"\\xpath.json")));
            root = new JSONObject(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Convert to JSON object
        return root.getJSONObject(key).getString("xpath_"+MyConfig.driverType);
        */
        return MyConfig.mapStrXpath.get(key);
    }

    public static void setXpath(String key, String value){
        MyConfig.mapStrXpath.replace(key, value);
    }

    public static String getChromeVersion () {
        String chromeVersion = null;
        try {
            Process proccess = Runtime.getRuntime().exec(
                "reg query \"HKEY_CURRENT_USER\\Software\\Google\\Chrome\\BLBeacon\" /v version"
            );
            BufferedReader reader = new BufferedReader(new InputStreamReader(proccess.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("version")) {
                    chromeVersion = line.split("\\s+")[line.split("\\s+").length - 1];
                    chromeVersion = chromeVersion.substring(0, chromeVersion.indexOf('.'));
                    return chromeVersion;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot detect Chrome version", e);
        }
        
        return chromeVersion;
    }

    public static String getProperties(String key) {
        Properties prop = new Properties();
        try {
            InputStream is = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("utils/Automation.properties");
            prop.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties file: " + e.getMessage());
        }
        return prop.getProperty(key);
    }

    public static void start_driver() {
        try {
            if (MyConfig.driverType.equalsIgnoreCase("web")) {
                if (getProperties("browser").equalsIgnoreCase("chrome")) {
                    WebDriverManager.chromedriver().setup(); //automatically download chromedriver

                    // System.setProperty("webdriver.chrome.driver", "LocalDriver\\chromedriver" + TestFactory.getChromeVersion()+ ".exe");

                    ChromeOptions options = new ChromeOptions();
                    options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
                    options.addArguments("--start-maximized");
                    options.addArguments(Arrays.asList("--no-sandbox","--ignore-certificate-errors","--homepage=about:blank","--no-first-run"));
                    options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
                    options.addArguments("--disable-notifications");
                    options.addArguments("--disable-infobars");

                    if (Boolean.parseBoolean(getProperties("headless"))) {
                        options.addArguments("--headless=new");
                    }

                    Map<String, Object> prefs = new HashMap<>();
                    prefs.put("credentials_enable_service", false);
                    prefs.put("profile.password_manager_enabled", false);
                    options.setExperimentalOption("prefs", prefs);
                    
                    MyConfig.driver = new ChromeDriver(options);
                } 
            } else if (MyConfig.driverType.equalsIgnoreCase("mobile")) {
                if (getProperties("platformName").equalsIgnoreCase("android")) {
                    // Setup Appium driver for Android
                    DesiredCapabilities capabilities = new DesiredCapabilities();
                    capabilities.setCapability("platformName", "Android");
                    if (getProperties("browser/native").equalsIgnoreCase("native")) {
                        capabilities.setCapability("appPackage", getProperties("appPackage"));
                        capabilities.setCapability("appActivity", getProperties("appActivity"));
                    } else {
                        capabilities.setCapability("browserName", getProperties("browser"));
                    }
                    capabilities.setCapability("automationName", "UiAutomator2");
                    capabilities.setCapability("autoLaunch", true);
                    capabilities.setCapability("autoGrantPermissions", true);
                    MyConfig.driver = new AndroidDriver(new URL(getProperties("urlServer")), capabilities);
                } else if (getProperties("platformName").equalsIgnoreCase("ios")) {
                    // Setup Appium driver for iOS
                    DesiredCapabilities capabilities = new DesiredCapabilities();
                    capabilities.setCapability("platformName", "iOS");
                    capabilities.setCapability("automationName", "XCUITest");
                    capabilities.setCapability("deviceName", getProperties("deviceName"));
                    capabilities.setCapability("platformVersion", getProperties("platformVersion"));
                    if (getProperties("browser/native").equalsIgnoreCase("native")) {
                        capabilities.setCapability("bundleId", getProperties("bundleId"));
                    } else {
                        capabilities.setCapability("browserName", getProperties("browser"));
                    }
                    capabilities.setCapability("udid", getProperties("udID"));
                    capabilities.setCapability("autoLaunch", true);
                    capabilities.setCapability("autoGrantPermissions", true);
                    MyConfig.driver = new IOSDriver(new URL(getProperties("urlServer")), capabilities);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to start driver: " + e.getMessage());
        }
    }
}
