package test.listeners.github1029;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class Issue1029SampleTestClassWithOneMethod {
  @Test(invocationCount = 5, threadPoolSize = 10)
  public void a() {
    assertThat(true).isTrue();
  }
}
