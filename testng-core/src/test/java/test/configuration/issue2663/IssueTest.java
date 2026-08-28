package test.configuration.issue2663;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

/**
 * Pins what {@code priority} does on a configuration method.
 *
 * <p>Lower runs first, and it only ever orders methods TestNG is otherwise free to run in any
 * order: a hard dependency and the inheritance guarantee both outrank it. The scope it is compared
 * in is the scope TestNG already collects the methods in -- the class for the class, method and
 * group level ones, the &lt;test&gt; tag for the test and suite level ones -- so a priority never
 * couples two configuration methods that TestNG would not have ordered against each other anyway.
 *
 * <p>Every sample names its methods the other way round from its priorities, so none of these
 * expectations can be met by the alphabetical tie-break that applies when no priority is given.
 */
public class IssueTest extends SimpleBaseTest {

  @DataProvider(name = "singleClassSamples")
  public Object[][] getSingleClassSamples() {
    return new Object[][] {
      // Same kind, same class, different priorities: lower first, both before and after.
      {
        SameClassSample.class,
        Arrays.asList("bravoBefore", "alphaBefore", "test", "bravoAfter", "alphaAfter")
      },
      // Equal priorities hand the ordering back to the existing tie-break, which is by name.
      {EqualPrioritySample.class, Arrays.asList("alphaBefore", "bravoBefore", "test")},
      // No priority at all: the ordering existing suites already get, unchanged.
      {
        NoPrioritySample.class,
        Arrays.asList("alphaBefore", "bravoBefore", "test", "alphaAfter", "bravoAfter")
      },
      // Three levels: the inheritance guarantee decides the levels, the priority decides what
      // happens inside one. Note the priority is not mirrored for the after methods the way the
      // inheritance order is -- lower still runs first there.
      {
        InheritanceChildSample.class,
        Arrays.asList(
            "grandParentBeforeB",
            "grandParentBeforeA",
            "parentBeforeB",
            "parentBeforeA",
            "childBeforeB",
            "childBeforeA",
            "test",
            "childAfterB",
            "childAfterA",
            "parentAfterB",
            "parentAfterA",
            "grandParentAfterB",
            "grandParentAfterA")
      },
      // The priority contradicts the lifecycle and loses: the superclass still goes first.
      {LifecycleChildSample.class, Arrays.asList("parentSetup", "childSetup", "test")},
      // A dependency chain and an independent method in one set. The chain is emitted first
      // whatever the priorities say, so the independent bravoBefore(priority=1) runs last. This is
      // the pre-existing shape of MethodHelper.sortMethods, which returns the methods it has a
      // hard ordering for ahead of the ones it does not; the priority orders neither against the
      // other.
      {
        DependsOnMethodsSample.class,
        Arrays.asList("charlieBefore", "alphaBefore", "bravoBefore", "test")
      },
      {
        DependsOnGroupsSample.class,
        Arrays.asList("charlieBefore", "alphaBefore", "bravoBefore", "test")
      },
      {
        SuiteConfigSample.class,
        Arrays.asList(
            "bravoBeforeSuite", "alphaBeforeSuite", "test", "bravoAfterSuite", "alphaAfterSuite")
      },
      {
        TestConfigSample.class,
        Arrays.asList(
            "bravoBeforeTest", "alphaBeforeTest", "test", "bravoAfterTest", "alphaAfterTest")
      },
      {
        ClassConfigSample.class,
        Arrays.asList(
            "bravoBeforeClass", "alphaBeforeClass", "test", "bravoAfterClass", "alphaAfterClass")
      },
      {
        GroupsConfigSample.class,
        Arrays.asList(
            "bravoBeforeGroups",
            "alphaBeforeGroups",
            "test",
            "bravoAfterGroups",
            "alphaAfterGroups")
      },
    };
  }

  @Test(dataProvider = "singleClassSamples", description = "GITHUB-2663")
  public void priorityOrdersConfigurationMethods(Class<?> sample, List<String> expected) {
    InvokedMethodNameListener listener = run(sample);

    assertThat(listener.getInvokedMethodNames())
        .describedAs(sample.getSimpleName())
        .containsExactlyElementsOf(expected);
  }

  @Test(description = "GITHUB-2663")
  public void priorityOrdersConfigurationMethodsOfUnrelatedClasses() {
    InvokedMethodNameListener listener =
        run(CrossClassAlphaSample.class, CrossClassBravoSample.class);

    // Both @BeforeTest methods belong to the same <test>, so they are compared against each other
    // even though nothing relates the two classes.
    assertThat(listener.getInvokedMethodNames()).startsWith("bravoBeforeTest", "alphaBeforeTest");
  }

