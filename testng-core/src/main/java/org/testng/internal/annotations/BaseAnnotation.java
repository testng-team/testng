package org.testng.internal.annotations;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.jspecify.annotations.Nullable;

public class BaseAnnotation {

  private @Nullable Class<?> m_testClass;
  private @Nullable Method m_method;
  private @Nullable Constructor m_constructor;

  public @Nullable Constructor getConstructor() {
    return m_constructor;
  }

  public void setConstructor(Constructor constructor) {
    m_constructor = constructor;
  }

  public @Nullable Method getMethod() {
    return m_method;
  }

  public void setMethod(Method method) {
    m_method = method;
  }

  public @Nullable Class<?> getTestClass() {
    return m_testClass;
  }

  public void setTestClass(Class<?> testClass) {
    m_testClass = testClass;
  }
}
