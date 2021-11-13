package test.junitreports.issue993;

import java.lang.reflect.Method;
import org.testng.ITest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SampleTestClass implements ITest {

  private String testName;

  @BeforeMethod
  public void setTestName(Method method, Object[] testData) {
    testName = (String) testData[0];
  }

  @Test(dataProvider = "DP")
  public void Test_001(String param) {}

  @DataProvider(name = "DP")
  public Object[][] getData() {
    return new Object[][] {{null}};
  }

  @Override
  public String getTestName() {
    return testName;
  }
}
