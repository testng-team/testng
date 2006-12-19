package org.testng.internal.mix;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * A marker annotation for injecting @Configuration methods.
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.FIELD)
public @interface TestConfiguration {
}
