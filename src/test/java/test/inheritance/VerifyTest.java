package test.inheritance;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.util.List;

public class VerifyTest {

  @Test(dependsOnGroups = {"before"})
  public void verify() {
    String[] expected = {
      "initApplication",
      "initDialog",
      "initDialog2",
      "test",
      "tearDownDialog2",
      "tearDownDialog",
      "tearDownApplication"
    };

    int i = 0;
    List<String> l = ZBase_0.getMethodList();
    AssertJUnit.assertEquals(expected.length, l.size());
    for (String s : l) {
      AssertJUnit.assertEquals(expected[i++], s);
    }
  }

  private static void ppp(String s) {
    System.out.println("[VerifyTest] " + s);
  }
}
