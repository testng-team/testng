package test.dependsongroup;

import org.testng.annotations.Test;

@Test(groups = { "first" }, dependsOnGroups = { "zero" })
public class FirstSampleTest {

  @Test
  public void firstA() {
//    System.out.println("firstA");
  }

  @Test
  public void firstB() {
//    System.out.println("firstB");
  }

}

