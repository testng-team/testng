package test.configuration;

import org.testng.annotations.Test;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class AfterMethodWithGroupFiltersTest extends SimpleBaseTest {

  @Test
  public void beforeMethodWithBeforeGroupsShouldOnlyRunBeforeGroupMethods() {
    InvokedMethodNameListener nameListener = run(AfterMethodWithGroupFiltersSampleTest.class);
    assertThat(nameListener.getInvokedMethodNames())
        .containsExactly(AfterMethodWithGroupFiltersSampleTest.EXPECTED_INVOCATIONS);
  }
}
