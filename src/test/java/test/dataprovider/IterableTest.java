package test.dataprovider;

import org.testng.Assert;

import java.util.Iterator;

public class IterableTest {
  private boolean m_ok1 = false;
  private boolean m_ok2 = false;

  public static final String FN2 = "Anne Marie";
  public static final Integer LN2 = 37;
  public static final String FN1 = "Cedric";
  public static final Integer LN1 = 36;

  public static final Object[][] DATA = new Object[][] {
    new Object[] { FN1, LN1 },
    new Object[] { FN2, LN2 },
  };

  /**
   * @testng.data-provider name="test1"
   */
  public Iterator createData() {
    return new MyIterator(DATA);
  }

  /**
   * @testng.test dataProvider="test1"
   */
  public void verifyNames(String firstName, Integer age) {
    if (firstName.equals(FN1) && age.equals(LN1)) {
      m_ok1 = true;
      Assert.assertEquals(MyIterator.getCount(), 1);
    }
    if (firstName.equals(FN2) && age.equals(LN2)) {
      m_ok2 = true;
      Assert.assertEquals(MyIterator.getCount(), 2);
    }
  }

  /**
   * @testng.test dependsOnMethods = "verifyNames"
   */
  public void verifyCount() {
    Assert.assertTrue(m_ok1 && m_ok2);
  }

  private static void ppp(String s) {
    System.out.println("[IterableTest] " + s);
  }
}


