package org.testng.annotations;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.testng.IAnnotationTransformer;
import org.testng.ITestNGListener;

/**
 * This annotation lets you define listeners directly on a test class instead of doing so in your
 * testng.xml. Any class that implements the interface {@link org.testng.ITestNGListener} is
 * allowed, except {@link IAnnotationTransformer} which need to be defined in XML since they have to
 * be known before we even start looking for annotations.
 *
 * <p>Note that listeners specified this way are global to your entire suite, just like listeners
 * specified in testng.xml.
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({TYPE})
public @interface Listeners {
  Class<? extends ITestNGListener>[] value() default {};
}
