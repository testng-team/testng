package test.listeners.issue2456;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestClassSample {

  @Test(dataProvider = "dp")
  public void sampleTest(int i) {}

  @DataProvider(name = "dp")
  public Object[][] getData() {
    throw new TestCaseFailedException("dataprovider failed");
  }

  public static class TestCaseFailedException extends RuntimeException {

    public TestCaseFailedException(String message) {
      super(message);
    }
  }
}
