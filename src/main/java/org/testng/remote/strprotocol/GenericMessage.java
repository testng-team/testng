package org.testng.remote.strprotocol;




/**
 * A generic message to be used with remote listeners.
 * It is described by a {@link #m_messageType} and can contain a <code>Map</code>
 * or values.
 *
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class GenericMessage implements IStringMessage {
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

//  public GenericMessage(final int type, Map props) {
//    m_messageType = type;
//    m_properties = props;
//  }

//  public GenericMessage addProperty(final String propName, final Object propValue) {
//    m_properties.put(propName, propValue);
//
//    return this;
//  }

  public GenericMessage addProperty(final String propName, final int propValue) {
    return addProperty(propName, Integer.valueOf(propValue));
  }

//  public String getProperty(final String propName) {
//    return (String) m_properties.get(propName);
//  }

  /**
   * @see net.noco.testng.runner.IStringMessage#getMessageAsString()
   */
  @Override
  public String getMessageAsString() {
    StringBuffer buf = new StringBuffer();

    buf.append(m_messageType);
    buf.append(MessageHelper.DELIMITER).append("testCount").append(getTestCount())
        .append(MessageHelper.DELIMITER).append("suiteCount").append(getSuiteCount());

//        for(Iterator it = m_properties.entrySet().iterator(); it.hasNext(); ) {
//          Map.Entry entry = (Map.Entry) it.next();
//
//          buf.append(MessageHelper.DELIMITER)
//              .append(entry.getKey())
//              .append(MessageHelper.DELIMITER)
//              .append(entry.getValue())
//              ;
//        }


    return buf.toString();
  }

  @Override
  public String toString() {
    return "[GenericMessage suiteCount:" + m_suiteCount + " testCount:" + m_testCount + "]";
  }
}
