package test.parameters;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

public class ParameterTest extends SimpleBaseTest {

  @Test
  public void stringSingle() {
    XmlSuite suite = createXmlSuite("stringSingle");
    XmlTest test = createXmlTest(suite, "Before with parameter sample", ParameterSample.class);
    test.addParameter("first-name", "Cedric");

    TestNG tng = create(suite);

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);

    tng.run();

    assertThat(listener.getFailedMethodNames()).isEmpty();
    assertThat(listener.getSkippedMethodNames()).isEmpty();
    assertThat(listener.getSucceedMethodNames())
        .containsExactly(
            "beforeTest(Cedric)", "testNonExistentParameter(null)",
            "beforeTest(Cedric)", "testSingleString(Cedric)");
  }

  @Test
  public void beforeMethodWithParameters() {
    XmlSuite suite = createXmlSuite("beforeMethodWithParameters");
    XmlTest test =
        createXmlTest(suite, "Before with parameter sample", BeforeWithParameterSample.class);
    test.addParameter("parameter", "parameter value");

    TestNG tng = create(suite);

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);

    tng.run();

    assertThat(listener.getFailedMethodNames()).isEmpty();
    assertThat(listener.getSkippedMethodNames()).isEmpty();
    assertThat(listener.getSucceedMethodNames())
        .containsExactly("beforeMethod(parameter value)", "testExample(abc,def)");
  }

  @Test
  public void enumParameters() {
    XmlSuite suite = createXmlSuite("enumParameters");
    XmlTest test = createXmlTest(suite, "Enum parameter sample", EnumParameterSample.class);
    test.addParameter("parameter", "VALUE_1");

    TestNG tng = create(suite);

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);

    tng.run();

    assertThat(listener.getFailedMethodNames()).isEmpty();
    assertThat(listener.getSkippedMethodNames()).isEmpty();
    assertThat(listener.getSucceedMethodNames()).containsExactly("testMethod(VALUE_1)");
  }

  @Test(description = "GITHUB-1105: Test skipped instead failed if incorrect enum value")
  public void invalidEnumParameters() {
    XmlSuite suite = createXmlSuite("enumParameters");
    XmlTest test = createXmlTest(suite, "Enum parameter sample", EnumParameterSample.class);
    test.addParameter("parameter", "INVALID_VALUE");

    TestNG tng = create(suite);

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);

    tng.run();

    assertThat(listener.getSucceedMethodNames()).isEmpty();
    assertThat(listener.getSkippedMethodNames()).isEmpty();
    assertThat(listener.getFailedBeforeInvocationMethodNames()).containsExactly("testMethod");
    Throwable exception = listener.getResult("testMethod").getThrowable();
    assertThat(exception)
        .isInstanceOf(TestNGException.class)
        .hasCauseInstanceOf(IllegalArgumentException.class);
  }

  @Test(description = "GITHUB-1061")
  public void testNativeInjection() {
    TestNG testng = create(Issue1061Sample.class);
    TestListenerAdapter listener = new TestListenerAdapter();
    testng.addListener(listener);
    testng.run();
    assertThat(listener.getFailedTests().size()).isEqualTo(2);
    String[] expectedMsgs =
        new String[] {
          "Method test.parameters.Issue1061Sample.test() didn't finish within the time-out 1000",
          "Method test.parameters.Issue1061Sample.test() didn't finish within the time-out 3000"
        };
    List<String> actualMsgs = Lists.newArrayList();
    for (ITestResult result : listener.getFailedTests()) {
      actualMsgs.add(result.getThrowable().getMessage());
    }
    assertThat(actualMsgs).containsExactlyInAnyOrder(expectedMsgs);
  }

  @Test(description = "GITHUB-1554")
  public void testNativeInjectionAndParamsInjection() {
    XmlSuite suite = createXmlSuite("suite");
    XmlTest test = createXmlTest(suite, "test", Issue1554TestClassSample.class);
    Map<String, String> params = new HashMap<>();
    params.put("browser", "chrome");
    test.setParameters(params);
    TestNG testng = create(suite);
    TestListenerAdapter listener = new TestListenerAdapter();
    testng.addListener(listener);
    testng.run();
    assertThat(listener.getPassedTests().isEmpty()).isFalse();
  }
}
