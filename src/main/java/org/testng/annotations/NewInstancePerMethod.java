package org.testng.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Marking a class with this tells TestNG to use a new instance of the class per test method.  You can also do this
 * globally with a system property (testng.newInstancePerMethod) or environment variable
 * "TESTNG_NEW_INSTANCE_PER_METHOD".
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(TYPE)
public @interface NewInstancePerMethod {
}
