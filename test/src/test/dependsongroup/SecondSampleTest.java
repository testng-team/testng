package test.dependsongroup;

import org.testng.annotations.Test;

@Test(groups = { "second" }, dependsOnGroups = { "zero" })
public class SecondSampleTest {

  @Test
  public void secondA() {
//    System.out.println("secondA");
  }

  @Test
  public void secondB() {
//    System.out.println("secondB");
  }

}