package org.testng.listeners.samples;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(ResultContextListener.class)
public class ResultContextListenerSample {

  @Test
  public void f() {}
}
