package org.testng.internal.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This class can be replaceable by java.lang.reflect.Parameter if using jdk 1.8.
 *
 * @author <a href="mailto:nitin.matrix@gmail.com">Nitin Verma</a>
 */
public class Parameter implements AnnotatedElement {

  private final int index;
  private final Class<?> type;
  /** preserving the order of annotations. */
  private final Annotation[] declaredAnnotations;
  /** For efficient back to back searches. */
  private final Map<Class<? extends Annotation>, Annotation> declaredAnnotationsMap;

  public Parameter(final int index, final Class<?> type, final Annotation[] declaredAnnotations) {
    this.index = index;
    this.type = type;
    this.declaredAnnotations = declaredAnnotations;
    this.declaredAnnotationsMap = declaredAnnotations(declaredAnnotations);
  }

  private static Map<Class<? extends Annotation>, Annotation> declaredAnnotations(
      final Annotation[] ann) {
    final Map<Class<? extends Annotation>, Annotation> map = new HashMap<>();
    if (ann != null) {
      for (Annotation anAnn : ann) {
        map.put(anAnn.annotationType(), anAnn);
      }
    }
    return map;
  }

  @Override
  public String toString() {
    return "Parameter{"
        + "index="
        + index
        + ", type="
        + (type != null ? type.getName() : null)
        + ", declaredAnnotations="
        + Arrays.toString(declaredAnnotations)
        + '}';
  }

  /**
   * Returns a {@code Class} object that identifies the declared type for the parameter represented
   * by this {@code Parameter} object.
   *
   * @return a {@code Class} object identifying the declared type of the parameter represented by
   *     this object
   */
  public Class<?> getType() {
    return type;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
    return getAnnotation(annotationClass) != null;
  }

  /** {@inheritDoc} */
  @Override
  public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
    return annotationClass.cast(declaredAnnotationsMap.get(annotationClass));
  }

  /** {@inheritDoc} */
  @Override
  public Annotation[] getAnnotations() {
    // For parameter all annotations are declared.
    return getDeclaredAnnotations();
  }

  /** {@inheritDoc} */
  @Override
  public Annotation[] getDeclaredAnnotations() {
    return declaredAnnotations;
  }
}
