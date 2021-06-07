package test.thread.parallelization;

import static org.testng.Assert.assertEquals;
import static test.thread.parallelization.TestNgRunStateTracker.getAllEventLogsForSuite;
import static test.thread.parallelization.TestNgRunStateTracker.getAllSuiteLevelEventLogs;
import static test.thread.parallelization.TestNgRunStateTracker.getAllSuiteListenerStartEventLogs;
import static test.thread.parallelization.TestNgRunStateTracker.getAllTestLevelEventLogs;
import static test.thread.parallelization.TestNgRunStateTracker.getAllTestMethodLevelEventLogs;
import static test.thread.parallelization.TestNgRunStateTracker.getSuiteAndTestLevelEventLogsForSuite;
import static test.thread.parallelization.TestNgRunStateTracker.getSuiteLevelEventLogsForSuite;
import static test.thread.parallelization.TestNgRunStateTracker.getSuiteListenerFinishEventLog;
import static test.thread.parallelization.TestNgRunStateTracker.getSuiteListenerStartEventLog;
import static test.thread.parallelization.TestNgRunStateTracker.getTestLevelEventLogsForSuite;
import static test.thread.parallelization.TestNgRunStateTracker.getTestListenerFinishEventLog;
import static test.thread.parallelization.TestNgRunStateTracker.getTestListenerStartEventLog;
import static test.thread.parallelization.TestNgRunStateTracker.getTestMethodLevelEventLogsForSuite;
import static test.thread.parallelization.TestNgRunStateTracker.getTestMethodLevelEventLogsForTest;
import static test.thread.parallelization.TestNgRunStateTracker.reset;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.log4testng.Logger;
import org.testng.xml.XmlSuite;
import test.thread.parallelization.sample.TestClassAFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample;
import test.thread.parallelization.sample.TestClassBSixMethodsWithDataProviderOnSomeMethodsAndNoDepsSample;
import test.thread.parallelization.sample.TestClassCFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample;
import test.thread.parallelization.sample.TestClassDThreeMethodsWithDataProviderOnSomeMethodsAndNoDepsSample;
import test.thread.parallelization.sample.TestClassEFourMethodsWithDataProviderOnSomeMethodsAndNoDepsSample;
import test.thread.parallelization.sample.TestClassFSixMethodsWithDataProviderOnSomeMethodsAndNoDepsSample;

/**
 * This class covers PTP_TC_3, Scenario 2 in the Parallelization Test Plan.
 *
 * <p>Test Case Summary: Parallel by methods mode with sequential test suites using a non-parallel
 * data provider but no dependencies and no factories.
 *
 * <p>Scenario Description: Two suites with 1 and 2 tests respectively. One test for a suite shall
 * consist of a single test class while the rest shall consist of more than one test class. Each
 * test class has some methods with use a data provider and some which do not. Two data providers
 * are used: one which provides two sets of data, one which provide three sets of data.
 *
 * <p>1) For both suites, the thread count and parallel mode are specified at the suite level 2) The
 * thread count is less than the number of test methods for all tests in both suites, so methods
 * will have to wait the active thread count to drop below the maximum thread count before they can
 * begin execution. The expectation is that a thread for a method does not terminate until the
 * method has been invoked for all sets of data if it uses a data provider. 3) There are NO
 * configuration methods 4) All test methods pass 5) NO ordering is specified 6) group-by-instances
 * is NOT set 7) There are no method exclusions
 */
public class ParallelByMethodsTestCase3Scenario2 extends BaseParallelizationTest {
  private static final Logger log = Logger.getLogger(ParallelByMethodsTestCase3Scenario2.class);

  private static final String SUITE_A = "TestSuiteA";
  private static final String SUITE_B = "TestSuiteB";

  private static final String SUITE_A_TEST_A = "TestSuiteA-TwoTestClassTest";

  private static final String SUITE_B_TEST_A = "TestSuiteB-SingleTestClassTest";
  private static final String SUITE_B_TEST_B = "TestSuiteB-ThreeTestClassTest";

  private Map<String, List<TestNgRunStateTracker.EventLog>> suiteEventLogsMap = new HashMap<>();
  private Map<String, Integer> expectedInvocationCounts = new HashMap<>();

