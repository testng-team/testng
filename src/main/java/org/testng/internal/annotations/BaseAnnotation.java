package org.testng.internal.annotations;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class BaseAnnotation {
  private Class testClass;
  private Method method;
  private Constructor constructor;

  public Constructor getConstructor() {
    return constructor;
  }
  public void setConstructor(Constructor constructor) {
    this.constructor = constructor;
  }
  public Method getMethod() {
    return method;
  }
  public void setMethod(Method method) {
    this.method = method;
  }
  public Class getTestClass() {
    return testClass;
  }
  public void setTestClass(Class testClass) {
    this.testClass = testClass;
  }

}
