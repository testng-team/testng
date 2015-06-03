package test.interleavedorder;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import test.BaseTest;
import testhelper.OutputDirectoryPatch;

import java.util.ArrayList;
import java.util.List;


public class InterleavedInvocationTest extends BaseTest {
  public static List<String> LOG = new ArrayList<>();

  @BeforeTest
  public void beforeTest() {
    LOG = new ArrayList<>();
  }

  private void verifyInvocation(int number, List<String> log, int index) {
    Assert.assertEquals(log.get(index), "beforeTestChild" + number + "Class");
    Assert.assertTrue(("test1".equals(log.get(index + 1)) && "test2".equals(LOG.get(index + 2)))
        || ("test2".equals(LOG.get(index + 1)) && "test1".equals(LOG.get(index + 2))),
        "test methods were not invoked correctly");
    Assert.assertEquals(log.get(index + 3), "afterTestChild" + number + "Class");
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

    Assert.assertEquals(LOG.size(), 8, LOG.toString());
    int number1 = "beforeTestChild1Class".equals(LOG.get(0)) ? 1 : 2;
    int number2 = number1 == 1 ? 2 : 1;
    verifyInvocation(number1, LOG, 0);
    verifyInvocation(number2, LOG, 4);
  }

  public static void ppp(String s) {
    System.out.println("[InterleavedTest] " + s);
  }
}
