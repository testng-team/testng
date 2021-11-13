package org.testng.junit;

import java.lang.reflect.Method;
import org.junit.runner.Description;
import org.testng.internal.ConstructorOrMethod;

public class JUnit4SpockMethod extends ConstructorOrMethod {

  private static final Class<?> SPOCK_SPEC_CLASS = getSpockSpecClass();

  private static Class<?> getSpockSpecClass() {
    try {
      return Class.forName("spock.lang.Specification");
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  public static boolean isSpockClass(Class<?> testClass) {
    return JUnit4SpockMethod.SPOCK_SPEC_CLASS != null
        && JUnit4SpockMethod.SPOCK_SPEC_CLASS.isAssignableFrom(testClass);
  }

  private final Class<?> declaringClass;
  private final String description;

  public JUnit4SpockMethod(Description description) {
    super((Method) null);
    this.declaringClass = description.getTestClass();
    this.description = description.getDisplayName();
  }

  @Override
  public Class<?> getDeclaringClass() {
    return declaringClass;
  }

  @Override
  public String getName() {
    return description;
  }

  @Override
  public String toString() {
    return getName();
  }
}
