package test;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import test.junit.SetNameTest;
import test.sample.JUnitSample1;
import test.sample.JUnitSample2;


/**
 * This class
 *
 * @author Cedric Beust, May 5, 2004
 *
 */
public class JUnitTest1 extends BaseTest {
  @BeforeMethod(dependsOnGroups = { "initTest"} )
  public void initJUnitFlag() {
    getTest().setJUnit(true);
  }

  @Test
  public void methodsThatStartWithTest() {
    addClass("test.sample.JUnitSample1");
    assert getTest().isJUnit();

    run();
    String[] passed = {
        JUnitSample1.EXPECTED1, JUnitSample1.EXPECTED2
    };
    String[] failed = {
    };

    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }

  @Test
  public void methodsWithSetup() {
    addClass("test.sample.JUnitSample2");
    run();
    String[] passed = {
      "testSample2ThatSetUpWasRun",
    };
    String[] failed = {
    };

    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }

  @Test
  public void testSuite() {
    addClass("test.sample.AllJUnitTests");
    run();
    String[] passed = {
        JUnitSample1.EXPECTED1, /*JUnitSample1.EXPECTED2,*/
        JUnitSample2.EXPECTED,
    };
    String[] failed = {
    };

    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }

  @Test
  public void testNewInstance() {
    addClass("test.sample.JUnitSample3");
    run();
    String[] passed = {
      "test1", "test2"
    };
    String[] failed = {
    };

    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }

  @Test
  public void setUpFailingShouldCauseMethodsToBeSkipped() {
    addClass("test.junit.SetUpExceptionSampleTest");
    run();
    String[] passed = {
    };
    String[] failed = {
      "testM1"/*, "testM1", "tearDown"*/
    };
    String[] skipped = {
      /*"testM1", "tearDown"*/
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
    verifyTests("Failed", failed, getFailedTests());
  }

  @Test
  public void setNameShouldBeInvoked() {
    addClass("test.junit.SetNameTest");
    SetNameTest.m_ctorCount = 0;
    run();
    String[] passed = {
      "testFoo", "testBar",
    };
    String[] failed = {
    };
    String[] skipped = {
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
    verifyTests("Failed", failed, getFailedTests());

    Assert.assertEquals(SetNameTest.m_ctorCount, 2,
        "Expected 2 instances to be created, found " + SetNameTest.m_ctorCount);
  }

  public static void ppp(String s) {
    System.out.println("[JUnitTest1] " + s);
  }

  @Test
  public void testAbstract() {
    addClass("test.sample.JUnitSample4");
    run();
    String[] passed = {
      "testXY", "testXY", "testXY"
    };
    String[] failed = {
    };

    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }
}
