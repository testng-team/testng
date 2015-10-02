package org.testng;

import org.testng.annotations.ITestAnnotation;
import org.testng.annotations.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class TestClassDisabler implements IAnnotationTransformer {

  @Override
  public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor,
                        Method testMethod) {
    if (testMethod != null) {
      Test test = testMethod.getDeclaringClass().getAnnotation(Test.class);
      if (test != null && !test.enabled()) {
        annotation.setEnabled(false);
      }
    }
  }
}
