package test.testng37;

import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;


/**
 * This class/interface
 */
public class NullParameterTest {
  @Test
  @Parameters({"notnull", "nullvalue"})
  public void nullParameter(String notNull, int mustBeNull) {
    Assert.assertNotNull(notNull, "not null parameter expected");
    Assert.assertNull(mustBeNull, "null parameter expected");
  }
}
