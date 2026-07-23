package test.expectedexceptions;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.lang.reflect.Method;
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
    assertThat(false).withFailMessage("forced failure").isTrue();
  }
}
