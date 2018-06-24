package test.github1490;

import org.testng.Assert;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class TwoFactoriesShareSameDataProviderSampleTwo {
  private final String cookieName;
  private final int count;

  @TestInfo(name = "nibbler")
  @Factory(dataProvider = "cookie-master", dataProviderClass = DataProviderHouse.class)
  public TwoFactoriesShareSameDataProviderSampleTwo(String cookieName, int count) {
    this.cookieName = cookieName;
    this.count = count;
  }

  @Test
  public void testHowMuchMasterShifuAte() {
    Assert.assertEquals("marie-gold", cookieName);
    Assert.assertTrue(count < 100);
  }
}
