package test.annotationtransformer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.IConfigurationAnnotation;

public class ConfigurationTransformer implements IAnnotationTransformer {

  @Override
  public void transform(
      IConfigurationAnnotation annotation,
      Class testClass,
      Constructor testConstructor,
      Method testMethod) {
    if (annotation.getBeforeTestMethod()) {
      annotation.setEnabled(false);
    }
  }
}
