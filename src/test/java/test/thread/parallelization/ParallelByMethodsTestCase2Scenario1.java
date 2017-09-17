package test.thread.parallelization;

import org.testng.ITestNGListener;
import org.testng.TestNG;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.testng.xml.XmlSuite;

import test.thread.parallelization.TestNgRunStateTracker.EventLog;

import test.thread.parallelization.sample.TestClassAFiveMethodsWithNoDepsSample;
import test.thread.parallelization.sample.TestClassBFourMethodsWithNoDepsSample;
import test.thread.parallelization.sample.TestClassCSixMethodsWithNoDepsSample;
import test.thread.parallelization.sample.TestClassDThreeMethodsWithNoDepsSample;
import test.thread.parallelization.sample.TestClassEFiveMethodsWithNoDepsSample;
import test.thread.parallelization.sample.TestClassFSixMethodsWithNoDepsSample;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.testng.Assert.assertEquals;

import static test.thread.parallelization.TestNgRunStateTracker.getAllSuiteLevelEventLogs;
import static test.thread.parallelization.TestNgRunStateTracker.getAllSuiteListenerStartEventLogs;
import static test.thread.parallelization.TestNgRunStateTracker.getAllTestLevelEventLogs;
import static test.thread.parallelization.TestNgRunStateTracker.getAllTestMethodLevelEventLogs;
import static test.thread.parallelization.TestNgRunStateTracker.getSuiteAndTestLevelEventLogsForSuite;
import static test.thread.parallelization.TestNgRunStateTracker.getSuiteLevelEventLogsForSuite;
import static test.thread.parallelization.TestNgRunStateTracker.getSuiteListenerFinishEventLog;
import static test.thread.parallelization.TestNgRunStateTracker.getSuiteListenerStartEventLog;
import static test.thread.parallelization.TestNgRunStateTracker.getTestLevelEventLogsForSuite;
import static test.thread.parallelization.TestNgRunStateTracker.getTestLevelEventLogsForTest;
import static test.thread.parallelization.TestNgRunStateTracker.getTestListenerFinishEventLog;

import static test.thread.parallelization.TestNgRunStateTracker.getTestListenerStartEventLog;
import static test.thread.parallelization.TestNgRunStateTracker.getTestMethodLevelEventLogsForSuite;
import static test.thread.parallelization.TestNgRunStateTracker.getTestMethodLevelEventLogsForTest;
import static test.thread.parallelization.TestNgRunStateTracker.reset;

/**
 * This class covers PTP_TC_2, Scenario 1 in the Parallelization Test Plan.
 *
 * Test Case Summary: Parallel by methods mode with parallel test suites, no dependencies and no factories or data
 *                    providers.
 *
 * Scenario Description: Two suites with 1 and 2 tests respectively. One test for a suite shall consist of a single
 *                       test class while the rest shall consist of more than one test class.
 *
 * 1) The suite thread pool is 2, so neither suite should have to wait for the other to complete execution
 * 2) For both suites, the thread count and parallel mode are specified at the suite level
 * 3) The thread count is less than the number of test methods in a test for one of the suites, so some methods will
 *    have to wait the active thread count to drop below the maximum thread count before they can begin execution.
 * 4) The thread count is more than the number of test methods for the rest of the tests, ensuring that none of their
 *    test methods should have to wait for any other method to complete execution
 * 5) There are NO configuration methods
 * 6) All test methods pass
 * 7) NO ordering is specified
 * 8) group-by-instances is NOT set
 * 9) There are no method exclusions
 */
public class ParallelByMethodsTestCase2Scenario1 extends BaseParallelizationTest {

    private static final String SUITE_A = "TestSuiteA";
    private static final String SUITE_B = "TestSuiteB";

    private static final String SUITE_A_TEST_A = "TestSuiteA-TwoTestClassTest";

    private static final String SUITE_B_TEST_A = "TestSuiteB-SingleTestClassTest";
    private static final String SUITE_B_TEST_B = "TestSuiteB-ThreeTestClassTest";

    private static final int THREAD_POOL_SIZE = 2;

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

    private List<EventLog> suiteOneTestOneTestMethodLevelEventLogs;

    private List<EventLog> suiteTwoTestOneTestMethodLevelEventLogs;
    private List<EventLog> suiteTwoTestTwoTestMethodLevelEventLogs;

