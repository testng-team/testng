package test.factory.github2428;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {
  @Test
  public void issue2438() {
    TestNG testng = create(FactoryTest.class);
    testng.setDefaultSuiteName("factory tests");
    Reporter reporter = new Reporter();
    testng.addListener(reporter);
    testng.run();
    Assert.assertEquals(reporter.getResults().size(), 5);
  }
}
