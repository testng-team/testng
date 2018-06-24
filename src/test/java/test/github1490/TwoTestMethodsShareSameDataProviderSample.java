package test.github1490;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.internal.ConstructorOrMethod;

public class TwoTestMethodsShareSameDataProviderSample {
  @TestInfo(name = "glutton")
  @Test(dataProvider = "cookie-master")
  public void testHowMuchPoAte(String cookieName, int count) {
    Assert.assertEquals("oreo", cookieName);
    Assert.assertTrue(count > 100);
  }

  @TestInfo(name = "nibbler")
  @Test(dataProvider = "cookie-master")
  public void testHowMuchMasterShifuAte(String cookieName, int count) {
    Assert.assertEquals("marie-gold", cookieName);
    Assert.assertTrue(count < 100);
  }

  @DataProvider(name = "cookie-master")
  public Object[][] getCookies(ConstructorOrMethod method) {
    return DataProviderHouse.cookies(method);
  }
}
