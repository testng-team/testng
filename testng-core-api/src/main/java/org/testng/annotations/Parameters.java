package org.testng.annotations;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** Describes how to pass parameters to a &#64;Test method. */
@Retention(RUNTIME)
@Target({METHOD, CONSTRUCTOR, TYPE})
public @interface Parameters {
  public static final String NULL_VALUE = "null";

  /**
   * The list of variables used to fill the parameters of this method. These variables must be
   * defined in your testng.xml file. For example
   *
   * <p><code>
   * &#064;Parameters({ "xmlPath" })<br>
   * &#064;Test<br>
   * public void verifyXmlFile(String path) { ... }<br>
   * </code>
   *
   * <p>and in <code>testng.xml</code>:
   *
   * <p><code>
   * &lt;parameter name="xmlPath" value="account.xml" /&gt;<br>
   * </code>
   *
   * @return the value
   */
  String[] value() default {};
}
