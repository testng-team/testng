package test.annotationtransformer;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MyTransformer implements IAnnotationTransformer {

  private final List<String> methodNames = new ArrayList<>();

  @Override
  public void transform(ITestAnnotation annotation, Class testClass,
      Constructor testConstructor, Method testMethod) {
    annotation.setTimeOut(10000);
    if (testMethod != null) {
      switch (testMethod.getName()) {
        case "three":
          annotation.setInvocationCount(3);
          break;
        case "four":
          annotation.setInvocationCount(4);
          break;
        case "five":
          annotation.setInvocationCount(5);
          break;
      }
      methodNames.add(testMethod.getName());
    }
  }

  public List<String> getMethodNames() {
    return methodNames;
  }
}
