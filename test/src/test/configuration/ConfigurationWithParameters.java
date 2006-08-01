package test.configuration;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class ConfigurationWithParameters {
  private String m_param;

  @Parameters({ "param" })
  @BeforeTest
  public void testInit(String param) {
    m_param = param;
  }

  @Parameters({ "param" })
  @Test
  public void testMethod(String param) {
    Assert.assertEquals(m_param, param);
  }

}
