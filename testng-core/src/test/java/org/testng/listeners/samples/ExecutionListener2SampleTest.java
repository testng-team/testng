package org.testng.listeners.samples;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.listeners.ExecutionListenerTest;

@Listeners(ExecutionListenerTest.ExecutionListener.class)
public class ExecutionListener2SampleTest {
  @Test
  public void f() {}
}
