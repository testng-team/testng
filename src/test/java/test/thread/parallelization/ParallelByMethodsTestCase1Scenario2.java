package test.thread.parallelization;

import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import test.thread.parallelization.TestNgRunStateTracker.EventLog;

import test.thread.parallelization.sample.TestClassAFiveMethodsWithNoDepsSample;
import test.thread.parallelization.sample.TestClassBFourMethodsWithNoDepsSample;
import test.thread.parallelization.sample.TestClassCSixMethodsWithNoDepsSample;
import test.thread.parallelization.sample.TestClassDThreeMethodsWithNoDepsSample;
import test.thread.parallelization.sample.TestClassEFiveMethodsWithNoDepsSample;
import test.thread.parallelization.sample.TestClassFSixMethodsWithNoDepsSample;
import test.thread.parallelization.sample.TestClassGThreeMethodsWithNoDepsSample;
import test.thread.parallelization.sample.TestClassHFourMethodsWithNoDepsSample;
import test.thread.parallelization.sample.TestClassIFiveMethodsWithNoDepsSample;
import test.thread.parallelization.sample.TestClassJFourMethodsWithNoDepsSample;
import test.thread.parallelization.sample.TestClassKFiveMethodsWithNoDepsSample;
import test.thread.parallelization.sample.TestClassLThreeMethodsWithNoDepsSample;
import test.thread.parallelization.sample.TestClassMFourMethodsWithNoDepsSample;
import test.thread.parallelization.sample.TestClassNFiveMethodsWithNoDepsSample;
import test.thread.parallelization.sample.TestClassOSixMethodsWithNoDepsSample;

import static org.testng.Assert.assertEquals;

import static test.thread.parallelization.TestNgRunStateTracker.getAllEventLogsForSuite;

import static test.thread.parallelization.TestNgRunStateTracker.getAllSuiteLevelEventLogs;
import static test.thread.parallelization.TestNgRunStateTracker.getAllTestLevelEventLogs;
import static test.thread.parallelization.TestNgRunStateTracker.getAllTestMethodLevelEventLogs;
import static test.thread.parallelization.TestNgRunStateTracker.getSuiteAndTestLevelEventLogsForSuite;
import static test.thread.parallelization.TestNgRunStateTracker.getSuiteLevelEventLogsForSuite;
import static test.thread.parallelization.TestNgRunStateTracker.getSuiteListenerFinishEventLog;
import static test.thread.parallelization.TestNgRunStateTracker.getSuiteListenerStartEventLog;
import static test.thread.parallelization.TestNgRunStateTracker.getAllSuiteListenerStartEventLogs;

import static test.thread.parallelization.TestNgRunStateTracker.getTestLevelEventLogsForSuite;
import static test.thread.parallelization.TestNgRunStateTracker.getTestLevelEventLogsForTest;
import static test.thread.parallelization.TestNgRunStateTracker.getTestListenerFinishEventLog;
import static test.thread.parallelization.TestNgRunStateTracker.getTestListenerStartEventLog;

import static test.thread.parallelization.TestNgRunStateTracker.getTestMethodLevelEventLogsForSuite;
import static test.thread.parallelization.TestNgRunStateTracker.getTestMethodLevelEventLogsForTest;

import static test.thread.parallelization.TestNgRunStateTracker.reset;

/**
 * This class covers PTP_TC_1, Scenario 2 in the Parallelization Test Plan.
 *
 * Test Case Summary: Parallel by methods mode with sequential test suites, no dependencies and no factories or
 *                    data providers.
 *
 * Scenario Description: Three suites with 1, 2 and 3 tests respectively. One test for a suite shall consist of a
 *                       single test class while the rest shall consist of more than one test class.
 *
 * 1) For one of the suites, the thread count and parallel mode are specified at the suite level
 * 2) For one of the suites, the thread count and parallel mode are specified at the test level
 * 3) For one of the suites, the parallel mode is specified at the suite level, and the thread counts are specified at
 *    the test level (thread counts for each test differ)
 * 4) The thread count is less than the number of test methods for the tests in two of the suites, so some methods will
 *    have to wait the active thread count to drop below the maximum thread count before they can begin execution.
 * 5) The thread count is more than the number of test methods for the tests in one of the suites, ensuring that none
 *    of the methods in that suite should have to wait for any other method to complete execution
 * 6) There are NO configuration methods
 * 7) All test methods pass
 * 8) NO ordering is specified
 * 9) group-by-instances is NOT set
 * 10) There are no method exclusions
 */
public class ParallelByMethodsTestCase1Scenario2 extends BaseParallelizationTest {

    private static final String SUITE_A = "TestSuiteA";
    private static final String SUITE_B = "TestSuiteB";
    private static final String SUITE_C = "TestSuiteC";
    
