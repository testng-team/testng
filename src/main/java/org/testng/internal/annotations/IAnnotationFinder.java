package org.testng.internal.annotations;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.testng.ITestNGMethod;
import org.testng.annotations.IAnnotation;


/**
 * This interface defines how annotations are found on classes, methods
 * and constructors.  It will be implemented by both JDK 1.4 and JDK 5
 * annotation finders.
 *
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public interface IAnnotationFinder {

  /**
   * @param cls
   * @param annotationClass
   * @return The annotation on the class or null if none found.
   */
  public <A extends IAnnotation> A findAnnotation(Class<?> cls, Class<A> annotationClass);

  /**
   * @param m
   * @param annotationClass
   * @return The annotation on the method.
   * If not found, return the annotation on the declaring class.
   * If not found, return null.
   */
  public <A extends IAnnotation> A findAnnotation(Method m, Class<A> annotationClass);
  <A extends IAnnotation> A findAnnotation(ITestNGMethod m, Class<A> annotationClass);

  /**
   * @param cons
   * @param annotationClass
   * @return The annotation on the method.
   * If not found, return the annotation on the declaring class.
   * If not found, return null.
   */
  public <A extends IAnnotation> A findAnnotation(Constructor<?> cons, Class<A> annotationClass);

  /**
   * @return true if the ith parameter of the given method has the annotation @TestInstance.
   */
  public boolean hasTestInstance(Method method, int i);

  /**
   * @return the @Optional values of this method's parameters (<code>null</code>
   * if the parameter isn't optional)
   */
  public String[] findOptionalValues(Method method);

  /**
   * @return the @Optional values of this method's parameters (<code>null</code>
   * if the parameter isn't optional)
   */
  public String[] findOptionalValues(Constructor ctor);
}
