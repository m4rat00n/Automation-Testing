package org.main;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import Services.TestFactory;
import Utils.ExcelReader;
import Utils.MyConfig;

public class Main {

    /**
    public static void main(String[] args) throws NumberFormatException, Exception {
        String filePath = TestFactory.getProperties("datatablePath");
        MyConfig.mapStrXpath = new HashMap<>();
        MyConfig.datatableFile = filePath;
        String dataInfo = "DATA-INFO";

        MyConfig.intStartData = Integer.parseInt(ExcelReader.getStrCellValueByRow(filePath, dataInfo, 1, "Start Data"));
        MyConfig.intEndData = Integer.parseInt(ExcelReader.getStrCellValueByRow(filePath, dataInfo, 1, "End Data"));
        MyConfig.mapStrXpath = ExcelReader.readXpathSheet(filePath, "XPATH");

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
    */

    public static void main(String[] args) throws Exception {
        String datatablePath = TestFactory.getProperties("datatablePath");
        Path configuredPath = Paths.get(datatablePath);

        if (!Files.exists(configuredPath)) {
            throw new IllegalArgumentException("Datatable path tidak ditemukan: " + configuredPath.toAbsolutePath());
        }

        List<Path> excelFiles = getExcelFiles(configuredPath);
        if (excelFiles.isEmpty()) {
            throw new IllegalStateException("Tidak ditemukan file Excel pada: " + configuredPath.toAbsolutePath());
        }

        for (int fileIndex = 0;fileIndex < excelFiles.size();fileIndex++) {
            Path excelFile = excelFiles.get(fileIndex);
            System.out.println("========================================");
            System.out.println("Menjalankan datatable " + (fileIndex + 1) + " dari " + excelFiles.size());
            System.out.println("File: " + excelFile.getFileName());
            System.out.println("========================================");
            runDatatable(excelFile);
        }

        System.out.println("Seluruh datatable selesai dijalankan.");
    }

    private static List<Path> getExcelFiles(Path configuredPath) throws Exception {
        if (Files.isRegularFile(configuredPath)) {
            if (!isExcelFile(configuredPath)) {
                throw new IllegalArgumentException("File bukan datatable Excel: " + configuredPath);
            }
            return List.of(configuredPath);
        }

        try (Stream<Path> files = Files.list(configuredPath)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(Main::isExcelFile)
                    .filter(Main::isNotTemporaryExcelFile)
                    .sorted(Comparator.comparing(path -> path.getFileName().toString(),String.CASE_INSENSITIVE_ORDER))
                    .collect(Collectors.toList());
        }
    }

    private static boolean isExcelFile(Path file) {
        String fileName = file.getFileName().toString().toLowerCase(Locale.ROOT);
        return fileName.endsWith(".xlsx") || fileName.endsWith(".xls");
    }

    private static boolean isNotTemporaryExcelFile(Path file) {
        return !file.getFileName().toString().startsWith("~$");
    }

    private static void runDatatable(Path excelFile) throws Exception {
        String filePath = excelFile.toString();
        String fileName = excelFile.getFileName().toString();
        String dataInfo = "DATA-INFO";

        MyConfig.mapStrXpath = new HashMap<>();
        MyConfig.datatableFile = filePath;
        MyConfig.intStartData = Integer.parseInt(ExcelReader.getStrCellValueByRow(filePath,dataInfo,1,"Start Data").trim());
        MyConfig.intEndData = Integer.parseInt(ExcelReader.getStrCellValueByRow(filePath,dataInfo,1,"End Data").trim());
        MyConfig.mapStrXpath = ExcelReader.readXpathSheet(filePath,"XPATH");

        validateDataRange(fileName);

        XmlSuite suite = new XmlSuite();

        suite.setName("AutomationSuite_" + removeExcelExtension(fileName));
        suite.addListener("org.DynamicServices.DynamicListener");
        suite.setParallel(XmlSuite.ParallelMode.NONE);

        List<XmlTest> tests = new ArrayList<>();

        for (int startData = MyConfig.intStartData; startData <= MyConfig.intEndData;startData++) {
            XmlTest test =new XmlTest(suite);
            test.setName("AutomationTest_" + startData);
            test.addParameter("dataNumber",String.valueOf(startData));
            XmlClass testClass = new XmlClass("org.DynamicServices.DynamicTest");
            test.setXmlClasses(List.of(testClass));
            tests.add(test);
        }
        suite.setTests(tests);
        TestNG testNG = new TestNG();
        testNG.setXmlSuites(List.of(suite));
        testNG.run();

        if (testNG.hasFailure()) {
            System.out.println("Datatable selesai dengan failure: " + fileName);
        } else {
            System.out.println("Datatable berhasil dijalankan: " + fileName);
        }
    }

    private static void validateDataRange(String fileName) {
        if (MyConfig.intEndData < MyConfig.intStartData) {
            throw new IllegalArgumentException("End Data lebih kecil dari Start Data pada file " + fileName);
        }
    }

    private static String removeExcelExtension(String fileName) {
        return fileName.replaceFirst(
                "(?i)\\.xlsx?$",
                ""
        );
    }
}