package test.github1490;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TwoTestMethodsShareSameDataProviderSampleTwo {
  @TestInfo(name = "glutton")
  @Test(dataProvider = "cookie-master", dataProviderClass = DataProviderHouse.class)
  public void testHowMuchPoAte(String cookieName, int count) {
    Assert.assertEquals("oreo", cookieName);
    Assert.assertTrue(count > 100);
  }

  @TestInfo(name = "nibbler")
  @Test(dataProvider = "cookie-master", dataProviderClass = DataProviderHouse.class)
  public void testHowMuchMasterShifuAte(String cookieName, int count) {
    Assert.assertEquals("marie-gold", cookieName);
    Assert.assertTrue(count < 100);
  }
}
