package test.sample;

import org.testng.annotations.Test;

/**
 * This class tests paramete scopes.
 *
 * @author cbeust
 */
public class Scope {

  @Test(groups = { "outer-group" }, parameters = { "parameter" })
  public void outerDeprecated(String s) {
    assert "out".equals(s)
      : "Expected out got " + s;
  }

  @Test(groups = { "inner-group" }, parameters = { "parameter" })
  public void innerDeprecated(String s) {
    assert "in".equals(s)
      : "Expected in got " + s;
  }
}
