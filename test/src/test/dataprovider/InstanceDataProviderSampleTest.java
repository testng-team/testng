package test.dataprovider;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class InstanceDataProviderSampleTest {
  @DataProvider
  public Object[][] dp() {
    p("DATA PROVIDER");
    return new Object[][] {
        new Object[] { new Integer(hashCode()) },
    };
  }
  
  @BeforeClass
  public void beforeTest() {
    p("BEFORE");
  }

  @Test(dataProvider = "dp")
  public void f(Integer n) {
    p("  PARAM:" + n);
    Assert.assertEquals(n, new Integer(hashCode()));
  }
  
  @AfterClass
  public void afterTest() {
    p("AFTER");
  }

  private void p(String s) {
    if (false) {
      System.out.println(hashCode() + " " + s);
    }
  }

}
