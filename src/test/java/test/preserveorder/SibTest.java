package test.preserveorder;

import org.testng.annotations.Test;

@Test(singleThreaded = true)
public class SibTest {
  @Test
  public void sib1() {
  }

  @Test(dependsOnMethods = "sib1")
  public void sib2() {
    // System.out.println("sib2");
  }

}
