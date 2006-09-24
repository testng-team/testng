package org.testng.internal.annotations;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public interface IAnnotationTransformer {

  public void transform(ITest annotation, Class testClass,
      Constructor testConstructor, Method testMethod);
  
}
