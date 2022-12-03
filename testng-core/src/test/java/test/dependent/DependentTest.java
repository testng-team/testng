package test.dependent;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.testng.Assert;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.TestNGException;
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
import test.dependent.issue141.ErrorScenarioNestedSample;
import test.dependent.issue141.MultipleMatchesTestClassSample;
import test.dependent.issue141.NestedTestClassSample;
import test.dependent.issue141.NestedTestClassSample2;
import test.dependent.issue141.SimpleSample;
import test.dependent.issue141.SkipReasoner;
import test.dependent.issue141.TestClassSample;
import test.dependent.issue2658.FailingClassSample;
import test.dependent.issue2658.PassingClassSample;
import test.dependent.issue550.ConfigDependencySample;
import test.dependent.issue550.ConfigDependencyWithMismatchedLevelSample;
import test.dependent.issue550.ConfigDependsOnTestAndConfigMethodSample;
import test.dependent.issue550.ConfigDependsOnTestMethodSample;
import test.dependent.issue550.OrderedResultsGatherer;
import test.dependent.issue893.DependencyTrackingListener;
import test.dependent.issue893.MultiLevelDependenciesTestClassSample;

public class DependentTest extends SimpleBaseTest {

  @Test(description = "GITHUB-141")
  public void ensureDependsOnMethodsHonoursRegexPatternsAcrossClasses() {
    TestNG testng =
        create(test.dependent.issue141.ASample.class, test.dependent.issue141.BSample.class);
    MethodNameCollector listener = new MethodNameCollector();
    testng.addListener(listener);
    testng.run();
    assertThat(listener.getPassedNames()).containsExactly("b", "bb", "a");
  }

  @Test(
      description = "GITHUB-141",
      expectedExceptions = TestNGException.class,
      expectedExceptionsMessageRegExp =
          "\ntest.dependent.issue141.SimpleSample.testMethod\\(\\) "
              + "depends on nonexistent method test.dependent.issue141.BSample.*")
  public void ensureDependsOnMethodsHonoursRegexPatternsAcrossClassesErrorCondition() {
    TestNG testng = create(SimpleSample.class, test.dependent.issue141.BSample.class);
    MethodNameCollector listener = new MethodNameCollector();
    testng.addListener(listener);
    testng.run();
  }

  @Test(
      description = "GITHUB-141",
      expectedExceptions = TestNGException.class,
      expectedExceptionsMessageRegExp =
          "\ntest.dependent.issue141.ErrorScenarioNestedSample.a\\(\\) "
              + "depends on nonexistent method .*")
  public void ensureDependsOnMethodsHonoursRegexPatternsNestedClassesErrorCondition() {
    TestNG testng = create(ErrorScenarioNestedSample.class);
    MethodNameCollector listener = new MethodNameCollector();
    testng.addListener(listener);
    testng.run();
  }

  @Test(description = "GITHUB-141")
  public void ensureDependsOnMethodsHonoursRegexPatternsUniqueMatch() {
    TestNG testng = create(TestClassSample.class);
    MethodNameCollector listener = new MethodNameCollector();
    testng.addListener(listener);
    testng.run();
    assertThat(listener.getPassedNames()).containsExactly("test_C6390323", "randomTest");
  }

  @Test(description = "GITHUB-141")
  public void ensureDependsOnMethodsHonoursRegexPatternsDuplicateMatches() {
    TestNG testng = create(MultipleMatchesTestClassSample.class);
    MethodNameCollector listener = new MethodNameCollector();
    SkipReasoner reasoner = new SkipReasoner();
    testng.addListener(listener);
    testng.addListener(reasoner);
    testng.run();
    assertThat(listener.getPassedNames()).containsExactly("test_C6390324");
    assertThat(listener.getFailedNames()).containsExactly("test_C6390323");
    assertThat(listener.getSkippedNames()).containsExactly("randomTest");
    assertThat(reasoner.getUpstreamFailures()).containsExactly("test_C6390323");
  }

  @Test(
      description = "GITHUB-141",
      expectedExceptions = TestNGException.class,
      expectedExceptionsMessageRegExp = "\n.* depends on nonexistent method .*")
  public void ensureDependsOnMethodsHonoursRegexPatternsDuplicateMatchesNestedClasses() {
    TestNG testng = create(NestedTestClassSample.class);
    MethodNameCollector listener = new MethodNameCollector();
    SkipReasoner reasoner = new SkipReasoner();
    testng.addListener(listener);
    testng.addListener(reasoner);
    testng.run();
  }

  @Test(
      description = "GITHUB-141",
      expectedExceptions = TestNGException.class,
      expectedExceptionsMessageRegExp =
          "\ntest.dependent.issue141.NestedTestClassSample2.randomTest\\(\\) depends on "
              + "nonexistent method .*")
  public void ensureDependsOnMethodsHonourRegexPatternsNestedClasses() {
    TestNG testng = create(NestedTestClassSample2.class);
    MethodNameCollector listener = new MethodNameCollector();
    SkipReasoner reasoner = new SkipReasoner();
    testng.addListener(listener);
    testng.addListener(reasoner);
    testng.run();
  }

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

