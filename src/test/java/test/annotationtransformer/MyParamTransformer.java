package test.annotationtransformer;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MyParamTransformer implements IAnnotationTransformer {

  private boolean checkNull = true;

  @Override
  public void transform(ITestAnnotation annotation, Class testClass,
      Constructor testConstructor, Method testMethod) {
    if ((testClass == null && testConstructor != null && testMethod != null) ||
        (testClass != null && testConstructor == null && testMethod != null) ||
        (testClass != null && testConstructor != null && testMethod == null)) {
      checkNull = false;
    }
  }

  public boolean isCheckNull() {
    return checkNull;
  }
}
