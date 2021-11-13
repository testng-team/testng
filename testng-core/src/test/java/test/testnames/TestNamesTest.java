package test.testnames;

import java.util.Collections;
import org.testng.Assert;
import org.testng.ITestNGListener;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class TestNamesTest extends SimpleBaseTest {

  @Test
  public void checkWithoutChildSuites() {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG tng = create();
    tng.setTestNames(Collections.singletonList("testGroup2"));
    tng.setTestSuites(Collections.singletonList(getPathToResource("testnames/upstream-suite.xml")));
    tng.addListener((ITestNGListener) tla);
    tng.run();
    Assert.assertEquals(tla.getFailedTests().size(), 0);
    Assert.assertEquals(tla.getPassedTests().size(), 1);
    Assert.assertEquals(
        tla.getPassedTests().get(0).getMethod().getMethodName(), "sampleOutputTest2");
  }
}
