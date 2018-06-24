package test.github1490;

import org.testng.Assert;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class TwoFactoriesShareSameDataProviderSampleOne {
  private final String cookieName;
  private final int count;

  @TestInfo(name = "glutton")
  @Factory(dataProvider = "cookie-master", dataProviderClass = DataProviderHouse.class)
  public TwoFactoriesShareSameDataProviderSampleOne(String cookieName, int count) {
    this.cookieName = cookieName;
    this.count = count;
  }

  @Test
  public void testHowMuchMasterShifuAte() {
    Assert.assertEquals("oreo", cookieName);
    Assert.assertTrue(count > 100);
  }
}