  @Test(description = "GITHUB-2663")
  public void priorityIsNotComparedAcrossTestTags() {
    XmlSuite suite = createXmlSuite("2663-two-tests");
    createXmlTest(suite, "first", FirstTestSuiteConfigSample.class);
    createXmlTest(suite, "second", SecondTestSuiteConfigSample.class);

    InvokedMethodNameListener listener = run(suite);

    // @BeforeSuite methods are collected per <test> and concatenated in <test> order, so the
    // priority 9 one still runs before the priority 0 one. Deliberate: a suite wide sort would
    // couple configuration methods of otherwise unrelated <test> tags.
    assertThat(listener.getInvokedMethodNames())
        .startsWith("firstTestBeforeSuite", "secondTestBeforeSuite");
  }

  @Test(description = "GITHUB-2663")
  public void priorityIsHonouredWhenClassesRunInParallel() {
    XmlSuite suite = createXmlSuite("2663-parallel-classes");
    XmlTest test =
        createXmlTest(suite, "classes", ParallelSampleOne.class, ParallelSampleTwo.class);
    test.setParallel(XmlSuite.ParallelMode.CLASSES);
    suite.setThreadCount(2);

    assertEachClassKeptItsPriorityOrder(run(suite));
  }

  @Test(description = "GITHUB-2663")
  public void priorityIsHonouredWhenTestTagsRunInParallel() {
    XmlSuite suite = createXmlSuite("2663-parallel-tests");
    createXmlTest(suite, "first", ParallelSampleOne.class);
    createXmlTest(suite, "second", ParallelSampleTwo.class);
    suite.setParallel(XmlSuite.ParallelMode.TESTS);
    suite.setThreadCount(2);

    assertEachClassKeptItsPriorityOrder(run(suite));
  }

  private static void assertEachClassKeptItsPriorityOrder(InvokedMethodNameListener listener) {
    assertThat(listener.getMethodsForTestClass(ParallelSampleOne.class))
        .containsExactly("oneBravoBeforeClass", "oneAlphaBeforeClass", "oneTest");
    assertThat(listener.getMethodsForTestClass(ParallelSampleTwo.class))
        .containsExactly("twoBravoBeforeClass", "twoAlphaBeforeClass", "twoTest");
  }

  @Test(description = "GITHUB-2663")
  public void priorityIsHonouredWhenMethodsRunInParallel() {
    ParallelBeforeMethodSample.reset();
    XmlSuite suite = createXmlSuite("2663-parallel-methods");
    XmlTest test = createXmlTest(suite, "methods", ParallelBeforeMethodSample.class);
    test.setParallel(XmlSuite.ParallelMode.METHODS);
    suite.setThreadCount(3);

    run(suite);

    Map<String, List<String>> byThread = ParallelBeforeMethodSample.recordsByThread();
    assertThat(byThread).isNotEmpty();
    byThread.forEach(
        (thread, invoked) -> {
          // Each invocation contributes its two @BeforeMethod calls to the thread that ran it, so
          // whatever the interleaving between threads, each thread sees bravo before alpha. An odd
          // count fails on size, which is what a half recorded invocation would produce.
          List<String> expected = new ArrayList<>();
          for (int invocation = 0; invocation < invoked.size() / 2; invocation++) {
            expected.addAll(Arrays.asList("bravoBefore", "alphaBefore"));
          }
          assertThat(invoked).describedAs(thread).containsExactlyElementsOf(expected);
        });
  }

  @DataProvider(name = "orderings")
  public Object[][] getOrderings() {
    return new Object[][] {{"none"}, {"methods"}, {"instances"}};
  }

  @Test(dataProvider = "orderings", description = "GITHUB-2663")
  public void priorityDoesNotDependOnTheMethodOrderingSetting(String ordering) {
    String previous = System.getProperty("testng.order");
    System.setProperty("testng.order", ordering);
    try {
      InvokedMethodNameListener listener = run(SameClassSample.class);

      // -Dtestng.order picks the tie-break, not whether priorities count. Only "instances", the
      // default, leads with the priority on its own; the other two would drop it.
      assertThat(listener.getInvokedMethodNames())
          .describedAs("testng.order=" + ordering)
          .startsWith("bravoBefore", "alphaBefore");
    } finally {
      if (previous == null) {
        System.clearProperty("testng.order");
      } else {
        System.setProperty("testng.order", previous);
      }
    }
  }
}
