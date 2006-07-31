package org.testng.remote.strprotocol;

import org.testng.ISuite;


/**
 * A <code>IStringMessage</code> implementation for suite running events.
 * 
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class SuiteMessage implements IStringMessage {
  protected final String m_suiteName;
  protected final int m_testMethodCount;
  protected final boolean m_startSuite;
  
  SuiteMessage(final String suiteName, final boolean startSuiteRun, final int methodCount) {
    m_suiteName = suiteName;
    m_startSuite = startSuiteRun;
    m_testMethodCount = methodCount;
  }
  
  public SuiteMessage(final ISuite suite, final boolean startSuiteRun) {
    m_suiteName = suite.getName();
    m_testMethodCount =suite.getInvokedMethods().size();
    m_startSuite = startSuiteRun;
  }
  
  public boolean isMessageOnStart() {
    return m_startSuite;
  }
  
  public String getSuiteName() {
    return m_suiteName;
  }
  
  public int getTestMethodCount() {
    return m_testMethodCount;
  }
  
  /**
   * @see net.noco.testng.runner.IStringMessage#getMessageAsString()
   */
  public String getMessageAsString() {
    StringBuffer buf = new StringBuffer();
    
    buf.append(m_startSuite ? MessageHelper.SUITE_START : MessageHelper.SUITE_FINISH)
        .append(MessageHelper.DELIMITER)
        .append(m_suiteName)
        .append(MessageHelper.DELIMITER)
        .append(m_testMethodCount)
        ;
    
    return buf.toString();
  }
}