    private static final String SUITE_A_TEST_A = "TestSuiteA-TwoTestClassTest";

    private static final String SUITE_B_TEST_A = "TestSuiteB-SingleTestClassTest";
    private static final String SUITE_B_TEST_B = "TestSuiteB-ThreeTestClassTest";
    
    private static final String SUITE_C_TEST_A = "TestSuiteC-ThreeTestClassTest";
    private static final String SUITE_C_TEST_B = "TestSuiteC-TwoTestClassTest";
    private static final String SUITE_C_TEST_C = "TestSuiteC-FourTestClassTest";

    private Map<String, List<EventLog>> suiteEventLogsMap = new HashMap<>();
    private Map<String, List<EventLog>> testEventLogsMap = new HashMap<>();

    private List<EventLog> suiteLevelEventLogs;
    private List<EventLog> testLevelEventLogs;
    private List<EventLog> testMethodLevelEventLogs;

    private List<EventLog> suiteOneSuiteAndTestLevelEventLogs;
    private List<EventLog> suiteOneSuiteLevelEventLogs;
    private List<EventLog> suiteOneTestLevelEventLogs;
    private List<EventLog> suiteOneTestMethodLevelEventLogs;

    private List<EventLog> suiteTwoSuiteAndTestLevelEventLogs;
    private List<EventLog> suiteTwoSuiteLevelEventLogs;
    private List<EventLog> suiteTwoTestLevelEventLogs;
    private List<EventLog> suiteTwoTestMethodLevelEventLogs;

    private List<EventLog> suiteThreeSuiteAndTestLevelEventLogs;
    private List<EventLog> suiteThreeSuiteLevelEventLogs;
    private List<EventLog> suiteThreeTestLevelEventLogs;
    private List<EventLog> suiteThreeTestMethodLevelEventLogs;

    private List<EventLog> suiteOneTestOneTestMethodLevelEventLogs;

    private List<EventLog> suiteTwoTestOneTestMethodLevelEventLogs;
    private List<EventLog> suiteTwoTestTwoTestMethodLevelEventLogs;

    private List<EventLog> suiteThreeTestOneTestMethodLevelEventLogs;
    private List<EventLog> suiteThreeTestTwoTestMethodLevelEventLogs;
    private List<EventLog> suiteThreeTestThreeTestMethodLevelEventLogs;

    private EventLog suiteOneSuiteListenerOnStartEventLog;
    private EventLog suiteOneSuiteListenerOnFinishEventLog;

    private EventLog suiteTwoSuiteListenerOnStartEventLog;
    private EventLog suiteTwoSuiteListenerOnFinishEventLog;

    private EventLog suiteThreeSuiteListenerOnStartEventLog;
    private EventLog suiteThreeSuiteListenerOnFinishEventLog;

    private EventLog suiteOneTestOneListenerOnStartEventLog;
    private EventLog suiteOneTestOneListenerOnFinishEventLog;

    private EventLog suiteTwoTestOneListenerOnStartEventLog;
    private EventLog suiteTwoTestOneListenerOnFinishEventLog;
    private EventLog suiteTwoTestTwoListenerOnStartEventLog;
    private EventLog suiteTwoTestTwoListenerOnFinishEventLog;

    private EventLog suiteThreeTestOneListenerOnStartEventLog;
    private EventLog suiteThreeTestOneListenerOnFinishEventLog;
    private EventLog suiteThreeTestTwoListenerOnStartEventLog;
    private EventLog suiteThreeTestTwoListenerOnFinishEventLog;
    private EventLog suiteThreeTestThreeListenerOnStartEventLog;
    private EventLog suiteThreeTestThreeListenerOnFinishEventLog;

