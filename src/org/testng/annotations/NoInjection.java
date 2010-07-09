package org.testng.annotations;

import static java.lang.annotation.ElementType.PARAMETER;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Turn off TestNG injection for a parameter.
 *
 * @author Cedric Beust, July 9th, 2010
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({PARAMETER})
public @interface NoInjection {
}

