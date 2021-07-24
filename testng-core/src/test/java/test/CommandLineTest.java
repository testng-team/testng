package test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.testng.Assert;
import org.testng.CommandLineArgs;
import org.testng.IInjectorFactory;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.internal.IConfiguration;
import test.sample.JUnitSample1;
import testhelper.OutputDirectoryPatch;

public class CommandLineTest {

  /** Test -junit */
  @Test(groups = {"current"})
  public void junitParsing() {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG testng = new TestNG();
    testng.addListener(tla);
    testng.setVerbose(0);
    testng.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    testng.setJUnit(true);
    testng.setTestClasses(new Class<?>[] {test.sample.JUnitSample1.class});
    testng.run();

    List<ITestResult> passed = tla.getPassedTests();
    assertEquals(passed.size(), 2);
    String test1 = passed.get(0).getMethod().getMethodName();
    String test2 = passed.get(1).getMethod().getMethodName();

    assertTrue(
        JUnitSample1.EXPECTED1.equals(test1) && JUnitSample1.EXPECTED2.equals(test2)
            || JUnitSample1.EXPECTED1.equals(test2) && JUnitSample1.EXPECTED2.equals(test1));
  }

  /** Test the absence of -junit */
  @Test(groups = {"current"})
  public void junitParsing2() {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG testng = new TestNG();
    testng.addListener(tla);
    testng.setVerbose(0);
    testng.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    testng.setTestClasses(new Class<?>[] {test.sample.JUnitSample1.class});
    testng.run();

    List<ITestResult> passed = tla.getPassedTests();
    assertEquals(passed.size(), 0);
  }

  /** Test the ability to override the default command line Suite name */
  @Test(groups = {"current"})
  public void suiteNameOverride() {
    String suiteName = "MySuiteName";

    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG testng = new TestNG();
    testng.addListener(tla);
    testng.setVerbose(0);
    testng.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    testng.setJUnit(true);
    testng.setTestClasses(new Class<?>[] {test.sample.JUnitSample1.class});
    testng.setDefaultSuiteName(suiteName);
    testng.run();

    List<ITestContext> contexts = tla.getTestContexts();
    assertTrue(contexts.size() > 0);
    for (ITestContext context : contexts) {
      assertEquals(context.getSuite().getName(), suiteName);
    }
  }

  /** Test the ability to override the default command line test name */
  @Test(groups = {"current"})
  public void testNameOverride() {
    String testName = "My Test Name";

    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG testng = new TestNG();
    testng.addListener(tla);
    testng.setVerbose(0);
    testng.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    testng.setJUnit(true);
    testng.setTestClasses(new Class<?>[] {test.sample.JUnitSample1.class});
    testng.setDefaultTestName(testName);
    testng.run();

    List<ITestContext> contexts = tla.getTestContexts();
    assertTrue(contexts.size() > 0);
    for (ITestContext context : contexts) {
      assertEquals(context.getName(), testName);
    }
  }

  @Test
  public void testUseDefaultListenersArgument() {
    TestNG testng = new TestNG();
    testng.setVerbose(0);
    testng.setUseDefaultListeners(false);
    testng.setTestClasses(new Class<?>[] {test.sample.JUnitSample1.class});
    testng.run();
  }

  @Test
  public void testMethodParameter() {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG testng = new TestNG();
    testng.addListener(tla);
    testng.setVerbose(0);
    testng.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    testng.setCommandLineMethods(
        Arrays.asList("test.sample.Sample2.method1", "test.sample.Sample2.method3"));
    testng.run();

    List<ITestResult> passed = tla.getPassedTests();
    Assert.assertEquals(passed.size(), 2);
    Assert.assertTrue(
        (passed.get(0).getName().equals("method1") && passed.get(1).getName().equals("method3"))
            || (passed.get(1).getName().equals("method1")
                && passed.get(0).getName().equals("method3")));
  }

  @SuppressWarnings("deprecation")
  @Test(description = "GITHUB-2207")
  public void testInjectorFactoryCanBeConfiguredViaProperties() {
    Map<String, String> params = new HashMap<>();
    params.put(CommandLineArgs.DEPENDENCY_INJECTOR_FACTORY, TestInjectorFactory.class.getName());
    TestNG testNG = new TestNG();
    testNG.configure(params);

    IInjectorFactory resolvedInjectorFactory = retrieveInjectionMechanism(testNG);
    Assert.assertEquals(resolvedInjectorFactory.getClass(), TestInjectorFactory.class);
  }

  private static IInjectorFactory retrieveInjectionMechanism(TestNG testNG) {
    try {
      Field field = TestNG.class.getDeclaredField("m_configuration");
      field.setAccessible(true);
      IConfiguration cfg = (IConfiguration) field.get(testNG);
      return cfg.getInjectorFactory();
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public static class TestInjectorFactory implements IInjectorFactory {

    @Override
    @SuppressWarnings("deprecation")
    public Injector getInjector(Stage stage, Module... modules) {
      return null;
    }
  }
}
