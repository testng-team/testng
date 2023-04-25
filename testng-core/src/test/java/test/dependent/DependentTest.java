package test.dependent;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import org.testng.Assert;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;
import test.dependent.github1156.ASample;
import test.dependent.github1156.BSample;
import test.dependent.github1380.GitHub1380Sample;
import test.dependent.github1380.GitHub1380Sample2;
import test.dependent.github1380.GitHub1380Sample3;
import test.dependent.github1380.GitHub1380Sample4;
import test.dependent.issue2658.FailingClassSample;
import test.dependent.issue2658.PassingClassSample;

public class DependentTest extends SimpleBaseTest {

  @Test
  public void simpleSkip() {
    TestNG testng = create(SampleDependent1.class);
    MethodNameCollector listener = new MethodNameCollector();
    testng.addListener(listener);
    testng.run();
    assertThat(listener.getPassedNames()).isEmpty();
    assertThat(listener.getFailedNames()).containsExactly("fail");
    assertThat(listener.getSkippedNames()).containsExactly("shouldBeSkipped");
  }

  @Test
  public void dependentMethods() {
    TestNG testng = create(SampleDependentMethods.class);
    MethodNameCollector listener = new MethodNameCollector();
    testng.addListener(listener);
    testng.run();
    assertThat(listener.getPassedNames())
        .contains("oneA", "oneB", "secondA", "thirdA", "canBeRunAnytime");
    assertThat(listener.getFailedNames()).isEmpty();
    assertThat(listener.getSkippedNames()).isEmpty();
  }

  @Test
  public void dependentMethodsWithSkip() {
    TestNG testng = create(SampleDependentMethods4.class);
    MethodNameCollector listener = new MethodNameCollector();
    testng.addListener(listener);
    testng.run();
    assertThat(listener.getPassedNames()).contains("step1");
    assertThat(listener.getFailedNames()).contains("step2");
    assertThat(listener.getSkippedNames()).contains("step3");
  }

  @Test(expectedExceptions = {org.testng.TestNGException.class})
  public void dependentMethodsWithNonExistentMethod() {
    TestNG testng = create(SampleDependentMethods5.class);
    testng.run();
  }

  @Test(expectedExceptions = org.testng.TestNGException.class)
  public void dependentMethodsWithCycle() {
    TestNG testng = create(SampleDependentMethods6.class);
    testng.run();
  }

  @Test
  public void multipleSkips() {
    TestNG testng = create(MultipleDependentSampleTest.class);
    MethodNameCollector listener = new MethodNameCollector();
    testng.addListener(listener);
    testng.run();
    assertThat(listener.getPassedNames()).contains("init");
    assertThat(listener.getFailedNames()).contains("fail");
    assertThat(listener.getSkippedNames()).contains("skip1", "skip2");
  }

  @Test
  public void instanceDependencies() {
    TestNG testng = create(InstanceSkipSampleTest.class);
    MethodNameCollector listener = new MethodNameCollector();
    testng.addListener(listener);
    testng.run();
    assertThat(listener.getPassedInstances()).contains("f#1", "f#3", "g#1", "g#3");
    assertThat(listener.getFailedInstances()).contains("f#2");
    assertThat(listener.getSkippedInstances()).contains("g#2");
  }

  @Test
  public void dependentWithDataProvider() {
    TestNG testng = create(DependentWithDataProviderSampleTest.class);
    testng.setGroupByInstances(true);
    List<String> log = DependentWithDataProviderSampleTest.m_log;
    log.clear();
    testng.run();
    for (int i = 0; i < 12; i += 4) {
      String[] s = log.get(i).split("#");
      String instance = s[1];
      Assert.assertEquals(log.get(i), "prepare#" + instance);
      Assert.assertEquals(log.get(i + 1), "test1#" + instance);
      Assert.assertEquals(log.get(i + 2), "test2#" + instance);
      Assert.assertEquals(log.get(i + 3), "clean#" + instance);
    }
  }

  @DataProvider
  public static Object[][] dp() {
    return new Object[][] {
      {new Class[] {ASample.class, BSample.class}, true},
      {new Class[] {ASample.class, BSample.class}, false},
      {new Class[] {BSample.class, ASample.class}, true},
      {new Class[] {BSample.class, ASample.class}, false}
    };
  }

  @Test(dataProvider = "dp", description = "GITHUB-1156")
  public void methodDependencyBetweenClassesShouldWork(Class<?>[] classes, boolean preserveOrder) {
    TestNG testng = create(classes);
    testng.setPreserveOrder(preserveOrder);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    testng.addListener(listener);
    testng.run();
    assertThat(listener.getSucceedMethodNames()).containsExactly("testB", "testA");
  }

