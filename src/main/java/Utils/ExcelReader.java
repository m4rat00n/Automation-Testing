package Utils;

import static Utils.MyConfig.actionSheetName;
import static Utils.MyConfig.intCurrentDataNo;
import static Utils.MyConfig.intCurrentRow;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelReader {
    public static List<Map<String, String>> readSheet(String filePath, String sheetName, int row) throws Exception {
        List<Map<String, String>> data = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

            DataFormatter formatter = new DataFormatter();

            XSSFSheet sheet = workbook.getSheet(sheetName);

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return data;
            }

            int colCount = headerRow.getLastCellNum();

            Row currentRow = sheet.getRow(row);

            Map<String, String> rowData = new HashMap<>();

            for (int j = 0; j < colCount; j++) {
                String header = "";
                if (headerRow.getCell(j) != null) {
                    header = formatter.formatCellValue(headerRow.getCell(j));
                }

                Cell cell = currentRow.getCell(j);
                
				String value = getCellValue(cell, workbook);		
                rowData.put(header, value);
            }

            data.add(rowData);

        } catch (Exception e) {
            e.printStackTrace();
			throw new Exception(e.getCause());
        }
        return data;
    }

    public static String getCellValue (Cell cell, XSSFWorkbook workbook) {
		DataFormatter formatter = new DataFormatter();
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        String value = "";
		if (cell != null) {
			CellType type = cell.getCellType();
			switch (type) {
				case STRING:
					value = cell.getStringCellValue();
					break;
				case NUMERIC:
					value = formatter.formatCellValue(cell);
					break;
				case BOOLEAN:
					value = Boolean.toString(cell.getBooleanCellValue());
					break;
				case FORMULA:
					try {
                        // Modify formula to use dynamic row number
                        String formula = cell.getCellFormula();
						formula = "IF(" + formula + "=\"\",\"\","+ formula + ")";
                        formula = formula.replaceAll("\\$1", String.valueOf(intCurrentDataNo+1));
                        cell.setCellFormula(formula);
                        evaluator.evaluateInCell(cell);
						value = formatter.formatCellValue(cell, evaluator);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					break;
				case BLANK:
					value = "";
					break;
				case ERROR:
					value = "#ERROR";
					break;
				default:
					value = formatter.formatCellValue(cell);
			}
		}
		return value;
    }

	public static int intNoHeaderCol(String filePath, String sheetName, String headerName) throws Exception{
		int incCol = 0 ;
		Boolean foundFlag = false;
		FileInputStream fis = new FileInputStream(filePath);
		XSSFWorkbook workbook = new XSSFWorkbook(fis);
		XSSFSheet sheet = workbook.getSheet(sheetName);
		Row row = sheet.getRow(0);
		for (int colNum = 0; colNum < row.getLastCellNum(); colNum++) {
			Cell cell = row.getCell(colNum);
			String cellValue = getCellValue(cell, workbook);
			if (cellValue.equalsIgnoreCase(headerName)) {
				incCol = colNum;
				foundFlag = true;
				break;
			}
		}
		if (!foundFlag) {
			System.out.println("[FLAG ERROR] "+headerName+" Not Found");
		}
		return incCol;
	}

	public static int get_anchor_row_from_currentRow (String filePath, String Keyword, String strValue) {
		int intRow = 0 ;
		Boolean foundFlag = false;
		try {
			FileInputStream fis = new FileInputStream(filePath);
			XSSFWorkbook workbook = new XSSFWorkbook(fis);
			XSSFSheet sheet = workbook.getSheet(actionSheetName);
			for (int i=intCurrentRow ; i<sheet.getLastRowNum()+1; i++){
				String strKeyword = getStrCellValueByRow(filePath, actionSheetName, i, "Keyword");
				String value = getStrCellValueByRow(filePath, actionSheetName, i, "Value");
				if (strKeyword.equalsIgnoreCase(Keyword) && value.equalsIgnoreCase(strValue)) {
					intRow = i;
					foundFlag = true;
					break;
				}
			}
			if (!foundFlag) {
				System.out.println("[FLAG ERROR] anchor_go_to_value with "+strValue+" Not Found");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getCause());
		}
		return intRow;
	}

	public static String getStrCellValueByRow(String filePath, String sheetName, int rowNum ,String headerName) {
		String value = "";
		Boolean foundFlag = false;
		FileInputStream fis;
		XSSFWorkbook workbook = null;
		try {
			fis = new FileInputStream(filePath);
			workbook = new XSSFWorkbook(fis);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		XSSFSheet sheet = workbook.getSheet(sheetName);
		Row row = sheet.getRow(0);
		for (int colNum = 0; colNum < row.getLastCellNum(); colNum++) {
			Cell cell = row.getCell(colNum);
			String cellValue = getCellValue(cell, workbook);
			if (cellValue.equalsIgnoreCase(headerName)) {
				Cell targetCell = sheet.getRow(rowNum).getCell(colNum);
				value = getCellValue(targetCell, workbook);
				foundFlag = true;
				break;
			}
		}
		if (!foundFlag) {
			System.out.println("[FLAG ERROR] "+headerName+" Not Found");
		}
		return value;
	}

	public static ArrayList<Integer> get_dataForSheet(int DataNo, String value){
		// [0] -> Sheet Name ; [1] -> Anchor Name
		ArrayList<Integer> arrDataList = new ArrayList<Integer>();

		String[] parts = value.split(";");
		FileInputStream fis;
		XSSFWorkbook workbook = null;
		String filePath = MyConfig.datatableFile;
		try {
			fis = new FileInputStream(filePath);
			workbook = new XSSFWorkbook(fis);
		} catch (Exception e) {
			e.printStackTrace();
		}
		XSSFSheet sheet = workbook.getSheet(parts[0]);
		for (int i = 0 ; i < sheet.getLastRowNum()+1; i++){
			String strNo = getStrCellValueByRow(filePath, parts[0], i, "NO");
			if (strNo.equalsIgnoreCase(String.valueOf(DataNo))) {
				arrDataList.add(i);
			}
		}

		return arrDataList;
	}

	public static Map<String, String> readXpathSheet(String filePath, String sheetName) throws Exception {
        List<String> data = new ArrayList<>();
		Map<String, String> rowData = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

            XSSFSheet sheet = workbook.getSheet(sheetName);

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return rowData;
            }

			for (int i=1; i < sheet.getLastRowNum()+1; i++){
				Row currentRow = sheet.getRow(i);
				data = new ArrayList<>();
				for (int j = 0; j < 2; j++) {
					Cell cell = currentRow.getCell(j);
					data.add(getCellValue(cell, workbook));		
				}
				rowData.put(data.get(0), data.get(1));
			}
        } catch (Exception e) {
            e.printStackTrace();
			throw new Exception(e.getCause());
        }
        return rowData;
    }
}
