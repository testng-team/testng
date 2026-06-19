package test.listeners.github1319;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class TestResultInstanceCheckTest extends SimpleBaseTest {
  @Test
  public void testInstances() {
    TestNG tng = create(TestSample.class);
    tng.run();
    int hashCode = TestSample.hashcode;
    assertThat(TestSample.Listener.maps)
        .withFailMessage("Validating the number of instances")
        .hasSize(6);
    for (Object object : TestSample.Listener.maps.values()) {
      assertThat(object).isNotNull();
      assertThat(object.hashCode()).isEqualTo(hashCode);
    }
  }
}
