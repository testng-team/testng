package org.testng;

/**
 * A state object that records the status of the suite run. Mainly used to
 * figure out if there are any @BeforeSuite failures.
 */
public class SuiteRunState {

  private boolean m_hasFailures;

  public synchronized void failed() {
    m_hasFailures= true;
  }

  public synchronized boolean isFailed() {
    return m_hasFailures;
  }
}
