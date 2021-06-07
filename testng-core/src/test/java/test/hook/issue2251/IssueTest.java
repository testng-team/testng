package test.hook.issue2251;

import org.assertj.core.api.Assertions;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test(description = "GITHUB-2251")
  public void runTest() {
    TestNG testng = create(SampleTestCase.class);
    TestListenerAdapter l = new TestListenerAdapter();
    testng.addListener(l);
    testng.run();
    Throwable t = l.getFailedTests().get(0).getThrowable();
    Assertions.assertThat(t).isInstanceOf(NullPointerException.class);
  }
}
