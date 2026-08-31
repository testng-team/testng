package test.inject.parameterresolver;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** Marks a test method parameter that {@link SampleParameterResolver} owns. */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface FromResolver {}
