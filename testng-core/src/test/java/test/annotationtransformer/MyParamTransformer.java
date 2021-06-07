package test.annotationtransformer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

public class MyParamTransformer implements IAnnotationTransformer {

  private boolean success = true;

  @Override
  public void transform(
      ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
    if (!onlyOneNonNull(testClass, testConstructor, testMethod)) {
      success = false;
    }
  }

  public static boolean onlyOneNonNull(
      Class testClass, Constructor testConstructor, Method testMethod) {
    return ((testClass != null && testConstructor == null && testMethod == null)
        || (testClass == null && testConstructor != null && testMethod == null)
        || (testClass == null && testConstructor == null && testMethod != null));
  }

  public boolean isSuccess() {
    return success;
  }
}
