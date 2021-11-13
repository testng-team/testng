package org.testng.annotations;

/** Encapsulate the @Parameters / @testng.parameters annotation */
public interface IParametersAnnotation extends IAnnotation {
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
  String[] getValue();
}
