package org.testng.internal;

import org.testng.IInstanceInfo;

public class InstanceInfo<T> implements IInstanceInfo<T> {
  private final Class<T> m_instanceClass;
  private final T m_instance;

  public InstanceInfo(Class<T> cls, T instance) {
    m_instanceClass = cls;
    m_instance = instance;
  }

  @Override
  public T getInstance() {
    return m_instance;
  }

  @Override
  public Class<T> getInstanceClass() {
    return m_instanceClass;
  }
}
