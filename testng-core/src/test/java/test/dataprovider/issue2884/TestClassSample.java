package test.dataprovider.issue2884;

import java.util.concurrent.atomic.AtomicInteger;
import org.testng.Assert;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestClassSample {

  public static final AtomicInteger dataProviderInvocationCount = new AtomicInteger(0);

  @Test(dataProvider = "dp", retryAnalyzer = TryAgain.class)
  public void testMethod(Pojo user) {
    Assert.fail();
  }

  @DataProvider(name = "dp")
  public static Object[][] getTestData() {
    dataProviderInvocationCount.incrementAndGet();
    return new Object[][] {{new Pojo().setName("John")}};
  }

  public static class TryAgain implements IRetryAnalyzer {

    private int counter = 1;

    @Override
    public boolean retry(ITestResult result) {
      return counter++ != 3;
    }
  }

  public static class Pojo {

    private String name;

    public String getName() {
      return name;
    }

    public Pojo setName(String name) {
      this.name = name;
      return this;
    }
  }
}
