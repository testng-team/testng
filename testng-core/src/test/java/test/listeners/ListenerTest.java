package test.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.testng.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;
import test.listeners.github1029.Issue1029InvokedMethodListener;
import test.listeners.github1029.Issue1029SampleTestClassWithDataDrivenMethod;
import test.listeners.github1029.Issue1029SampleTestClassWithFiveInstances;
import test.listeners.github1029.Issue1029SampleTestClassWithFiveMethods;
import test.listeners.github1029.Issue1029SampleTestClassWithOneMethod;
import test.listeners.github1393.Listener1393;
import test.listeners.github2558.CallHolder;
import test.listeners.github2558.ClassMethodListenersHolder;
import test.listeners.github2558.ExecutionListenersHolder;
import test.listeners.github2558.ReportersHolder;
import test.listeners.github2558.SuiteAlterListenersHolder;
import test.listeners.github2558.SuiteListenersHolder;
import test.listeners.github2558.TestClassSamples;
import test.listeners.github2558.TestListenersHolder;
import test.listeners.github956.ListenerFor956;
import test.listeners.github956.TestClassContainer;
import test.listeners.issue1952.TestclassSample;
import test.listeners.issue2043.SampleTestClass;
import test.listeners.issue2043.listeners.FailFastListener;
import test.listeners.issue2055.DynamicTestListener;
import test.listeners.issue2055.TestClassSample;

public class ListenerTest extends SimpleBaseTest {

  @BeforeMethod
  public void bm() {
    SimpleListener.m_list = Lists.newArrayList();
    CallHolder.clear();
  }

  @Test(
      description =
          "Ensure that if a listener is present, we get test(), onSuccess()," + " afterMethod()")
  public void listenerShouldBeCalledBeforeConfiguration() {
    TestNG tng = create(OrderedListenerSampleTest.class);
    tng.run();
    Assert.assertEquals(SimpleListener.m_list, Arrays.asList(1, 2, 3, 4));
  }

  @Test(description = "TESTNG-400: onTestFailure should be called before @AfterMethod")
  public void failureBeforeAfterMethod() {
    TestNG tng = create(FailingSampleTest.class);
    tng.run();
    Assert.assertEquals(SimpleListener.m_list, Arrays.asList(4, 5, 6));
  }

  @Test(description = "Inherited @Listeners annotations should aggregate")
  public void aggregateListeners() {
    TestNG tng = create(AggregateSampleTest.class);
    AggregateSampleTest.m_count = 0;
    tng.run();
    Assert.assertEquals(AggregateSampleTest.m_count, 2);
  }

  @Test(description = "Should attach only one instance of the same @Listener class per test")
  public void shouldAttachOnlyOneInstanceOfTheSameListenerClassPerTest() {
    TestNG tng = create(Derived1.class, Derived2.class);
    BaseWithListener.m_count = 0;
    tng.run();
    Assert.assertEquals(BaseWithListener.m_count, 2);
  }

  @Test(description = "@Listeners with an ISuiteListener")
  public void suiteListenersShouldWork() {
    TestNG tng = create(SuiteListenerSample.class);
    SuiteListener.start = 0;
    SuiteListener.finish = 0;
    tng.run();
    Assert.assertEquals(SuiteListener.start, 1);
    Assert.assertEquals(SuiteListener.finish, 1);
  }

  @Test(description = "GITHUB-767: ISuiteListener called twice when @Listeners")
  public void suiteListenerInListernersAnnotationShouldBeRunOnce() {
    TestNG tng = createTests("Suite", SuiteListenerSample2.class);
    SuiteListener2.start = 0;
    SuiteListener2.finish = 0;
    tng.run();
    Assert.assertEquals(SuiteListener2.start, 1);
    Assert.assertEquals(SuiteListener2.finish, 1);
  }

  @Test(description = "GITHUB-171")
  public void suiteListenersShouldBeOnlyRunOnceWithManyTests() {
    TestNG tng = createTests("suite", Derived1.class, Derived2.class);
    SuiteListener.start = 0;
    SuiteListener.finish = 0;
    tng.run();
    Assert.assertEquals(SuiteListener.start, 1);
    Assert.assertEquals(SuiteListener.finish, 1);
  }

