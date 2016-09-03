package test.parameters;

import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class ParameterTest extends SimpleBaseTest {

  @Test
  public void stringSingle() {
    XmlSuite suite = createXmlSuite("stringSingle");
    XmlTest test = createXmlTest(suite, "Before with parameter sample", ParameterSample.class);
    test.addParameter("first-name", "Cedric");

    TestNG tng = create(suite);

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener((ITestNGListener) listener);

    tng.run();

    assertThat(listener.getFailedMethodNames()).isEmpty();
    assertThat(listener.getSkippedMethodNames()).isEmpty();
    assertThat(listener.getSucceedMethodNames()).containsExactly(
            "beforeTest(Cedric)", "testNonExistentParameter(null)",
            "beforeTest(Cedric)", "testSingleString(Cedric)"
    );
  }

  @Test
  public void beforeMethodWithParameters() {
    XmlSuite suite = createXmlSuite("beforeMethodWithParameters");
    XmlTest test = createXmlTest(suite, "Before with parameter sample", BeforeWithParameterSample.class);
    test.addParameter("parameter", "parameter value");

    TestNG tng = create(suite);

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener((ITestNGListener) listener);

    tng.run();

    assertThat(listener.getFailedMethodNames()).isEmpty();
    assertThat(listener.getSkippedMethodNames()).isEmpty();
    assertThat(listener.getSucceedMethodNames()).containsExactly(
            "beforeMethod(parameter value)", "testExample(abc,def)"
    );
  }

  @Test
  public void enumParameters() {
    XmlSuite suite = createXmlSuite("enumParameters");
    XmlTest test = createXmlTest(suite, "Enum parameter sample", EnumParameterSample.class);
    test.addParameter("parameter", "VALUE_1");

    TestNG tng = create(suite);

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener((ITestNGListener) listener);

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
    tng.addListener((ITestNGListener) listener);

    tng.run();

    assertThat(listener.getSucceedMethodNames()).isEmpty();
    assertThat(listener.getSkippedMethodNames()).isEmpty();
    assertThat(listener.getFailedBeforeInvocationMethodNames()).containsExactly("testMethod");
    Throwable exception = listener.getResult("testMethod").getThrowable();
    assertThat(exception).isInstanceOf(TestNGException.class).hasCauseInstanceOf(IllegalArgumentException.class);
  }
}
