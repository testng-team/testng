package org.testng.annotations;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;

/**
 * Marks a method as a factory that returns objects that will be used by TestNG
 * as Test classes.  The method must return Object[].
 * 
 * @author <a href="mailto:cedric&#64;beust.com">Cedric Beust</a>
 */

@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.METHOD)
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
}
