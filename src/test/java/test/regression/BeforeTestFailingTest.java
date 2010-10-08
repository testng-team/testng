package test.regression;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

public class BeforeTestFailingTest extends SimpleBaseTest {

  @Test
  public void beforeTestFailingShouldCauseSkips() {
    TestNG tng = create(MyTestngTest2.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();

    Assert.assertEquals(tla.getSkippedTests().size(), 1);
    Assert.assertEquals(tla.getPassedTests().size(), 0);
  }
}
