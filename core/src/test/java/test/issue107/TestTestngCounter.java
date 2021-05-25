package test.issue107;

import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

@Listeners(MySuiteListener.class)
public class TestTestngCounter {

  public static final String PARAMETER_NAME = "key1";
  public static final String EXPECTED_VALUE = "zzz";

  @Parameters({PARAMETER_NAME})
  @Test
  public void testParameter(String key) {
    Assert.assertEquals(key, EXPECTED_VALUE);
  }

  @Parameters({PARAMETER_NAME})
  @Test
  public void testParameterAsOptional(@Optional("Unknown") String key) {
    Assert.assertEquals(key, EXPECTED_VALUE);
  }
}
