package test.factory;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class EmptyFactoryDataProviderTest extends SimpleBaseTest {

  @Test
  public void test() {
    TestNG testng = create(ArrayEmptyFactorySample.class, IteratorEmptyFactorySample.class);

    TestListenerAdapter tla = new TestListenerAdapter();
    testng.addListener(tla);
    // Used to check the warning message

    testng.run();

    Assert.assertTrue(tla.getFailedTests().isEmpty());
    Assert.assertTrue(tla.getSkippedTests().isEmpty());
    Assert.assertTrue(tla.getPassedTests().isEmpty());
  }
}
