package org.testng;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.testng.annotations.IConfigurationAnnotation;
import org.testng.annotations.IDataProviderAnnotation;
import org.testng.annotations.IFactoryAnnotation;
import org.testng.annotations.IListenersAnnotation;
import org.testng.annotations.ITestAnnotation;

public interface IAnnotationTransformer extends ITestNGListener {

  /**
   * This method will be invoked by TestNG to give you a chance to modify a TestNG annotation read
   * from your test classes. You can change the values you need by calling any of the setters on the
   * ITest interface.
   *
   * <p>Note that only one of the three parameters testClass, testConstructor and testMethod will be
   * non-null.
   *
   * @param annotation The annotation that was read from your test class.
   * @param testClass If the annotation was found on a class, this parameter represents this class
   *     (null otherwise).
   * @param testConstructor If the annotation was found on a constructor, this parameter represents
   *     this constructor (null otherwise).
   * @param testMethod If the annotation was found on a method, this parameter represents this
   *     method (null otherwise).
   */
  default void transform(
      ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
    // not implemented
  }

  /**
   * Transform an IConfiguration annotation.
   *
   * <p>Note that only one of the three parameters testClass, testConstructor and testMethod will be
   * non-null.
   *
   * @param annotation The annotation that was read from your test class.
   * @param testClass If the annotation was found on a class, this parameter represents this class
   *     (null otherwise).
   * @param testConstructor If the annotation was found on a constructor, this parameter represents
   *     this constructor (null otherwise).
   * @param testMethod If the annotation was found on a method, this parameter represents this
   *     method (null otherwise).
   */
  default void transform(
      IConfigurationAnnotation annotation,
      Class testClass,
      Constructor testConstructor,
      Method testMethod) {
    // not implemented
  }

  /**
   * Transform an IDataProvider annotation.
   *
   * @param annotation The &#64;DataProvider annotation
   * @param method The method annotated with the IDataProvider annotation.
   */
  default void transform(IDataProviderAnnotation annotation, Method method) {
    // not implemented
  }

  /**
   * Transform an IFactory annotation.
   *
   * @param annotation The annotation factory
   * @param method The method annotated with the IFactory annotation.
   */
  default void transform(IFactoryAnnotation annotation, Method method) {
    // not implemented
  }

  default void transform(IListenersAnnotation annotation, Class<?> testClass) {
    // not implemented
  }
}
