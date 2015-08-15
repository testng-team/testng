package test.listeners;

import org.testng.annotations.Listeners;

@Listeners(value = {L3.class, SuiteListener.class, MyInvokedMethodListener.class})
class BaseWithListener {
  static int m_count = 0;

  public static void incrementCount() {
    m_count++;
  }
}
