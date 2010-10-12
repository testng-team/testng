package test.dataprovider;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

public class MethodTest {

  @DataProvider(name = "dp1")
  public Object[][] createData(Method m) {
    Assert.assertEquals("test1", m.getName());
    Assert.assertEquals("test.dataprovider.MethodTest", m.getDeclaringClass().getName());
    return new Object[][] {
      new Object[] { "Cedric" },
      new Object[] { "Alois" },
    };
  }

  @Test(dataProvider = "dp1")
  public void test1(String s) {
    Assert.assertTrue("Cedric".equals(s) || "Alois".equals(s));
  }

  private int m_test2 = 0;
  private int m_test3 = 0;

  @DataProvider(name = "dp2")
  public Object[][] createData2(Method m) {
    if ("test2".equals(m.getName())) {
      m_test2++;
    } else if ("test3".equals(m.getName())) {
      m_test3++;
    } else {
      throw new RuntimeException("Received method " + m + ", expected test2 or test3");
    }
    Assert.assertEquals("test.dataprovider.MethodTest", m.getDeclaringClass().getName());
    return new Object[][] {
      new Object[] { "Cedric" },
    };
  }

  @Test(dataProvider = "dp2")
  public void test2(String s) {
    Assert.assertTrue("Cedric".equals(s));
  }

  @Test(dataProvider = "dp2")
  public void test3(String s) {
    Assert.assertTrue("Cedric".equals(s));
  }

  @Test(dependsOnMethods = {"test2", "test3"})
  public void multipleTestMethods() {
    Assert.assertEquals(1, m_test2);
    Assert.assertEquals(1, m_test3);
  }

}
