package test.listeners.issue2381;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.IConfigurationAnnotation;
import org.testng.annotations.IDataProviderAnnotation;
import org.testng.annotations.IFactoryAnnotation;
import org.testng.annotations.IListenersAnnotation;
import org.testng.annotations.ITestAnnotation;

public class SampleTransformer implements IAnnotationTransformer {

  private static final List<String> logs = new ArrayList<>();

  public static List<String> getLogs() {
    return Collections.unmodifiableList(logs);
  }

  public static void clearLogs() {
    logs.clear();
  }

  @Override
  public boolean isEnabled() {
    return false;
  }

  @Override
  public void transform(IListenersAnnotation annotation, Class<?> testClass) {
    logs.add("transform_listener");
  }

  @Override
  public void transform(IDataProviderAnnotation annotation, Method method) {
    logs.add("transform_data_provider");
  }

  @Override
  public void transform(IFactoryAnnotation annotation, Method method) {
    logs.add("transform_factory");
  }

  @Override
  public void transform(
      IConfigurationAnnotation annotation,
      Class testClass,
      Constructor testConstructor,
      Method testMethod) {
    logs.add("transform_configuration");
  }

  @Override
  public void transform(
      ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
    logs.add("transform_test");
  }
}
