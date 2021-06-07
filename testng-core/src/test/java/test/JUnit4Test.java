package test;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import test.junit4.*;

public class JUnit4Test extends BaseTest {

  @BeforeMethod(dependsOnGroups = {"initTest"})
  public void initJUnitFlag() {
    getTest().setJUnit(true);
  }

  @DataProvider
  public static Object[][] dp() {
    return new Object[][] {
      new Object[] {
        new Class<?>[] {JUnit4Sample2.class},
        JUnit4Sample2.EXPECTED,
        JUnit4Sample2.FAILED,
        JUnit4Sample2.SKIPPED
      },
      new Object[] {
        new Class<?>[] {JUnit4SampleSuite.class},
        JUnit4SampleSuite.EXPECTED,
        JUnit4SampleSuite.FAILED,
        JUnit4SampleSuite.SKIPPED
      },
      new Object[] {
        new Class<?>[] {JUnit4Child.class}, JUnit4Child.EXPECTED, new String[0], new String[0]
      },
      new Object[] {
        new Class<?>[] {InheritedTest.class, JUnit4Sample1.class},
        new String[] {"t1", "t1"},
        new String[0],
        new String[0]
      },
      new Object[] {
        new Class<?>[] {JUnit4ParameterizedTest.class},
        JUnit4ParameterizedTest.EXPECTED,
        JUnit4ParameterizedTest.FAILED,
        JUnit4ParameterizedTest.SKIPPED
      },
      new Object[] {
        new Class<?>[] {BeforeClassJUnit4Sample.class},
        new String[0],
        new String[0],
        new String[] {"myTest"}
      },
      new Object[] {
        new Class<?>[] {ClassRuleJUnit4Sample.class},
        new String[0],
        new String[0],
        new String[] {"myTest"}
      }
    };
  }

  @Test(dataProvider = "dp")
  public void testTests(
      Class<?>[] classes,
      String[] expectedPassedTests,
      String[] expectedFailedTests,
      String[] expectedSkippedTests) {
    addClasses(classes);
    assert getTest().isJUnit();

    run();

    verifyPassedTests(expectedPassedTests);
    verifyFailedTests(expectedFailedTests);
    verifySkippedTests(expectedSkippedTests);
  }
}
