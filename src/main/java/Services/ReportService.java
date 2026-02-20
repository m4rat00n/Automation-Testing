package Services;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import javax.imageio.ImageIO;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.TableRowAlign;
import org.apache.poi.xwpf.usermodel.XWPFAbstractNum;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFNumbering;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.officeDocument.x2006.sharedTypes.STOnOff;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTAbstractNum;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFonts;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTLvl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSpacing;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTrPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STNumberFormat;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STSectionMark;
import org.testng.ITestResult;
import org.testng.annotations.AfterTest;

import Utils.ExcelReader;
import Utils.MyConfig;

public class ReportService {

    private static BigInteger master_heading_number;

    private static String getDTFileName(){
        Path dtFile = Paths.get(MyConfig.datatableFile);
        String dtFileName = dtFile.getFileName().toString().replace(".xlsx", "").replace(".xlsm", "");
        return dtFileName;
    }

    public static String getReportFolder() {
        String reportPath = System.getProperty("user.dir") 
                + File.separator + "Report" 
                + File.separator + getDTFileName()
                + File.separator + MyConfig.timestamp.split("_")[0] 
                + File.separator + MyConfig.timestamp.split("_")[1] 
                + File.separator;

        return reportPath;
    }

    private static void makeFolder(String path) {
        File destFolder = new File(path);
        if (!destFolder.exists()) {
            destFolder.mkdirs();
        }
    }

