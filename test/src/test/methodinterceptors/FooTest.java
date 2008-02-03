package test.methodinterceptors;

import org.testng.annotations.Test;

public class FooTest {
  
  @Test(groups = "fast")
  public void fast() {}
  
  @Test
  public void slow() {}

}
