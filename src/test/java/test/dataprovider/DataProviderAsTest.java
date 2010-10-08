package test.dataprovider;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


/**
 * Test that if a class @Test is used, the @DataProvider
 * method won't be considered as a test.
 */
@Test
public class DataProviderAsTest {

  public void f() {
  }

  @DataProvider
  public Object[][] dataProvider() {
    throw new RuntimeException();
  }

}
