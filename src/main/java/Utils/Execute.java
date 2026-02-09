package Utils;

import Services.BaseFunction;
import Services.TestFactory;

import static Utils.MyConfig.TestCaseCount;
import static Utils.MyConfig.actionSheetName;
import static Utils.MyConfig.intCurrentDataNo;
import static Utils.MyConfig.intCurrentRow;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class Execute {

    protected static ThreadLocal<String> thrTestCase = new ThreadLocal<>();
    protected static ThreadLocal<String> thrApplication = new ThreadLocal<>();
    protected static ThreadLocal<String> thrValue = new ThreadLocal<>();
    protected static ThreadLocal<String> thrObjectName = new ThreadLocal<>();
    protected static ThreadLocal<String> thrScenario = new ThreadLocal<>();

    //get method for ThreadLocal variables
    public static String getValue() { return thrValue.get(); }
    public static String getTestCase() { return thrTestCase.get(); }
    public static String getObjectName() { return thrObjectName.get(); }
    public static String getScenario() { return thrScenario.get(); }
    public static String getApplication() { return thrApplication.get(); }

    //set method for ThreadLocal variables
    public static void setValue(String value) { thrValue.set(value); }

    public void runExcelDrivenTest() throws Exception {
        String filePath = MyConfig.datatablePath;
        TestCaseCount = 0;
        System.out.println("\n================= Starting Data No: "+ intCurrentDataNo +" =================");
        String datatable = "Datatable";
        Boolean isrunning = false;
        actionSheetName = ExcelReader.getStrCellValueByRow(filePath, datatable, intCurrentDataNo, "ACTION");
        thrScenario.set(ExcelReader.getStrCellValueByRow(filePath, datatable, intCurrentDataNo, "SCENARIO"));

        List<Map<String, String>> rows = ExcelReader.readSheet(filePath, actionSheetName);
        BaseFunction base = new BaseFunction();

        for (intCurrentRow = 0; intCurrentRow < rows.size() ; intCurrentRow++) {
            Map<String, String> row = rows.get(intCurrentRow);
            String TestCase = row.get("TestCase");
            String strRunning = row.get("IsRunning");
            String keyword = row.get("Keyword");
            thrObjectName.set(row.get("ObjectName"));
            thrValue.set(row.get("Value"));
            String Application = row.get("Application");

            if (TestCase.equalsIgnoreCase("") && !keyword.equalsIgnoreCase("") && isrunning) {
                System.out.println("\nExecuting: " + keyword +" in row : "+ (intCurrentRow + 2));

                // First, try BaseFunction
                boolean methodFound = executeMethodInBaseFunction(base, keyword);

                // If not found, try Application class dynamically
                if (!methodFound) {
                    methodFound = executeMethodInApplication(Application, keyword);
                }

                if (!methodFound) {
                    throw new Exception("Method '" + keyword + "' not found in BaseFunction and " + thrApplication.get() + "!");
                }
            } else if (!TestCase.equalsIgnoreCase("")) {
                thrTestCase.set(TestCase);
                thrApplication.set(Application);
                TestCaseCount++;
                isrunning = strRunning.equalsIgnoreCase("YES") ? true : false;
            }
        }
        if (MyConfig.driver != null) {
            TestFactory.teardown();
        }
    }
    private boolean executeMethodInBaseFunction(BaseFunction base, String keyword) throws Exception {
        try {
            for (Method method : BaseFunction.class.getDeclaredMethods()) {
                if (method.getName().equalsIgnoreCase(keyword)) {
                    method.setAccessible(true);
                    method.invoke(base);
                    return true;
                }
            }
        } catch (Exception err) {
            err.printStackTrace();
            System.out.println("[ALERT] Error in BaseFunction Class!!!");
            throw new RuntimeException("Row : " + (intCurrentRow + 2) + ", " + err.getCause());
        }
        return false;
    }

    private boolean executeMethodInApplication(String applicationName, String keyword) throws Exception {
        try {
            // Dynamically load the application class (e.g., "Application.ILOAN")
            Class<?> appClass = Class.forName("Application." + thrApplication.get()+"."+ thrApplication.get());
            Object appInstance = appClass.getDeclaredConstructor().newInstance();

            // Find and invoke the method
            for (Method method : appClass.getDeclaredMethods()) {
                if (method.getName().equalsIgnoreCase(keyword)) {
                    method.setAccessible(true);
                    if (method.getParameterCount() == 0) {
                        method.invoke(appInstance);
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            return false;
        } catch (Exception err) {
            err.printStackTrace();
            System.out.println("[ALERT]Error in Application Class!!!");
            throw new RuntimeException("Row : " + (intCurrentRow + 2) + ", " + err.getCause());
        }
        return false;
    }
}
