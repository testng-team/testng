package test.dataprovider;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Make sure that all before methods except beforeTestMethod are invoked
 * before DataProvider.
 *
 * Created on Jan 19, 2006
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class ConfigurationAndDataProvidersTest {
  private boolean m_beforeSuite = false;
  private boolean m_beforeTest = false;
  private boolean m_beforeClass = false;
  private boolean m_beforeTestMethod = false;

  @DataProvider(name = "test1")
  public Object[][] createData() {
    Assert.assertTrue(m_beforeSuite, "beforeSuite should have been invoked");
    Assert.assertTrue(m_beforeTest, "beforeTest should have been invoked");
    Assert.assertTrue(m_beforeClass, "beforeClass should have been invoked");
    Assert.assertFalse(m_beforeTestMethod, "beforeMethod should not have been invoked");
    return new Object[][] { new Object[] { "Test" } };
  }

  @Test(dataProvider = "test1")
  public void verifyNames(Object p) {
    // do nothing
  }


  @BeforeSuite
  public void setUpSuite () {
    m_beforeSuite  = true;
    ppp("BEFORE SUITE");
  }

  @BeforeTest
  public void setUpTest() {
    m_beforeTest = true;
    ppp("BEFORE TEST");
  }

  @BeforeClass
  public void setUpClass() {
    m_beforeClass = true;
    ppp("BEFORE TEST CLASS");
  }

  @BeforeMethod
  public void setUp() {
    m_beforeTestMethod = true;
    ppp("BEFORE TEST METHOD");
  }

  private static void ppp(String s) {
    if (false) {
      System.out.println("[ConfigurationAndDataProvidersTest] " + s);
    }
  }

}
