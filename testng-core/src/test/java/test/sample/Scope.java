package test.sample;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/** This class tests paramete scopes. */
public class Scope {

  @Parameters({"parameter"})
  @Test(groups = {"outer-group"})
  public void outerDeprecated(String s) {
    assert "out".equals(s) : "Expected out got " + s;
  }

  @Parameters({"parameter"})
  @Test(groups = {"inner-group"})
  public void innerDeprecated(String s) {
    assert "in".equals(s) : "Expected in got " + s;
  }
}
