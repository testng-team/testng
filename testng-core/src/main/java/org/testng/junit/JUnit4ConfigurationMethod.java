package org.testng.junit;

import java.lang.reflect.Method;
import org.testng.internal.ConstructorOrMethod;

public class JUnit4ConfigurationMethod extends ConstructorOrMethod {

  private final Class<?> declaringClass;

  public JUnit4ConfigurationMethod(Class<?> declaringClass) {
    super((Method) null);
    this.declaringClass = declaringClass;
  }

  @Override
  public Class<?> getDeclaringClass() {
    return declaringClass;
  }

  @Override
  public String getName() {
    return "Configuration method from '" + declaringClass.getName() + "'";
  }

  @Override
  public String toString() {
    return getName();
  }
}