  private List<TestNgRunStateTracker.EventLog> suiteLevelEventLogs;
  private List<TestNgRunStateTracker.EventLog> testLevelEventLogs;
  private List<TestNgRunStateTracker.EventLog> testMethodLevelEventLogs;

  private List<TestNgRunStateTracker.EventLog> suiteOneSuiteAndTestLevelEventLogs;
  private List<TestNgRunStateTracker.EventLog> suiteOneSuiteLevelEventLogs;
  private List<TestNgRunStateTracker.EventLog> suiteOneTestLevelEventLogs;
  private List<TestNgRunStateTracker.EventLog> suiteOneTestMethodLevelEventLogs;

  private List<TestNgRunStateTracker.EventLog> suiteTwoSuiteAndTestLevelEventLogs;
  private List<TestNgRunStateTracker.EventLog> suiteTwoSuiteLevelEventLogs;
  private List<TestNgRunStateTracker.EventLog> suiteTwoTestLevelEventLogs;
  private List<TestNgRunStateTracker.EventLog> suiteTwoTestMethodLevelEventLogs;

  private List<TestNgRunStateTracker.EventLog> suiteOneTestOneTestMethodLevelEventLogs;

  private List<TestNgRunStateTracker.EventLog> suiteTwoTestOneTestMethodLevelEventLogs;
  private List<TestNgRunStateTracker.EventLog> suiteTwoTestTwoTestMethodLevelEventLogs;

  private TestNgRunStateTracker.EventLog suiteOneSuiteListenerOnStartEventLog;
  private TestNgRunStateTracker.EventLog suiteOneSuiteListenerOnFinishEventLog;

  private TestNgRunStateTracker.EventLog suiteTwoSuiteListenerOnStartEventLog;
  private TestNgRunStateTracker.EventLog suiteTwoSuiteListenerOnFinishEventLog;

  private TestNgRunStateTracker.EventLog suiteOneTestOneListenerOnStartEventLog;
  private TestNgRunStateTracker.EventLog suiteOneTestOneListenerOnFinishEventLog;

  private TestNgRunStateTracker.EventLog suiteTwoTestOneListenerOnStartEventLog;
  private TestNgRunStateTracker.EventLog suiteTwoTestOneListenerOnFinishEventLog;
  private TestNgRunStateTracker.EventLog suiteTwoTestTwoListenerOnStartEventLog;
  private TestNgRunStateTracker.EventLog suiteTwoTestTwoListenerOnFinishEventLog;

