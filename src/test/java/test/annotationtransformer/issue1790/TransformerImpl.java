package test.annotationtransformer.issue1790;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;
import org.testng.collections.Sets;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Set;

public class TransformerImpl implements IAnnotationTransformer {
  private Set<Class<?>> classes = Sets.newHashSet();
  private Set<Constructor<?>> constructors = Sets.newHashSet();
  private Set<Method> methods = Sets.newHashSet();

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
