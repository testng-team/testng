package test.dataprovider;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CreateDataTest {

  @DataProvider(name = "create-data")
  public Object[][] create() {
    return new Object[][] {{new MyObject()}};
  }

  @Test(dataProvider = "create-data")
  public void testMyTest(MyObject o) {
    // do something with o
  }
}

class MyObject {

  @Override
  public String toString() {
    return "MyObject{}";
  }
}
