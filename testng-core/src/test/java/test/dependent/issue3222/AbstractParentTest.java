package test.dependent.issue3222;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.Test;

public abstract class AbstractParentTest {

  public static class ChildTest extends AbstractParentTest {

    @Test
    public void test1() {
      fail();
    }

    @Test(dependsOnMethods = {"test1"})
    public void test2() {}
  }
}
