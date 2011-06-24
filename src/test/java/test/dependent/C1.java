package test.dependent;

import static org.testng.Assert.fail;

import org.testng.annotations.Test;

@Test(groups="group1")
public class C1 {

   public void failingTest() {
      fail("always fails");
   }

}
