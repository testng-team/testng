package test.configuration.issue2432;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class Test1 extends Base {
  @BeforeSuite(groups = "prepareConfig")
  public void prepareConfigForTest1() {}

  @Test(groups = "test")
  public void test1() {}
}
