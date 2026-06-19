package test.listeners;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class ResultContextTest extends SimpleBaseTest {

  @Test
  public void testResultContext() {
    TestNG tng = create(ResultContextListenerSample.class);
    tng.run();
    assertThat(ResultContextListener.contextProvided)
        .withFailMessage("Test context was not provided to the listener")
        .isTrue();
  }
}
