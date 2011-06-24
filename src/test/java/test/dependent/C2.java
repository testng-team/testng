package test.dependent;


import org.testng.annotations.Test;

@Test(groups={"group2"}, dependsOnGroups={"group1"})
public class C2 {

   public void shouldBeSkipped() {
      // the expectation is that this test will be SKIPPED because
      // a test in group1 failed and we have a dependsOnGroups={"group1"}
   }

}
