package test.configuration;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

/**
 * Verify that a base class with a BeforeGroups method only gets invoked
 * once, no matter how many subclasses it has
 *
 * Created on Jan 23, 2007
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class BaseGroupsTest {

    @Test
    public void verifySingleInvocation() {
      TestNG tng = new TestNG();
      tng.setVerbose(0);
      tng.setTestClasses(new Class[] {
          BaseGroupsASampleTest.class,
          BaseGroupsBSampleTest.class,
      });
      TestListenerAdapter tla = new TestListenerAdapter();
      tng.addListener(tla);

      tng.run();

      Assert.assertEquals(Base.m_count, 1);
    }

    private static void ppp(String s) {
      System.out.println("[BaseGroupsTest] " + s);
    }
}
