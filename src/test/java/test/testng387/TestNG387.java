package test.testng387;

import java.util.Collections;
import java.util.List;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class TestNG387 extends SimpleBaseTest {
  @Test
  public void testInvocationCounterIsCorrectForMethodWithDataProvider() throws Exception {
    final TestNG tng = create(FailedDPTest.class);
    tng.setThreadCount(1);
    tng.setParallel("false");
    tng.setPreserveOrder(true);
    final TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();

    final List<ITestContext> contexts = tla.getTestContexts();
    Assert.assertNotNull(contexts);
    Assert.assertEquals(contexts.size(), 1);
    final ITestContext context = contexts.iterator().next();
    Assert.assertNotNull(context);
    final ITestNGMethod[] methods = context.getAllTestMethods();
    Assert.assertNotNull(methods.length);
    Assert.assertEquals(methods.length, 1);

    List<Integer> failed = methods[0].getFailedInvocationNumbers();
    Collections.sort(failed); // Is that correct? Shouldn't be already sorted by Invoker (like DataProvider parameters)
    Assert.assertEquals(failed, FailedDPTest.primes, "FailedInvocationNumbers mismatch");
  }
}
