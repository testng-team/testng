package test.thread.parallelization;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.log4testng.Logger;
import org.testng.xml.XmlSuite;
import test.thread.parallelization.TestNgRunStateTracker.EventLog;
import test.thread.parallelization.sample.TestClassAFiveMethodsWithDataProviderOnAllMethodsAndNoDepsSample;

/**
 * This class covers PTP_TC_3, Scenario 1 in the Parallelization Test Plan.
 *
 * <p>Test Case Summary: Parallel by methods mode with sequential test suites using a non-parallel
 * data provider but no dependencies and no factories.
 *
 * <p>Scenario Description: Single suite with a single test consisting of a single test class with
 * five methods with a data provider specifying 3 sets of data
 *
 * <p>1) Thread count and parallel mode are specified at the suite level 2) The thread count is
 * equal to the number of test methods times 3, the number of times each method will be invoked with
 * a data set from the data provider. Expectation is that only 5 threads will be spawned, one for
 * each of the methods and that in this thread, the method will be invoked 3 times, once for each
 * set of data from the data provider. 3) There are NO configuration methods 4) All test methods
 * pass 5) NO ordering is specified 6) group-by-instances is NOT set 7) There are no method
 * exclusions
 */
public class ParallelByMethodsTestCase3Scenario1 extends BaseParallelizationTest {
  private static final Logger log = Logger.getLogger(ParallelByMethodsTestCase3Scenario1.class);

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

  private Map<String, Integer> expectedInvocationCounts = new HashMap<>();

  @BeforeClass
  public void setUp() {
    reset();

    XmlSuite suite = createXmlSuite(SUITE);
    suite.setParallel(XmlSuite.ParallelMode.METHODS);
    suite.setThreadCount(15);

    expectedInvocationCounts.put(
        TestClassAFiveMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodA",
        3);

    expectedInvocationCounts.put(
        TestClassAFiveMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodB",
        3);

    expectedInvocationCounts.put(
        TestClassAFiveMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodC",
        3);

    expectedInvocationCounts.put(
        TestClassAFiveMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodD",
        3);

    expectedInvocationCounts.put(
        TestClassAFiveMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodE",
        3);

    createXmlTest(
        suite, TEST, TestClassAFiveMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class);

    addParams(suite, SUITE, TEST, "100", "paramOne,paramTwo,paramThree");

    TestNG tng = create(suite);

    tng.addListener((ITestNGListener) new TestNgRunStateListener());

    log.debug(
        "Beginning ParallelByMethodsTestCase3Scenario1. This test scenario consists of a "
            + "single suite with a single test consisting of a single test class with five methods with a data "
            + "provider specifying 3 sets of data. There are no dependencies or factories.");

    log.debug(
        "Suite: "
            + SUITE
            + ", Test: "
            + TEST
            + ", Test class: "
            + TestClassAFiveMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class
                .getCanonicalName()
            + ". Thread count: 15");

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

  // Verifies that the expected number of suite, test and test method level events were logged.
  @Test
  public void sanityCheck() {
    assertEquals(
        suiteLevelEventLogs.size(),
        2,
        "There should be 2 suite level events logged for " + SUITE + ": " + suiteLevelEventLogs);
    assertEquals(
        testLevelEventLogs.size(),
        2,
        "There should be 2 test level events logged for " + SUITE + ": " + testLevelEventLogs);
    assertEquals(
        testMethodLevelEventLogs.size(),
        45,
        "There should be 45 test method level events logged for "
            + SUITE
            + ": "
            + testMethodLevelEventLogs);
  }

  // Verify that the suite listener and test listener events have timestamps in the following order:
  // suite start,
  // test start, test finish, suite finish. Verify that all of these events run in the same thread
  // because the
  // parallelization mode is by methods only.
  @Test
  public void verifySuiteAndTestLevelEventsRunInSequentialOrderInSameThread() {
    verifySameThreadIdForAllEvents(
        suiteAndTestLevelEventLogs,
        "The thread ID for all the suite and test level "
            + "event logs should be the same because there is no parallelism specified at the suite or test level: "
            + suiteAndTestLevelEventLogs);
    verifySequentialTimingOfEvents(
        suiteAndTestLevelEventLogs,
        "The timestamps of suite and test level events "
            + "logged first should be earlier than those which are logged afterwards because there is no "
            + "parallelism specified at the suite or test level: "
            + suiteAndTestLevelEventLogs);
    verifyEventsOccurBetween(
        suiteListenerOnStartEventLog,
        testLevelEventLogs,
        suiteListenerOnFinishEventLog,
        "All of the test level event logs should have timestamps between the suite listener's onStart and "
            + "onFinish event logs. Suite listener onStart event log: "
            + suiteListenerOnStartEventLog
            + ". Suite listener onFinish event log: "
            + suiteListenerOnFinishEventLog
            + ". Test level "
            + "event logs: "
            + testLevelEventLogs);
  }

  // Verify that there is only a single test class instance associated with each of the test methods
  // from the
  // sample test class
  // Verify that the same test class instance is associated with each of the test methods from the
  // sample test class
  @Test
  public void verifyOnlyOneInstanceOfTestClassForAllTestMethods() {

    verifyNumberOfInstancesOfTestClassForMethods(
        SUITE, TEST, TestClassAFiveMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class, 1);

    verifySameInstancesOfTestClassAssociatedWithMethods(
        SUITE, TEST, TestClassAFiveMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class);
  }

  // Verifies that all the test method level events execute between the test listener onStart and
  // onFinish methods
  @Test
  public void verifyTestMethodLevelEventsAllOccurBetweenTestListenerStartAndFinish() {
    verifyEventsOccurBetween(
        testListenerOnStartEventLog,
        testMethodLevelEventLogs,
        testListenerOnFinishEventLog,
        "All of the test method level event logs should have timestamps between the test listener's onStart "
            + "and onFinish event logs. Test Listener onStart event log: "
            + testListenerOnStartEventLog
            + ". Test Listener onFinish event log: "
            + testListenerOnFinishEventLog
            + ". Test method level "
            + "event logs: "
            + testMethodLevelEventLogs);
  }

  // Verifies that the method level events all run in different threads from the test and suite
  // level events.
  @Test
  public void verifyThatMethodLevelEventsRunInDifferentThreadsFromSuiteAndTestLevelEvents() {
    verifyEventThreadsSpawnedAfter(
        testListenerOnStartThreadId,
        testMethodLevelEventLogs,
        "All the thread IDs "
            + "for the test method level events should be greater than the thread ID for the suite and test level "
            + "events. The expectation is that since the suite and test level events are running sequentially, and "
            + "all the test methods are running in parallel, new threads will be spawned after the thread "
            + "executing the suite and test level events when new methods begin executing. Suite and test level "
            + "events thread ID: "
            + testListenerOnStartThreadId
            + ". Test method level event logs: "
            + testMethodLevelEventLogs);
  }

  // Verifies that the test methods execute in different threads in parallel fashion.
  @Test
  public void verifyThatTestMethodsRunInParallelThreads() {
    verifyParallelTestMethodsWithNonParallelDataProvider(
        getTestMethodLevelEventLogsForTest(SUITE, TEST), TEST, expectedInvocationCounts, 5, 5);
  }

  // Verifies that all the test method level events for any given test method run in the same
  // thread.
  @Test
  public void verifyThatAllEventsForATestMethodExecuteInSameThread() {
    verifyEventsForTestMethodsRunInTheSameThread(
        TestClassAFiveMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class, SUITE, TEST);
  }
}
