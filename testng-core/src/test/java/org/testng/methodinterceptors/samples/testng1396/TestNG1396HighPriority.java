package test.testng1396;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({METHOD, TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TestNG1396HighPriority {}
