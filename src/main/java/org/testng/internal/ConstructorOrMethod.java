package org.testng.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Encapsulation of either a method or a constructor.
 *
 * @author Cedric Beust <cedric@beust.com>
 */
public class ConstructorOrMethod {

  private String m_class_name_index = null;
  private String m_name_index = null;

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

  public String getClassNameIndex() {
    return m_class_name_index;
  }

  public void setClassNameIndex(String m_class_name_index) {
    this.m_class_name_index = m_class_name_index;
  }

  public String getNameIndex() {
    return m_name_index;
  }

  public void setNameIndex(String m_name_index) {
    this.m_name_index = m_name_index;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((getConstructor() == null) ? 0 : getConstructor().hashCode());
    result = prime * result + ((getMethod() == null) ? 0 : getMethod().hashCode());
    result = prime * result + ((getClassNameIndex() == null) ? 0 : getClassNameIndex().hashCode());
    result = prime * result + ((getNameIndex() == null) ? 0 : getNameIndex().hashCode());
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
//        return true;
    return Objects.equals(m_class_name_index, other.m_class_name_index)
      && Objects.equals(m_name_index, other.m_name_index);
  }

  public boolean getEnabled() {
    return m_enabled;
  }

  public void setEnabled(boolean enabled) {
    m_enabled = enabled;
  }

  @Override
  public String toString() {
//        if (m_method != null) return m_method.toString();
//        else return m_constructor.toString();
    return m_class_name_index + "_" +
      m_name_index + "_" +
      ((m_method != null) ? m_method.toString() : m_constructor.toString());
  }
}