  @Test(description = "GITHUB-795")
  public void suiteListenersShouldBeOnlyRunOnceWithManyIdenticalTests() {
    TestNG tng = createTests("suite", Derived1.class, Derived1.class);
    SuiteListener.start = 0;
    SuiteListener.finish = 0;
    tng.run();
    Assert.assertEquals(SuiteListener.start, 1);
    Assert.assertEquals(SuiteListener.finish, 1);
  }

  @Test(description = "GITHUB-169")
  public void invokedMethodListenersShouldBeOnlyRunOnceWithManyTests() {
    TestNG tng = createTests("suite", Derived1.class, Derived2.class);
    MyInvokedMethodListener.beforeInvocation.clear();
    MyInvokedMethodListener.afterInvocation.clear();
    tng.run();
    assertThat(MyInvokedMethodListener.beforeInvocation).containsOnly(entry("t", 1), entry("s", 1));
    assertThat(MyInvokedMethodListener.afterInvocation).containsOnly(entry("t", 1), entry("s", 1));
  }

  @Test(description = "GITHUB-154: MethodInterceptor will be called twice")
  public void methodInterceptorShouldBeRunOnce() {
    TestNG tng = create(SuiteListenerSample.class);
    MyMethodInterceptor interceptor = new MyMethodInterceptor();
    tng.addListener(interceptor);
    tng.run();
    Assert.assertEquals(interceptor.getCount(), 1);
  }

  @Test(
      description =
          "GITHUB-1863:IMethodInterceptor will be invoked twice when listener implements both ITestListener and IMethodInterceptor via eclipse execution way")
  public void
      methodInterceptorShouldBeRunOnceWhenCustomisedListenerImplementsITestListenerAndIMethodInterceptor() {
    TestNG tng = create(LSampleTest.class);
    InterceptorInvokeTwiceSimulateListener interceptor =
        new InterceptorInvokeTwiceSimulateListener();
    tng.addListener(interceptor);
    tng.run();
    Assert.assertEquals(interceptor.getCount(), 1);
  }

  @Test(description = "GITHUB-356: Add listeners for @BeforeClass/@AfterClass")
  public void classListenerShouldWork() {
    MyClassListener.names.clear();
    TestNG tng = create(Derived1.class, Derived2.class);
    MyClassListener listener = new MyClassListener();
    tng.addListener(listener);
    TestListenerAdapter adapter = new TestListenerAdapter();
    tng.addListener(adapter);
    tng.run();
    assertThat(adapter.getFailedTests()).isEmpty();
    assertThat(adapter.getSkippedTests()).isEmpty();
    assertThat(MyClassListener.names)
        .containsExactly(
            "BeforeClass=Derived1",
            "BeforeMethod=Derived1.t",
            "AfterMethod=Derived1.t",
            "AfterClass=Derived1",
            "BeforeClass=Derived2",
            "BeforeMethod=Derived2.s",
            "AfterMethod=Derived2.s",
            "AfterClass=Derived2");
  }

  @Test
  public void classListenerShouldWorkWithManyTestMethods() {
    MyClassListener.names.clear();
    TestNG tng = create(Derived3.class);
    MyClassListener listener = new MyClassListener();
    tng.addListener(listener);
    TestListenerAdapter adapter = new TestListenerAdapter();
    tng.addListener(adapter);
    tng.run();
    assertThat(adapter.getFailedTests()).isEmpty();
    assertThat(adapter.getSkippedTests()).isEmpty();
    assertThat(MyClassListener.names)
        .containsExactly(
            "BeforeClass=Derived3",
            "BeforeMethod=Derived3.r",
            "AfterMethod=Derived3.r",
            "BeforeMethod=Derived3.r1",
            "AfterMethod=Derived3.r1",
            "AfterClass=Derived3");
  }

