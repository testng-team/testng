package test.inheritance.github949;

import org.testng.ITestResult;
import org.testng.Reporter;

import java.util.ArrayList;
import java.util.List;

public class CommonBaseClass {
  static List<String> messages = new ArrayList<>();

  protected static void logMessage() {
    ITestResult iTestResult = Reporter.getCurrentTestResult();
    String method = iTestResult.getMethod().getMethodName();
    String clazz = iTestResult.getMethod().getTestClass().getName();
    messages.add(clazz + "." + method);
  }
}
