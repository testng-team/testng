package test.annotationtransformer.issue1790;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

public class TransformerImpl implements IAnnotationTransformer {
  private Set<Class<?>> classes = new HashSet<>();
  private Set<Constructor<?>> constructors = new HashSet<>();
  private Set<Method> methods = new HashSet<>();

  @Override
  public void transform(
      ITestAnnotation iTestAnnotation, Class aClass, Constructor constructor, Method method) {
    if (aClass != null) {
      classes.add(aClass);
    }
    if (constructor != null) {
      constructors.add(constructor);
    }
    if (method != null) {
      methods.add(method);
    }
  }

  public Set<Class<?>> getClasses() {
    return classes;
  }

  public Set<Constructor<?>> getConstructors() {
    return constructors;
  }

  public Set<Method> getMethods() {
    return methods;
  }
}
