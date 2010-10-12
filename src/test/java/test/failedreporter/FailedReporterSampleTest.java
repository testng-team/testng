package test.failedreporter;

import org.testng.SkipException;
import org.testng.annotations.Test;

public class FailedReporterSampleTest {
  @Test
  public void f2() {
    throw new RuntimeException();
  }

  @Test
  public void f1() {
    throw new SkipException("Skipped");
  }

  @Test
  public void f3() {
  }

}
