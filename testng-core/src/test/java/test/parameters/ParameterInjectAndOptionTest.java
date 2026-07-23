package test.parameters;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITestNGListener;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class ParameterInjectAndOptionTest extends SimpleBaseTest {

  @Test
  public void test() {
    TestNG tng = create(ParameterInjectAndOptionSample.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener((ITestNGListener) tla);
    tng.run();
    assertThat(tla.getPassedTests().size()).isEqualTo(1);
  }
}
