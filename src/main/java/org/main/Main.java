package org.main;

import static Utils.MyConfig.intEndData;
import static Utils.MyConfig.intStartData;

import java.util.ArrayList;
import java.util.List;

import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import Utils.ExcelReader;
import Utils.MyConfig;

public class Main {
    public static void main(String[] args) throws NumberFormatException, Exception {
        String filePath = MyConfig.datatablePath;
        MyConfig.datatableFile = filePath;
        String dataInfo = "DATA-INFO";

        MyConfig.intStartData = Integer.valueOf(ExcelReader.getStrCellValueByRow(filePath, dataInfo, 1, "Start Data"));
        MyConfig.intEndData = Integer.valueOf(ExcelReader.getStrCellValueByRow(filePath, dataInfo, 1, "End Data"));

        // MyConfig.intCurrentDataNo = MyConfig.intStartData;
        
        XmlSuite suite = new XmlSuite();
        suite.setName("AutomationSuite");
        suite.addListener("org.DynamicServices.DynamicListener");

        List<XmlTest> tests = new ArrayList<>();
        for (int startData = intStartData; startData <= intEndData; startData++){
            XmlTest test = new XmlTest(suite);
            test.setName("AutomationTest_" + startData);
            XmlClass testClass = new XmlClass("org.DynamicServices.DynamicTest");
            test.setXmlClasses(List.of(testClass));
            tests.add(test);
        }
        
        suite.setTests(tests);
        TestNG testNG = new TestNG();
        testNG.setXmlSuites(List.of(suite));
        testNG.run();
    }
}