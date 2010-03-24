package org.testng.annotations;

import static java.lang.annotation.ElementType.TYPE;

import org.testng.ITestNGListener;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({TYPE})
public @interface Listeners {
  Class<? extends ITestNGListener>[] value() default {};
}
