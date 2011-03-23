package org.testng.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class ConstructorOrMethod {

  public Method method;
  public Constructor constructor;

  public ConstructorOrMethod(Method m) {
    method = m;
  }

  public ConstructorOrMethod(Constructor c) {
    constructor = c;
  }

  public Class<?> getDeclaringClass() {
    return method != null ? method.getDeclaringClass() : constructor.getDeclaringClass();
  }

  public String getName() {
    return method != null ? method.getName() : constructor.getName();
  }

  public Class[] getParameterTypes() {
    return method != null ? method.getParameterTypes() : constructor.getParameterTypes();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((constructor == null) ? 0 : constructor.hashCode());
    result = prime * result + ((method == null) ? 0 : method.hashCode());
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
    if (constructor == null) {
      if (other.constructor != null)
        return false;
    } else if (!constructor.equals(other.constructor))
      return false;
    if (method == null) {
      if (other.method != null)
        return false;
    } else if (!method.equals(other.method))
      return false;
    return true;
  }

}
