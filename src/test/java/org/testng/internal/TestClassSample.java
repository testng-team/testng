package org.testng.internal;

import org.testng.annotations.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

public class TestClassSample {
    @Test
    @Occurs(times = 2)
    public void testMethod() {}

    @Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @Target({METHOD})
    public @interface Occurs {
        int times() default 1;
    }
}
