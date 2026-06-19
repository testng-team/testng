package test.listeners.factory.issue3120;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(CustomFactory.class)
public class TestClassSample {

  @Test
  public void sampleTestMethod() {
    assertThat(CustomFactory.factoryInvoked)
        .withFailMessage("Factory should have been invoked")
        .isTrue();
    assertThat(CustomFactory.listenerInvoked)
        .withFailMessage("Listener should have been invoked")
        .isTrue();
  }
}
