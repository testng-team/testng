package org.testng.annotations;

import static java.lang.annotation.ElementType.TYPE;

import com.google.inject.Module;

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
  Class<? extends Module>[] modules() default {};
}
