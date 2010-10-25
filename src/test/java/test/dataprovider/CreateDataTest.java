package test.dataprovider;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CreateDataTest {

  /**
   * @testng.data-provider name = "create-data"
   */
  @DataProvider(name = "create-data")
  public Object[][] create() {
         return new Object[][] {
                 new Object[] { new MyObject() }
         };
  }

  /**
   * @testng.test data-provider = "create-data"
   */
  @Test(dataProvider = "create-data")
  public void testMyTest(MyObject o) {
   // do something with o
  }


}

class MyObject {}
