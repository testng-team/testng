package test.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

public class BeforeMethodWithGroupFiltersTest extends SimpleBaseTest {

  @Test
  public void beforeMethodWithBeforeGroupsShouldOnlyRunBeforeGroupMethods() {
    InvokedMethodNameListener nameListener = run(BeforeMethodWithGroupFiltersSampleTest.class);
    assertThat(nameListener.getInvokedMethodNames())
        .containsExactly(BeforeMethodWithGroupFiltersSampleTest.EXPECTED_INVOCATIONS);
  }
}
