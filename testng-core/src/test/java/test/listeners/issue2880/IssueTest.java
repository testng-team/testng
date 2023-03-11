package test.listeners.issue2880;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test(dataProvider = "dp")
  public void testListenerInvocation(
      Class<?> clazz, XmlSuite.FailurePolicy policy, List<String> expected) {
    TestNG tng = create(clazz);
    ListenerForIssue2880 listener = new ListenerForIssue2880();
    tng.setConfigFailurePolicy(policy);
    tng.addListener(listener);
    tng.run();
    assertThat(listener.getLogs()).containsExactlyElementsOf(expected);
  }

  @DataProvider(name = "dp")
  public Object[][] getData() {
    List<String> passList =
        Arrays.asList(
            "BeforeInvocation_beforeClass_STARTED",
            "AfterInvocation_beforeClass_SUCCESS",
            "BeforeInvocation_beforeMethod_STARTED",
            "AfterInvocation_beforeMethod_SUCCESS",
            "BeforeInvocation_testMethod_STARTED",
            "AfterInvocation_testMethod_SUCCESS",
            "BeforeInvocation_afterMethod_STARTED",
            "AfterInvocation_afterMethod_SUCCESS",
            "BeforeInvocation_afterClass_STARTED",
            "AfterInvocation_afterClass_SUCCESS");

    List<String> skipList =
        Arrays.asList(
            "BeforeInvocation_beforeClass_STARTED",
            "AfterInvocation_beforeClass_FAILURE",
            "BeforeInvocation_beforeMethod_SKIP",
            "AfterInvocation_beforeMethod_SKIP",
            "BeforeInvocation_testMethod_SKIP",
            "AfterInvocation_testMethod_SKIP",
            "BeforeInvocation_afterMethod_SKIP",
            "AfterInvocation_afterMethod_SKIP",
            "BeforeInvocation_afterClass_SKIP",
            "AfterInvocation_afterClass_SKIP");

    List<String> failList =
        Arrays.asList(
            "BeforeInvocation_beforeClass_STARTED",
            "AfterInvocation_beforeClass_FAILURE",
            "BeforeInvocation_beforeMethod_SKIP",
            "AfterInvocation_beforeMethod_SKIP",
            "BeforeInvocation_testMethod_SKIP",
            "AfterInvocation_testMethod_SKIP",
            "BeforeInvocation_afterMethod_SKIP",
            "AfterInvocation_afterMethod_SKIP",
            "BeforeInvocation_afterClass_SKIP",
            "AfterInvocation_afterClass_SKIP");

    return new Object[][] {
      {TestClassWithPassingConfigsSample.class, XmlSuite.FailurePolicy.SKIP, passList},
      {TestClassWithFailingConfigsSample.class, XmlSuite.FailurePolicy.SKIP, skipList},
      {TestClassWithPassingConfigsSample.class, XmlSuite.FailurePolicy.CONTINUE, passList},
      {TestClassWithFailingConfigsSample.class, XmlSuite.FailurePolicy.CONTINUE, failList}
    };
  }
}
