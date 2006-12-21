package test.annotationtransformer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.testng.internal.annotations.IAnnotationTransformer;
import org.testng.internal.annotations.ITest;

public class MyTimeOutTransformer implements IAnnotationTransformer {

  public void transform(ITest annotation, Class testClass,
      Constructor testConstructor, Method testMethod) 
  {
    annotation.setTimeOut(5000); // 5 seconds
  }
  

}
