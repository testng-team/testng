package test.tmp;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ChildTest extends ParentTest {

  @BeforeMethod
  public void btm2() {}

  @AfterMethod
  public void atm2() {}

  @Override
  @Test
  public void t1() {}
}
