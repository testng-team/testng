package org.testng;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.internal.Configuration;
import org.testng.internal.IConfiguration;
import org.testng.internal.MethodSorting;
import org.testng.internal.objects.DefaultTestObjectFactory;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

public class TestRunnerTest {
  @Test
  public void listenersShouldBeAppliedInDeclarationOrder_one() {
    TestRunner testRunner = createTestRunner(TestWithOneListener.class);

    List<IConfigurationListener> configurationListeners = testRunner.getConfigurationListeners();

    assertThat(classesOf(configurationListeners, 1)).containsExactly(FooListener.class);
  }

  @Test
  public void listenersShouldBeAppliedInDeclarationOrder_two() {
    TestRunner testRunner = createTestRunner(TestWithTwoListeners.class);

    List<IConfigurationListener> configurationListeners = testRunner.getConfigurationListeners();

    assertThat(classesOf(configurationListeners, 2))
        .containsExactly(FooListener.class, BarListener.class);
  }

  @Test
  public void listenersShouldBeAppliedInDeclarationOrder_three() {
    TestRunner testRunner = createTestRunner(TestWithThreeListeners.class);

    List<IConfigurationListener> configurationListeners = testRunner.getConfigurationListeners();

    assertThat(classesOf(configurationListeners, 3))
        .containsExactly(FooListener.class, BarListener.class, ZooListener.class);
  }

  @Listeners(FooListener.class)
  public static class TestWithOneListener {
    @Test
    public void some() {}
  }

  @Listeners({FooListener.class, BarListener.class})
  public static class TestWithTwoListeners {
    @BeforeMethod
    public void setup() {}

    @Test
    public void some() {}
  }

  @Listeners({FooListener.class, BarListener.class, ZooListener.class})
  public static class TestWithThreeListeners {
    @Test
    public void some() {}
  }

  public static class FooListener implements IConfigurationListener {}

  public static class BarListener implements IConfigurationListener {}

  public static class ZooListener implements IConfigurationListener {}

  /**
   * A test context reports when it started from the moment it exists, and reports no end until it
   * has run. Nothing covered either, and both are published through ITestContext.
   */
  @Test
  public void aTestContextReportsWhenItStartedAndNotYetWhenItEnded() {
    TestRunner runner = createTestRunner(TestWithOneListener.class);

    assertThat(runner.getStartInstant()).isNotNull();
    assertThat(runner.getEndInstant()).isNull();
  }

  /**
   * The deprecated pair still has to answer the same moment as the one that replaced it, to the
   * millisecond it has always been published with. Deprecated itself, which is how a test of
   * deprecated API says so rather than suppressing the warning it earns.
   */
  @Deprecated
  @Test
  public void theDeprecatedDatePairAgreesWithTheInstantPair() {
    TestRunner runner = createTestRunner(TestWithOneListener.class);

    assertThat(runner.getStartDate()).isEqualTo(Date.from(runner.getStartInstant()));
  }

  /** The answer is a copy, so a caller that writes to it cannot move the runner. */
  @Deprecated
  @SuppressWarnings("JavaUtilDate") // writing to a Date is the claim under test, not an oversight
  @Test
  public void theDeprecatedStartDateIsACopy() {
    TestRunner runner = createTestRunner(TestWithOneListener.class);

    Date first = runner.getStartDate();
    first.setTime(0);

    assertThat(runner.getStartDate()).isNotEqualTo(first);
  }

  private <T> List<Class<?>> classesOf(List<T> values, int limit) {
    return values.stream().limit(limit).map(Object::getClass).collect(Collectors.toList());
  }

  private TestRunner createTestRunner(Class<?> testClass) {
    IConfiguration configuration = new Configuration();
    configuration.setObjectFactory(new DefaultTestObjectFactory());
    XmlSuite xmlSuite = new XmlSuite();
    XmlTest xmlTest = new XmlTest(xmlSuite);
    xmlSuite.addTest(xmlTest);
    XmlClass xmlClass = new XmlClass(testClass.getName());
    xmlTest.getXmlClasses().add(xmlClass);
    String outputDir = "build/reports/tests/test";
    // The SuiteRunner handed to newTestRunner is its own ISuiteRunnerListener, as in production.
    ITestRunnerFactory factory =
        (suite, test, listeners, classListeners) ->
            new TestRunner(
                configuration,
                suite,
                test,
                false,
                listeners,
                classListeners,
                (ISuiteRunnerListener) suite);
    SuiteRunner suite =
        new SuiteRunner(configuration, xmlSuite, outputDir, factory, MethodSorting.NONE);
    return factory.newTestRunner(suite, xmlTest, emptyList(), emptyList());
  }
}
