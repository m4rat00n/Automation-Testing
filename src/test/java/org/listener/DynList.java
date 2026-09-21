package org.listener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import Services.ReportService;
import Services.ScreenshotService;

public class DynList implements ITestListener{

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getName();
        ScreenshotService.screenshot_full_whole();
        ReportService.appendDTStatus(result);
        ReportService.generateReport(result);
        System.out.println("Test Failed: " + testName);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = result.getName();
        ReportService.appendDTStatus(result);
        ReportService.generateReport(result);
        System.out.println("Test Passed: " + testName);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        System.out.println("Test Skipped: " + result.getName());
    }
}
