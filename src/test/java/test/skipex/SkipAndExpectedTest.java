package test.skipex;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

public class SkipAndExpectedTest extends SimpleBaseTest {

  @Test
  public void shouldSkip() {
    TestNG tng = create(SkipAndExpectedSampleTest.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();

    Assert.assertEquals(tla.getPassedTests().size(), 0);
    Assert.assertEquals(tla.getSkippedTests().size(), 1);
    Assert.assertEquals(tla.getFailedTests().size(), 0);
  }
}
