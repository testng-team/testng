package test.testnames;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import org.testng.ITestNGListener;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class TestNamesTest extends SimpleBaseTest {

  @Test
  public void checkWithoutChildSuites() {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG tng = create();
    tng.setTestNames(Collections.singletonList("testGroup2"));
    tng.setTestSuites(Collections.singletonList(getPathToResource("testnames/upstream-suite.xml")));
    tng.addListener((ITestNGListener) tla);
    tng.run();
    assertThat(tla.getFailedTests()).isEmpty();
    assertThat(tla.getPassedTests()).hasSize(1);
    assertThat(tla.getPassedTests().get(0).getMethod().getMethodName())
        .isEqualTo("sampleOutputTest2");
  }
}
