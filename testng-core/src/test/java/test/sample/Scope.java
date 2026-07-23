package test.sample;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/** This class tests paramete scopes. */
public class Scope {

  @Parameters({"parameter"})
  @Test(groups = {"outer-group"})
  public void outerDeprecated(String s) {
    assertThat(s).withFailMessage("Expected out got " + s).isEqualTo("out");
  }

  @Parameters({"parameter"})
  @Test(groups = {"inner-group"})
  public void innerDeprecated(String s) {
    assertThat(s).withFailMessage("Expected in got " + s).isEqualTo("in");
  }
}
