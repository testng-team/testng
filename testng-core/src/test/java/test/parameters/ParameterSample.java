package test.parameters;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class ParameterSample {

  @Parameters({"first-name"})
  @BeforeMethod
  public void beforeTest(String firstName) {
    Assert.assertEquals(firstName, "Cedric");
  }

  @Parameters({"first-name"})
  @Test(groups = {"singleString"})
  public void testSingleString(String firstName) {
    Assert.assertEquals(firstName, "Cedric");
  }

  @Parameters({"this parameter doesn't exist"})
  @Test
  public void testNonExistentParameter(@Optional String foo) {}
}
