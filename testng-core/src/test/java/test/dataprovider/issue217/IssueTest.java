package test.dataprovider.issue217;

import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test(description = "GITHUB-217", expectedExceptions = TestNGException.class)
  public void ensureTestNGThrowsExceptionWhenAllTestsAreSkipped() {
    TestNG testng = create(SampleTestCase.class);
    testng.toggleFailureIfAllTestsWereSkipped(true);
    testng.run();
  }
}
