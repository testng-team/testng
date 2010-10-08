package test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.sample.JUnitSample1;
import testhelper.OutputDirectoryPatch;

import java.util.List;

public class CommandLineTest {

  /**
   * Test -junit
   */
  @Test(groups = { "current" } )
  public void junitParsing() {
    String[] argv = {
      "-log", "0",
      "-d", OutputDirectoryPatch.getOutputDirectory(),
      "-junit",
      "-testclass", "test.sample.JUnitSample1"
    };
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG.privateMain(argv, tla);

    List<ITestResult> passed = tla.getPassedTests();
    assertEquals(passed.size(), 2);
    String test1 = passed.get(0).getMethod().getMethodName();
    String test2 = passed.get(1).getMethod().getMethodName();

    assertTrue(JUnitSample1.EXPECTED1.equals(test1) && JUnitSample1.EXPECTED2.equals(test2) ||
        JUnitSample1.EXPECTED1.equals(test2) && JUnitSample1.EXPECTED2.equals(test1));
    }

  /**
   * Test the absence of -junit
   */
  @Test(groups = { "current" } )
  public void junitParsing2() {
    String[] argv = {
      "-log", "0",
      "-d", OutputDirectoryPatch.getOutputDirectory(),
      "-testclass", "test.sample.JUnitSample1"
    };
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG.privateMain(argv, tla);

    List<ITestResult> passed = tla.getPassedTests();
    assertEquals(passed.size(), 0);
    }

  /**
   * Test the ability to override the default command line Suite name
   */
  @Test(groups = { "current" } )
  public void suiteNameOverride() {
    String suiteName="MySuiteName";
    String[] argv = {
      "-log", "0",
      "-d", OutputDirectoryPatch.getOutputDirectory(),
      "-junit",
      "-testclass", "test.sample.JUnitSample1",
      "-suitename", "\""+suiteName+"\""
    };
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG.privateMain(argv, tla);

    List<ITestContext> contexts = tla.getTestContexts();
    assertTrue(contexts.size()>0);
    for (ITestContext context:contexts) {
    	assertEquals(context.getSuite().getName(),suiteName);
    }
  }

  /**
   * Test the ability to override the default command line test name
   */
  @Test(groups = { "current" } )
  public void testNameOverride() {
    String testName="My Test Name";
    String[] argv = {
      "-log", "0",
      "-d", OutputDirectoryPatch.getOutputDirectory(),
      "-junit",
      "-testclass", "test.sample.JUnitSample1",
      "-testname", "\""+testName+"\""
    };
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG.privateMain(argv, tla);

    List<ITestContext> contexts = tla.getTestContexts();
    assertTrue(contexts.size()>0);
    for (ITestContext context:contexts) {
    	assertEquals(context.getName(),testName);
    }
  }

  @Test
  public void testUseDefaultListenersArgument() {
    TestNG.privateMain(new String[] {
        "-log", "0", "-usedefaultlisteners", "false", "-testclass", "test.sample.JUnitSample1"
    }, null);
  }

  @Test
  public void testMethodParameter() {
    String[] argv = {
      "-log", "0",
      "-d", OutputDirectoryPatch.getOutputDirectory(),
      "-methods", "test.sample.Sample2.method1,test.sample.Sample2.method3",
    };
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG.privateMain(argv, tla);

    List<ITestResult> passed = tla.getPassedTests();
    Assert.assertEquals(passed.size(), 2);
    Assert.assertTrue((passed.get(0).getName().equals("method1") &&
        passed.get(1).getName().equals("method3"))
        ||
        (passed.get(1).getName().equals("method1") &&
        passed.get(0).getName().equals("method3")));
  }

  private static void ppp(String s) {
    System.out.println("[CommandLineTest] " + s);
  }

}
