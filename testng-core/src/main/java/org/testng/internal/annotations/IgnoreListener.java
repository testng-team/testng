package org.testng.internal.annotations;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.testng.annotations.ITestAnnotation;
import org.testng.annotations.Ignore;
import org.testng.internal.reflect.ReflectionHelper;
import org.testng.util.Strings;

public class IgnoreListener implements IAnnotationTransformer {

  @Override
  public void transform(
      ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
    transform(annotation, testClass, testConstructor, testMethod, null);
  }

  @Override
  public void transform(
      ITestAnnotation annotation,
      Class testClass,
      Constructor tc,
      Method testMethod,
      Class<?> clazz) {
    if (!annotation.getEnabled()) {
      return;
    }
    Class<?> typedTestClass = testClass;
    if (testMethod != null) {
      ignoreTest(annotation, testMethod.getAnnotation(Ignore.class));
      typedTestClass = testMethod.getDeclaringClass();
    }
    ignoreTestAtClass(typedTestClass, annotation);
    ignoreTestAtClass(clazz, annotation);
  }

  private static void ignoreTestAtClass(Class<?> clazz, ITestAnnotation annotation) {
    if (clazz != null) {
      ignoreTest(annotation, ReflectionHelper.findAnnotation(clazz, Ignore.class));
      Package testPackage = clazz.getPackage();
      if (testPackage != null) {
        ignoreTest(annotation, findAnnotation(testPackage));
      }
    }
  }

  private static void ignoreTest(ITestAnnotation annotation, Ignore ignore) {
    if (ignore == null) {
      return;
    }
    annotation.setEnabled(false);
    updateDescription(annotation, ignore);
  }

  private static void updateDescription(ITestAnnotation annotation, Ignore ignore) {
    if (ignore.value().isEmpty()) {
      return;
    }
    String ignoredDescription;
    if (annotation.getDescription() == null || annotation.getDescription().isEmpty()) {
      ignoredDescription = ignore.value();
    } else {
      ignoredDescription = ignore.value() + ": " + annotation.getDescription();
    }
    annotation.setDescription(ignoredDescription);
  }

  @SuppressWarnings("deprecation")
  private static Ignore findAnnotation(Package testPackage) {
    if (testPackage == null) {
      return null;
    }
    Ignore result = testPackage.getAnnotation(Ignore.class);
    if (result != null) {
      return result;
    }
    String[] parts = testPackage.getName().split("\\.");
    String[] parentParts = Arrays.copyOf(parts, parts.length - 1);
    String parentPackageName = Strings.join(".", parentParts);
    if (parentPackageName.isEmpty()) {
      return null;
    }
    return findAnnotation(Package.getPackage(parentPackageName));
  }
}
