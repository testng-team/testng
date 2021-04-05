package test.thread.parallelization;

import org.testng.ITestNGListener;
import org.testng.TestNG;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.testng.xml.XmlSuite;

import test.thread.parallelization.TestNgRunStateTracker.EventLog;
import test.thread.parallelization.sample.TestClassAFiveMethodsWithNoDepsSample;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.testng.Assert.assertEquals;

import static test.thread.parallelization.TestNgRunStateTracker.getAllSuiteAndTestLevelEventLogs;
import static test.thread.parallelization.TestNgRunStateTracker.getAllSuiteLevelEventLogs;
import static test.thread.parallelization.TestNgRunStateTracker.getAllTestLevelEventLogs;
import static test.thread.parallelization.TestNgRunStateTracker.getAllTestMethodLevelEventLogs;
import static test.thread.parallelization.TestNgRunStateTracker.getSuiteListenerFinishEventLog;
import static test.thread.parallelization.TestNgRunStateTracker.getSuiteListenerStartEventLog;
import static test.thread.parallelization.TestNgRunStateTracker.getTestListenerFinishEventLog;
import static test.thread.parallelization.TestNgRunStateTracker.getTestListenerStartEventLog;
import static test.thread.parallelization.TestNgRunStateTracker.getTestListenerStartThreadId;

import static test.thread.parallelization.TestNgRunStateTracker.reset;

/**
 * This class covers PTP_TC_1, Scenario 1 in the Parallelization Test Plan.
 *
 * Test Case Summary: Parallel by methods mode with sequential test suites, no dependencies and no factories or
 *                    data providers.
 *
 * Scenario Description: Single suite with a single test consisting of a single test class with five methods
 *
 * 1) Thread count and parallel mode are specified at the suite level
 * 2) The thread count is equal to the number of test methods, ensuring that no method should have to wait for any other
 *    method to complete execution
 * 3) There are NO configuration methods
 * 4) All test methods pass
 * 5) NO ordering is specified
 * 6) group-by-instances is NOT set
 * 7) There are no method exclusions
 */
public class ParallelByMethodsTestCase1Scenario1 extends BaseParallelizationTest {

    private static final String SUITE = "SingleTestSuite";
    private static final String TEST = "SingleTestClassTest";

    private List<EventLog> suiteLevelEventLogs;
    private List<EventLog> testLevelEventLogs;
    private List<EventLog> suiteAndTestLevelEventLogs;
    private List<EventLog> testMethodLevelEventLogs;

    private EventLog suiteListenerOnStartEventLog;
    private EventLog suiteListenerOnFinishEventLog;

    private EventLog testListenerOnStartEventLog;
    private EventLog testListenerOnFinishEventLog;

    private Long testListenerOnStartThreadId;

    @BeforeClass
    public void setUp() {
        reset();

        XmlSuite suite = createXmlSuite(SUITE);
        suite.setParallel(XmlSuite.ParallelMode.METHODS);
        suite.setThreadCount(5);

        createXmlTest(suite, TEST, TestClassAFiveMethodsWithNoDepsSample.class);

        addParams(suite, SUITE, TEST, "100");

        TestNG tng = create(suite);
        tng.addListener((ITestNGListener)new TestNgRunStateListener());

        System.out.println("Beginning ParallelByMethodsTestCase1Scenario1. This test scenario consists of a " +
                "single suite with a single test which consists of one test class with five test methods. There " +
                "are no dependencies, data providers or factories.");
        System.out.println("Suite: " + SUITE + ", Test: " + TEST + ", Test class: "
                + TestClassAFiveMethodsWithNoDepsSample.class.getCanonicalName() +". Thread count: 5");

        tng.run();

        suiteLevelEventLogs = getAllSuiteLevelEventLogs();
        testLevelEventLogs = getAllTestLevelEventLogs();
        suiteAndTestLevelEventLogs = getAllSuiteAndTestLevelEventLogs();
        testMethodLevelEventLogs = getAllTestMethodLevelEventLogs();

        suiteListenerOnStartEventLog = getSuiteListenerStartEventLog(SUITE);
        suiteListenerOnFinishEventLog = getSuiteListenerFinishEventLog(SUITE);

        testListenerOnStartEventLog = getTestListenerStartEventLog(SUITE, TEST);
        testListenerOnFinishEventLog = getTestListenerFinishEventLog(SUITE, TEST);

        testListenerOnStartThreadId = getTestListenerStartThreadId(SUITE, TEST);
    }

