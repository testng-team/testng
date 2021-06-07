package test.github799;

import java.util.List;
import org.testng.Assert;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import test.SimpleBaseTest;

public class EnsureInstancesAreOrderedViaFactories extends SimpleBaseTest {

  @Test
  public void testMethod() {
    System.setProperty("testng.order", "none");
    runTest(TestSample.class, "1", "2", "3", "4");
  }

  @Test
  public void randomOrderTestMethod() {
    System.setProperty("testng.order", "none");
    runTest(ReverseOrderTestSample.class, "4", "1", "3", "2");
  }

  @Test
  public void methodsOrderTest() {
    System.setProperty("testng.order", "methods");
    runTest(MethodsTestSample.class, "android", "angry", "birds");
  }

  @Test
  public void testInstancesOrder() {
    System.setProperty("testng.order", "instances");
    runTest(InstanceTestSample.class, "Master Oogway:90", "Master Shifu:50");
  }

  private void runTest(Class<?> clazz, String... expected) {
    TestNG tng = create(clazz);
    OrderEavesdropper listener = new OrderEavesdropper();
    tng.addListener(listener);
    tng.run();

    for (int i = 0; i < expected.length; i++) {
      String actual = listener.messages.get(i);
      Assert.assertEquals(actual, expected[i]);
    }
  }

  public static class OrderEavesdropper implements IInvokedMethodListener {
    List<String> messages = Lists.newArrayList();

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {}

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
      messages.addAll(Reporter.getOutput(testResult));
    }
  }
}
