package test.testng387;

import org.testng.*;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;

import test.SimpleBaseTest;

import java.util.List;

import static org.testng.Assert.assertEqualsNoOrder;

public class TestNG387 extends SimpleBaseTest {
  @Test(invocationCount = 500)
  public void testInvocationCounterIsCorrectForMethodWithDataProvider() throws Exception {
    final TestNG tng = create(FailedDPTest.class);
    tng.setThreadCount(1);
    tng.setParallel(XmlSuite.ParallelMode.FALSE);
    tng.setPreserveOrder(true);
    final TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();

    ITestNGMethod method = tla.getTestContexts().get(0).getAllTestMethods()[0];

    List<Integer> failed = method.getFailedInvocationNumbers();
    assertEqualsNoOrder(failed.toArray(), FailedDPTest.primes.toArray());
  }
}
