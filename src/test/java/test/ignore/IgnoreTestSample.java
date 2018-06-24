package test.ignore;

import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

public class IgnoreTestSample {

  @Test
  public void test() {}

  @Test
  @Ignore
  public void ignoredTest() {}
}
