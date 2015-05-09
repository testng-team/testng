package test.expectedexceptions;

import org.testng.annotations.Test;

import test.BaseTest;

public class ExpectedExceptionsTest extends BaseTest {

  @Test
  public void expectedExceptionsDeprecatedSyntax() {
    runTest("test.expectedexceptions.SampleExceptions",
        new String[] { "shouldPass" },
        new String[] { "shouldFail1", "shouldFail2", "shouldFail3" },
        new String[] {});
  }

  @Test
  public void expectedExceptions() {
    runTest("test.expectedexceptions.SampleExceptions2",
        new String[] { "shouldPass", "shouldPass2", "shouldPass3", "shouldPass4" },
        new String[] { "shouldFail1", "shouldFail2", "shouldFail3", "shouldFail4" },
        new String[] {});
  }

}


