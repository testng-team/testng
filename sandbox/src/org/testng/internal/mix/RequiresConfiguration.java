package org.testng.internal.mix;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * This class/interface 
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.TYPE)
public @interface RequiresConfiguration {
  public Class[] value();
}
