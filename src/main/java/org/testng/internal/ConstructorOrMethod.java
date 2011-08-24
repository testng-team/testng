package org.testng.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Encapsulation of either a method or a constructor.
 *
 * @author Cedric Beust <cedric@beust.com>
 */
public class ConstructorOrMethod {

  private Method m_method;
  private Constructor m_constructor;
  private boolean m_enabled = true;

  public ConstructorOrMethod(Method m) {
    m_method = m;
  }

  public ConstructorOrMethod(Constructor c) {
    m_constructor = c;
  }

  public Class<?> getDeclaringClass() {
    return getMethod() != null ? getMethod().getDeclaringClass() : getConstructor().getDeclaringClass();
  }

  public String getName() {
    return getMethod() != null ? getMethod().getName() : getConstructor().getName();
  }

  public Class[] getParameterTypes() {
    return getMethod() != null ? getMethod().getParameterTypes() : getConstructor().getParameterTypes();
  }

  public Method getMethod() {
    return m_method;
  }

  public Constructor getConstructor() {
    return m_constructor;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((getConstructor() == null) ? 0 : getConstructor().hashCode());
    result = prime * result + ((getMethod() == null) ? 0 : getMethod().hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ConstructorOrMethod other = (ConstructorOrMethod) obj;
    if (getConstructor() == null) {
      if (other.getConstructor() != null)
        return false;
    } else if (!getConstructor().equals(other.getConstructor()))
      return false;
    if (getMethod() == null) {
      if (other.getMethod() != null)
        return false;
    } else if (!getMethod().equals(other.getMethod()))
      return false;
    return true;
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
}
