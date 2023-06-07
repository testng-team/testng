package test.parameters.issue581;

import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class TestClassSample {

  @Parameters(value = "url")
  @Test
  public void test(@Optional String ignored) {}
}
