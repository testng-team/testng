package test.inheritance.testng234;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class PolymorphicFailureTest extends SimpleBaseTest {

  @Test
  public void superclassFailureShouldCauseFailure() {
    TestNG tng = create(ChildTest.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();

    assertTestResultsEqual(
        tla.getSkippedTests(), Arrays.asList("polymorphicMethod", "executePolymorphicMethod"));
    assertThat(0).isEqualTo(tla.getPassedTests().size());
    assertThat(0).isEqualTo(tla.getFailedTests().size());
  }
}