  @Test(description = "GITHUB-893", dataProvider = "getTestData")
  public void testDownstreamDependencyRetrieval(
      Class<?> clazz, String independentMethod, String[] dependentMethods) {
    TestNG testng = create(clazz);
    DependencyTrackingListener listener = new DependencyTrackingListener();
    testng.addListener(listener);
    testng.run();
    String cls = clazz.getCanonicalName();
    String key = cls + "." + independentMethod;
    Set<String> downstream = listener.getDownstreamDependencies().get(key);
    dependentMethods =
        Arrays.stream(dependentMethods).map(each -> cls + "." + each).toArray(String[]::new);
    assertThat(downstream).containsExactly(dependentMethods);
  }

  @DataProvider(name = "getTestData")
  public Object[][] getTestData() {
    return new Object[][] {
      {
        test.dependent.issue893.TestClassSample.class,
        "independentTest",
        new String[] {"anotherDependentTest", "dependentTest"}
      },
      {MultiLevelDependenciesTestClassSample.class, "father", new String[] {"child"}},
      {
        MultiLevelDependenciesTestClassSample.class,
        "grandFather",
        new String[] {"father", "mother"}
      },
      {MultiLevelDependenciesTestClassSample.class, "child", new String[] {}}
    };
  }

  @Test(description = "GITHUB-893", dataProvider = "getUpstreamTestData")
  public void testUpstreamDependencyRetrieval(
      Class<?> clazz, String independentMethod, String[] dependentMethods) {
    TestNG testng = create(clazz);
    DependencyTrackingListener listener = new DependencyTrackingListener();
    testng.addListener(listener);
    testng.run();
    String cls = clazz.getCanonicalName();
    String key = cls + "." + independentMethod;
    Set<String> upstream = listener.getUpstreamDependencies().get(key);
    dependentMethods =
        Arrays.stream(dependentMethods).map(each -> cls + "." + each).toArray(String[]::new);
    assertThat(upstream).containsExactly(dependentMethods);
  }

  @DataProvider(name = "getUpstreamTestData")
  public Object[][] getUpstreamTestData() {
    return new Object[][] {
      {
        test.dependent.issue893.TestClassSample.class,
        "dependentTest",
        new String[] {"independentTest"}
      },
      {MultiLevelDependenciesTestClassSample.class, "father", new String[] {"grandFather"}},
      {MultiLevelDependenciesTestClassSample.class, "child", new String[] {"father", "mother"}},
      {MultiLevelDependenciesTestClassSample.class, "grandFather", new String[] {}}
    };
  }

  @Test(description = "GITHUB-550", dataProvider = "configDependencyTestData")
  public void testConfigDependencies(String expectedErrorMsg, Class<?> testClassToUse) {
    TestNG testng = create(testClassToUse);
    String actualErrorMsg = null;
    try {
      testng.run();
    } catch (TestNGException e) {
      actualErrorMsg = e.getMessage().replace("\n", "");
    }
    assertThat(actualErrorMsg).isEqualTo(expectedErrorMsg);
  }

  @Test(description = "GITHUB-550")
  public void testConfigDependenciesHappyCase() {
    TestNG testng = create(ConfigDependencySample.class);
    OrderedResultsGatherer gatherer = new OrderedResultsGatherer();
    testng.addListener(gatherer);
    testng.run();
    assertThat(gatherer.getStartTimes()).isSorted();
  }

  @DataProvider(name = "configDependencyTestData")
  public Object[][] configDependencyTestData() {
    String template1 = "None of the dependencies of the method %s.%s are annotated with [@%s].";
    String template2 =
        "%s.%s() is depending on method public void %s.%s(), " + "which is not annotated with @%s.";
    return new Object[][] {
      {
        String.format(
            template1,
            ConfigDependencyWithMismatchedLevelSample.class.getCanonicalName(),
            "beforeMethod",
            "BeforeMethod"),
        ConfigDependencyWithMismatchedLevelSample.class
      },
      {
        String.format(
            template2,
            ConfigDependsOnTestAndConfigMethodSample.class.getCanonicalName(),
            "beforeMethod",
            ConfigDependsOnTestAndConfigMethodSample.class.getCanonicalName(),
            "testMethod",
            "BeforeMethod"),
        ConfigDependsOnTestAndConfigMethodSample.class
      },
      {
        String.format(
            template1,
            ConfigDependsOnTestMethodSample.class.getCanonicalName(),
            "beforeMethod",
            "BeforeMethod"),
        ConfigDependsOnTestMethodSample.class
      }
    };
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
