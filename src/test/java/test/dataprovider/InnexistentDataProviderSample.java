package test.dataprovider;

import org.testng.annotations.Test;

public class InnexistentDataProviderSample {

  @Test(dataProvider = "doesnotexist")
  public void testMethod(String s) {}
}
