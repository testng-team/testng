package test.dependent;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;
import org.testng.Assert;

public class DepBugVerifyTest {

  @Test
  public void verify() {
    List<String> log = DepBugSampleTest.getLog();
    
    Assert.assertEquals(log, Arrays.asList(new String[] {
      "setup", "send", "get", "list", "destroy"
    }));
  }
}