    @BeforeClass
    public void setUp() {
        reset();

        XmlSuite suiteOne = createXmlSuite(SUITE_A);
        XmlSuite suiteTwo = createXmlSuite(SUITE_B);
        XmlSuite suiteThree = createXmlSuite(SUITE_C);

        suiteOne.setParallel(XmlSuite.ParallelMode.METHODS);
        suiteOne.setThreadCount(3);

        createXmlTest(suiteOne, SUITE_A_TEST_A, TestClassAFiveMethodsWithNoDepsSample.class,
                TestClassCSixMethodsWithNoDepsSample.class);
        createXmlTest(suiteTwo, SUITE_B_TEST_A, TestClassEFiveMethodsWithNoDepsSample.class);
        createXmlTest(suiteTwo, SUITE_B_TEST_B, TestClassDThreeMethodsWithNoDepsSample.class,
                TestClassBFourMethodsWithNoDepsSample.class, TestClassFSixMethodsWithNoDepsSample.class);

        suiteTwo.setParallel(XmlSuite.ParallelMode.METHODS);

        for(XmlTest test : suiteTwo.getTests()) {
            if(test.getName().equals(SUITE_B_TEST_A)) {
                test.setThreadCount(6);
            } else {
                test.setThreadCount(20);
            }
        }

        createXmlTest(suiteThree, SUITE_C_TEST_A, TestClassGThreeMethodsWithNoDepsSample.class,
                TestClassHFourMethodsWithNoDepsSample.class, TestClassIFiveMethodsWithNoDepsSample.class);
        createXmlTest(suiteThree, SUITE_C_TEST_B, TestClassJFourMethodsWithNoDepsSample.class,
                TestClassKFiveMethodsWithNoDepsSample.class);
        createXmlTest(suiteThree, SUITE_C_TEST_C, TestClassLThreeMethodsWithNoDepsSample.class,
                TestClassMFourMethodsWithNoDepsSample.class, TestClassNFiveMethodsWithNoDepsSample.class,
                TestClassOSixMethodsWithNoDepsSample.class);

        for(XmlTest test : suiteThree.getTests()) {
            test.setParallel(XmlSuite.ParallelMode.METHODS);

            switch(test.getName()) {
                case SUITE_C_TEST_A:
                    test.setThreadCount(10);
                    break;
                case SUITE_C_TEST_B:
                    test.setThreadCount(5);
                    break;
                default:
                    test.setThreadCount(12);
                    break;
            }
        }

        addParams(suiteOne, SUITE_A, SUITE_A_TEST_A, "100");

        addParams(suiteTwo, SUITE_B, SUITE_B_TEST_A, "100");
        addParams(suiteTwo, SUITE_B, SUITE_B_TEST_B, "100");

        addParams(suiteThree, SUITE_C, SUITE_C_TEST_A, "100");
        addParams(suiteThree, SUITE_C, SUITE_C_TEST_B, "100");
        addParams(suiteThree, SUITE_C, SUITE_C_TEST_C, "100");

        TestNG tng = create(suiteOne, suiteTwo, suiteThree);
        tng.addListener((ITestNGListener) new TestNgRunStateListener());

        System.out.println("Beginning ParallelByMethodsTestCase1Scenario2. This test scenario consists of three " +
                "sequentially executed suites with 1, 2 and 3 tests respectively. One test for a suite consists of a " +
                "single test class while the rest shall consist of more than one test class. There are no " +
                "dependencies, data providers or factories.");

        System.out.println("Suite: " + SUITE_A + ", Test: " + SUITE_A_TEST_A + ", Test classes: " +
                TestClassAFiveMethodsWithNoDepsSample.class.getCanonicalName() +
                        ", " + TestClassCSixMethodsWithNoDepsSample.class.getCanonicalName() + ". Thread count: 3");

        System.out.println("Suite: " + SUITE_B + ", Test: " + SUITE_B_TEST_A + ", Test class: " +
                TestClassEFiveMethodsWithNoDepsSample.class.getCanonicalName() + ". Thread count: 6");

        System.out.println("Suite " + SUITE_B + ", Test: " + SUITE_B_TEST_B + ", Test classes: " +
                TestClassDThreeMethodsWithNoDepsSample.class + ", " +
                TestClassBFourMethodsWithNoDepsSample.class + ", " +
                TestClassFSixMethodsWithNoDepsSample.class + ". Thread count: 20");

        System.out.println("Suite: " + SUITE_C + ", Test: " + SUITE_C_TEST_A + ", Test classes: " +
                TestClassGThreeMethodsWithNoDepsSample.class.getCanonicalName() + ", " +
                TestClassHFourMethodsWithNoDepsSample.class.getCanonicalName() + ", " +
                TestClassIFiveMethodsWithNoDepsSample.class + ". Thread count: 10");

        System.out.println("Suite: " + SUITE_C + ", Test: " + SUITE_C_TEST_B + ", Test classes: " +
                TestClassJFourMethodsWithNoDepsSample.class.getCanonicalName() + ", " +
                TestClassKFiveMethodsWithNoDepsSample.class + ". Thread count: 5");

        System.out.println("Suite: " + SUITE_C + ", Test: " + SUITE_C_TEST_C + ", Test classes: " +
                TestClassLThreeMethodsWithNoDepsSample.class.getCanonicalName() + ", " +
                TestClassMFourMethodsWithNoDepsSample.class.getCanonicalName() + ", " +
                TestClassNFiveMethodsWithNoDepsSample.class.getCanonicalName() + ", " +
                TestClassOSixMethodsWithNoDepsSample.class.getCanonicalName() + ". Thread count: 12.");

        tng.run();

        suiteLevelEventLogs = getAllSuiteLevelEventLogs();
        testLevelEventLogs = getAllTestLevelEventLogs();
        testMethodLevelEventLogs = getAllTestMethodLevelEventLogs();

        suiteOneSuiteAndTestLevelEventLogs = getSuiteAndTestLevelEventLogsForSuite(SUITE_A);
        suiteOneSuiteLevelEventLogs = getSuiteLevelEventLogsForSuite(SUITE_A);
        suiteOneTestLevelEventLogs = getTestLevelEventLogsForSuite(SUITE_A);

        suiteTwoSuiteAndTestLevelEventLogs = getSuiteAndTestLevelEventLogsForSuite(SUITE_B);
        suiteTwoSuiteLevelEventLogs = getSuiteLevelEventLogsForSuite(SUITE_B);
        suiteTwoTestLevelEventLogs = getTestLevelEventLogsForSuite(SUITE_B);

        suiteThreeSuiteAndTestLevelEventLogs = getSuiteAndTestLevelEventLogsForSuite(SUITE_C);
        suiteThreeSuiteLevelEventLogs = getSuiteLevelEventLogsForSuite(SUITE_C);
        suiteThreeTestLevelEventLogs = getTestLevelEventLogsForSuite(SUITE_C);

        suiteEventLogsMap.put(SUITE_A, getAllEventLogsForSuite(SUITE_A));
        suiteEventLogsMap.put(SUITE_B, getAllEventLogsForSuite(SUITE_B));
        suiteEventLogsMap.put(SUITE_C, getAllEventLogsForSuite(SUITE_C));

        suiteOneTestMethodLevelEventLogs = getTestMethodLevelEventLogsForSuite(SUITE_A);
        suiteTwoTestMethodLevelEventLogs = getTestMethodLevelEventLogsForSuite(SUITE_B);
        suiteThreeTestMethodLevelEventLogs = getTestMethodLevelEventLogsForSuite(SUITE_C);

        suiteOneTestOneTestMethodLevelEventLogs = getTestMethodLevelEventLogsForTest(SUITE_A, SUITE_A_TEST_A);

        suiteTwoTestOneTestMethodLevelEventLogs = getTestMethodLevelEventLogsForTest(SUITE_B, SUITE_B_TEST_A);
        suiteTwoTestTwoTestMethodLevelEventLogs = getTestMethodLevelEventLogsForTest(SUITE_B, SUITE_B_TEST_B);

        suiteThreeTestOneTestMethodLevelEventLogs = getTestMethodLevelEventLogsForTest(SUITE_C, SUITE_C_TEST_A);
        suiteThreeTestTwoTestMethodLevelEventLogs = getTestMethodLevelEventLogsForTest(SUITE_C, SUITE_C_TEST_B);
        suiteThreeTestThreeTestMethodLevelEventLogs = getTestMethodLevelEventLogsForTest(SUITE_C, SUITE_C_TEST_C);

        testEventLogsMap.put(SUITE_B_TEST_A, getTestLevelEventLogsForTest(SUITE_B, SUITE_B_TEST_A));
        testEventLogsMap.put(SUITE_B_TEST_B, getTestLevelEventLogsForTest(SUITE_B, SUITE_B_TEST_B));

        testEventLogsMap.put(SUITE_C_TEST_A, getTestLevelEventLogsForTest(SUITE_C, SUITE_C_TEST_A));
        testEventLogsMap.put(SUITE_C_TEST_B, getTestLevelEventLogsForTest(SUITE_C, SUITE_C_TEST_B));
        testEventLogsMap.put(SUITE_C_TEST_C, getTestLevelEventLogsForTest(SUITE_C, SUITE_C_TEST_C));

        suiteOneSuiteListenerOnStartEventLog = getSuiteListenerStartEventLog(SUITE_A);
        suiteOneSuiteListenerOnFinishEventLog = getSuiteListenerFinishEventLog(SUITE_A);

        suiteTwoSuiteListenerOnStartEventLog = getSuiteListenerStartEventLog(SUITE_B);
        suiteTwoSuiteListenerOnFinishEventLog = getSuiteListenerFinishEventLog(SUITE_B);

        suiteThreeSuiteListenerOnStartEventLog = getSuiteListenerStartEventLog(SUITE_C);
        suiteThreeSuiteListenerOnFinishEventLog = getSuiteListenerFinishEventLog(SUITE_C);

        suiteOneTestOneListenerOnStartEventLog = getTestListenerStartEventLog(SUITE_A, SUITE_A_TEST_A);
        suiteOneTestOneListenerOnFinishEventLog = getTestListenerFinishEventLog(SUITE_A, SUITE_A_TEST_A);

        suiteTwoTestOneListenerOnStartEventLog = getTestListenerStartEventLog(SUITE_B, SUITE_B_TEST_A);
        suiteTwoTestOneListenerOnFinishEventLog = getTestListenerFinishEventLog(SUITE_B, SUITE_B_TEST_A);

        suiteTwoTestTwoListenerOnStartEventLog = getTestListenerStartEventLog(SUITE_B, SUITE_B_TEST_B);
        suiteTwoTestTwoListenerOnFinishEventLog = getTestListenerFinishEventLog(SUITE_B, SUITE_B_TEST_B);

        suiteThreeTestOneListenerOnStartEventLog = getTestListenerStartEventLog(SUITE_C, SUITE_C_TEST_A);
        suiteThreeTestOneListenerOnFinishEventLog = getTestListenerFinishEventLog(SUITE_C, SUITE_C_TEST_A);
        suiteThreeTestTwoListenerOnStartEventLog = getTestListenerStartEventLog(SUITE_C, SUITE_C_TEST_B);
        suiteThreeTestTwoListenerOnFinishEventLog = getTestListenerFinishEventLog(SUITE_C, SUITE_C_TEST_B);
        suiteThreeTestThreeListenerOnStartEventLog = getTestListenerStartEventLog(SUITE_C, SUITE_C_TEST_C);
        suiteThreeTestThreeListenerOnFinishEventLog = getTestListenerFinishEventLog(SUITE_C, SUITE_C_TEST_C);
    }

