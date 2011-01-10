package org.testng.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a method as the object factory to use for creating all test instances.
 * The test classes can only contain one method marked with this annotation,
 * and the method must return an instance of {@link org.testng.ITestObjectFactory}.
 *
 * @author Hani Suleiman
 * @since 5.6
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.METHOD)
public @interface ObjectFactory
{
}
