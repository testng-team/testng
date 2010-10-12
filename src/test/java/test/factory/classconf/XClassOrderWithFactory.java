package test.factory.classconf;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;


/**
 * This class/interface
 */
public class XClassOrderWithFactory {
  public static final String EXPECTED_LOG= "BTABTABTA";
  public static final StringBuffer LOG= new StringBuffer();

  @Factory
  public Object[] createInstances() throws Exception {
      return new Object[] {
              new XClassOrderTest(), new XClassOrderTest(), new XClassOrderTest()
          };
  }

  public static class XClassOrderTest {
    @BeforeClass
    public void beforeClass() {
      LOG.append("B");
    }

    public @Test void test() {
      LOG.append("T");
    }

    public @AfterClass void afterClass() {
      LOG.append("A");
    }
  }
}
