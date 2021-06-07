package org.testng.internal.listeners.parameterindex;

import org.testng.TestNG;
import org.testng.annotations.Test;

public class ParameterIndexListenerTest {

  @Test
  public void listenerShouldReceiveRightParameterIndexTest() {
    TestNG testng = new TestNG();
    testng.setTestClasses(new Class[] {TestWithProviderTest.class});
    testng.run();
  }
}
