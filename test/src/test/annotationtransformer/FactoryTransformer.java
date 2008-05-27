package test.annotationtransformer;

import org.testng.annotations.IConfiguration;
import org.testng.annotations.IDataProvider;
import org.testng.annotations.IFactory;
import org.testng.annotations.ITest;
import org.testng.internal.annotations.IAnnotationTransformer2;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class FactoryTransformer implements IAnnotationTransformer2 {

  public void transform(IConfiguration annotation, Class testClass,
      Constructor testConstructor, Method testMethod) 
  {
  }

  public void transform(IDataProvider annotation, Method testMethod) {
  }

  public void transform(ITest annotation, Class testClass,
      Constructor testConstructor, Method testMethod)
  {
  }

  public void transform(IFactory annotation, Method testMethod) {
    annotation.setDataProvider("dataProvider");
  }
}
