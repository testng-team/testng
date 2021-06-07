package test.annotationtransformer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Set;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.IConfigurationAnnotation;
import org.testng.annotations.IDataProviderAnnotation;
import org.testng.annotations.IFactoryAnnotation;
import org.testng.annotations.IListenersAnnotation;
import org.testng.annotations.ITestAnnotation;
import org.testng.collections.Sets;

public class FactoryTransformer implements IAnnotationTransformer {
  private final Set<String> logs = Sets.newLinkedHashSet();

  public Set<String> getLogs() {
    return logs;
  }

  @Override
  public void transform(
      IConfigurationAnnotation annotation,
      Class testClass,
      Constructor testConstructor,
      Method testMethod) {
    logs.add("transform_config");
  }

  @Override
  public void transform(IDataProviderAnnotation annotation, Method testMethod) {
    logs.add("transform_data_provider");
  }

  @Override
  public void transform(
      ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
    logs.add("transform_test");
  }

  @Override
  public void transform(IFactoryAnnotation annotation, Method testMethod) {
    annotation.setDataProvider("dataProvider");
    logs.add("transform_factory");
  }

  @Override
  public void transform(IListenersAnnotation annotation, Class testClass) {
    logs.add("transform_listener");
  }
}
