package test.annotationtransformer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.testng.annotations.ITestAnnotation;
import org.testng.IAnnotationTransformer;

public class MyTransformer implements IAnnotationTransformer {

  public void transform(ITestAnnotation annotation, Class testClass, 
      Constructor testConstructor, Method testMethod) 
  {
    annotation.setTimeOut(10000);
    if (testMethod != null) {
      String name = testMethod.getName();
      if ("three".equals(name)) {
        annotation.setInvocationCount(3);
      }
      else if ("four".equals(name)) {
        annotation.setInvocationCount(4);
      }
      else if ("five".equals(name)) {
        annotation.setInvocationCount(5);
      }
    }
  }

  private void ppp(String string) {
    System.out.println("[MyTransformer] " + string);
  }
  


}
