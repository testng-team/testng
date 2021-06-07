package test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.testng.Assert;
import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.sample.Sample1;

public class Test1 extends SimpleBaseTest {

  /**
   * This constructor is package protected on purpose, to test that TestNG can still instantiate the
   * class.
   */
  Test1() {}

  @Test(groups = {"current"})
  public void includedGroups() {
    XmlSuite suite = createXmlSuite("Internal_suite");
    XmlTest test = createXmlTest(suite, "Internal_test_failures_are_expected", Sample1.class);
    Assert.assertEquals(test.getXmlClasses().size(), 1);
    test.addIncludedGroup("odd");
    TestNG tng = create(suite);

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener((ITestNGListener) listener);

    tng.run();

    assertThat(listener.getSucceedMethodNames()).containsExactly("method1", "method3");
    assertThat(listener.getFailedMethodNames()).isEmpty();
  }

  @Test
  public void groupsOfGroupsSimple() {
    XmlSuite suite = createXmlSuite("Internal_suite");
    XmlTest test = createXmlTest(suite, "Internal_test_failures_are_expected", Sample1.class);
    Assert.assertEquals(test.getXmlClasses().size(), 1);
    // should match all methods belonging to group "odd" and "even"
    test.addIncludedGroup("evenodd");
    test.addMetaGroup("evenodd", "even", "odd");
    TestNG tng = create(suite);

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener((ITestNGListener) listener);

    tng.run();

    assertThat(listener.getSucceedMethodNames()).containsExactly("method1", "method2", "method3");
    assertThat(listener.getFailedMethodNames()).isEmpty();
  }

  @Test
  public void groupsOfGroupsWithIndirections() {
    XmlSuite suite = createXmlSuite("Internal_suite");
    XmlTest test = createXmlTest(suite, "Internal_test_failures_are_expected", Sample1.class);
    test.addIncludedGroup("all");
    test.addMetaGroup("all", "methods", "broken");
    test.addMetaGroup("methods", "odd", "even");
    test.addMetaGroup("broken", "broken");
    TestNG tng = create(suite);

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener((ITestNGListener) listener);

    tng.run();

    assertThat(listener.getSucceedMethodNames())
        .containsExactly("method1", "broken", "method2", "method3");
    assertThat(listener.getFailedMethodNames()).isEmpty();
  }

  @Test
  public void groupsOfGroupsWithCycle() {
    XmlSuite suite = createXmlSuite("Internal_suite");
    XmlTest test = createXmlTest(suite, "Internal_test_failures_are_expected", Sample1.class);
    test.addIncludedGroup("all");
    test.addMetaGroup("all", "all2");
    test.addMetaGroup("all2", "methods");
    test.addMetaGroup("methods", "all");
    TestNG tng = create(suite);

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener((ITestNGListener) listener);

    tng.run();

    assertThat(listener.getSucceedMethodNames()).isEmpty();
    assertThat(listener.getFailedMethodNames()).isEmpty();
  }

  @Test
  public void excludedGroups() {
    XmlSuite suite = createXmlSuite("Internal_suite");
    XmlTest test = createXmlTest(suite, "Internal_test_failures_are_expected", Sample1.class);
    test.addExcludedGroup("odd");
    TestNG tng = create(suite);

    InvokedMethodNameListener listener = new InvokedMethodNameListener(true);
    tng.addListener((ITestNGListener) listener);

    tng.run();

    assertThat(listener.getSucceedMethodNames())
        .containsExactly(
            "broken",
            "method2",
            "throwExpectedException1ShouldPass",
            "throwExpectedException2ShouldPass");
    assertThat(listener.getFailedMethodNames())
        .containsExactly("throwExceptionShouldFail", "verifyLastNameShouldFail");
  }

  @Test
  public void regexp() {
    XmlSuite suite = createXmlSuite("Internal_suite");
    XmlTest test = createXmlTest(suite, "Internal_test_failures_are_expected", Sample1.class);
    // should matches all methods belonging to group "odd"
    test.addIncludedGroup("o.*");
    TestNG tng = create(suite);

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener((ITestNGListener) listener);

    tng.run();

    assertThat(listener.getSucceedMethodNames()).containsExactly("method1", "method3");
    assertThat(listener.getFailedMethodNames()).isEmpty();
  }

  @Test(groups = {"currentold"})
  public void logger() {
    Logger logger = Logger.getLogger("");
    for (Handler handler : logger.getHandlers()) {
      handler.setLevel(Level.WARNING);
      handler.setFormatter(new org.testng.log.TextFormatter());
    }
    logger.setLevel(Level.SEVERE);
  }
}
