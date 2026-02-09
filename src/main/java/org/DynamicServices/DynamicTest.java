package org.DynamicServices;

import static Utils.MyConfig.driver;
import static Utils.MyConfig.intCurrentDataNo;

import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import Services.TestFactory;
import Utils.Execute;
import Utils.MyConfig;

public class DynamicTest {
    @Test
    public void DynamicTesting(ITestContext context) throws Exception {
        try {
            String testName = context.getName();
            MyConfig.intCurrentDataNo = Integer.parseInt(testName.split("_")[1]);
            Execute execute = new Execute();
            execute.runExcelDrivenTest();
            System.out.println("Test execution completed successfully!");
        } catch (Exception error) {
            throw error;
        }
    }

    @AfterClass
    public void teardown() {
        if (driver != null) {
            for (String handle : driver.getWindowHandles()) {
                driver.switchTo().window(handle);
                TestFactory.teardown();
            }
        }
    }
}