    //Verifies that the expected number of suite, test and test method level events were logged.
    @Test
    public void sanityCheck() {
        assertEquals(suiteLevelEventLogs.size(), 2, "There should be 2 suite level events logged for " + SUITE + ": " +
                suiteLevelEventLogs);
        assertEquals(testLevelEventLogs.size(), 2, "There should be 2 test level events logged for " + SUITE + ": " +
                testLevelEventLogs);
        assertEquals(testMethodLevelEventLogs.size(), 15, "There should be 15 test method level event logged for " +
                SUITE + ": " + testMethodLevelEventLogs);
    }

    //Verify that the suite listener and test listener events have timestamps in the following order: suite start,
    //test start, test finish, suite finish. Verify that all of these events run in the same thread because the
    //parallelization mode is by methods only.
    @Test
    public void verifySuiteAndTestLevelEventsRunInSequentialOrderInSameThread() {
        verifySameThreadIdForAllEvents(suiteAndTestLevelEventLogs, "The thread ID for all the suite and test level " +
                "event logs should be the same because there is no parallelism specified at the suite or test level: " +
                suiteAndTestLevelEventLogs);
        verifySequentialTimingOfEvents(suiteAndTestLevelEventLogs, "The timestamps of suite and test level events " +
                "logged first should be earlier than those which are logged afterwards because there is no " +
                "parallelism specified at the suite or test level: " + suiteAndTestLevelEventLogs);
        verifyEventsOccurBetween(suiteListenerOnStartEventLog, testLevelEventLogs, suiteListenerOnFinishEventLog,
                "All of the test level event logs should have timestamps between the suite listener's onStart and " +
                        "onFinish event logs. Suite listener onStart event log: " + suiteListenerOnStartEventLog +
                        ". Suite listener onFinish event log: " + suiteListenerOnFinishEventLog + ". Test level " +
                        "event logs: " + testLevelEventLogs);
    }

    //Verify that there is only a single test class instance associated with each of the test methods from the
    //sample test class
    //Verify that the same test class instance is associated with each of the test methods from the sample test class
    @Test
    public void verifyOnlyOneInstanceOfTestClassForAllTestMethods() {

        verifyNumberOfInstancesOfTestClassForMethods(
                SUITE,
                TEST,
                TestClassAFiveMethodsWithNoDepsSample.class,
                1);

        verifySameInstancesOfTestClassAssociatedWithMethods(
                SUITE,
                TEST,
                TestClassAFiveMethodsWithNoDepsSample.class);
    }

    //Verifies that all the test method level events execute between the test listener onStart and onFinish methods
    @Test
    public void verifyTestMethodLevelEventsAllOccurBetweenTestListenerStartAndFinish() {
        verifyEventsOccurBetween(testListenerOnStartEventLog, testMethodLevelEventLogs, testListenerOnFinishEventLog,
                "All of the test method level event logs should have timestamps between the test listener's onStart " +
                        "and onFinish event logs. Test Listener onStart event log: " + testListenerOnStartEventLog +
                        ". Test Listener onFinish event log: " + testListenerOnFinishEventLog + ". Test method level " +
                        "event logs: " + testMethodLevelEventLogs);
    }

    //Verifies that the method level events all run in different threads from the test and suite level events.
    @Test
    public void verifyThatMethodLevelEventsRunInDifferentThreadsFromSuiteAndTestLevelEvents() {
        verifyEventThreadsSpawnedAfter(testListenerOnStartThreadId, testMethodLevelEventLogs, "All the thread IDs " +
                "for the test method level events should be greater than the thread ID for the suite and test level " +
                "events. The expectation is that since the suite and test level events are running sequentially, and " +
                "all the test methods are running in parallel, new threads will be spawned after the thread " +
                "executing the suite and test level events when new methods begin executing. Suite and test level " +
                "events thread ID: " + testListenerOnStartThreadId + ". Test method level event logs: " +
                testMethodLevelEventLogs);
    }

    //Verifies that the test methods execute in different threads in parallel fashion.
    @Test
    public void verifyThatTestMethodsRunInParallelThreads() {
        verifySimultaneousTestMethods(testMethodLevelEventLogs, TEST, 5);
    }

    //Verifies that all the test method level events for any given test method run in the same thread.
    @Test
    public void verifyThatAllEventsForATestMethodExecuteInSameThread() {
        verifyEventsForTestMethodsRunInTheSameThread(TestClassAFiveMethodsWithNoDepsSample.class, SUITE, TEST);
    }
}
