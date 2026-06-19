package test.github1490;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class TwoTestMethodsShareSameDataProviderSampleTwo {
  @TestInfo(name = "glutton")
  @Test(dataProvider = "cookie-master", dataProviderClass = DataProviderHouse.class)
  public void testHowMuchPoAte(String cookieName, int count) {
    assertThat("oreo").isEqualTo(cookieName);
    assertThat(count).isGreaterThan(100);
  }

  @TestInfo(name = "nibbler")
  @Test(dataProvider = "cookie-master", dataProviderClass = DataProviderHouse.class)
  public void testHowMuchMasterShifuAte(String cookieName, int count) {
    assertThat("marie-gold").isEqualTo(cookieName);
    assertThat(count).isLessThan(100);
  }
}
