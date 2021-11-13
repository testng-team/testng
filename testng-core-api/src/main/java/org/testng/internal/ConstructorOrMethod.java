package org.testng.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Objects;

/** Encapsulation of either a method or a constructor. */
public class ConstructorOrMethod {

  private Method m_method;
  private Constructor<?> m_constructor;
  private boolean m_enabled = true;

  public ConstructorOrMethod(Method m) {
    m_method = m;
  }

  public ConstructorOrMethod(Constructor<?> c) {
    m_constructor = c;
  }

  public ConstructorOrMethod(Executable e) {
    if (e instanceof Constructor) {
      m_constructor = (Constructor<?>) e;
    } else {
      m_method = (Method) e;
    }
  }

  public Class<?> getDeclaringClass() {
    return getMethod() != null
        ? getMethod().getDeclaringClass()
        : getConstructor().getDeclaringClass();
  }

  public String getName() {
    return getMethod() != null ? getMethod().getName() : getConstructor().getName();
  }

  public Class<?>[] getParameterTypes() {
    return getMethod() != null
        ? getMethod().getParameterTypes()
        : getConstructor().getParameterTypes();
  }

  public Method getMethod() {
    return m_method;
  }

  public Constructor<?> getConstructor() {
    return m_constructor;
  }

  private Executable getInternalConstructorOrMethod() {
    if (m_method != null) {
      return m_method;
    }
    return m_constructor;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConstructorOrMethod that = (ConstructorOrMethod) o;
    return getInternalConstructorOrMethod().equals(that.getInternalConstructorOrMethod());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getInternalConstructorOrMethod());
  }

  public void setEnabled(boolean enabled) {
    m_enabled = enabled;
  }

  public boolean getEnabled() {
    return m_enabled;
  }

  @Override
  public String toString() {
    if (m_method != null) return m_method.toString();
    else return m_constructor.toString();
  }

  public String stringifyParameterTypes() {
    return Utils.stringifyTypes(getParameterTypes());
  }
}
