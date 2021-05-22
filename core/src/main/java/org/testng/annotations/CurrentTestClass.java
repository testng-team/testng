package org.testng.annotations;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation allows to inject the {@code Class} of the current test via Guice injector.
 * Note: Guice does not support polymorphic lookup, so the injected type should be either
 * {@code Class} or {@code Class<?>}.
 */
@Qualifier
@Documented
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface CurrentTestClass {
}
