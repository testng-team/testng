package org.testng;

import org.testng.annotations.IConfiguration;
import org.testng.annotations.IDataProvider;
import org.testng.annotations.IFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Use this interface instead of IAnnotationTransformer if you want to modify any TestNG
 * annotation besides @Test.
 */
public interface IAnnotationTransformer2 extends IAnnotationTransformer {
  /**
   * Transform an IConfiguration annotation. 
   *
   * Note that only one of the three parameters testClass,
   * testConstructor and testMethod will be non-null.
   * 
   * @param annotation The annotation that was read from your
   * test class.
   * @param testClass If the annotation was found on a class, this
   * parameter represents this class (null otherwise).
   * @param testConstructor If the annotation was found on a constructor,
   * this parameter represents this constructor (null otherwise).
   * @param testMethod If the annotation was found on a method,
   * this parameter represents this method (null otherwise).
   */
  public void transform(IConfiguration annotation, Class testClass,
      Constructor testConstructor, Method testMethod);
  
  /**
   * Transform an IDataProvider annotation. 
   *
   * @param method The method annotated with the IDataProvider annotation.
   */
  public void transform(IDataProvider annotation, Method method);

  /**
   * Transform an IFactory annotation. 
   *
   * @param method The method annotated with the IFactory annotation.
   */
  public void transform(IFactory annotation, Method method);
}
