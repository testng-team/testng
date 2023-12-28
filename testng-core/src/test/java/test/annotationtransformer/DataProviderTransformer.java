package test.annotationtransformer;

import java.lang.reflect.Method;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.IDataProviderAnnotation;

public class DataProviderTransformer implements IAnnotationTransformer {

  @Override
  public void transform(IDataProviderAnnotation annotation, Method testMethod) {
    annotation.setName("dataProvider");
  }
}
