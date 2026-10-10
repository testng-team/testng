package org.testng.listeners.issue2328;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.listeners.samples.issue2328.ConfigListener;
import org.testng.listeners.samples.issue2328.SampleWithConfiguration;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  private static final String TEST_METHOD =
      "org.testng.listeners.samples.issue2328.SampleWithConfiguration.testMethod";

  @Test(description = "GITHUB-2328")
  public void testIfTestMethodInfoPassedToConfigListeners() {
    TestNG testng = create(SampleWithConfiguration.class);
    ConfigListener listener = new ConfigListener();
    testng.addListener(listener);
    testng.run();
    assertThat(listener.getDataFor("before").getQualifiedName()).isEqualTo(TEST_METHOD);
    assertThat(listener.getDataFor("failed").getQualifiedName()).isEqualTo(TEST_METHOD);
    assertThat(listener.getDataFor("skip").getQualifiedName()).isEqualTo(TEST_METHOD);
    assertThat(listener.getDataFor("success").getQualifiedName()).isEqualTo(TEST_METHOD);
  }
}
