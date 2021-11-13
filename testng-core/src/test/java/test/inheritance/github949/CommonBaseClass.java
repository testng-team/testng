package test.inheritance.github949;

import java.util.ArrayList;
import java.util.List;
import org.testng.ITestResult;
import org.testng.Reporter;

public class CommonBaseClass {
  static List<String> messages = new ArrayList<>();

  protected static void logMessage() {
    ITestResult iTestResult = Reporter.getCurrentTestResult();
    String method = iTestResult.getMethod().getMethodName();
    String clazz = iTestResult.getMethod().getTestClass().getName();
    messages.add(clazz + "." + method);
  }
}