  @Test(description = "GITHUB-356: Add listeners for @BeforeClass/@AfterClass")
  public void classListenerShouldWorkFromAnnotation() {
    MyClassListener.names.clear();
    TestNG tng = create(ClassListenerSample.class);
    TestListenerAdapter adapter = new TestListenerAdapter();
    tng.addListener(adapter);
    tng.run();
    assertThat(adapter.getFailedTests()).isEmpty();
    assertThat(adapter.getSkippedTests()).isEmpty();
    assertThat(MyClassListener.names)
        .containsExactly(
            "BeforeClass=ClassListenerSample",
            "BeforeMethod=ClassListenerSample.test",
            "AfterMethod=ClassListenerSample.test",
            "BeforeMethod=ClassListenerSample.test2",
            "AfterMethod=ClassListenerSample.test2",
            "AfterClass=ClassListenerSample");
  }

  @Test
  public void classListenerShouldBeOnlyRunOnce() {
    MyClassListener.names.clear();
    TestNG tng = create(Derived3.class);
    MyClassListener listener = new MyClassListener();
    tng.addListener(listener);
    TestListenerAdapter adapter = new TestListenerAdapter();
    tng.addListener(adapter);
    TestAndClassListener tacl = new TestAndClassListener();
    tng.addListener(tacl);
    tng.run();
    assertThat(adapter.getFailedTests()).isEmpty();
    assertThat(adapter.getSkippedTests()).isEmpty();
    assertThat(tacl.getBeforeClassCount()).isEqualTo(1);
    assertThat(tacl.getAfterClassCount()).isEqualTo(1);
  }

  @Test(description = "GITHUB-911: Should not call method listeners for skipped methods")
  public void methodListenersShouldNotBeCalledForSkippedMethods() {
    GitHub911Listener listener = new GitHub911Listener();
    TestNG tng = create(GitHub911Sample.class);
    tng.addListener(listener);
    tng.run();
    Assert.assertEquals(listener.onStart, 1);
    Assert.assertEquals(listener.onFinish, 1);
    Assert.assertEquals(listener.onTestStart, 2);
    Assert.assertEquals(listener.onTestSuccess, 0);
    Assert.assertEquals(listener.onTestFailure, 0);
    Assert.assertEquals(listener.onTestFailedButWithinSuccessPercentage, 0);
    Assert.assertEquals(listener.onTestSkipped, 2);
  }

  @Test(description = "GITHUB-895: Changing status of test by setStatus of ITestResult")
  public void setStatusShouldWorkInListener() {
    SetStatusListener listener = new SetStatusListener();
    TestNG tng = create(SetStatusSample.class);
    tng.addListener(listener);
    tng.run();
    Assert.assertEquals(listener.getContext().getFailedTests().size(), 0);
    Assert.assertEquals(listener.getContext().getFailedButWithinSuccessPercentageTests().size(), 0);
    Assert.assertEquals(listener.getContext().getSkippedTests().size(), 0);
    Assert.assertEquals(listener.getContext().getPassedTests().size(), 1);
  }

  @Test(
      description =
          "GITHUB-1084: Using deprecated addListener methods should not register many times")
  public void listenerRegistration() {
    MultiListener listener = new MultiListener();
    TestNG tng = create(SimpleSample.class);
    // Keep using deprecated addListener methods. It is what the test is testing
    tng.addListener((ITestNGListener) listener);
    tng.addListener((ITestNGListener) listener);
    tng.addListener((ITestNGListener) listener);
    tng.run();
    Assert.assertEquals(listener.getOnSuiteStartCount(), 1);
    Assert.assertEquals(listener.getOnSuiteFinishCount(), 1);
    Assert.assertEquals(listener.getOnTestStartCount(), 1);
    Assert.assertEquals(listener.getOnTestFinishCount(), 1);
    Assert.assertEquals(listener.getBeforeInvocationCount(), 1);
    Assert.assertEquals(listener.getAfterInvocationCount(), 1);
    Assert.assertEquals(listener.getOnMethodTestStartCount(), 1);
    Assert.assertEquals(listener.getOnMethodTestSuccessCount(), 1);
  }

