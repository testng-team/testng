package org.testng.annotations;

import static java.lang.annotation.ElementType.TYPE;

import com.google.inject.Module;

import org.testng.IModuleFactory;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation specifies what Guice modules should be used to instantiate
 * this test class.
 * 
 * @author Cedric Beust <cedric@beust.com>
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(TYPE)
public @interface Guice {
  /**
   * @return the list of modules to query when trying to create an instance of this test class.
   */
  Class<? extends Module>[] modules() default {};

  Class<? extends IModuleFactory> moduleFactory() default IModuleFactory.class;
}
