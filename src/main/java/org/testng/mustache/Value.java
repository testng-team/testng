package org.testng.mustache;

public class Value {
  private Object m_object;

  public Value(Object object) {
    m_object = object;
  }

  public Object get() {
    return m_object;
  }

}
