package test;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import test.junit4.JUnit4Child;
import test.junit4.JUnit4ParameterizedTest;
import test.junit4.JUnit4Sample2;
import test.junit4.JUnit4SampleSuite;

/**
 *
 * @author lukas
 */
public class JUnit4Test extends BaseTest {

    @BeforeMethod(dependsOnGroups = {"initTest"})
    public void initJUnitFlag() {
        getTest().setJUnit(true);
    }

    @Test
    public void testTests() {
        addClass("test.junit4.JUnit4Sample2");
        assert getTest().isJUnit();

        run();
        String[] passed = JUnit4Sample2.EXPECTED;
        String[] failed = JUnit4Sample2.FAILED;
        String[] skipped = JUnit4Sample2.SKIPPED;

        verifyTests("Passed", passed, getPassedTests());
        verifyTests("Failed", failed, getFailedTests());
        verifyTests("Skipped", skipped, getSkippedTests());
    }

    @Test
    public void testSuite() {
        addClass("test.junit4.JUnit4SampleSuite");
        assert getTest().isJUnit();

        run();
        String[] passed = JUnit4SampleSuite.EXPECTED;
        String[] failed = JUnit4SampleSuite.FAILED;
        String[] skipped = JUnit4SampleSuite.SKIPPED;

        verifyTests("Passed", passed, getPassedTests());
        verifyTests("Failed", failed, getFailedTests());
        verifyTests("Skipped", skipped, getSkippedTests());
    }

    @Test
    public void testSuiteInheritance() {
        addClass("test.junit4.JUnit4Child");
        assert getTest().isJUnit();

        run();
        String[] passed = JUnit4Child.EXPECTED;
        String[] failed = {};
        String[] skipped = {};

        verifyTests("Passed", passed, getPassedTests());
        verifyTests("Failed", failed, getFailedTests());
        verifyTests("Skipped", skipped, getSkippedTests());
    }

    @Test
    public void testTestInheritance() {
        addClass("test.junit4.InheritedTest");
        addClass("test.junit4.JUnit4Sample1");
        assert getTest().isJUnit();

        run();
        String[] passed = {"t1", "t1"};
        String[] failed = {};
        String[] skipped = {};

        verifyTests("Passed", passed, getPassedTests());
        verifyTests("Failed", failed, getFailedTests());
        verifyTests("Skipped", skipped, getSkippedTests());
    }

    @Test
    public void testTestParameterized() {
        addClass("test.junit4.JUnit4ParameterizedTest");
        assert getTest().isJUnit();

        run();
        String[] passed = JUnit4ParameterizedTest.EXPECTED;
        String[] failed = JUnit4ParameterizedTest.FAILED;
        String[] skipped = JUnit4ParameterizedTest.SKIPPED;

        verifyTests("Passed", passed, getPassedTests());
        verifyTests("Failed", failed, getFailedTests());
        verifyTests("Skipped", skipped, getSkippedTests());
    }
}