  @BeforeClass
  public void setUp() {
    reset();

    XmlSuite suiteOne = createXmlSuite(SUITE_A);
    XmlSuite suiteTwo = createXmlSuite(SUITE_B);

    createXmlTest(
        suiteOne,
        SUITE_A_TEST_A,
        TestClassAFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class,
        TestClassBSixMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class);

    createXmlTest(
        suiteTwo,
        SUITE_B_TEST_A,
        TestClassCFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class);
    createXmlTest(
        suiteTwo,
        SUITE_B_TEST_B,
        TestClassDThreeMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class,
        TestClassEFourMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class,
        TestClassFSixMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class);

    suiteOne.setParallel(XmlSuite.ParallelMode.METHODS);
    suiteOne.setThreadCount(3);

    suiteTwo.setParallel(XmlSuite.ParallelMode.METHODS);
    suiteTwo.setThreadCount(4);

    addParams(suiteOne, SUITE_A, SUITE_A_TEST_A, "100", "paramOne,paramTwo,paramThree");

    addParams(suiteTwo, SUITE_B, SUITE_B_TEST_A, "100", "paramOne,paramTwo");
    addParams(suiteTwo, SUITE_B, SUITE_B_TEST_B, "100", "paramOne,paramTwo");

    TestNG tng = create(suiteOne, suiteTwo);
    tng.addListener((ITestNGListener) new TestNgRunStateListener());

    log.debug(
        "Beginning ParallelByMethodsTestCase3Scenario2. This test scenario consists of two "
            + "suites with 1 and 2 tests respectively. One test for a suite shall consist of a single test class "
            + "while the rest shall consist of more than one test class. Each test class has some methods with use "
            + "a data provider and some which do not. Two data providers are used: one which provides two sets of "
            + "data, one which provide three sets of data. There are no dependencies or factories.");

    log.debug(
        "Suite: "
            + SUITE_A
            + ", Test: "
            + SUITE_A_TEST_A
            + ", Test classes: "
            + TestClassAFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class
                .getCanonicalName()
            + ", "
            + TestClassBSixMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class
                .getCanonicalName()
            + ". Thread count: 3");

    log.debug(
        "Suite: "
            + SUITE_B
            + ", Test: "
            + SUITE_B_TEST_A
            + ", Test class: "
            + TestClassCFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class
                .getCanonicalName()
            + ". Thread count: 4");

    log.debug(
        "Suite: "
            + SUITE_B
            + ", Test: "
            + SUITE_B_TEST_B
            + ", Test classes: "
            + TestClassDThreeMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class
            + ", "
            + TestClassEFourMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class
            + ", "
            + TestClassFSixMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class
            + ". Thread count: 4");

    tng.run();

    expectedInvocationCounts.put(
        TestClassAFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodA",
        3);
    expectedInvocationCounts.put(
        TestClassAFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodB",
        1);
    expectedInvocationCounts.put(
        TestClassAFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodC",
        3);
    expectedInvocationCounts.put(
        TestClassAFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodD",
        1);
    expectedInvocationCounts.put(
        TestClassAFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodE",
        3);

    expectedInvocationCounts.put(
        TestClassBSixMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodA",
        1);
    expectedInvocationCounts.put(
        TestClassBSixMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodB",
        3);
    expectedInvocationCounts.put(
        TestClassBSixMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodC",
        1);
    expectedInvocationCounts.put(
        TestClassBSixMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodD",
        3);
    expectedInvocationCounts.put(
        TestClassBSixMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodE",
        1);
    expectedInvocationCounts.put(
        TestClassBSixMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodF",
        3);

    expectedInvocationCounts.put(
        TestClassCFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodA",
        2);
    expectedInvocationCounts.put(
        TestClassCFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodB",
        1);
    expectedInvocationCounts.put(
        TestClassCFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodC",
        2);
    expectedInvocationCounts.put(
        TestClassCFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodD",
        1);
    expectedInvocationCounts.put(
        TestClassCFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodE",
        2);

    expectedInvocationCounts.put(
        TestClassDThreeMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodA",
        1);
    expectedInvocationCounts.put(
        TestClassDThreeMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodB",
        2);
    expectedInvocationCounts.put(
        TestClassDThreeMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodC",
        1);

    expectedInvocationCounts.put(
        TestClassEFourMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodA",
        2);
    expectedInvocationCounts.put(
        TestClassEFourMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodB",
        2);
    expectedInvocationCounts.put(
        TestClassEFourMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodC",
        1);
    expectedInvocationCounts.put(
        TestClassEFourMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodD",
        1);

    expectedInvocationCounts.put(
        TestClassFSixMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodA",
        1);
    expectedInvocationCounts.put(
        TestClassFSixMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodB",
        2);
    expectedInvocationCounts.put(
        TestClassFSixMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodC",
        1);
    expectedInvocationCounts.put(
        TestClassFSixMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodD",
        2);
    expectedInvocationCounts.put(
        TestClassFSixMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodE",
        1);
    expectedInvocationCounts.put(
        TestClassFSixMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodF",
        2);

    suiteLevelEventLogs = getAllSuiteLevelEventLogs();
    testLevelEventLogs = getAllTestLevelEventLogs();
    testMethodLevelEventLogs = getAllTestMethodLevelEventLogs();

    suiteOneSuiteAndTestLevelEventLogs = getSuiteAndTestLevelEventLogsForSuite(SUITE_A);
    suiteOneSuiteLevelEventLogs = getSuiteLevelEventLogsForSuite(SUITE_A);
    suiteOneTestLevelEventLogs = getTestLevelEventLogsForSuite(SUITE_A);

    suiteTwoSuiteAndTestLevelEventLogs = getSuiteAndTestLevelEventLogsForSuite(SUITE_B);
    suiteTwoSuiteLevelEventLogs = getSuiteLevelEventLogsForSuite(SUITE_B);
    suiteTwoTestLevelEventLogs = getTestLevelEventLogsForSuite(SUITE_B);

    suiteEventLogsMap.put(SUITE_A, getAllEventLogsForSuite(SUITE_A));
    suiteEventLogsMap.put(SUITE_B, getAllEventLogsForSuite(SUITE_B));

    suiteOneTestMethodLevelEventLogs = getTestMethodLevelEventLogsForSuite(SUITE_A);
    suiteTwoTestMethodLevelEventLogs = getTestMethodLevelEventLogsForSuite(SUITE_B);

    suiteOneTestOneTestMethodLevelEventLogs =
        getTestMethodLevelEventLogsForTest(SUITE_A, SUITE_A_TEST_A);

    suiteTwoTestOneTestMethodLevelEventLogs =
        getTestMethodLevelEventLogsForTest(SUITE_B, SUITE_B_TEST_A);
    suiteTwoTestTwoTestMethodLevelEventLogs =
        getTestMethodLevelEventLogsForTest(SUITE_B, SUITE_B_TEST_B);

    suiteOneSuiteListenerOnStartEventLog = getSuiteListenerStartEventLog(SUITE_A);
    suiteOneSuiteListenerOnFinishEventLog = getSuiteListenerFinishEventLog(SUITE_A);

    suiteTwoSuiteListenerOnStartEventLog = getSuiteListenerStartEventLog(SUITE_B);
    suiteTwoSuiteListenerOnFinishEventLog = getSuiteListenerFinishEventLog(SUITE_B);

    suiteOneTestOneListenerOnStartEventLog = getTestListenerStartEventLog(SUITE_A, SUITE_A_TEST_A);
    suiteOneTestOneListenerOnFinishEventLog =
        getTestListenerFinishEventLog(SUITE_A, SUITE_A_TEST_A);

    suiteTwoTestOneListenerOnStartEventLog = getTestListenerStartEventLog(SUITE_B, SUITE_B_TEST_A);
    suiteTwoTestOneListenerOnFinishEventLog =
        getTestListenerFinishEventLog(SUITE_B, SUITE_B_TEST_A);

    suiteTwoTestTwoListenerOnStartEventLog = getTestListenerStartEventLog(SUITE_B, SUITE_B_TEST_B);
    suiteTwoTestTwoListenerOnFinishEventLog =
        getTestListenerFinishEventLog(SUITE_B, SUITE_B_TEST_B);
  }

