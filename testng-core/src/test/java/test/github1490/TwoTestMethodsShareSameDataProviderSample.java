package test.github1490;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.internal.ConstructorOrMethod;

public class TwoTestMethodsShareSameDataProviderSample {
  @TestInfo(name = "glutton")
  @Test(dataProvider = "cookie-master")
  public void testHowMuchPoAte(String cookieName, int count) {
    assertThat("oreo").isEqualTo(cookieName);
    assertThat(count).isGreaterThan(100);
  }

  @TestInfo(name = "nibbler")
  @Test(dataProvider = "cookie-master")
  public void testHowMuchMasterShifuAte(String cookieName, int count) {
    assertThat("marie-gold").isEqualTo(cookieName);
    assertThat(count).isLessThan(100);
  }

  @DataProvider(name = "cookie-master")
  public Object[][] getCookies(ConstructorOrMethod method) {
    return DataProviderHouse.cookies(method);
  }
}
