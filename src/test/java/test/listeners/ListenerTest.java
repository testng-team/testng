package test.listeners;

import java.util.Arrays;

import junit.framework.Assert;

import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

public class ListenerTest extends SimpleBaseTest {

  /**
   * Ensure that if a listener is present, we get test(), onSuccess(), afterMethod()
   */
  @Test
  public void listenerShouldBeCalledBeforeConfiguration() {
    TestNG tng = create(OrderedListenerSampleTest.class);
    tng.run();
    Assert.assertEquals(Arrays.asList(1, 2, 3, 4), SimpleListener.m_list);
  }
}
