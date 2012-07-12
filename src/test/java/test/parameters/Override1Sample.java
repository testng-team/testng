package test.parameters;

import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

@Test
public class Override1Sample {

  @Parameters("InheritedFromSuite")
  public void g(String p) {
    Assert.assertEquals(p, "InheritedFromSuite");
  }

  public void h() {
    System.out.println("h()");
  }

  @Parameters("a")
  public void f(String p) {
    Assert.assertEquals(p, "Correct");
  }
}
