package org.testng.internal;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.Nullable;
import org.testng.IAttributes;

/** Simple implementation of IAttributes. */
public class Attributes implements IAttributes {

  private final Map<String, Object> m_attributes = new ConcurrentHashMap<>();

  @Override
  public @Nullable Object getAttribute(String name) {
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
  public @Nullable Object removeAttribute(String name) {
    return m_attributes.remove(name);
  }
}