    private EventLog suiteOneSuiteListenerOnStartEventLog;
    private EventLog suiteOneSuiteListenerOnFinishEventLog;

    private EventLog suiteTwoSuiteListenerOnStartEventLog;
    private EventLog suiteTwoSuiteListenerOnFinishEventLog;

    private EventLog suiteOneTestOneListenerOnStartEventLog;
    private EventLog suiteOneTestOneListenerOnFinishEventLog;

    private EventLog suiteTwoTestOneListenerOnStartEventLog;
    private EventLog suiteTwoTestOneListenerOnFinishEventLog;
    private EventLog suiteTwoTestTwoListenerOnStartEventLog;
    private EventLog suiteTwoTestTwoListenerOnFinishEventLog;

    @BeforeClass
    public void setUp() {
        reset();

        XmlSuite suiteOne = createXmlSuite(SUITE_A);
        XmlSuite suiteTwo = createXmlSuite(SUITE_B);

        createXmlTest(suiteOne, SUITE_A_TEST_A, TestClassAFiveMethodsWithNoDepsSample.class,
                TestClassCSixMethodsWithNoDepsSample.class);
        createXmlTest(suiteTwo, SUITE_B_TEST_A, TestClassEFiveMethodsWithNoDepsSample.class);
        createXmlTest(suiteTwo, SUITE_B_TEST_B, TestClassDThreeMethodsWithNoDepsSample.class,
                TestClassBFourMethodsWithNoDepsSample.class, TestClassFSixMethodsWithNoDepsSample.class);

        suiteOne.setParallel(XmlSuite.ParallelMode.METHODS);
        suiteOne.setThreadCount(3);
        suiteTwo.setParallel(XmlSuite.ParallelMode.METHODS);
        suiteTwo.setThreadCount(14);

        addParams(suiteOne, SUITE_A, SUITE_A_TEST_A, "100");

        addParams(suiteTwo, SUITE_B, SUITE_B_TEST_A, "100");
        addParams(suiteTwo, SUITE_B, SUITE_B_TEST_B, "100");

        TestNG tng = create(suiteOne, suiteTwo);
        tng.setSuiteThreadPoolSize(2);
        tng.addListener((ITestNGListener) new TestNgRunStateListener());

        System.out.println("Beginning ParallelByMethodsTestCase2Scenario1. This test scenario consists of two " +
                "suites with 1 and 2 tests respectively. The suites run in parallel and the thread pool size is 2. " +
                "One test for a suite shall consist of a single test class while the rest shall consist of more than " +
                "one test class. There are no dependencies, data providers or factories.");

        System.out.println("Suite: " + SUITE_A + ", Test: " + SUITE_A_TEST_A + ", Test classes: " +
                TestClassAFiveMethodsWithNoDepsSample.class.getCanonicalName() + ", " +
                TestClassCSixMethodsWithNoDepsSample.class.getCanonicalName() + ". Thread count: 3");

        System.out.println("Suite: " + SUITE_B + ", Test: " + SUITE_B_TEST_A + ", Test class: " +
                TestClassEFiveMethodsWithNoDepsSample.class.getCanonicalName() + ". Thread count: 14");

        System.out.println("Suite: " + SUITE_B + ", Test: " + SUITE_B_TEST_B + ", Test classes: " +
                TestClassDThreeMethodsWithNoDepsSample.class + ", " +
                TestClassBFourMethodsWithNoDepsSample.class + ", " +
                TestClassFSixMethodsWithNoDepsSample.class + ". Thread count: 14");

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

        suiteOneTestMethodLevelEventLogs = getTestMethodLevelEventLogsForSuite(SUITE_A);
        suiteTwoTestMethodLevelEventLogs = getTestMethodLevelEventLogsForSuite(SUITE_B);

        suiteOneTestOneTestMethodLevelEventLogs = getTestMethodLevelEventLogsForTest(SUITE_A, SUITE_A_TEST_A);

        suiteTwoTestOneTestMethodLevelEventLogs = getTestMethodLevelEventLogsForTest(SUITE_B, SUITE_B_TEST_A);
        suiteTwoTestTwoTestMethodLevelEventLogs = getTestMethodLevelEventLogsForTest(SUITE_B, SUITE_B_TEST_B);

        testEventLogsMap.put(SUITE_B_TEST_A, getTestLevelEventLogsForTest(SUITE_B, SUITE_B_TEST_A));
        testEventLogsMap.put(SUITE_B_TEST_B, getTestLevelEventLogsForTest(SUITE_B, SUITE_B_TEST_B));

        suiteOneSuiteListenerOnStartEventLog = getSuiteListenerStartEventLog(SUITE_A);
        suiteOneSuiteListenerOnFinishEventLog = getSuiteListenerFinishEventLog(SUITE_A);

        suiteTwoSuiteListenerOnStartEventLog = getSuiteListenerStartEventLog(SUITE_B);
        suiteTwoSuiteListenerOnFinishEventLog = getSuiteListenerFinishEventLog(SUITE_B);

        suiteOneTestOneListenerOnStartEventLog = getTestListenerStartEventLog(SUITE_A, SUITE_A_TEST_A);
        suiteOneTestOneListenerOnFinishEventLog = getTestListenerFinishEventLog(SUITE_A, SUITE_A_TEST_A);

        suiteTwoTestOneListenerOnStartEventLog = getTestListenerStartEventLog(SUITE_B, SUITE_B_TEST_A);
        suiteTwoTestOneListenerOnFinishEventLog = getTestListenerFinishEventLog(SUITE_B, SUITE_B_TEST_A);

        suiteTwoTestTwoListenerOnStartEventLog = getTestListenerStartEventLog(SUITE_B, SUITE_B_TEST_B);
        suiteTwoTestTwoListenerOnFinishEventLog = getTestListenerFinishEventLog(SUITE_B, SUITE_B_TEST_B);

    }

