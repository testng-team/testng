package test.groups.issue2152;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test(description = "GITHUB-2152")
  public void ensureConfigurationsDontInheritGroupsWhenRunningWithoutGroupFiltering() {
    TestNG testng = create(TestClassSample.class);
    testng.run();
    String[] expected = new String[] {"setup", "test1", "teardown"};
    assertThat(TestClassSample.logs).containsExactly(expected);
  }
}
