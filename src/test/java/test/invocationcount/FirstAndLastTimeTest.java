package test.invocationcount;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Test various combination of @BeforeMethod(firstTimeOnly = true/false) and
 * @AfterMethod(lastTimeOnly = true/false) with invocation counts and data
 * providers.
 * @author cbeust@google.com
 *
 */
public class FirstAndLastTimeTest {
  @Test
  public void verifyDataProviderFalseFalse() {
    run(DataProviderFalseFalseTest.class, 3, 3);
  }

  @Test
  public void verifyDataProviderTrueFalse() {
    run(DataProviderTrueFalseTest.class, 1, 3);
  }

  @Test
  public void verifyDataProviderFalseTrue() {
    run(DataProviderFalseTrueTest.class, 3, 1);
  }

  @Test
  public void verifyDataProviderTrueTrue() {
    run(DataProviderTrueTrueTest.class, 1, 1);
  }
  
  @Test
  public void verifyInvocationCountFalseFalse() {
    run(InvocationCountFalseFalseTest.class, 3, 3);
  }

  @Test
  public void verifyInvocationCountTrueFalse() {
    run(InvocationCountTrueFalseTest.class, 1, 3);
  }

  @Test
  public void verifyInvocationCountFalseTrue() {
    run(InvocationCountFalseTrueTest.class, 3, 1);
  }

  @Test
  public void verifyInvocationCountTrueTrue() {
    run(InvocationCountTrueTrueTest.class, 1, 1);
  }
  
  private void run(Class cls, int expectedBefore, int expectedAfter) {
    TestNG tng = new TestNG();
    tng.setVerbose(0);
    tng.setTestClasses(new Class[] { cls });
    tng.run();
    
    try {
      Method before = cls.getMethod("getBeforeCount", new Class[0]);
      Integer beforeCount = (Integer) before.invoke(null, (Object[]) null);
      Assert.assertEquals(beforeCount.intValue(), expectedBefore);

      Method after = cls.getMethod("getAfterCount", new Class[0]);
      Integer afterCount = (Integer) after.invoke(null, (Object[]) null);
      Assert.assertEquals(afterCount.intValue(), expectedAfter);
    } catch (SecurityException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
  }
  
}
