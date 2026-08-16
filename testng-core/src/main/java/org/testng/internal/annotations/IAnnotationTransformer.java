package org.testng.internal.annotations;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.jspecify.annotations.Nullable;
import org.testng.annotations.ITestAnnotation;

/** For backward compatibility. */
public interface IAnnotationTransformer extends org.testng.IAnnotationTransformer {

  default void transform(
      ITestAnnotation annotation,
      @Nullable Class testClass,
      @Nullable Constructor testConstructor,
      @Nullable Method testMethod,
      @Nullable Class<?> occurringClazz) {
    // not implemented
  }
}
