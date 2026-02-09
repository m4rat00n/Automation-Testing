package Services;

import Utils.Execute;
import Utils.MyConfig;

import com.assertthat.selenium_shutterbug.core.Capture;
import com.assertthat.selenium_shutterbug.core.Shutterbug;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v128.page.Page;
import org.openqa.selenium.io.FileHandler;

import javax.imageio.ImageIO;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Optional;

import static Utils.MyConfig.driver;

public class ScreenshotService {
    static TakesScreenshot ssDriver = ((TakesScreenshot) driver);
    
    public static String getImageName(){

        String folderPath = ReportService.getReportFolder()
                + File.separator +"screenshots";
                
        String fileName = Execute.getScenario() + "_" + Execute.getTestCase();

        File destFolder = new File(folderPath);
        if (!destFolder.exists()) {
            destFolder.mkdirs();
        }

        String filePath = folderPath + File.separator + MyConfig.intCurrentDataNo + "_" + fileName + "_" +MyConfig.TestCaseCount+ "_" + MyConfig.screenshotCount++ + ".png";
        return filePath;
    }
    public static void screenshot(){
        String filePath = getImageName();

        try {
            File source = ssDriver.getScreenshotAs(OutputType.FILE);
            // String screenshotBase64 = ssDriver.getScreenshotAs(OutputType.BASE64);
            FileHandler.copy(source, new File(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void screenshot_full_whole() {
        String filePath = getImageName();
        
        try {
            BufferedImage image = Shutterbug.shootPage(driver, Capture.FULL_SCROLL).getImage();
            ImageIO.write(image, "png", new File(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void screenshot_full_page(){
        String filePath = getImageName();
        JavascriptExecutor js = (JavascriptExecutor) driver;

        long pageHeight = (long) js.executeScript(
            "return document.body.scrollHeight"
        );
        long viewportHeight = (long) js.executeScript(
            "return window.innerHeight"
        );

        Long stickyHeight = getHeaderHeight();

        double dpr = (double) js.executeScript("return window.devicePixelRatio");

        int screenshots = (int) Math.ceil(
            (double) (pageHeight * dpr) / (viewportHeight * dpr)
        );

        BufferedImage finalImage = new BufferedImage(
            1920,
            (int) ((pageHeight * dpr) - ((stickyHeight * dpr) * (screenshots-1))),
            BufferedImage.TYPE_INT_RGB
        );

        Graphics2D grp = finalImage.createGraphics();

        try {
            int heightTemp = 0;
            for (int i = 0; i < screenshots; i++) {
                js.executeScript("window.scrollTo(0, arguments[0])", ((i * viewportHeight)-(stickyHeight*dpr)));
                Thread.sleep(500);
                File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                BufferedImage img = ImageIO.read(src);
                
                int y = (int) (i * viewportHeight * dpr);
                int cropTopPx; //dpr
                if (i > 0) {
                    cropTopPx = Math.min((int) (getHeaderHeight() * dpr), img.getHeight() - 1);
                    if (i==1) {
                        heightTemp = y - cropTopPx;
                    }
                } else {
                    cropTopPx = img.getHeight() - ((int) (pageHeight * dpr) - y);
                }
                
                // if (y + img.getHeight() > (pageHeight * dpr)) {
                if (i > 0 && cropTopPx > 0) {
                    int plus = img.getHeight() - cropTopPx;
                    img = img.getSubimage(
                        0,
                        cropTopPx,
                        img.getWidth(),
                        // (int) (pageHeight * dpr) - y
                        img.getHeight() - cropTopPx
                    );
                    grp.drawImage(img, 0, heightTemp, null);
                    heightTemp += plus;
                } else {
                    grp.drawImage(img, 0, y, null);
                } 
            }

            ImageIO.write(finalImage, "png", new File(filePath));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getCause());
        }
    }

    private static Long getHeaderHeight(){
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Long stickyHeight = (Long) js.executeScript(
            "var max = 0;" +
            "document.querySelectorAll('header').forEach(h => {" +
            "  var s = getComputedStyle(h);" +
            "  if (s.position === 'fixed' || s.position === 'sticky') {" +
            "    max = Math.max(max, h.offsetHeight);" +
            "  }" +
            "});" +
            "return max;"
        );

        String frameName = null;
        if (stickyHeight == 0) {
            frameName = (String) ((JavascriptExecutor) driver)
                .executeScript(
                    "return window.frameElement ? (window.frameElement.name || window.frameElement.id || null) : null;"
                );

            driver.switchTo().defaultContent();
            stickyHeight = (Long) js.executeScript(
                "return document.getElementsByName('header')[0].clientHeight;"
            );
            driver.switchTo().frame(frameName);
        }
        return stickyHeight;
    }
}