  @DataProvider
  public static Object[][] dp1380() {
    return new Object[][] {
      {GitHub1380Sample.class, new String[] {"testMethodA", "testMethodB", "testMethodC"}},
      {GitHub1380Sample2.class, new String[] {"testMethodC", "testMethodB", "testMethodA"}},
      {GitHub1380Sample3.class, new String[] {"testMethodA", "testMethodB", "testMethodC"}},
      {GitHub1380Sample4.class, new String[] {"testMethodB", "testMethodA", "testMethodC"}},
    };
  }

  @Test(dataProvider = "dp1380", description = "GITHUB-1380")
  public void simpleCyclingDependencyShouldWorkWithoutParallelism(
      Class<?> testClass, String[] runMethods) {
    TestNG tng = create(testClass);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);
    tng.run();

    // When not running parallel, invoke order and succeed order are the same.
    assertThat(listener.getInvokedMethodNames()).containsExactly(runMethods);
    assertThat(listener.getSucceedMethodNames()).containsExactly(runMethods);
  }

  @DataProvider
  public static Object[][] dp1380Parallel() {
    return new Object[][] {
      {GitHub1380Sample.class, new String[] {"testMethodA", "testMethodB", "testMethodC"}},
      {
        GitHub1380Sample2.class,
        // A dependsOn B; C can be anywhere even though B has "sleep 5 sec"
        // C is the first
        new String[] {"testMethodC", "testMethodB", "testMethodA"},
        // C is the second
        new String[] {"testMethodB", "testMethodC", "testMethodA"},
        // C is the third
        new String[] {"testMethodB", "testMethodA", "testMethodC"},
      },
      {GitHub1380Sample3.class, new String[] {"testMethodA", "testMethodB", "testMethodC"}},
      {
        GitHub1380Sample4.class,
        // A dependsOn B; C can be anywhere
        // C is the first
        new String[] {"testMethodC", "testMethodB", "testMethodA"},
        // C is the second
        new String[] {"testMethodB", "testMethodC", "testMethodA"},
        // C is the third
        new String[] {"testMethodB", "testMethodA", "testMethodC"},
      },
    };
  }

  @Test(dataProvider = "dp1380Parallel", description = "GITHUB-1380")
  public void simpleCyclingDependencyShouldWorkWitParallelism(
      Class<?> testClass, String[]... runMethods) {
    TestNG tng = create(testClass);
    tng.setParallel(ParallelMode.METHODS);

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);

    tng.run();

    assertThat(listener.getInvokedMethodNames())
        .matches(
            strings -> {
              boolean result = false;
              for (String[] runMethod : runMethods) {
                result = result || Arrays.asList(runMethod).equals(strings);
              }
              return result;
            },
            "When running parallel, invoke order is consistent, but succeed order isn't "
                + Arrays.deepToString(runMethods));
    assertThat(listener.getSucceedMethodNames()).containsExactlyInAnyOrder(runMethods[0]);
  }

  @Test(description = "GITHUB-2658")
  public void testMethodDependencyAmidstInheritance() {
    TestNG testng = create(PassingClassSample.class, FailingClassSample.class);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    testng.addListener(listener);
    testng.run();
    assertThat(listener.getFailedMethodNames()).containsExactly("test");
    assertThat(listener.getSucceedMethodNames()).containsExactly("test", "passingMethod");
    assertThat(listener.getSkippedMethodNames()).containsExactly("failingMethod");
  }

  public static class MethodNameCollector implements ITestListener {

    private static final Function<ITestResult, String> asString =
        itr -> itr.getMethod().getMethodName() + "#" + itr.getInstance().toString();
    private final List<String> passedNames = new ArrayList<>();
    private final List<String> failedNames = new ArrayList<>();
    private final List<String> skippedNames = new ArrayList<>();

    private final List<String> passedInstances = new ArrayList<>();
    private final List<String> failedInstances = new ArrayList<>();
    private final List<String> skippedInstances = new ArrayList<>();

    public List<String> getPassedInstances() {
      return passedInstances;
    }

    public List<String> getFailedInstances() {
      return failedInstances;
    }

    public List<String> getSkippedInstances() {
      return skippedInstances;
    }

    public List<String> getFailedNames() {
      return failedNames;
    }

    public List<String> getPassedNames() {
      return passedNames;
    }

    public List<String> getSkippedNames() {
      return skippedNames;
    }

    @Override
    public void onTestFailure(ITestResult result) {
      failedNames.add(result.getMethod().getMethodName());
      failedInstances.add(asString.apply(result));
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
      failedNames.add(result.getMethod().getMethodName());
      failedInstances.add(asString.apply(result));
    }

    @Override
    public void onTestFailedWithTimeout(ITestResult result) {
      failedNames.add(result.getMethod().getMethodName());
      failedInstances.add(asString.apply(result));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
      passedNames.add(result.getMethod().getMethodName());
      passedInstances.add(asString.apply(result));
    }

    @Override
    public void onTestSkipped(ITestResult result) {
      skippedNames.add(result.getMethod().getMethodName());
      skippedInstances.add(asString.apply(result));
    }
  }
}
