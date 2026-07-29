package test.commandline.issue341;

import org.testng.Reporter;
import org.testng.annotations.Test;

public class TestSampleB {
  @Test
  public void aa() {
    Reporter.log(Long.toString(Thread.currentThread().getId()));
  }
}
