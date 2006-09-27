package test.dataprovider;

import org.testng.Assert;

public class StaticDataProviderSampleTest {

  /**
   * @testng.test dataProvider="static" dataProviderClass="test.dataprovider.StaticProvider"
   */
  public void verifyStatic(String s) {
    System.out.println("PE AICI");
    Assert.assertEquals(s, "Cedric");
  }
}
