package Services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONObject;
import org.testng.annotations.Test;

import Utils.Execute;
import Utils.MyConfig;

public class TestFactory {
    public static void teardown() {
        MyConfig.driver.quit();
    }

    public static String getXpath(String key){
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
}
