package test.retryAnalyzer.issue2148;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.FailurePolicy;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test(description = "GITHUB-2148")
  public void ensureTestsAreRetriedWhenConfigFailurePolicySetToContinue() {
    TestNG testng = create(ExceptionAfterMethodTestSample.class);
    testng.setConfigFailurePolicy(FailurePolicy.CONTINUE);
    testng.run();
    String[] expected =
        new String[] {
          "Before Method [testMethod] #1",
          "Test Method [testMethod] #1",
          "Before Method [testMethod] #1",
          "Before Method [testMethod] #2",
          "Test Method [testMethod] #2",
          "Before Method [testMethod] #2"
        };
    assertThat(ExceptionAfterMethodTestSample.logs).containsExactly(expected);
  }
}
