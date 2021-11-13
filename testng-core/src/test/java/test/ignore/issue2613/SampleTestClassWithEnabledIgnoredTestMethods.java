package test.ignore.issue2613;

import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

public class SampleTestClassWithEnabledIgnoredTestMethods {

  @Test
  public void enabledTest() {}

  @Test
  @Ignore
  public void ignoredTest() {}
}
