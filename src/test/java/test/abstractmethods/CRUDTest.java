package test.abstractmethods;

import org.testng.annotations.Test;

abstract public class CRUDTest {

  @Test
  public abstract void create();

  @Test(dependsOnMethods = "create")
  public abstract void read();
}
