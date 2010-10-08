package test.annotationtransformer;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class MyTimeOutTransformer implements IAnnotationTransformer {

  @Override
  public void transform(ITestAnnotation annotation, Class testClass,
      Constructor testConstructor, Method testMethod)
  {
    annotation.setTimeOut(5000); // 5 seconds
  }


}
