package org.testng.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Alternative of @Test(enabled=false)
 *
 * <p>Notice that @Ignore on a class will disable all test methods of the class.
 *
 * <p>Ignoring a class will ignore tests from child classes too.
 *
 * <p>Ignoring a package will ignore all tests in the package and its sub-packages
 *
 * <p>A package annotation is done in {@code package-info.java}. For example:
 *
 * <pre>
 * {@literal @}Ignore
 * package test.ignorePackage;
 *
 * import org.testng.annotations.Ignore;
 * </pre>
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({METHOD, TYPE, PACKAGE})
public @interface Ignore {
  String value() default "";
}
