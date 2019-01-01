package org.testng.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marker interface which when used on a type will ensure that TestNG does not clone the object but
 * instead uses it as is when TestNG resorts to dependency injection.
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SkipCloning {

}
