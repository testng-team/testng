package test.abstractmethods;

import org.testng.annotations.Test;

public abstract class CRUDTest {

  @Test
  public abstract void create();

  @Test(dependsOnMethods = "create")
  public abstract void read();
}
