package test.configuration.issue3239;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class BeforeClassOrderingSample extends ZBaseClass {

  @BeforeClass
  public void thisSetup() {}

  @Test
  public void test() {}
}
