package test.thread.parallelization;

import org.testng.ITestNGListener;
import org.testng.TestNG;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.testng.xml.XmlSuite;

import test.thread.parallelization.TestNgRunStateTracker.EventLog;
import test.thread.parallelization.sample.TestClassAFiveMethodsWithFactoryUsingDataProviderAndNoDepsSample;

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

import static test.thread.parallelization.TestNgRunStateTracker.getTestMethodLevelEventLogsForTest;
import static test.thread.parallelization.TestNgRunStateTracker.reset;

/** This class covers PTP_TC_7, Scenario 1 in the Parallelization Test Plan.
 *
 * Test Case Summary: Parallel by methods mode with sequential test suites using a factory which uses a non-parallel
 *                    data provider and there are no dependencies.
 *
 * Scenario Description: Single suite with a single test consisting of a single test class with five methods with a
 *                       factory method using a data provider specifying 3 sets of data.
 *
 * 1) Thread count and parallel mode are specified at the suite level
 * 2) The thread count is equal to the number of test methods times 3, the number of times each method will be invoked
 *    with a data set from the data provider. Expectation is that only 15 threads will be spawned at once. There will
 *    be three instances of the test class, one for each of the three data sets and each instance will have the five
 *    test methods defined on the class, for a total of 15 test method invocations.
 * 3) No test method invocation should be queued because the thread count is sufficient to service all the test method
 *    invocations.
 * 4) There are NO configuration methods
 * 5) All test methods pass
 * 6) NO ordering is specified
 * 7) group-by-instances is NOT set
 * 8) There are no method exclusions
 */
public class ParallelByMethodsTestCase7Scenario1 extends BaseParallelizationTest {

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
        suite.setThreadCount(15);

        createXmlTest(suite, TEST, TestClassAFiveMethodsWithFactoryUsingDataProviderAndNoDepsSample.class);

        addParams(suite, SUITE, TEST, "100", "paramOne,paramTwo,paramThree");

        TestNG tng = create(suite);

        tng.addListener((ITestNGListener)new TestNgRunStateListener());

        System.out.println("Beginning ParallelByMethodsTestCase7Scenario1. This test scenario consists of a " +
                "single suite with a single test consisting of a single test class with five methods with a " +
                "factory method using a data provider specifying 3 sets of data. There are no dependencies.");

        System.out.println("Suite: " + SUITE + ", Test: " + TEST + ", Test class: "
                + TestClassAFiveMethodsWithFactoryUsingDataProviderAndNoDepsSample.class.getCanonicalName() +
                ". Thread count: 15");

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
        assertEquals(testMethodLevelEventLogs.size(), 45, "There should be 45 test method level events logged for " +
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

    //Verify that there are three test class instance associated with each of the test methods from the sample test
    //class
    //Verify that the same test class instances are associated with each of the test methods from the sample test class
    @Test
    public void verifyThreeInstancesOfTestClassForAllTestMethods() {

        verifyNumberOfInstancesOfTestClassForMethods(
                SUITE,
                TEST,
                TestClassAFiveMethodsWithFactoryUsingDataProviderAndNoDepsSample.class,
                3);

        verifySameInstancesOfTestClassAssociatedWithMethods(
                SUITE,
                TEST,
                TestClassAFiveMethodsWithFactoryUsingDataProviderAndNoDepsSample.class);
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

    //Verifies that all the test method level events for any given test method in any given instance run in the same
    //thread.
    @Test
    public void verifyThatAllEventsForATestMethodExecuteInSameThread() {
        verifyEventsForTestMethodsRunInTheSameThread(
                TestClassAFiveMethodsWithFactoryUsingDataProviderAndNoDepsSample.class, SUITE, TEST);
    }

    //Verifies that the test methods execute in different threads in parallel fashion.
    @Test
    public void verifyThatTestMethodsRunInParallelThreads() {
        verifySimultaneousTestMethods(getTestMethodLevelEventLogsForTest(SUITE, TEST), TEST, 15);
    }
}
