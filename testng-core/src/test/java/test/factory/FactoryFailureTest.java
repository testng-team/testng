package test.factory;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class FactoryFailureTest extends SimpleBaseTest {

  @Test
  public void factoryThrowingShouldNotRunTests() {
    TestNG tng = create(FactoryFailureSample.class);
    Throwable actual = new Throwable();
    try {
      tng.run();
    } catch (Exception ex) {
      actual = ex;
    }
    assertThat(actual.getCause()).hasCauseInstanceOf(NullPointerException.class);
  }

  @Test(description = "GITHUB-1953")
  public void factoryProducesNoInstancesTest() {
    TestNG tng = create(FactoryFailureNoInstancesSample.class);
    String actualErr = "";
    String expected =
        String.format(
            "The Factory method %s should have produced at-least one instance.",
            FactoryFailureNoInstancesSample.METHOD_NAME);
    try {
      tng.run();
    } catch (Exception ex) {
      actualErr = ex.getMessage();
    }
    assertThat(actualErr).contains(expected);
  }
}
