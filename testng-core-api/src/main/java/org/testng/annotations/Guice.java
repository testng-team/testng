package org.testng.annotations;

import static java.lang.annotation.ElementType.TYPE;

import com.google.inject.Module;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.testng.IModuleFactory;

/** This annotation specifies what Guice modules should be used to instantiate this test class. */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(TYPE)
@Documented
public @interface Guice {
  /** @return the list of modules to query when trying to create an instance of this test class. */
  Class<? extends Module>[] modules() default {};

  Class<? extends IModuleFactory> moduleFactory() default IModuleFactory.class;
}
