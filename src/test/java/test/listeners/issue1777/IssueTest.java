package test.listeners.issue1777;

import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class IssueTest extends SimpleBaseTest {

  public static final String GITHUB_1777 = "GITHUB-1777";

  @Test(description = GITHUB_1777)
  public void testOnStartInvokedForSkippedTests() {
    TestNG testNG = create(TestClassSample.class);
    testNG.alwaysRunListeners(true);
    testNG.setConfigFailurePolicy(XmlSuite.FailurePolicy.CONTINUE);
    MyListener listener = new MyListener();
    testNG.addListener(listener);
    testNG.run();
    List<String> expectedTestMessages =
        Arrays.asList(
            "testStart_test_method: test1",
            "before_test_method: test1", "after_test_method: test1",
            "testSkipped_test_method: test1",
            "testStart_test_method: test2",
            "before_test_method: test2", "after_test_method: test2",
            "testSuccess_test_method: test2");
    assertThat(listener.tstMsgs).containsExactlyElementsOf(expectedTestMessages);
    List<String> expectedConfigMessages =
        Arrays.asList(
            "before_configuration_method: beforeMethod[test1]",
                "after_configuration_method: beforeMethod[test1]",
            "before_configuration_method: afterMethod[test1]",
                "after_configuration_method: afterMethod[test1]",
            "before_configuration_method: beforeMethod[test2]",
                "after_configuration_method: beforeMethod[test2]",
            "before_configuration_method: afterMethod[test2]",
                "after_configuration_method: afterMethod[test2]");
    assertThat(listener.cfgMsgs).containsExactlyElementsOf(expectedConfigMessages);
  }

}
