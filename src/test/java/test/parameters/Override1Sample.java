package test.parameters;

import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

@Test
public class Override1Sample {

  @Parameters({"InheritedFromSuite", "InheritedFromTest", "InheritedFromClass"})
  public void g(String suite, String test, String cls) {
    Assert.assertEquals(suite, "InheritedFromSuite");
    Assert.assertEquals(test, "InheritedFromTest");
    Assert.assertEquals(cls, "InheritedFromClass");
  }

  public void h() {
    System.out.println("h()");
  }

  @Parameters("a")
  public void f(String p) {
    Assert.assertEquals(p, "Correct");
  }
}
