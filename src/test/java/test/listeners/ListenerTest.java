package test.listeners;

import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import test.SimpleBaseTest;

import java.util.Arrays;

import junit.framework.Assert;

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
    Assert.assertEquals(Arrays.asList(1, 2, 3, 4), SimpleListener.m_list);
  }

  @Test(description = "TESTNG-400: onTestFailure should be called before @AfterMethod")
  public void failureBeforeAfterMethod() {
    TestNG tng = create(FailingSampleTest.class);
    tng.run();
    Assert.assertEquals(Arrays.asList(4, 5, 6), SimpleListener.m_list);
  }
}
