package org.testng.listeners.samples;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(SuiteListener.class)
public class SuiteListenerSample {

  @Test
  public void foo() {}
}
