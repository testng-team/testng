package test.parameters;

import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

public class OptionalParameterTest {

  @Parameters("optional")
  public OptionalParameterTest(@Optional String optional) {}

}
