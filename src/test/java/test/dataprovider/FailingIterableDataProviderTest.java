package test.dataprovider;

import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TESTNG-291: Exceptions thrown by Iterable DataProviders are not caught, no failed test reported
 */
public class FailingIterableDataProviderTest extends SimpleBaseTest {

  @Test
  public void failingDataProvider() {
    InvokedMethodNameListener listener = run(FailingIterableDataProvider.class);

    assertThat(listener.getFailedBeforeInvocationMethodNames()).containsExactly("happyTest");
    assertThat(listener.getSucceedMethodNames())
        .containsExactly(
            "happyTest(1)", "happyTest(2)", "happyTest(3)", "happyTest(4)", "happyTest(5)");
  }

  @Test
  public void failingDataProviderWithInvocationNumber() {
    XmlSuite xmlSuite = createXmlSuite("Suite");
    XmlTest xmlTest = createXmlTest(xmlSuite, "Test");
    XmlClass xmlClass = createXmlClass(xmlTest, FailingIterableDataProvider.class);
    createXmlInclude(xmlClass, "happyTest", /* index */ 0, /* list */ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9);

    TestNG tng = create(xmlSuite);

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);

    tng.run();

    assertThat(listener.getFailedBeforeInvocationMethodNames()).containsExactly("happyTest");
    assertThat(listener.getSucceedMethodNames())
        .containsExactly(
            "happyTest(1)", "happyTest(2)", "happyTest(3)", "happyTest(4)", "happyTest(5)");
  }
}