    //Verifies that the expected number of suite, test and test method level events were logged for each of the three
    //suites.
    @Test
    public void sanityCheck() {
        assertEquals(suiteLevelEventLogs.size(), 6, "There should be 6 suite level events logged for " + SUITE_A +
                ", " + SUITE_B + " and " + SUITE_C + ": " + suiteLevelEventLogs);
        assertEquals(testLevelEventLogs.size(), 12, "There should be 12 test level events logged for " + SUITE_A +
                ", " + SUITE_B + " and " + SUITE_C + ": " + testLevelEventLogs);

        assertEquals(testMethodLevelEventLogs.size(), 204, "There should 204 test method level events logged for " +
                SUITE_A + ", " + SUITE_B + " and " + SUITE_C + ": " + testMethodLevelEventLogs);

        assertEquals(suiteOneSuiteLevelEventLogs.size(), 2, "There should be 2 suite level events logged for " +
                SUITE_A + ": " + suiteOneSuiteLevelEventLogs);
        assertEquals(suiteOneTestLevelEventLogs.size(), 2, "There should be 2 test level events logged for " + SUITE_A +
                ": " + suiteOneTestLevelEventLogs);
        assertEquals(suiteOneTestMethodLevelEventLogs.size(), 33, "There should be 33 test method level events " +
                "logged for " + SUITE_A + ": " + suiteOneTestMethodLevelEventLogs);

        assertEquals(suiteTwoSuiteLevelEventLogs.size(), 2, "There should be 2 suite level events logged for " +
                SUITE_B + ": " + suiteTwoSuiteLevelEventLogs);
        assertEquals(suiteTwoTestLevelEventLogs.size(), 4, "There should be 4 test level events logged for " + SUITE_B +
                ": " + suiteTwoTestLevelEventLogs);
        assertEquals(suiteTwoTestMethodLevelEventLogs.size(), 54, "There should be 54 test method level events " +
                "logged for " + SUITE_B + ": " + suiteTwoTestMethodLevelEventLogs);

        assertEquals(suiteThreeSuiteLevelEventLogs.size(), 2, "There should be 2 suite level events logged for " +
                SUITE_C + ": " + suiteThreeSuiteLevelEventLogs);
        assertEquals(suiteThreeTestLevelEventLogs.size(), 6, "There should be 6 test level events logged for " +
                SUITE_C + ": " + suiteThreeTestLevelEventLogs);
        assertEquals(suiteThreeTestMethodLevelEventLogs.size(), 117, "There should be 87 test method level events " +
                "logged for " + SUITE_C + ": " + suiteThreeTestMethodLevelEventLogs);

    }

