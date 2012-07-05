package test.skippingexceptions;

import org.testng.annotations.Test;

import test.BaseTest;

public class SkippingExceptionsTest extends BaseTest {

  @Test
  public void expectedExceptions() {
    runTest("test.skippingexceptions.SampleExceptions",
        new String[] { "shouldPass1", "shouldPass2" },
        new String[] { "shouldFail" },
        new String[] { "shouldSkip" });
  }

}


