package test.factory;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class DisabledFactoryTest extends SimpleBaseTest {

  @Test
  public void disabledFactoryShouldNotRun() {
    TestNG tng = create(DisabledFactory.class);

    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);

    tng.run();

    assertThat(tla.getPassedTests()).isEmpty();
    assertThat(tla.getSkippedTests()).isEmpty();
    assertThat(tla.getFailedTests()).isEmpty();
  }
}
