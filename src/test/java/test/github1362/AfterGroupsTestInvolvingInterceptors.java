package test.github1362;

import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertTrue;

public class AfterGroupsTestInvolvingInterceptors extends SimpleBaseTest {

  @Test
  public void testMethod() {
    runTests(new LocalMethodInterceptor(), "setup", "test1", "test3", "clear");
  }

  @Test
  public void testMethodWithoutInterceptor() {
    runTests(null, "setup", "test1", "test2", "test3", "clear");
  }

  private void runTests(ITestNGListener interceptor, String... names) {
    List<String> expected = Arrays.asList(names);
    XmlSuite xmlsuite = createXmlSuite("suite", "test", TestSample.class);
    xmlsuite.getTests().get(0).setIncludedGroups(Collections.singletonList("exTests"));
    TestNG testng = create(xmlsuite);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    testng.addListener(listener);
    if (interceptor != null) {
      testng.addListener(interceptor);
    }
    testng.run();
    for (String each : listener.getInvokedMethodNames()) {
      assertTrue(
          expected.contains(each), each + " not found in expected invocation methods " + expected);
    }
  }
}
