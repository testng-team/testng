package org.testng;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A state object that records the status of the suite run. Mainly used to figure out if there are
 * any @BeforeSuite failures.
 */
public class SuiteRunState {

  private final AtomicBoolean m_hasFailures = new AtomicBoolean();

  public void failed() {
    m_hasFailures.set(true);
  }

  public boolean isFailed() {
    return m_hasFailures.get();
  }
}
