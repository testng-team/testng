package test.testng387;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.testng.ITestNGListener;
import org.testng.ITestNGMethod;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

public class TestNG387 extends SimpleBaseTest {
  @Test(invocationCount = 500)
  public void testInvocationCounterIsCorrectForMethodWithDataProvider() {
    final TestNG tng = create(FailedDPTest.class);
    tng.setThreadCount(1);
    tng.setParallel(XmlSuite.ParallelMode.NONE);
    tng.setPreserveOrder(true);
    final TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener((ITestNGListener) tla);
    tng.run();

    ITestNGMethod method = tla.getTestContexts().get(0).getAllTestMethods()[0];

    List<Integer> failed = method.getFailedInvocationNumbers();
    assertThat(failed.toArray()).containsExactlyInAnyOrder(FailedDPTest.primes.toArray());
  }
}
