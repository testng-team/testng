package org.testng.internal.annotations;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.testng.annotations.ITestAnnotation;

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
