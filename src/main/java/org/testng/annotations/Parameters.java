package org.testng.annotations;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Describes how to pass parameters to a &#64;Test method.
 *
 * @author <a href="mailto:cedric&#64;beust.com">Cedric Beust</a>
 */
@Retention(RUNTIME)
@Target({METHOD, CONSTRUCTOR, TYPE })
public @interface Parameters {
  /**
   * The list of variables used to fill the parameters of this method.
   * These variables must be defined in your testng.xml file.
   * For example
   * <p>
   * <code>
   * &#064;Parameters({ "xmlPath" })<br>
   * &#064;Test<br>
   * public void verifyXmlFile(String path) { ... }<br>
   * </code>
   * <p>and in <tt>testng.xml</tt>:<p>
   * <code>
   * &lt;parameter name="xmlPath" value="account.xml" /&gt;<br>
   * </code>
   */
  public String[] value() default {};
}
