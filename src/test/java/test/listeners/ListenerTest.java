package test.listeners;

import org.testng.Assert;
import org.testng.ITestNGListener;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import test.SimpleBaseTest;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class ListenerTest extends SimpleBaseTest {

  @BeforeMethod
  public void bm() {
    SimpleListener.m_list = Lists.newArrayList();
  }

  @Test(description = "Ensure that if a listener is present, we get test(), onSuccess()," +
  		" afterMethod()")
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
    assertThat(MyInvokedMethodListener.beforeInvocation).containsOnly(
            entry("t", 1), entry("s", 1)
    );
    assertThat(MyInvokedMethodListener.afterInvocation).containsOnly(
            entry("t", 1), entry("s", 1)
    );
  }

  @Test(description = "GITHUB-154: MethodInterceptor will be called twice")
  public void methodInterceptorShouldBeRunOnce() {
    TestNG tng = create(SuiteListenerSample.class);
    MyMethodInterceptor interceptor = new MyMethodInterceptor();
    tng.addListener(interceptor);
    tng.run();
    Assert.assertEquals(interceptor.getCount(), 1);
  }

  @Test(description = "GITHUB-356: Add listeners for @BeforeClass/@AfterClass")
  public void classListenerShouldWork() {
    MyClassListener.names.clear();
    TestNG tng = create(Derived1.class, Derived2.class);
    MyClassListener listener = new MyClassListener();
    tng.addListener((Object)listener);
    TestListenerAdapter adapter = new TestListenerAdapter();
    tng.addListener(adapter);
    tng.run();
    assertThat(adapter.getFailedTests()).isEmpty();
    assertThat(adapter.getSkippedTests()).isEmpty();
    assertThat(MyClassListener.names).containsExactly(
        "BeforeClass=Derived1",
          "BeforeMethod=Derived1.t", "AfterMethod=Derived1.t",
        "AfterClass=Derived1",
        "BeforeClass=Derived2",
          "BeforeMethod=Derived2.s", "AfterMethod=Derived2.s",
        "AfterClass=Derived2");
  }

  @Test
  public void classListenerShouldWorkWithManyTestMethods() {
    MyClassListener.names.clear();
    TestNG tng = create(Derived3.class);
    MyClassListener listener = new MyClassListener();
    tng.addListener((Object)listener);
    TestListenerAdapter adapter = new TestListenerAdapter();
    tng.addListener(adapter);
    tng.run();
    assertThat(adapter.getFailedTests()).isEmpty();
    assertThat(adapter.getSkippedTests()).isEmpty();
    assertThat(MyClassListener.names).containsExactly(
        "BeforeClass=Derived3",
          "BeforeMethod=Derived3.r", "AfterMethod=Derived3.r",
          "BeforeMethod=Derived3.r1", "AfterMethod=Derived3.r1",
        "AfterClass=Derived3"
    );
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
    assertThat(MyClassListener.names).containsExactly(
        "BeforeClass=ClassListenerSample",
          "BeforeMethod=ClassListenerSample.test", "AfterMethod=ClassListenerSample.test",
          "BeforeMethod=ClassListenerSample.test2", "AfterMethod=ClassListenerSample.test2",
        "AfterClass=ClassListenerSample"
    );
  }

  @Test
  public void classListenerShouldBeOnlyRunOnce() {
    MyClassListener.names.clear();
    TestNG tng = create(Derived3.class);
    MyClassListener listener = new MyClassListener();
    tng.addListener((ITestNGListener) listener);
    TestListenerAdapter adapter = new TestListenerAdapter();
    tng.addListener((ITestNGListener) adapter);
    TestAndClassListener tacl = new TestAndClassListener();
    tng.addListener((ITestNGListener) tacl);
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
    Assert.assertEquals(listener.onTestStart, 0);
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
}