  // Verifies that the expected number of suite, test and test method level events were logged for
  // each of the three
  // suites.
  @Test
  public void sanityCheck() {
    assertEquals(
        suiteLevelEventLogs.size(),
        4,
        "There should be 4 suite level events logged for "
            + SUITE_A
            + ", "
            + SUITE_B
            + ": "
            + suiteLevelEventLogs);
    assertEquals(
        testLevelEventLogs.size(),
        6,
        "There should be 6 test level events logged for "
            + SUITE_A
            + ", "
            + SUITE_B
            + ": "
            + testLevelEventLogs);

    assertEquals(
        testMethodLevelEventLogs.size(),
        150,
        "There should 150 test method level events logged for "
            + SUITE_A
            + ", "
            + SUITE_B
            + ": "
            + testMethodLevelEventLogs);

    assertEquals(
        suiteOneSuiteLevelEventLogs.size(),
        2,
        "There should be 2 suite level events logged for "
            + SUITE_A
            + ": "
            + suiteOneSuiteLevelEventLogs);
    assertEquals(
        suiteOneTestLevelEventLogs.size(),
        2,
        "There should be 2 test level events logged for "
            + SUITE_A
            + ": "
            + suiteOneTestLevelEventLogs);
    assertEquals(
        suiteOneTestMethodLevelEventLogs.size(),
        69,
        "There should be 69 test method level events "
            + "logged for "
            + SUITE_A
            + ": "
            + suiteOneTestMethodLevelEventLogs);

    assertEquals(
        suiteTwoSuiteLevelEventLogs.size(),
        2,
        "There should be 2 suite level events logged for "
            + SUITE_B
            + ": "
            + suiteTwoSuiteLevelEventLogs);
    assertEquals(
        suiteTwoTestLevelEventLogs.size(),
        4,
        "There should be 4 test level events logged for "
            + SUITE_B
            + ": "
            + suiteTwoTestLevelEventLogs);
    assertEquals(
        suiteTwoTestMethodLevelEventLogs.size(),
        81,
        "There should be 54 test method level events "
            + "logged for "
            + SUITE_B
            + ": "
            + suiteTwoTestMethodLevelEventLogs);
  }