    //Verify that all the events in the second suite and third suites run have timestamps later than the suite
    //listener's onFinish event for the first suite run.
    //Verify that all the events in the third suite run have timestamps later than the suite listener's onFinish
    //event for the second suite run.
    //Verify that all suite level events run in the same thread
    @Test
    public void verifySuitesRunSequentiallyInSameThread() {
        verifySequentialSuites(suiteLevelEventLogs, suiteEventLogsMap);
    }

    //For all suites, verify that the test level events run sequentially because the parallel mode is by methods only.
    @Test
    public void verifySuiteAndTestLevelEventsRunInSequentialOrderForIndividualSuites() {

        verifySequentialTests(suiteOneSuiteAndTestLevelEventLogs, suiteOneTestLevelEventLogs,
                suiteOneSuiteListenerOnStartEventLog, suiteOneSuiteListenerOnFinishEventLog);

        verifySequentialTests(suiteTwoSuiteAndTestLevelEventLogs, suiteTwoTestLevelEventLogs,
                suiteTwoSuiteListenerOnStartEventLog, suiteTwoSuiteListenerOnFinishEventLog);

        verifySequentialTests(suiteThreeSuiteAndTestLevelEventLogs, suiteThreeTestLevelEventLogs,
                suiteThreeSuiteListenerOnStartEventLog, suiteThreeSuiteListenerOnFinishEventLog);
    }

