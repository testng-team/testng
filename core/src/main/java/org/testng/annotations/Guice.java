package org.testng.annotations;

import static java.lang.annotation.ElementType.TYPE;

import com.google.inject.Module;

import org.testng.IModuleFactory;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation specifies what Guice modules should be used to instantiate this test class.
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(TYPE)
@Documented
public @interface Guice {
  /** @return the list of modules to query when trying to create an instance of this test class. */
  Class<? extends Module>[] modules() default {};

  /**
   * @deprecated use {@code @Inject ITestContext} and {@code @Inject @CurrentTestClass Class<?>} instead
   */
  @Deprecated
  Class<? extends IModuleFactory> moduleFactory() default IModuleFactory.class;
}
