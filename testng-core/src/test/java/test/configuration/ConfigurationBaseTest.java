package test.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITestNGListener;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import test.SimpleBaseTest;

public abstract class ConfigurationBaseTest extends SimpleBaseTest {
  protected void testConfiguration(Class<?>... classes) {
    TestNG tng = create(classes);

    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener((ITestNGListener) tla);
    tng.run();

    assertThat(tla.getConfigurationFailures().size())
        .withFailMessage(getFailedResultMessage(tla.getConfigurationFailures()))
        .isEqualTo(0);
    assertThat(tla.getFailedTests().size())
        .withFailMessage(getFailedResultMessage(tla.getFailedTests()))
        .isEqualTo(0);
    assertThat(tla.getSkippedTests().size())
        .withFailMessage(getFailedResultMessage(tla.getSkippedTests()))
        .isEqualTo(0);
    assertThat(tla.getPassedTests().isEmpty()).withFailMessage("All tests should pass").isFalse();
  }
}
