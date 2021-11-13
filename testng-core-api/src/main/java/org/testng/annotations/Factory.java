package org.testng.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a method as a factory that returns objects that will be used by TestNG as Test classes. The
 * method must return Object[].
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({java.lang.annotation.ElementType.METHOD, java.lang.annotation.ElementType.CONSTRUCTOR})
@Documented
public @interface Factory {
  /**
   * The name of the data provider for this test method.
   *
   * @return the data provider name (default none)
   * @see org.testng.annotations.DataProvider
   */
  String dataProvider() default "";

  /**
   * The class where to look for the data provider. If not specified, the dataprovider will be
   * looked on the class of the current test method or one of its super classes. If this attribute
   * is specified, the data provider method needs to be static on the specified class.
   *
   * @return the data provider class (default none)
   */
  Class<?> dataProviderClass() default Object.class;

  /**
   * Whether this factory is enabled.
   *
   * @return the value (default true)
   */
  boolean enabled() default true;

  int[] indices() default {};
}
