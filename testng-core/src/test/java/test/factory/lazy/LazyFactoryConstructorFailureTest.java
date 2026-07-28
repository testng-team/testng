package test.factory.lazy;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class LazyFactoryConstructorFailureTest extends SimpleBaseTest {

  @Test
  public void lazyConstructorFailureIsLocalizedToThatInstance() {
    ThrowingLazyFactorySample.reset();
    TestNG tng = create(ThrowingLazyFactorySample.class);
    tng.setPreserveOrder(true);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();

    // The three well-behaved instances run their test; the failing instance (index 2) does not.
    assertThat(ThrowingLazyFactorySample.TESTS_RUN).containsExactlyInAnyOrder(0, 1, 3);

    // The failing instance is localized to a single skip carrying the constructor exception; the
    // run is not aborted for the others.
    assertThat(tla.getPassedTests()).hasSize(3);
    assertThat(tla.getSkippedTests()).hasSize(1);
    ITestResult skip = tla.getSkippedTests().get(0);
    assertThat(skip.getThrowable())
        .isInstanceOf(org.testng.TestNGException.class)
        .hasMessageContaining("Cannot instantiate");
  }
}