    public static void appendDTStatus(ITestResult result) {
        String reportPath = getReportFolder();
        Path source = Paths.get(MyConfig.datatableFile);
        makeFolder(reportPath);

        String reportFilePath = reportPath + "Report " + source.getFileName();
        copyWPFiles(reportPath, reportFilePath);
        String datatableSheet = "Datatable";
        try {
            int statusCol = ExcelReader.intNoHeaderCol(MyConfig.datatableFile, datatableSheet, "STATUS");
            int ketCol = ExcelReader.intNoHeaderCol(MyConfig.datatableFile, datatableSheet, "KETERANGAN");
            FileInputStream fis = new FileInputStream(reportFilePath);
            XSSFWorkbook workbook = new XSSFWorkbook(fis);

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                if (!workbook.getSheetName(i).equalsIgnoreCase(datatableSheet)) {
                    workbook.setSheetHidden(i, true);
                }
            }

            XSSFSheet sheet = workbook.getSheet(datatableSheet);
            Row row = sheet.getRow(MyConfig.intCurrentDataNo);
            Font font = workbook.createFont();
            CellStyle style = workbook.createCellStyle();
            if (result.getStatus() == ITestResult.FAILURE){
                if (row.getCell(statusCol) == null) {row.createCell(statusCol);}
                if (row.getCell(ketCol) == null) {row.createCell(ketCol);}
                row.getCell(statusCol).setCellValue("FAILED");
                font.setColor(IndexedColors.RED.getIndex());
                row.getCell(ketCol).setCellValue((result.getThrowable().getMessage() != null && result.getThrowable().getMessage().contains("(Session info")) 
                    ? result.getThrowable().getMessage().substring(0, result.getThrowable().getMessage().indexOf("(Session info")).trim() 
                    : result.getThrowable().getMessage()
                );
            }
            else if (result.getStatus() == ITestResult.SUCCESS){
                if (row.getCell(statusCol) == null) {row.createCell(statusCol);}
                row.getCell(statusCol).setCellValue("PASSED");
                font.setColor(IndexedColors.BLUE.getIndex());
            }
            else if (result.getStatus() == ITestResult.SKIP){
                if (row.getCell(statusCol) == null) {row.createCell(statusCol);}
                row.getCell(statusCol).setCellValue("SKIPPED");
                font.setColor(IndexedColors.GREY_80_PERCENT.getIndex());
            }
            font.setBold(true);
            style.setFont(font);
            row.getCell(statusCol).setCellStyle(style);

            sheet.autoSizeColumn(statusCol);
            sheet.autoSizeColumn(ketCol);
            FileOutputStream writeOut = new FileOutputStream(reportFilePath);
            workbook.write(writeOut);
            writeOut.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void copyWPFiles(String reportPath, String reportFilePath) {
        Path source = Paths.get(MyConfig.datatableFile);
        File targetFile = new File(reportFilePath);
        Path targetDir = Paths.get(reportPath);
        try {
            if (!targetFile.exists()) {
                Files.copy(
                    source,
                    targetDir.resolve("Report " + source.getFileName()),
                    StandardCopyOption.REPLACE_EXISTING
                );
            } 
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("null")
    public static void generateReport() {
        String reportPath = getReportFolder();
        makeFolder(reportPath);
        File file = new File(reportPath + File.separator + getDTFileName() + ".docx");
        XWPFDocument document = null;
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                document = new XWPFDocument(fis);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        } else {
            document = new XWPFDocument();
            createHeaderFooter(document);
        }
        
        String screenshotPath = getReportFolder() + File.separator + "screenshots"+ File.separator ;
        File screenshotDir = new File(screenshotPath);
        File[] screenshotFiles = screenshotDir.listFiles();

        //Sort the files by date modified and ascending
        Arrays.sort(screenshotFiles, Comparator.comparingLong(File::lastModified));
        String[] arrScreenshotFiles = screenshotFiles != null ? 
            Arrays.stream(screenshotFiles).map(File::getName).toArray(String[]::new) : new String[0];
        ArrayList<String> sortedScreenshots = new ArrayList<String>();
        for (String fileName : arrScreenshotFiles) {
            if (fileName.startsWith(MyConfig.intCurrentDataNo+"_")) {
                sortedScreenshots.add(fileName);
            }
        }
        
        Integer lastCount = Integer.parseInt(sortedScreenshots.get(sortedScreenshots.size()-1).split("_")[3]);
        String strTestcase, strTestcaseCount = "0";
        int intFlagSub = 1, intCountSub = 1;
        XWPFTable table = null;
        for (String screenshotFile : sortedScreenshots) {
            String[] splitImgName = screenshotFile.split("_");
            if (!strTestcaseCount.equalsIgnoreCase(splitImgName[3])) {
                if (strTestcaseCount.equals("0")) {
                    // Create Heading 1 for Testcase
                    XWPFParagraph h1 = document.createParagraph();
                    h1.setStyle("Heading1");
                    master_heading_number = getNumID_Heading(document);
                    h1.setNumID(master_heading_number);
                    h1.setNumILvl(BigInteger.ZERO);

                    h1.getCTP()
                        .getPPr()
                        .addNewOutlineLvl()
                        .setVal(BigInteger.ZERO);
                    
                    XWPFRun r1 = h1.createRun();
                    if (!splitImgName[0].equalsIgnoreCase(String.valueOf(MyConfig.intStartData))) {
                        r1.addBreak(BreakType.COLUMN);
                    }
                    r1.setText(splitImgName[1]);
                    setFont(r1, "Arial", 12, true);

                    String jenisTest = ExcelReader.getStrCellValueByRow(MyConfig.datatableFile, "DATA-INFO", 1, "SIT/UAT/REGRESI");
                    table = document.createTable();
                    table.setTableAlignment(TableRowAlign.CENTER);
                    XWPFTableRow headerRow = table.getRow(0);
                    headerRow.getCell(0).setText("No.");
                    headerRow.addNewTableCell().setText("Hasil " + (jenisTest.equalsIgnoreCase("SIT") ? "System Integration Testing (SIT)" : 
                         jenisTest.equalsIgnoreCase("UAT") ? "User Acceptance Testing (UAT)" : "Regression Testing"));

                    for (XWPFTableCell cell : headerRow.getTableCells()) {
                        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
                        cell.setColor("006747");
                        XWPFParagraph para = cell.getParagraphs().get(0);
                        setSpacing(para, true);
                        XWPFRun run = para.getRuns().get(0);
                        setFont(run, "Arial", 10, true);
                        run.setColor("FFFFFF");
                    }
                         
                    setRepeatHeader(headerRow);

                    for (int i = 1; i <= lastCount*3; i++) {
                        XWPFTableRow row = table.createRow();
                    }
                    table.setWidth("100%");
                    table.getRow(0).getCell(0).setWidth("8%");
                    table.getRow(0).getCell(1).setWidth("92%");
                }                
            }

            if (!strTestcaseCount.equalsIgnoreCase(splitImgName[3])) {
                XWPFTableRow tableBody = table.getRow(intFlagSub);
                tableBody.getCell(0).removeParagraph(0);
                XWPFParagraph subNumber = tableBody.getCell(0).addParagraph();
                setSpacing(subNumber, true);
                XWPFRun headingText = subNumber.createRun();
                headingText.setText(master_heading_number + "." + intCountSub);
                setFont(headingText, "Arial", 10, false);
                
                tableBody.getCell(1).removeParagraph(0);
                XWPFParagraph subHeaderText = tableBody.getCell(1).addParagraph();
                setSpacing(subHeaderText, false);
                XWPFRun cell1 = subHeaderText.createRun();
                cell1.setText(" ");
                cell1.setText(splitImgName[2]);
                cell1.setColor("4F6EA6");
                setFont(cell1, "Arial", 10, false);

                for (XWPFTableCell cell : tableBody.getTableCells()) {
                    cell.setColor("D0CECE");
                }

                intCountSub++;
                intFlagSub += 3;
            }

            XWPFTableRow imageBody = table.getRow(intFlagSub-2);
            if (!strTestcaseCount.equalsIgnoreCase(splitImgName[3])) {
                imageBody.getCell(1).removeParagraph(0);
            }
            XWPFParagraph imageCellPara = imageBody.getCell(1).addParagraph();
            setSpacing(imageCellPara, true);
            XWPFRun imageCellRun = imageCellPara.createRun();
            try {
                BufferedImage img = ImageIO.read(new File(screenshotPath + screenshotFile));
                int widthPx = img.getWidth();
                int heightPx = img.getHeight();
                int dpi = 96;
                double widthPt = Math.round((widthPx / (double) dpi) * 72);
                double heightPt = Math.round((heightPx / (double) dpi) * 72);
                
                if (MyConfig.driverType.equalsIgnoreCase("mobile")) {
                    heightPt = 265.2;
                    widthPt = 119.9;
                } else if (heightPt/widthPt > 1.5 && heightPt/widthPt < 4) {
                    heightPt = 550;
                    widthPt = 350;
                } else if (heightPt/widthPt > 4) {
                    heightPt = 561.26;
                    widthPt = 131.81;
                }else {
                    heightPt = 224.5;
                    widthPt = 400.4;
                }
                imageCellRun.addPicture(
                    new FileInputStream(screenshotPath + screenshotFile),
                    XWPFDocument.PICTURE_TYPE_PNG,
                    screenshotFile,
                    Units.toEMU(widthPt),
                    Units.toEMU(heightPt)
                );
            } catch (Exception error) {
                throw new RuntimeException(error);
            }

            //Initialize globally variables for testcase
            strTestcase = splitImgName[2];
            strTestcaseCount = splitImgName[3];
        }

        // Save file
        try {
            document.write(new FileOutputStream(file));
            document.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }  
    }
    
    private static void createHeaderFooter(XWPFDocument document) {
        setA4Layout(document);

        XWPFHeaderFooterPolicy policy = document.createHeaderFooterPolicy();

        // ===== HEADER =====
        XWPFHeader header = policy.createHeader(XWPFHeaderFooterPolicy.DEFAULT);
        XWPFTable table = header.createTable(1, 3);
        table.setWidth("100%");
        table.getRow(0).getCell(0).setWidth("30%");
        table.getRow(0).getCell(1).setWidth("39%");
        table.getRow(0).getCell(2).setWidth("30%");
        table.setTableAlignment(TableRowAlign.CENTER);
        XWPFTableCell logoCell = table.getRow(0).getCell(0);
        logoCell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        logoCell.removeParagraph(0);

        XWPFParagraph logoPara = logoCell.addParagraph();
        logoPara.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun logoRun = logoPara.createRun();

        try {
            logoRun.addPicture(
                new FileInputStream("SupportSources\\Logo_BSN.png"),
                XWPFDocument.PICTURE_TYPE_PNG,
                "Logo_BSN.png",
                Units.toEMU(80),
                Units.toEMU(60)
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        XWPFTableCell titleCell = table.getRow(0).getCell(1);
        titleCell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        XWPFParagraph titlePara = titleCell.getParagraphs().get(0);
        titlePara.setAlignment(ParagraphAlignment.CENTER);

        String jenisTest = ExcelReader.getStrCellValueByRow(MyConfig.datatableFile, "DATA-INFO", 1, "SIT/UAT/REGRESI");

        XWPFRun titleRun = titlePara.createRun();
        titleRun.setText("Dokumen Hasil Uji");
        titleRun.addBreak();
        titleRun.setText(jenisTest.equalsIgnoreCase("SIT") ? "System Integration Testing" : 
                         jenisTest.equalsIgnoreCase("UAT") ? "User Acceptance Testing" : "Regression Testing");
        titleRun.setBold(true);
        titleRun.setFontFamily("Arial");
        titleRun.setFontSize(12);

        XWPFParagraph p = header.createParagraph();
        p.createRun().setText("");

        // ===== FOOTER =====
        XWPFFooter footer = policy.createFooter(XWPFHeaderFooterPolicy.DEFAULT);

        XWPFParagraph footerPara = footer.createParagraph();
        footerPara.setAlignment(ParagraphAlignment.RIGHT);

        XWPFRun footerRun = footerPara.createRun();
        footerRun.setText("Page ");
        footerRun.setFontFamily("Arial");
        footerRun.setFontSize(8);

        // Add page number
        footerRun = footerPara.createRun();
        footerRun.getCTR().addNewFldChar().setFldCharType(
                org.openxmlformats.schemas.wordprocessingml.x2006.main.STFldCharType.BEGIN);
        footerRun = footerPara.createRun();
        footerRun.getCTR().addNewInstrText().setStringValue("PAGE");
        footerRun = footerPara.createRun();
        footerRun.getCTR().addNewFldChar().setFldCharType(
                org.openxmlformats.schemas.wordprocessingml.x2006.main.STFldCharType.END);  
    }

    private static void setA4Layout(XWPFDocument document) {
        CTSectPr sectPr = document.getDocument()
            .getBody()
            .addNewSectPr();

        CTPageSz pageSize = sectPr.addNewPgSz();
        pageSize.setW(BigInteger.valueOf(11906));
        pageSize.setH(BigInteger.valueOf(16838));
    }   

    private static BigInteger getNumID_Heading(XWPFDocument document){
        XWPFNumbering numbering = document.createNumbering();

        CTAbstractNum abstractNum = CTAbstractNum.Factory.newInstance();
        abstractNum.setAbstractNumId(BigInteger.valueOf(0));

        for (int i = 0; i < 3; i++) {
            CTLvl lvl = abstractNum.addNewLvl();
            lvl.setIlvl(BigInteger.valueOf(i));
            lvl.addNewNumFmt().setVal(STNumberFormat.DECIMAL);
            if (i == 0) {
                lvl.addNewLvlText().setVal("%1.");
            } else if (i == 1) {
                lvl.addNewLvlText().setVal("%1.%2.");
                lvl.addNewLvlRestart().setVal(BigInteger.ONE);
            } else if (i == 2) {
                lvl.addNewLvlText().setVal("%1.%2.%3.");
                lvl.addNewLvlRestart().setVal(BigInteger.ONE);
            }
            lvl.addNewStart().setVal(BigInteger.ONE);

            //Style formatting
            CTRPr rPr = lvl.addNewRPr();
            CTFonts fonts = rPr.addNewRFonts();
            fonts.setAscii("Arial");
            fonts.setHAnsi("Arial");
            rPr.addNewB().setVal(true);
        }

        XWPFAbstractNum absNum = new XWPFAbstractNum(abstractNum);
        BigInteger absNumId = numbering.addAbstractNum(absNum);
        BigInteger numId = numbering.addNum(absNumId);
        return numId;
    }

    private static void setRepeatHeader(XWPFTableRow row) {
        CTRow ctRow = row.getCtRow();
        CTTrPr trPr = ctRow.isSetTrPr() ? ctRow.getTrPr() : ctRow.addNewTrPr();
        trPr.addNewTblHeader().setVal(true);
    }

    private static void setSpacing(XWPFParagraph para, Boolean center){
        if (center) {
            para.setAlignment(ParagraphAlignment.CENTER);
        } else {
            para.setAlignment(ParagraphAlignment.LEFT);
        }   
        CTSpacing spacing = para.getCTP().getPPr().addNewSpacing();
        spacing.setAfter(BigInteger.valueOf(100));
        spacing.setBefore(BigInteger.valueOf(100));
    }

    private static void setFont(XWPFRun run, String fontFam, int fontSize, Boolean bold){
        run.setFontFamily(fontFam);
        run.setFontSize(fontSize);
        run.setBold(bold);
    }
}
