package org.testng.remote.strprotocol;

import org.testng.collections.Maps;

import java.util.Iterator;
import java.util.Map;



/**
 * A generic message to be used with remote listeners.
 * It is described by a {@link #m_messageType} and can contain a <code>Map</code>
 * or values.
 * 
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class GenericMessage implements IStringMessage {
  protected Map m_properties;
  protected final int m_messageType;
  
  public GenericMessage(final int type) {
    this(type, Maps.newHashMap());
  }
  
  public GenericMessage(final int type, Map props) {
    m_messageType = type;
    m_properties = props;
  }
  
  public GenericMessage addProperty(final String propName, final Object propValue) {
    m_properties.put(propName, propValue);
    
    return this;
  }
  
  public GenericMessage addProperty(final String propName, final int propValue) {
    return addProperty(propName, new Integer(propValue));
  }
  
  public String getProperty(final String propName) {
    return (String) m_properties.get(propName);
  }
  
  /**
   * @see net.noco.testng.runner.IStringMessage#getMessageAsString()
   */
  public String getMessageAsString() {
    StringBuffer buf = new StringBuffer();
    
    buf.append(m_messageType);
    
        for(Iterator it = m_properties.entrySet().iterator(); it.hasNext(); ) {
          Map.Entry entry = (Map.Entry) it.next();
          
          buf.append(MessageHelper.DELIMITER)
              .append(entry.getKey())
              .append(MessageHelper.DELIMITER)
              .append(entry.getValue())
              ;
        }

    
    return buf.toString();
  }

}
