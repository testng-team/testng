package test.tmp;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ParentTest {

  @BeforeMethod
  public void btm1() {}

  @AfterMethod
  public void atm1() {}

  @Test
  public void t1() {}
}
