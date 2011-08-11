package org.testng.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a method as a factory that returns objects that will be used by TestNG
 * as Test classes.  The method must return Object[].
 *
 * @author <a href="mailto:cedric&#64;beust.com">Cedric Beust</a>
 */

@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({java.lang.annotation.ElementType.METHOD, java.lang.annotation.ElementType.CONSTRUCTOR})
public @interface Factory {
  /**
   * The list of variables used to fill the parameters of this method.
   * These variables must be defined in the property file.
   *
   * @deprecated Use @Parameters
   */
  @Deprecated
  public String[] parameters() default {};

  /**
   * The name of the data provider for this test method.
   * @see org.testng.annotations.DataProvider
   */
  public String dataProvider() default "";

  /**
   * The class where to look for the data provider.  If not
   * specified, the dataprovider will be looked on the class
   * of the current test method or one of its super classes.
   * If this attribute is specified, the data provider method
   * needs to be static on the specified class.
   */
  public Class<?> dataProviderClass() default Object.class;

  /**
   * Whether this factory is enabled.
   */
  public boolean enabled() default true;
}
