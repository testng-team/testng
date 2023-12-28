package test.factory.github1631;

import java.lang.reflect.Method;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.IFactoryAnnotation;

public class DataProviderTransformer implements IAnnotationTransformer {

  private Class<?> dataProviderClass;

  @Override
  public void transform(final IFactoryAnnotation annotation, final Method testMethod) {
    dataProviderClass = annotation.getDataProviderClass();
  }

  public Class<?> getDataProviderClass() {
    return dataProviderClass;
  }
}
