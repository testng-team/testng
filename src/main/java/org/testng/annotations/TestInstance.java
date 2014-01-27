package org.testng.annotations;

import static java.lang.annotation.ElementType.PARAMETER;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * If this annotation is used on a parameter of a data provider, that parameter is the instance
 * of the test method which is going to be fed by this data provider.
 *
 * This annotation is ignored everywhere else.
 *
 * @author cbeust
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({PARAMETER})
public @interface TestInstance {
}
