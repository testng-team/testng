package org.testng.internal.annotations;

import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/** For backward compatibility. */
public interface IAnnotationTransformer extends org.testng.IAnnotationTransformer {

  default void transform(
      ITestAnnotation annotation,
      Class testClass,
      Constructor testConstructor,
      Method testMethod,
      Class<?> occurringClazz) {
    // not implemented
  }
}
