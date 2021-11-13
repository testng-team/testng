package test.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

public class AfterMethodWithGroupFiltersTest extends SimpleBaseTest {

  @Test
  public void beforeMethodWithBeforeGroupsShouldOnlyRunBeforeGroupMethods() {
    InvokedMethodNameListener nameListener = run(AfterMethodWithGroupFiltersSampleTest.class);
    assertThat(nameListener.getInvokedMethodNames())
        .containsExactly(AfterMethodWithGroupFiltersSampleTest.EXPECTED_INVOCATIONS);
  }
}
