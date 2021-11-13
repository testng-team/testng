package org.testng.internal;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.testng.annotations.Test;

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
