package org.DynamicServices;

import org.testng.ITestListener;
import org.testng.ITestResult;

import Services.ReportService;
import Services.ScreenshotService;

public class DynamicListener implements ITestListener {
    @Override
    public void onTestFailure(ITestResult result) {
        String meesage = result.getThrowable().getMessage();
        if (meesage != null && meesage.contains("(Session info")) {
            meesage = meesage.substring(0, meesage.indexOf("(Session info")).trim();
            System.out.println("Test Failed: " + meesage);
        } else {
            System.out.println("Test Failed: ");
            result.getThrowable().printStackTrace();
        }
        
        ScreenshotService.screenshot_full_whole();
        ReportService.appendDTStatus(result);
        ReportService.generateReport(result);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ReportService.appendDTStatus(result);
        ReportService.generateReport(result);
        System.out.println("Test Passed: " + result.getName());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        System.out.println("Test Skipped: " + result.getThrowable().getCause());
    }
}
