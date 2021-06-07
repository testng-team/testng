package test.listeners.issue2220;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(Listener1.class)
public class TestClass01 {

  @BeforeClass
  public void setup() {}

  @Test(priority = 1)
  public void test01() {}

  @Test(priority = 2)
  public void test02() {}

  @Test(priority = 3)
  public void test03() {}
}
