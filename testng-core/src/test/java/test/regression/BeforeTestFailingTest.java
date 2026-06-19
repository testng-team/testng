package test.regression;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITestNGListener;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class BeforeTestFailingTest extends SimpleBaseTest {

  @Test
  public void beforeTestFailingShouldCauseSkips() {
    TestNG tng = create(MyTestngTest2.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener((ITestNGListener) tla);
    tng.run();

    assertThat(tla.getSkippedTests()).hasSize(1);
    assertThat(tla.getPassedTests()).isEmpty();
  }
}