    //Verify that there is only a single test class instance associated with each of the test methods from the sample
    //classes for every test in all the suites.
    //Verify that the same test class instance is associated with each of the test methods from the sample test class
    @Test
    public void verifyOnlyOneInstanceOfTestClassForAllTestMethodsForAllSuites() {

        verifyNumberOfInstancesOfTestClassesForMethods(
                SUITE_A,
                SUITE_A_TEST_A,
                Arrays.asList(TestClassAFiveMethodsWithNoDepsSample.class, TestClassCSixMethodsWithNoDepsSample.class),
                1);

        verifySameInstancesOfTestClassesAssociatedWithMethods(
                SUITE_A,
                SUITE_A_TEST_A,
                Arrays.asList(TestClassAFiveMethodsWithNoDepsSample.class, TestClassCSixMethodsWithNoDepsSample.class)
        );

        verifyNumberOfInstancesOfTestClassForMethods(
                SUITE_B,
                SUITE_B_TEST_A,
                TestClassEFiveMethodsWithNoDepsSample.class,
                1);

        verifySameInstancesOfTestClassAssociatedWithMethods(
                SUITE_B,
                SUITE_B_TEST_A,
                TestClassEFiveMethodsWithNoDepsSample.class
        );

        verifyNumberOfInstancesOfTestClassesForMethods(
                SUITE_B,
                SUITE_B_TEST_B,
                Arrays.asList(
                        TestClassDThreeMethodsWithNoDepsSample.class,
                        TestClassBFourMethodsWithNoDepsSample.class,
                        TestClassFSixMethodsWithNoDepsSample.class
                ),
                1
        );

        verifySameInstancesOfTestClassesAssociatedWithMethods(
                SUITE_B,
                SUITE_B_TEST_B,
                Arrays.asList(
                        TestClassDThreeMethodsWithNoDepsSample.class,
                        TestClassBFourMethodsWithNoDepsSample.class,
                        TestClassFSixMethodsWithNoDepsSample.class
                )
        );

        verifyNumberOfInstancesOfTestClassesForMethods(
                SUITE_C,
                SUITE_C_TEST_A,
                Arrays.asList(
                        TestClassGThreeMethodsWithNoDepsSample.class,
                        TestClassHFourMethodsWithNoDepsSample.class,
                        TestClassIFiveMethodsWithNoDepsSample.class
                ),
                1
        );

        verifySameInstancesOfTestClassesAssociatedWithMethods(
                SUITE_C,
                SUITE_C_TEST_A,
                Arrays.asList(
                        TestClassGThreeMethodsWithNoDepsSample.class,
                        TestClassHFourMethodsWithNoDepsSample.class,
                        TestClassIFiveMethodsWithNoDepsSample.class
                )
        );

        verifyNumberOfInstancesOfTestClassesForMethods(
                SUITE_C,
                SUITE_C_TEST_B,
                Arrays.asList(TestClassJFourMethodsWithNoDepsSample.class, TestClassKFiveMethodsWithNoDepsSample.class),
                1);

        verifySameInstancesOfTestClassesAssociatedWithMethods(
                SUITE_C,
                SUITE_C_TEST_B,
                Arrays.asList(TestClassJFourMethodsWithNoDepsSample.class, TestClassKFiveMethodsWithNoDepsSample.class)
        );

        verifyNumberOfInstancesOfTestClassesForMethods(
                SUITE_C,
                SUITE_C_TEST_C,
                Arrays.asList(
                        TestClassLThreeMethodsWithNoDepsSample.class,
                        TestClassMFourMethodsWithNoDepsSample.class,
                        TestClassNFiveMethodsWithNoDepsSample.class,
                        TestClassOSixMethodsWithNoDepsSample.class
                ),
                1
        );

        verifySameInstancesOfTestClassesAssociatedWithMethods(
                SUITE_C,
                SUITE_C_TEST_C,
                Arrays.asList(
                        TestClassLThreeMethodsWithNoDepsSample.class,
                        TestClassMFourMethodsWithNoDepsSample.class,
                        TestClassNFiveMethodsWithNoDepsSample.class,
                        TestClassOSixMethodsWithNoDepsSample.class
                )
        );
    }

