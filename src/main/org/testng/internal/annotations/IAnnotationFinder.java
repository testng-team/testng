package org.testng.internal.annotations;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;


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
   * @param annotationName
   * @return The annotation on the class or null if none found.
   */
  public IAnnotation findAnnotation(Class cls, Class annotationClass);

  /**
   * @param m
   * @param annotationName
   * @return The annotation on the method.
   * If not found, return the annotation on the declaring class.
   * If not found, return null.
   */
  public IAnnotation findAnnotation(Method m, Class annotationClass);
  
  /**
   * @param m
   * @param annotationName
   * @return The annotation on the method.
   * If not found, return the annotation on the declaring class.
   * If not found, return null.
   */
  public IAnnotation findAnnotation(Constructor m, Class annotationClass);
  
  // No op for JDK15
  public void addSourceDirs(String[] dirs);
  
  // No-op for JDK5
  void initialize();
}
