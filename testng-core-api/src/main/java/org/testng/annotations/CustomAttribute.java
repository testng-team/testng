package org.testng.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** Represents a means to add in custom attributes to @{@link Test} annotated tests. */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({METHOD, TYPE})
public @interface CustomAttribute {

  /** @return - The name for the custom attribute */
  String name();

  /** @return - The custom attribute values as an array. */
  String[] values() default {};
}
