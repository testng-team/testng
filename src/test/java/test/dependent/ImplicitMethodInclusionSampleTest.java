package test.dependent;

import org.testng.annotations.Test;

public class ImplicitMethodInclusionSampleTest {
  @Test(groups = {"linux"})
  public void a() {
//    ppp("A");
  }

  @Test(groups = {"linux", "windows"} , dependsOnMethods={"a"})
  public void b() {
//    ppp("B");
  }

  private void ppp(String string) {
    System.out.println("[Implicit] " + string);
  }

}