  @Test
  public void testListenerCallInvocation() {
    XmlSuite suite =
        createXmlSuite(
            "suite956",
            "test956",
            TestClassContainer.FirstTestClass.class,
            TestClassContainer.SecondTestClass.class);
    TestNG tng = create(suite);
    ListenerFor956 listener = new ListenerFor956();
    tng.addListener(listener);
    tng.run();
    List<String> messages = ListenerFor956.getMessages();
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get(0), "Executing test956");
  }

  @Test(description = "GITHUB-1393: fail a test from onTestStart method")
  public void testFailATestFromOnTestStart() {
    TestNG tng = create(SimpleSample.class);
    TestListenerAdapter adapter = new TestListenerAdapter();
    tng.addListener(adapter);
    tng.addListener(new Listener1393());
    tng.run();
    Assert.assertEquals(adapter.getPassedTests().size(), 0);
    Assert.assertEquals(adapter.getFailedTests().size(), 1);
  }

  @Test(dataProvider = "dp", description = "GITHUB-1029")
  public void ensureXmlTestIsNotNull(Class<?> clazz, XmlSuite.ParallelMode mode) {
    XmlSuite xmlSuite = createXmlSuite("Suite");
    createXmlTest(xmlSuite, "GITHUB-1029-Test", clazz);
    xmlSuite.setParallel(mode);
    Issue1029InvokedMethodListener listener = new Issue1029InvokedMethodListener();
    TestNG testng = create(xmlSuite);
    testng.addListener(listener);
    testng.setThreadCount(10);
    testng.setDataProviderThreadCount(10);
    testng.run();
    List<String> expected = Collections.nCopies(5, "GITHUB-1029-Test");
    assertThat(listener.getBeforeInvocation()).containsExactlyElementsOf(expected);
    assertThat(listener.getAfterInvocation()).containsExactlyElementsOf(expected);
  }

  @Test(description = "GITHUB-1952")
  public void ensureTimeoutListenerIsInvokedForTimingoutTests() {
    TestNG tng = create(TestclassSample.class);
    TestListenerAdapter adapter = new TestListenerAdapter();
    tng.addListener(adapter);
    tng.run();
    assertThat(adapter.getTimedoutTests()).hasSize(1);
  }

  @DataProvider(name = "dp")
  public Object[][] getData() {
    return new Object[][] {
      {Issue1029SampleTestClassWithFiveMethods.class, XmlSuite.ParallelMode.METHODS},
      {Issue1029SampleTestClassWithOneMethod.class, XmlSuite.ParallelMode.METHODS},
      {Issue1029SampleTestClassWithDataDrivenMethod.class, XmlSuite.ParallelMode.METHODS},
      {Issue1029SampleTestClassWithFiveInstances.class, XmlSuite.ParallelMode.INSTANCES}
    };
  }

  @Test(description = "GITHUB-2043")
  public void runTest() {
    TestNG testng = create(SampleTestClass.class);
    testng.run();
    assertThat(FailFastListener.msgs)
        .containsExactly(
            "FailFastListener:afterInvocation",
            "FailFastListener:beforeDataProviderExecution",
            "FailFastListener:beforeConfiguration");
  }

  @Test(description = "GITHUB-2055")
  public void ensureDynamicTestListenerInjection() {
    TestNG testng = create(TestClassSample.class);
    testng.run();
    assertThat(DynamicTestListener.MSGS).containsExactly("Starting testMethod");
  }

  @Test(description = "GITHUB-2061")
  public void ensureDynamicListenerAdditionsDontTriggerConcurrentModificationExceptions() {
    TestNG testng = create(test.listeners.issue2061.TestClassSample.class);
    testng.run();
    assertThat(testng.getStatus()).isEqualTo(0);
  }

  @Test(description = "GITHUB-2558")
  public void ensureInsertionOrderIsHonouredByListeners() {
    String prefix = "test.listeners.github2558.";
    String[] expectedOrder =
        new String[] {
          prefix + "ExecutionListenersHolder$ExecutionListenerB.onExecutionStart()",
          prefix + "ExecutionListenersHolder$ExecutionListenerA.onExecutionStart()",
          prefix + "SuiteAlterListenersHolder$SuiteAlterB.alter()",
          prefix + "SuiteAlterListenersHolder$SuiteAlterA.alter()",
          prefix + "SuiteListenersHolder$SuiteListenerB.onStart()",
          prefix + "SuiteListenersHolder$SuiteListenerA.onStart()",
          prefix + "TestListenersHolder$TestListenerB.onStart(ctx)",
          prefix + "TestListenersHolder$TestListenerA.onStart(ctx)",
          prefix + "ClassMethodListenersHolder$ClassMethodListenerB.onBeforeClass()",
          prefix + "ClassMethodListenersHolder$ClassMethodListenerA.onBeforeClass()",
          prefix + "ClassMethodListenersHolder$ClassMethodListenerB.beforeInvocation()",
          prefix + "ClassMethodListenersHolder$ClassMethodListenerA.beforeInvocation()",
          prefix + "ClassMethodListenersHolder$ClassMethodListenerA.afterInvocation()",
          prefix + "ClassMethodListenersHolder$ClassMethodListenerB.afterInvocation()",
          prefix + "ClassMethodListenersHolder$ClassMethodListenerB.onBeforeClass()",
          prefix + "ClassMethodListenersHolder$ClassMethodListenerA.onBeforeClass()",
          prefix + "ClassMethodListenersHolder$ClassMethodListenerB.onBeforeClass()",
          prefix + "ClassMethodListenersHolder$ClassMethodListenerA.onBeforeClass()",
          prefix + "ClassMethodListenersHolder$ClassMethodListenerB.beforeInvocation()",
          prefix + "ClassMethodListenersHolder$ClassMethodListenerA.beforeInvocation()",
          prefix + "ClassMethodListenersHolder$ClassMethodListenerA.afterInvocation()",
          prefix + "ClassMethodListenersHolder$ClassMethodListenerB.afterInvocation()",
          prefix + "ClassMethodListenersHolder$ClassMethodListenerB.onBeforeClass()",
          prefix + "ClassMethodListenersHolder$ClassMethodListenerA.onBeforeClass()",
          prefix + "TestListenersHolder$TestListenerA.onFinish(ctx)",
          prefix + "TestListenersHolder$TestListenerB.onFinish(ctx)",
          prefix + "SuiteListenersHolder$SuiteListenerA.onFinish()",
          prefix + "SuiteListenersHolder$SuiteListenerB.onFinish()",
          prefix + "ReportersHolder$ReporterB.generateReport()",
          prefix + "ReportersHolder$ReporterA.generateReport()",
          prefix + "ExecutionListenersHolder$ExecutionListenerA.onExecutionFinish()",
          prefix + "ExecutionListenersHolder$ExecutionListenerB.onExecutionFinish()"
        };
    List<Class<?>> listeners =
        Arrays.asList(
            ExecutionListenersHolder.ExecutionListenerB.class,
            SuiteAlterListenersHolder.SuiteAlterB.class,
            SuiteListenersHolder.SuiteListenerB.class,
            TestListenersHolder.TestListenerB.class,
            ClassMethodListenersHolder.ClassMethodListenerB.class,
            ReportersHolder.ReporterB.class,
            ExecutionListenersHolder.ExecutionListenerA.class,
            SuiteAlterListenersHolder.SuiteAlterA.class,
            SuiteListenersHolder.SuiteListenerA.class,
            TestListenersHolder.TestListenerA.class,
            ClassMethodListenersHolder.ClassMethodListenerA.class,
            ReportersHolder.ReporterA.class);

    XmlSuite xmlSuite = new XmlSuite();
    xmlSuite.setName("Random_Suite");
    listeners.forEach(each -> xmlSuite.addListener(each.getName()));
    createXmlTest(
        xmlSuite,
        "random_test",
        TestClassSamples.TestClassSampleA.class,
        TestClassSamples.TestClassSampleB.class);
    TestNG testng = create(xmlSuite);
    testng.setUseDefaultListeners(false);
    testng.run();
    List<String> actual = CallHolder.getCalls();
    assertThat(actual).containsExactly(expectedOrder);
  }
}
