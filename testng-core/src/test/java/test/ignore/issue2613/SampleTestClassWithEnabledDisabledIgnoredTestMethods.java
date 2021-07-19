package test.ignore.issue2613;

import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

public class SampleTestClassWithEnabledDisabledIgnoredTestMethods {

  @Test
  public void enabledTest() {}

  @Test(enabled = false)
  public void disabledTest() {}

  @Test
  @Ignore
  public void ignoredTest() {}
}
