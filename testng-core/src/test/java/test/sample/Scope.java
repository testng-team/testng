package test.sample;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/** This class tests paramete scopes. */
public class Scope {

  @Parameters({"parameter"})
  @Test(groups = {"outer-group"})
  public void outerDeprecated(String s) {
    assertEquals(s, "out", "Expected out got " + s);
  }

  @Parameters({"parameter"})
  @Test(groups = {"inner-group"})
  public void innerDeprecated(String s) {
    assertEquals(s, "in", "Expected in got " + s);
  }
}
