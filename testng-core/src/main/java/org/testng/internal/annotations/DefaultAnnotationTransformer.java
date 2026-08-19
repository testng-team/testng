package org.testng.internal.annotations;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.jspecify.annotations.Nullable;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

public class DefaultAnnotationTransformer extends IgnoreListener implements IAnnotationTransformer {

  @Override
  public void transform(
      ITestAnnotation annotation,
      @Nullable Class testClass,
      @Nullable Constructor testConstructor,
      @Nullable Method testMethod) {
    super.transform(annotation, testClass, testConstructor, testMethod);
  }

  @Override
  public void transform(
      ITestAnnotation annotation,
      @Nullable Class testClass,
      @Nullable Constructor cons,
      @Nullable Method tm,
      @Nullable Class<?> clazz) {
    super.transform(annotation, testClass, cons, tm, clazz);
  }
}
