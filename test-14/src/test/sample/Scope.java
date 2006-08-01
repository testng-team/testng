package test.sample;



/**
 * This class tests paramete scopes.
 *
 * @author cbeust
 */
public class Scope {
  /**
   * @testng.test groups="outer-group" parameters="parameter"
   */
  public void outerDeprecated(String s) {
    assert "out".equals(s)
        : "Expected out got " + s;
  }

  /**
   * @testng.test groups="inner-group" parameters="parameter"
   */
  public void innerDeprecated(String s) {
    assert "in".equals(s)
        : "Expected in got " + s;
  }
  
  /**
   * @testng.parameters value = "parameter"
   * @testng.test groups="outer-group"
   */
  public void outer(String s) {
    assert "out".equals(s)
        : "Expected out got " + s;
  }

  /**
   * @testng.parameters value = "parameter"
   * @testng.test groups="inner-group" 
   */
  public void inner(String s) {
    assert "in".equals(s)
        : "Expected in got " + s;
  }

}
