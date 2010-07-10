package test.annotationtransformer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.testng.annotations.ITestAnnotation;
import org.testng.IAnnotationTransformer;

public class MyTimeOutTransformer implements IAnnotationTransformer {

  public void transform(ITestAnnotation annotation, Class testClass,
      Constructor testConstructor, Method testMethod) 
  {
    annotation.setTimeOut(5000); // 5 seconds
  }
  

}