    //Verify that the test method listener's onTestStart method runs after the test listener's onStart method for
    //all the test methods in all tests and suites.
    @Test
    public void verifyTestLevelMethodLevelEventLogsOccurBetweenAfterTestListenerStartAndFinishEventLogs() {
        verifyEventsOccurBetween(suiteOneTestOneListenerOnStartEventLog, suiteOneTestOneTestMethodLevelEventLogs,
                suiteOneTestOneListenerOnFinishEventLog,  "All of the test method level event logs for " +
                        SUITE_A_TEST_A + " should have timestamps between the test listener's onStart and onFinish " +
                        "event logs for " + SUITE_A_TEST_A + ". Test listener onStart event log: " +
                        suiteOneTestOneListenerOnStartEventLog + ". Test listener onFinish event log: " +
                        suiteOneTestOneListenerOnFinishEventLog + ". Test method level event logs: " +
                        suiteOneTestOneTestMethodLevelEventLogs);

        verifyEventsOccurBetween(suiteTwoTestOneListenerOnStartEventLog, suiteTwoTestOneTestMethodLevelEventLogs,
                suiteTwoTestOneListenerOnFinishEventLog,  "All of the test method level event logs for " +
                        SUITE_B_TEST_A + " should have timestamps between the test listener's onStart and onFinish " +
                        "event logs for " + SUITE_B_TEST_A + ". Test listener onStart event log: " +
                        suiteTwoTestOneListenerOnStartEventLog + ". Test listener onFinish event log: " +
                        suiteTwoTestOneListenerOnFinishEventLog + ". Test method level event logs: " +
                        suiteTwoTestOneTestMethodLevelEventLogs);

        verifyEventsOccurBetween(suiteTwoTestTwoListenerOnStartEventLog, suiteTwoTestTwoTestMethodLevelEventLogs,
                suiteTwoTestTwoListenerOnFinishEventLog,  "All of the test method level event logs for " +
                        SUITE_B_TEST_B + " should have timestamps between the test listener's onStart and onFinish " +
                        "event logs for " + SUITE_B_TEST_B + ". Test listener onStart event log: " +
                        suiteTwoTestTwoListenerOnStartEventLog + ". Test listener onFinish event log: " +
                        suiteTwoTestTwoListenerOnFinishEventLog + ". Test method level event logs: " +
                        suiteTwoTestTwoTestMethodLevelEventLogs);

        verifyEventsOccurBetween(suiteThreeTestOneListenerOnStartEventLog, suiteThreeTestOneTestMethodLevelEventLogs,
                suiteThreeTestOneListenerOnFinishEventLog,  "All of the test method level event logs for " +
                        SUITE_C_TEST_A + " should have timestamps between the test listener's onStart and onFinish " +
                        "event logs for " + SUITE_C_TEST_A + ". Test listener onStart event log: " +
                        suiteThreeTestOneListenerOnStartEventLog + ". Test listener onFinish event log: " +
                        suiteThreeTestOneListenerOnFinishEventLog + ". Test method level event logs: " +
                        suiteThreeTestOneTestMethodLevelEventLogs);

        verifyEventsOccurBetween(suiteThreeTestTwoListenerOnStartEventLog, suiteThreeTestTwoTestMethodLevelEventLogs,
                suiteThreeTestTwoListenerOnFinishEventLog,  "All of the test method level event logs for " +
                        SUITE_C_TEST_B + " should have timestamps between the test listener's onStart and onFinish " +
                        "event logs for " + SUITE_C_TEST_B + ". Test listener onStart event log: " +
                        suiteThreeTestTwoListenerOnStartEventLog + ". Test listener onFinish event log: " +
                        suiteThreeTestTwoListenerOnFinishEventLog + ". Test method level event logs: " +
                        suiteThreeTestTwoTestMethodLevelEventLogs);

        verifyEventsOccurBetween(suiteThreeTestThreeListenerOnStartEventLog, suiteThreeTestThreeTestMethodLevelEventLogs,
                suiteThreeTestThreeListenerOnFinishEventLog,  "All of the test method level event logs for " +
                        SUITE_C_TEST_C + " should have timestamps between the test listener's onStart and onFinish " +
                        "event logs for " + SUITE_C_TEST_C + ". Test listener onStart event log: " +
                        suiteThreeTestThreeListenerOnStartEventLog + ". Test listener onFinish event log: " +
                        suiteThreeTestThreeListenerOnFinishEventLog + ". Test method level event logs: " +
                        suiteThreeTestThreeTestMethodLevelEventLogs);

    }

