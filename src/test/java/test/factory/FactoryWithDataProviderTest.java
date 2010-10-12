package test.factory;

import org.testng.Assert;
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
    tng.addListener(tla);
    tng.run();

    Assert.assertEquals(tla.getPassedTests().size(), 4);

  }

  private static void ppp(String s) {
    System.out.println("[FactoryWithDataProviderTest] " + s);
  }
}
