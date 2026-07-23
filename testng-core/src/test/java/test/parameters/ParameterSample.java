package test.parameters;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class ParameterSample {

  @Parameters({"first-name"})
  @BeforeMethod
  public void beforeTest(String firstName) {
    assertThat(firstName).isEqualTo("Cedric");
  }

  @Parameters({"first-name"})
  @Test(groups = {"singleString"})
  public void testSingleString(String firstName) {
    assertThat(firstName).isEqualTo("Cedric");
  }

  @Parameters({"this parameter doesn't exist"})
  @Test
  public void testNonExistentParameter(@Optional String foo) {}
}
