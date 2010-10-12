package test.dataprovider;

import org.testng.annotations.Test;


/**
 * This class/interface
 */
public class InnexistentDataProvider {
  @Test(dataProvider="doesnotexist")
  public void testMethod(String s)
  {
    // doesn't matter
  }
}
