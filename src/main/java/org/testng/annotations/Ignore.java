package org.testng.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

/**
 * Alternative of @Test(enable=false)
 *
 * Notice that @Ignore on a class will disable all test methods of the class.
 *
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({METHOD, TYPE})
public @interface Ignore {
    String value() default "";
}
