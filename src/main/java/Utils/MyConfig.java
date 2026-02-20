package Utils;

import org.openqa.selenium.WebDriver;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MyConfig {
    //Automation Properties
    public static String datatableFile = "";
    public static HashMap<String, String> mapSaveData = new HashMap<String, String>();
    //driverType : web/mobile
    public static String driverType = "mobile";
    public static int waitDuration = 120;
    public static String actionSheetName = "";

    //Global Variable
    public static WebDriver driver;
    public static String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    public static Map<String, String> mapStrXpath = new HashMap<String, String>();

    // Screenshot and Test Case Counters
    public static int screenshotCount = 1;
    public static int TestCaseCount = 0;
    public static int intScreenshotDataNo = 0;

    // Data integer row numbers
    public static int intStartData = 1;
    public static int intEndData = 1;
    public static int intCurrentDataNo = 1;
    public static int intCurrentRow = 0;
    public static int tempIntDataNo = 0;

    // Looping Variables
    public static int intLoopCounter = 0;
    public static int intTotalLoopCounter = 0;
    public static int tempCurrentRow = 0;
}
