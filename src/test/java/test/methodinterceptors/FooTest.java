package test.methodinterceptors;

import org.testng.annotations.Test;

public class FooTest {

  @Test(groups = "fast")
  public void zzzfast() {}

  @Test
  public void slow() {}

  @Test
  public void a() {}
}
