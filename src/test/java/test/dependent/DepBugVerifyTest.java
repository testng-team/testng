package test.dependent;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class DepBugVerifyTest {

  @Test
  public void verify() {
    List<String> log = DepBugSampleTest.getLog();
    String[] expected = new String[] {
      "setup", "send", "get", "destroy"
    };
    for (int i = 0; i < log.size(); i++) {
      Assert.assertEquals(expected[i], log.get(i));
    }
  }
}
