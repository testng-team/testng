package test.factory;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

public class DisabledFactoryTest extends SimpleBaseTest {

  @Test
  public void disabledFactoryShouldNotRun() {
    TestNG tng = create(DisabledFactorySampleTest.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.run();

    Assert.assertEquals(tla.getPassedTests().size(), 0);
    Assert.assertEquals(tla.getSkippedTests().size(), 0);
    Assert.assertEquals(tla.getFailedTests().size(), 0);
  }
}
