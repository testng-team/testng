package test.listeners;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import test.SimpleBaseTest;

import java.util.Arrays;

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
    Assert.assertEquals( SimpleListener.m_list, Arrays.asList(4, 5, 6));
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
    TestNG tng = create(new Class [] {Derived1.class, Derived2.class});
    BaseWithListener.m_count = 0;
    tng.run();
    Assert.assertEquals(BaseWithListener.m_count, 2);
  }

  @Test(description = "@Listeners with an ISuiteListener")
  public void suiteListenersShouldWork() {
    TestNG tng = create(SuiteListenerSample.class);
    SuiteListener.start = false;
    SuiteListener.finish = false;
    tng.run();
    Assert.assertTrue(SuiteListener.start);
    Assert.assertTrue(SuiteListener.finish);
  }
}
