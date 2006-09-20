package org.testng.internal.annotations;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class BaseAnnotation {
  private Class m_testClass;
  private Method m_method;
  private Constructor m_constructor;

  public Constructor getConstructor() {
    return m_constructor;
  }
  public void setConstructor(Constructor constructor) {
    m_constructor = constructor;
  }
  public Method getMethod() {
    return m_method;
  }
  public void setMethod(Method method) {
    m_method = method;
  }
  public Class getTestClass() {
    return m_testClass;
  }
  public void setTestClass(Class testClass) {
    m_testClass = testClass;
  }

}
