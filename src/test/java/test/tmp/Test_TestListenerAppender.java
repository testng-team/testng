package test.tmp;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class Test_TestListenerAppender {

  @DataProvider(name = "test1")
  public Object[][] createData1() {
//      throw new RuntimeException("Intentionally thrown exception");
       return new Object[][] { { "Cedric", Integer.valueOf(36) }, {"Anne", Integer.valueOf(37) }, };
  }

  @Test(dataProvider = "test1")
  public void verifyData1(String n1, Integer n2) {
      System.out.println(n1 + " " + n2);
  }
}
