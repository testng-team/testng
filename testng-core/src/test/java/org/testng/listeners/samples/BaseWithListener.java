package org.testng.listeners.samples;

import org.testng.annotations.Listeners;

@Listeners(value = {L3.class, SuiteListener.class, MyInvokedMethodListener.class})
public class BaseWithListener {
  public static int m_count = 0;

  public static void incrementCount() {
    m_count++;
  }
}
