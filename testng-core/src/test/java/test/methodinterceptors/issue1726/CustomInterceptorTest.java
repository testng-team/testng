package test.methodinterceptors.issue1726;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestNGListener;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

public class CustomInterceptorTest extends SimpleBaseTest {

  @Test
  public void testOrderingWhenInvolvingCustomInterceptors() {
    XmlSuite suite = createXmlSuite("suite");
    suite.setPreserveOrder(false);
    XmlTest xmlTest = createXmlTest(suite, "test", TestClassSample1.class, TestClassSample2.class);
    xmlTest.setPreserveOrder(false);
    TestNG testng = create(suite);
    testng.addListener(new PriorityInterceptor());
    testng.addListener((ITestNGListener) new MethodOrderTracker());
    testng.run();
    List<String> expected =
        Arrays.asList(
            "TestClassSample2.Test3",
            "TestClassSample1.Test1",
            "TestClassSample1.Test2",
            "TestClassSample2.Test4");
    assertThat(MethodOrderTracker.ordered).containsExactlyElementsOf(expected);
  }

  public static class MethodOrderTracker implements IInvokedMethodListener {
    static List<String> ordered = new ArrayList<>();

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {}

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
      String clazz = method.getTestMethod().getRealClass().getSimpleName() + ".";
      ordered.add(clazz + method.getTestMethod().getMethodName());
    }
  }
}
