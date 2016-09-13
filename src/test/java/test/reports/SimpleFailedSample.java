package test.reports;

import org.testng.annotations.Test;

public class SimpleFailedSample {

  @Test
  public void failed() {
    throw new RuntimeException("Failing intentionally");
  }
}
