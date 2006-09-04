package org.testng;


/**
 * A state object that records the status of the suite run. Mainly used to
 * figure out if there are any @BeforeSuite failures. 
 * 
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class SuiteRunState {
  private boolean m_hasFailures;
  
  public void failed() {
    m_hasFailures= true;
  }
  
  public boolean isFailed() {
    return m_hasFailures;
  }
}
