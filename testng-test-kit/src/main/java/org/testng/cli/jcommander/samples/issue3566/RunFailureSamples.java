package org.testng.cli.jcommander.samples.issue3566;

import org.testng.SkipException;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class RunFailureSamples {
  public static class SimpleTest {
    @Test
    public void test() {}
  }

  public static class FailingSetupOnly {
    @BeforeSuite
    public void setUp() {
      throw new IllegalStateException("setup failed");
    }
  }

  public static class AllSkipped {
    @Test
    public void test() {
      throw new SkipException("skip");
    }
  }
}