    //Verifies that the expected number of suite, test and test method level events were logged for each of the three
    //suites.
    @Test
    public void sanityCheck() {
        assertEquals(suiteLevelEventLogs.size(), 4, "There should be 4 suite level events logged for " + SUITE_A +
                ", " + SUITE_B + ": " + suiteLevelEventLogs);
        assertEquals(testLevelEventLogs.size(), 6, "There should be 6 test level events logged for " + SUITE_A +
                ", " + SUITE_B + ": " + testLevelEventLogs);

        assertEquals(testMethodLevelEventLogs.size(), 87, "There should 87 test method level events logged for " +
                SUITE_A + ", " + SUITE_B + ": " + testMethodLevelEventLogs);

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
    }

    //Verify that the suites run in parallel by checking that the suite and test level events for both suites have
    //overlapping timestamps. Verify that there are two separate threads executing the suite-level and test-level
    //events for each suite.
    @Test
    public void verifyThatSuitesRunInParallelThreads() {
        verifyParallelSuitesWithUnequalExecutionTimes(suiteLevelEventLogs, THREAD_POOL_SIZE);
    }

    @Test
    public void verifyTestLevelEventsRunInSequentialOrderForIndividualSuites() {
        verifySequentialTests(suiteOneSuiteAndTestLevelEventLogs, suiteOneTestLevelEventLogs,
                suiteOneSuiteListenerOnStartEventLog, suiteOneSuiteListenerOnFinishEventLog);

        verifySequentialTests(suiteTwoSuiteAndTestLevelEventLogs, suiteTwoTestLevelEventLogs,
                suiteTwoSuiteListenerOnStartEventLog, suiteTwoSuiteListenerOnFinishEventLog);
    }

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
    }

    //Verify that the methods are run in separate threads in true parallel fashion by checking that the start and run
    //times of events that should be run simultaneously start basically at the same time using the timestamps and the
    //known values of the wait time specified for the event. Verify that the thread IDs of parallel events are
    //different.
    @Test
    public void verifyThatTestMethodsRunInParallelThreads() {
        verifySimultaneousTestMethods(getTestMethodLevelEventLogsForTest(SUITE_A, SUITE_A_TEST_A), SUITE_A_TEST_A, 3);
        verifySimultaneousTestMethods(getTestMethodLevelEventLogsForTest(SUITE_B, SUITE_B_TEST_A), SUITE_B_TEST_A, 14);
        verifySimultaneousTestMethods(getTestMethodLevelEventLogsForTest(SUITE_B, SUITE_B_TEST_B), SUITE_B_TEST_B, 14);
    }

}
