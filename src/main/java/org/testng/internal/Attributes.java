package org.testng.internal;

import org.testng.IAttributes;
import org.testng.collections.Maps;

import java.util.Map;
import java.util.Set;

/**
 * Simple implementation of IAttributes.
 *
 * @author cbeust@google.com (Cedric Beust), March 16th, 2010
 */
public class Attributes implements IAttributes {
  /**
   *
   */
  private static final long serialVersionUID = 6701159979281335152L;
  private Map<String, Object> m_attributes = Maps.newHashMap();

  @Override
  public Object getAttribute(String name) {
    return m_attributes.get(name);
  }

  @Override
  public Set<String> getAttributeNames() {
    return m_attributes.keySet();
  }

  @Override
  public void setAttribute(String name, Object value) {
    m_attributes.put(name, value);
  }

  @Override
  public Object removeAttribute(String name) {
    return m_attributes.remove(name);
  }
}
