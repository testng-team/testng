package org.testng.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;

/**
 * If this annotation is used on a parameter of a data provider, that parameter is the proxy to the
 * class which uses data provider. Call methods of that proxy to provide parameters for a test.
 *
 * <p>This annotation is ignored everywhere else.
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({PARAMETER})
public @interface ParameterCollector {}
