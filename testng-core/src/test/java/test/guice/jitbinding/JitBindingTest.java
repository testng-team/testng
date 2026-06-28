package test.guice.jitbinding;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class JitBindingTest extends SimpleBaseTest {

  @Test
  public void testConflictingJitBinding() {
    TestNG tng = create(FirstModuleSample.class, SecondModuleSample.class);
    TestListenerAdapter adapter = new TestListenerAdapter();
    tng.addListener(adapter);
    tng.run();
    assertThat(adapter.getFailedTests().isEmpty()).isTrue();
    assertThat(adapter.getSkippedTests().isEmpty()).isTrue();
    assertThat(adapter.getPassedTests().size()).isEqualTo(2);
  }
}
