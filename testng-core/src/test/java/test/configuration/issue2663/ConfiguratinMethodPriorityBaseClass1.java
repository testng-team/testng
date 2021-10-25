package test.configuration.issue2663;

import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class ConfiguratinMethodPriorityBaseClass1 {
  public static List<String> logs = new ArrayList<>();

  @BeforeSuite(
      groups = {"g1"},
      priority = 2)
  public void beforeSuite3() {
    print();
  }

  @BeforeClass(
      groups = {"g1"},
      priority = 2)
  public void beforeClass3() {
    print();
  }

  @BeforeTest(
      groups = {"g1"},
      priority = 2)
  public void beforeTest3() {
    print();
  }

  @BeforeMethod(
      groups = {"g1"},
      priority = 2)
  public void beforeMethod3() {
    print();
  }

  @Test(groups = {"g1"})
  public void test3() {
    print();
  }

  protected synchronized void print() {
    StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
    String methodName =
        stackTraceElements[2].getClassName() + "." + stackTraceElements[2].getMethodName();
    logs.add(methodName);
    System.out.println(methodName);
  }
}
