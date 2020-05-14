package test.cli.github1517;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class ExitCodeListenerBehaviorTest extends SimpleBaseTest {

  @Test(dataProvider = "getData")
  public void testMethod(Class<?> clazz, int expectedStatus) {
    TestNG testNG = create(clazz);
    testNG.run();
    Assert.assertEquals(testNG.getStatus(), expectedStatus);
  }

  @DataProvider
  public Object[][] getData() {
    return new Object[][] {
      {TestClassWithConfigFailureSample.class, 3},
      {TestClassWithConfigSkipSample.class, 2},
      {TestClassWithConfigSkipAndFailureSample.class, 3}
    };
  }
}
