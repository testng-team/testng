package test.inject;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class InjectBeforeMethodTest {
  private int m_index = 0;
  private static final Object[][] DATA = { 
      new Object[] { "a" },
      new Object[] { "b" },
  };
  
  @BeforeMethod
  public void before(Object[] parameters) {
      Assert.assertEquals(DATA[m_index], parameters);
      m_index++;
  }
  
  @DataProvider
  public Object[][] dp() {
    return DATA;
  }

  @Test(dataProvider = "dp")
  public void f(String a) {
  }

}
