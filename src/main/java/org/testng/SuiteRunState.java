package org.testng;

import java.io.Serializable;


/**
 * A state object that records the status of the suite run. Mainly used to
 * figure out if there are any @BeforeSuite failures.
 *
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class SuiteRunState implements Serializable {
  /**
   *
   */
  private static final long serialVersionUID = -2716934905049123874L;
  private boolean m_hasFailures;

  public synchronized void failed() {
    m_hasFailures= true;
  }

  public synchronized boolean isFailed() {
    return m_hasFailures;
  }
}
