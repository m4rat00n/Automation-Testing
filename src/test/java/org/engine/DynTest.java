package org.engine;

import static Utils.MyConfig.driver;
import static Utils.MyConfig.intCurrentDataNo;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import Services.TestFactory;
import Utils.Execute;

public class DynTest {
    @Test
    public void DynamicTesting() throws Exception {
        try {
            intCurrentDataNo++;
            Execute execute = new Execute();
            execute.runExcelDrivenTest();
            System.out.println("Test execution completed successfully!");
        } catch (Exception error) {
            System.err.println("Error during test execution: " + error.getMessage());
            throw error;
        }
    }

    @AfterClass
    public void teardown() {
        if (driver != null) {
            TestFactory.teardown();
        }
    }
}
