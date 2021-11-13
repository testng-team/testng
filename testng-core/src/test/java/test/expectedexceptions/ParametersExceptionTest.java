package test.expectedexceptions;

import java.io.IOException;
import java.lang.reflect.Method;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

/** This class/interface */
public class ParametersExceptionTest {
  @Test(dataProvider = "A")
  public void testA(Exception err) {
    System.out.println("testA");
  }

  @DataProvider(name = "A")
  protected Object[][] dp() {
    return new Object[][] {{new IOException(), new SAXException()}};
  }

  @AfterMethod
  protected void verify(Method method) {
    Assert.assertTrue(false, "forced failure");
  }
}
