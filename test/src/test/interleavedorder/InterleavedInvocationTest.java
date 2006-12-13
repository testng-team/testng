package test.interleavedorder;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import test.BaseTest;
import testhelper.OutputDirectoryPatch;


public class InterleavedInvocationTest extends BaseTest {
  public static List<String> LOG = new ArrayList<String>();

  @BeforeTest
  public void beforeTest() {
    LOG = new ArrayList<String>();
  }

  @Test
  public void invocationOrder() {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG testng = new TestNG();
    testng.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    testng.setTestClasses(new Class[] { TestChild1.class, TestChild2.class });
    testng.addListener(tla);
    testng.setVerbose(0);
    testng.run();
    
    final String log= LOG.toString();
    final String clsName= TestChild1.class.getName();


    Assert.assertEquals(LOG.size(), 8, "invocations");
    // @Configuration ordering
    Assert.assertEquals(LOG.get(0), "beforeTestChild1Class");
    Assert.assertTrue(("test1".equals(LOG.get(1)) && "test2".equals(LOG.get(2)))
        || ("test2".equals(LOG.get(1)) && "test1".equals(LOG.get(2))), "test methods were not invoked correctly");
    Assert.assertEquals(LOG.get(3), "afterTestChild1Class");
    Assert.assertEquals(LOG.get(4), "beforeTestChild2Class");
    Assert.assertTrue(("test1".equals(LOG.get(5)) && "test2".equals(LOG.get(6)))
        || ("test2".equals(LOG.get(5)) && "test1".equals(LOG.get(6))), "test methods were not invoked correctly");
    Assert.assertEquals(LOG.get(7), "afterTestChild2Class");
  }

  public static void ppp(String s) {
    System.out.println("[InterleavedTest] " + s);
  }
}
