package org.testng.internal;

import org.testng.IAttributes;
import org.testng.collections.Maps;

import java.util.Map;

/**
 * Simple implementation of IAttributes.
 * 
 * @author cbeust@google.com (Cedric Beust), March 16th, 2010
 */
public class Attributes implements IAttributes {
  private Map<String, Object> m_attributes = Maps.newHashMap();

  public Object getAttribute(String name) {
    return m_attributes.get(name);
  }

  public String[] getAttributeNames() {
    return m_attributes.keySet().toArray(new String[m_attributes.size()]);
  }

  public void setAttribute(String name, Object value) {
    m_attributes.put(name, value);
  }

  public Object removeAttribute(String name) {
    return m_attributes.remove(name);
  }
}