    //Verifies that the method level events all run in different threads from the test and suite level events.
    //Verifies that the test method listener and execution events for a given test method all run in the same thread.
    @Test
    public void verifyThatMethodLevelEventsRunInDifferentThreadsFromSuiteAndTestLevelEvents() {

        verifyEventThreadsSpawnedAfter(getAllSuiteListenerStartEventLogs().get(0).getThreadId(), testMethodLevelEventLogs,
                "All the thread IDs for the test method level events should be greater than the thread ID for the " +
                        "suite and test level events. The expectation is that since the suite and test level events " +
                        "are running sequentially, and all the test methods are running in parallel, new threads "  +
                        "will be spawned after the thread executing the suite and test level events when new methods " +
                        "begin executing. Suite and test level events thread ID: " +
                        getAllSuiteListenerStartEventLogs().get(0).getThreadId() + ". Test method level event logs: " +
                        testMethodLevelEventLogs);

        verifyEventsForTestMethodsRunInTheSameThread(TestClassAFiveMethodsWithNoDepsSample.class, SUITE_A,
                SUITE_A_TEST_A);
        verifyEventsForTestMethodsRunInTheSameThread(TestClassCSixMethodsWithNoDepsSample.class, SUITE_A,
                SUITE_A_TEST_A);

        verifyEventsForTestMethodsRunInTheSameThread(TestClassEFiveMethodsWithNoDepsSample.class, SUITE_B,
                SUITE_B_TEST_A);

        verifyEventsForTestMethodsRunInTheSameThread(TestClassDThreeMethodsWithNoDepsSample.class, SUITE_B,
                SUITE_B_TEST_B);
        verifyEventsForTestMethodsRunInTheSameThread(TestClassBFourMethodsWithNoDepsSample.class, SUITE_B,
                SUITE_B_TEST_B);
        verifyEventsForTestMethodsRunInTheSameThread(TestClassFSixMethodsWithNoDepsSample.class, SUITE_B,
                SUITE_B_TEST_B);

        verifyEventsForTestMethodsRunInTheSameThread(TestClassGThreeMethodsWithNoDepsSample.class, SUITE_C,
                SUITE_C_TEST_A);
        verifyEventsForTestMethodsRunInTheSameThread(TestClassHFourMethodsWithNoDepsSample.class, SUITE_C,
                SUITE_C_TEST_A);
        verifyEventsForTestMethodsRunInTheSameThread(TestClassIFiveMethodsWithNoDepsSample.class, SUITE_C,
                SUITE_C_TEST_A);

        verifyEventsForTestMethodsRunInTheSameThread(TestClassJFourMethodsWithNoDepsSample.class, SUITE_C,
                SUITE_C_TEST_B);
        verifyEventsForTestMethodsRunInTheSameThread(TestClassKFiveMethodsWithNoDepsSample.class, SUITE_C,
                SUITE_C_TEST_B);

        verifyEventsForTestMethodsRunInTheSameThread(TestClassLThreeMethodsWithNoDepsSample.class, SUITE_C,
                SUITE_C_TEST_C);
        verifyEventsForTestMethodsRunInTheSameThread(TestClassMFourMethodsWithNoDepsSample.class, SUITE_C,
                SUITE_C_TEST_C);
        verifyEventsForTestMethodsRunInTheSameThread(TestClassNFiveMethodsWithNoDepsSample.class, SUITE_C,
                SUITE_C_TEST_C);
        verifyEventsForTestMethodsRunInTheSameThread(TestClassOSixMethodsWithNoDepsSample.class, SUITE_C,
                SUITE_C_TEST_C);
    }

    //Verify that the methods are run in separate threads.
    @Test
    public void verifyThatTestMethodsRunInParallelThreads() {
        verifySimultaneousTestMethods(getTestMethodLevelEventLogsForTest(SUITE_A, SUITE_A_TEST_A), SUITE_A_TEST_A, 3);
        verifySimultaneousTestMethods(getTestMethodLevelEventLogsForTest(SUITE_B, SUITE_B_TEST_A), SUITE_B_TEST_A, 6);
        verifySimultaneousTestMethods(getTestMethodLevelEventLogsForTest(SUITE_B, SUITE_B_TEST_B), SUITE_B_TEST_B, 20);
        verifySimultaneousTestMethods(getTestMethodLevelEventLogsForTest(SUITE_C, SUITE_C_TEST_A), SUITE_C_TEST_A, 10);
        verifySimultaneousTestMethods(getTestMethodLevelEventLogsForTest(SUITE_C, SUITE_C_TEST_B), SUITE_C_TEST_B, 5);
        verifySimultaneousTestMethods(getTestMethodLevelEventLogsForTest(SUITE_C, SUITE_C_TEST_C), SUITE_C_TEST_C, 12);
    }
}
