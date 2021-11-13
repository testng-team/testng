package test.dependent;

import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DepBugVerifyTest {

  @Test
  public void verify() {
    List<String> log = DepBugSampleTest.getLog();
    String[] expected = new String[] {"setup", "send", "get", "destroy"};
    for (int i = 0; i < log.size(); i++) {
      Assert.assertEquals(expected[i], log.get(i));
    }
  }
}