  // Verify that all the events in the second suite and third suites run have timestamps later than
  // the suite
  // listener's onFinish event for the first suite run.
  // Verify that all the events in the third suite run have timestamps later than the suite
  // listener's onFinish
  // event for the second suite run.
  // Verify that all suite level events run in the same thread
  @Test
  public void verifySuitesRunSequentiallyInSameThread() {
    verifySequentialSuites(suiteLevelEventLogs, suiteEventLogsMap);
  }

  // For all suites, verify that the test level events run sequentially because the parallel mode is
  // by methods only.
  @Test
  public void verifySuiteAndTestLevelEventsRunInSequentialOrderForIndividualSuites() {

    verifySequentialTests(
        suiteOneSuiteAndTestLevelEventLogs,
        suiteOneTestLevelEventLogs,
        suiteOneSuiteListenerOnStartEventLog,
        suiteOneSuiteListenerOnFinishEventLog);

    verifySequentialTests(
        suiteTwoSuiteAndTestLevelEventLogs,
        suiteTwoTestLevelEventLogs,
        suiteTwoSuiteListenerOnStartEventLog,
        suiteTwoSuiteListenerOnFinishEventLog);
  }

  // Verify that there is only a single test class instance associated with each of the test methods
  // from the sample
  // classes for every test in all the suites.
  // Verify that the same test class instance is associated with each of the test methods from the
  // sample test class
  @Test
  public void verifyOnlyOneInstanceOfTestClassForAllTestMethodsForAllSuites() {

    verifyNumberOfInstancesOfTestClassesForMethods(
        SUITE_A,
        SUITE_A_TEST_A,
        Arrays.asList(
            TestClassAFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class,
            TestClassBSixMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class),
        1);

    verifySameInstancesOfTestClassesAssociatedWithMethods(
        SUITE_A,
        SUITE_A_TEST_A,
        Arrays.asList(
            TestClassAFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class,
            TestClassBSixMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class));

    verifyNumberOfInstancesOfTestClassForMethods(
        SUITE_B,
        SUITE_B_TEST_A,
        TestClassCFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class,
        1);

    verifySameInstancesOfTestClassAssociatedWithMethods(
        SUITE_B,
        SUITE_B_TEST_A,
        TestClassCFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class);

    verifyNumberOfInstancesOfTestClassesForMethods(
        SUITE_B,
        SUITE_B_TEST_B,
        Arrays.asList(
            TestClassDThreeMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class,
            TestClassEFourMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class,
            TestClassFSixMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class),
        1);

    verifySameInstancesOfTestClassesAssociatedWithMethods(
        SUITE_B,
        SUITE_B_TEST_B,
        Arrays.asList(
            TestClassDThreeMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class,
            TestClassEFourMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class,
            TestClassFSixMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class));
  }

