package test.listeners.issue3082;

import java.util.Arrays;
import java.util.UUID;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class TestClassSample implements IUniqueObject {

  private final UUID uuid = UUID.randomUUID();

  @Factory(dataProvider = "dp")
  public TestClassSample() {}

  @DataProvider(name = "dp")
  public static Object[][] dataProvider() {
    Object[][] objects = new Object[2][];
    Arrays.fill(objects, new Object[0]);
    return objects;
  }

  @BeforeClass
  public void beforeClass() {
    addMethodReference(Reporter.getCurrentTestResult().getMethod().getMethodName());
  }

  @BeforeMethod
  public void beforeMethod() {
    addMethodReference(Reporter.getCurrentTestResult().getMethod().getMethodName());
  }

  @Test
  public void testMethod() {
    addMethodReference(Reporter.getCurrentTestResult().getMethod().getMethodName());
  }

  @AfterMethod
  public void afterMethod() {
    addMethodReference(Reporter.getCurrentTestResult().getMethod().getMethodName());
  }

  @AfterClass
  public void afterClass() {
    addMethodReference(Reporter.getCurrentTestResult().getMethod().getMethodName());
  }

  private void addMethodReference(String methodName) {
    if (!ObjectRepository.hasMapping(id(), methodName)) {
      ObjectRepository.recordErrorFor(id());
    }
  }

  @Override
  public UUID id() {
    return uuid;
  }

  @Override
  public String toString() {
    return id().toString();
  }
}
