package test.annotationtransformer;

import org.testng.IAnnotationTransformer3;
import org.testng.annotations.IConfigurationAnnotation;
import org.testng.annotations.IDataProviderAnnotation;
import org.testng.annotations.IFactoryAnnotation;
import org.testng.annotations.IListenersAnnotation;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class MyListenerTransformer implements IAnnotationTransformer3 {

  @Override
  public void transform(IListenersAnnotation annotation, Class testClass) {
    annotation.setValue(new Class[]{MySuiteListener2.class});
  }

  @Override
  public void transform(IConfigurationAnnotation annotation, Class testClass,
                        Constructor testConstructor, Method testMethod) {}

  @Override
  public void transform(IDataProviderAnnotation annotation, Method method) {}

  @Override
  public void transform(IFactoryAnnotation annotation, Method method) {}

  @Override
  public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor,
                        Method testMethod) {}
}
