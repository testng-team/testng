package org.testng.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;

/**
 * Alternative of @Test(enable=false)
 *
 * Notice that @Ignore on a class will disable all test methods of the class.
 *
 * Ignoring a class will ignore tests from child classes too.
 *
 * Ignoring a package will ignore all tests in the package and its sub-packages
 *
 * A package annotation is done in {@code package-info.java}. For example:
 * <pre>{@code
 * @Ignore
 * package test.ignorePackage;
 *
 * import org.testng.annotations.Ignore;
 * }</pre>
 *
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({METHOD, TYPE, PACKAGE})
public @interface Ignore {
    String value() default "";
}
