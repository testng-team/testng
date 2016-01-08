package org.testng.remote.strprotocol;




/**
 * A generic message to be used with remote listeners.
 * It is described by a {@link #m_messageType} and can contain a <code>Map</code>
 * or values.
 *
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class GenericMessage implements IStringMessage {
  private static final long serialVersionUID = 1440074281953763545L;
//  protected Map m_properties;
  protected final int m_messageType;
  private int m_suiteCount;

  private int m_testCount;

  public GenericMessage(final int type) {
    m_messageType = type;
  }

  public int getSuiteCount() {
    return m_suiteCount;
  }

  public void setSuiteCount(int suiteCount) {
    m_suiteCount = suiteCount;
  }

  public int getTestCount() {
    return m_testCount;
  }

  public void setTestCount(int testCount) {
    m_testCount = testCount;
  }

  @Override
  public String getMessageAsString() {
    StringBuffer buf = new StringBuffer();

    buf.append(m_messageType);
    buf.append(MessageHelper.DELIMITER).append("testCount").append(getTestCount())
        .append(MessageHelper.DELIMITER).append("suiteCount").append(getSuiteCount());

    return buf.toString();
  }

  @Override
  public String toString() {
    return "[GenericMessage suiteCount:" + m_suiteCount + " testCount:" + m_testCount + "]";
  }
}
