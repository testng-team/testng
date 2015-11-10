package org.testng.annotations;

import static java.lang.annotation.ElementType.PARAMETER;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({PARAMETER})
public @interface ParameterOverride {
	 public String parameterRender() default "";
}
