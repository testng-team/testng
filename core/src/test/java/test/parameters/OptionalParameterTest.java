package test.parameters;

import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class OptionalParameterTest {

  @Parameters("optional")
  public OptionalParameterTest(@Optional String optional) {}

  @Test(description = "GITHUB-564")
  public void testWithParameterOnlyOptionalAnnotation(@Optional String unUsedParameter) {
  }

}