  // Verify that the test method listener's onTestStart method runs after the test listener's
  // onStart method for
  // all the test methods in all tests and suites.
  @Test
  public void
      verifyTestLevelMethodLevelEventLogsOccurBetweenAfterTestListenerStartAndFinishEventLogs() {
    verifyEventsOccurBetween(
        suiteOneTestOneListenerOnStartEventLog,
        suiteOneTestOneTestMethodLevelEventLogs,
        suiteOneTestOneListenerOnFinishEventLog,
        "All of the test method level event logs for "
            + SUITE_A_TEST_A
            + " should have timestamps between the test listener's onStart and onFinish "
            + "event logs for "
            + SUITE_A_TEST_A
            + ". Test listener onStart event log: "
            + suiteOneTestOneListenerOnStartEventLog
            + ". Test listener onFinish event log: "
            + suiteOneTestOneListenerOnFinishEventLog
            + ". Test method level event logs: "
            + suiteOneTestOneTestMethodLevelEventLogs);

    verifyEventsOccurBetween(
        suiteTwoTestOneListenerOnStartEventLog,
        suiteTwoTestOneTestMethodLevelEventLogs,
        suiteTwoTestOneListenerOnFinishEventLog,
        "All of the test method level event logs for "
            + SUITE_B_TEST_A
            + " should have timestamps between the test listener's onStart and onFinish "
            + "event logs for "
            + SUITE_B_TEST_A
            + ". Test listener onStart event log: "
            + suiteTwoTestOneListenerOnStartEventLog
            + ". Test listener onFinish event log: "
            + suiteTwoTestOneListenerOnFinishEventLog
            + ". Test method level event logs: "
            + suiteTwoTestOneTestMethodLevelEventLogs);

    verifyEventsOccurBetween(
        suiteTwoTestTwoListenerOnStartEventLog,
        suiteTwoTestTwoTestMethodLevelEventLogs,
        suiteTwoTestTwoListenerOnFinishEventLog,
        "All of the test method level event logs for "
            + SUITE_B_TEST_B
            + " should have timestamps between the test listener's onStart and onFinish "
            + "event logs for "
            + SUITE_B_TEST_B
            + ". Test listener onStart event log: "
            + suiteTwoTestTwoListenerOnStartEventLog
            + ". Test listener onFinish event log: "
            + suiteTwoTestTwoListenerOnFinishEventLog
            + ". Test method level event logs: "
            + suiteTwoTestTwoTestMethodLevelEventLogs);
  }

  // Verifies that the method level events all run in different threads from the test and suite
  // level events.
  // Verifies that the test method listener and execution events for a given test method all run in
  // the same thread.
  @Test
  public void verifyThatMethodLevelEventsRunInDifferentThreadsFromSuiteAndTestLevelEvents() {

    verifyEventThreadsSpawnedAfter(
        getAllSuiteListenerStartEventLogs().get(0).getThreadId(),
        testMethodLevelEventLogs,
        "All the thread IDs for the test method level events should be greater than the thread ID for the "
            + "suite and test level events. The expectation is that since the suite and test level events "
            + "are running sequentially, and all the test methods are running in parallel, new threads "
            + "will be spawned after the thread executing the suite and test level events when new methods "
            + "begin executing. Suite and test level events thread ID: "
            + getAllSuiteListenerStartEventLogs().get(0).getThreadId()
            + ". Test method level event logs: "
            + testMethodLevelEventLogs);

    verifyEventsForTestMethodsRunInTheSameThread(
        TestClassAFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class,
        SUITE_A,
        SUITE_A_TEST_A);

    verifyEventsForTestMethodsRunInTheSameThread(
        TestClassBSixMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class,
        SUITE_A,
        SUITE_A_TEST_A);

    verifyEventsForTestMethodsRunInTheSameThread(
        TestClassCFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class,
        SUITE_B,
        SUITE_B_TEST_A);

    verifyEventsForTestMethodsRunInTheSameThread(
        TestClassDThreeMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class,
        SUITE_B,
        SUITE_B_TEST_B);

    verifyEventsForTestMethodsRunInTheSameThread(
        TestClassEFourMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class,
        SUITE_B,
        SUITE_B_TEST_B);

    verifyEventsForTestMethodsRunInTheSameThread(
        TestClassFSixMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class,
        SUITE_B,
        SUITE_B_TEST_B);
  }

  // Verifies that the test methods execute in different threads in parallel fashion.
  @Test
  public void verifyThatTestMethodsRunInParallelThreads() {
    verifyParallelTestMethodsWithNonParallelDataProvider(
        getTestMethodLevelEventLogsForTest(SUITE_A, SUITE_A_TEST_A),
        SUITE_A_TEST_A,
        expectedInvocationCounts,
        11,
        3);

    verifyParallelTestMethodsWithNonParallelDataProvider(
        getTestMethodLevelEventLogsForTest(SUITE_B, SUITE_B_TEST_A),
        SUITE_B_TEST_A,
        expectedInvocationCounts,
        5,
        4);

    verifyParallelTestMethodsWithNonParallelDataProvider(
        getTestMethodLevelEventLogsForTest(SUITE_B, SUITE_B_TEST_B),
        SUITE_B_TEST_B,
        expectedInvocationCounts,
        13,
        4);
  }
}
