package test.dependsongroup;

import org.testng.annotations.Test;

@Test(groups = { "zero" })
public class ZeroSampleTest {

  @Test
  public void zeroA() {
//    System.out.println("zeroA");
  }

  @Test
  public void zeroB() {
//    System.out.println("zeroB");
  }

}

