package test.retryAnalyzer;

import org.testng.Assert;
import org.testng.ITest;
import org.testng.ITestResult;
import org.testng.annotations.Test;
import org.testng.util.RetryAnalyzerCount;

public class FactoryTest implements ITest {

  public static class ThreeTimeRetryer extends RetryAnalyzerCount {

    public ThreeTimeRetryer() {
      setCount(3);
    }

    @Override
    public boolean retryMethod(ITestResult result) {
      return true;
    }
  }

  public static int COUNT = 0;

  private final String name;

  public FactoryTest(int name) {
    this.name = "Test" + name;
  }

  @Override
  public String getTestName() {
    return this.name;
  }

  @Test(retryAnalyzer = ThreeTimeRetryer.class)
  public void someTest1() {
    if ("Test5".equals(name)) {
      COUNT++;
      Assert.fail();
    }
  }
}
