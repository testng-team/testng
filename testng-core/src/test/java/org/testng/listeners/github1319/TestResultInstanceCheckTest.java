package org.testng.listeners.github1319;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.listeners.samples.github1319.TestSample;
import test.SimpleBaseTest;

public class TestResultInstanceCheckTest extends SimpleBaseTest {
  @Test(description = "GITHUB-1319")
  public void testInstances() {
    TestNG tng = create(TestSample.class);
    tng.run();
    int hashCode = TestSample.hashcode;
    assertThat(TestSample.Listener.maps.size())
        .withFailMessage("Validating the number of instances")
        .isEqualTo(6);
    for (Object object : TestSample.Listener.maps.values()) {
      assertThat(object).isNotNull();
      assertThat(object.hashCode()).isEqualTo(hashCode);
    }
  }
}
