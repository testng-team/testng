package test.listeners;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

import java.util.Arrays;
import java.util.Collections;

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
    MyClassListener.beforeNames.clear();
    MyClassListener.afterNames.clear();
    TestNG tng = create(Derived1.class, Derived2.class);
    MyClassListener listener = new MyClassListener();
    tng.addListener(listener);
    tng.run();
    assertThat(MyClassListener.beforeNames).containsExactly("Derived1", "Derived2");
    assertThat(MyClassListener.afterNames).containsExactly("Derived1", "Derived2");
  }

  @Test(description = "GITHUB-356: Add listeners for @BeforeClass/@AfterClass")
  public void classListenerShouldWorkFromAnnotation() {
    MyClassListener.beforeNames.clear();
    MyClassListener.afterNames.clear();
    TestNG tng = create(ClassListenerSample.class);
    tng.run();
    assertThat(MyClassListener.beforeNames).containsExactly("ClassListenerSample");
    assertThat(MyClassListener.afterNames).containsExactly("ClassListenerSample");
  }
}
