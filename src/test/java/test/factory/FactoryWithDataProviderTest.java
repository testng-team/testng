package test.factory;

import org.testng.Assert;
import org.testng.ITestNGListener;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

public class FactoryWithDataProviderTest {

  /**
   * Verify that a factory can receive a data provider
   */
  @Test
  public void verifyDataProvider() {
    TestNG tng = new TestNG();
    tng.setVerbose(0);
    tng.setTestClasses(new Class[] { FactoryWithDataProvider.class });
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener((ITestNGListener) tla);
    tng.run();

    Assert.assertEquals(tla.getPassedTests().size(), 4);

  }

}
