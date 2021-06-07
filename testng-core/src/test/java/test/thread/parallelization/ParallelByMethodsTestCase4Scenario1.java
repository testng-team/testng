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
import static test.thread.parallelization.TestNgRunStateTracker.getTestLevelEventLogsForTest;
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
import org.testng.xml.XmlTest;
import test.thread.parallelization.sample.TestClassAFiveMethodsWithDataProviderOnAllMethodsAndNoDepsSample;
import test.thread.parallelization.sample.TestClassAFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample;
import test.thread.parallelization.sample.TestClassBFourMethodsWithDataProviderOnAllMethodsAndNoDepsSample;
import test.thread.parallelization.sample.TestClassBSixMethodsWithDataProviderOnAllMethodsAndNoDepsSample;
import test.thread.parallelization.sample.TestClassBSixMethodsWithDataProviderOnSomeMethodsAndNoDepsSample;
import test.thread.parallelization.sample.TestClassCFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample;
import test.thread.parallelization.sample.TestClassDThreeMethodsWithDataProviderOnAllMethodsAndNoDepsSample;
import test.thread.parallelization.sample.TestClassDThreeMethodsWithDataProviderOnSomeMethodsAndNoDepsSample;
import test.thread.parallelization.sample.TestClassEFourMethodsWithDataProviderOnSomeMethodsAndNoDepsSample;
import test.thread.parallelization.sample.TestClassFSixMethodsWithDataProviderOnSomeMethodsAndNoDepsSample;
import test.thread.parallelization.sample.TestClassGFourMethodsWithDataProviderOnSomeMethodsAndNoDepsSample;
import test.thread.parallelization.sample.TestClassHFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample;
import test.thread.parallelization.sample.TestClassIThreeMethodsWithDataProviderOnSomeMethodsAndNoDepsSample;
import test.thread.parallelization.sample.TestClassJFourMethodsWithDataProviderOnSomeMethodsAndNoDepsSample;
import test.thread.parallelization.sample.TestClassKFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample;

/**
 * This class covers PTP_TC_4, Scenario 1 in the Parallelization Test Plan.
 *
 * <p>Test Case Summary: Parallel by methods mode with parallel test suites using a non-parallel
 * data provider but no dependencies and no factories.
 *
 * <p>Scenario Description: Three suites with 1, 2 and 3 tests respectively. One test for a suite
 * shall consist of a single test class while the rest shall consist of more than one test class.
 * Some test classes have a mixture of some methods that use a data provider and some which do not.
 * The data providers provide data sets of varying sizes.
 *
 * <p>1) The suite thread pool is 2, so one suite will have to wait for one of the others to
 * complete execution before it can begin execution 2) For one of the suites, the thread count and
 * parallel mode are specified at the suite level 3) For one of the suites, the thread count and
 * parallel mode are specified at the test level 4) For one of the suites, the parallel mode is
 * specified at the suite level, and the thread counts are specified at the test level (thread
 * counts for each test differ) 5) The thread count is less than the number of test methods for the
 * tests in two of the suites, so some methods will have to wait the active thread count to drop
 * below the maximum thread count before they can begin execution. The expectation is that a thread
 * for a method does not terminate until the method has been invoked for all sets of data if it uses
 * a data provider. 6) The thread count is more than the number of test methods for the tests in one
 * of the suites, ensuring that none of the methods in that suite should have to wait for any other
 * method to complete execution. The expectation is that a thread for a method does not terminate
 * until the method has been invoked for all sets of data if it uses a data provider. 7) There are
 * NO configuration methods 8) All test methods pass 9) NO ordering is specified 10)
 * group-by-instances is NOT set 11) There are no method exclusions
 */
public class ParallelByMethodsTestCase4Scenario1 extends BaseParallelizationTest {
  private static final Logger log = Logger.getLogger(ParallelByMethodsTestCase4Scenario1.class);

  private static final String SUITE_A = "TestSuiteA";
  private static final String SUITE_B = "TestSuiteB";
  private static final String SUITE_C = "TestSuiteC";

  private static final String SUITE_A_TEST_A = "TestSuiteA-TwoTestClassTest";

  private static final String SUITE_B_TEST_A = "TestSuiteB-SingleTestClassTest";
  private static final String SUITE_B_TEST_B = "TestSuiteB-ThreeTestClassTest";

  private static final String SUITE_C_TEST_A = "TestSuiteC-ThreeTestClassTest";
  private static final String SUITE_C_TEST_B = "TestSuiteC-TwoTestClassTest";
  private static final String SUITE_C_TEST_C = "TestSuiteC-FourTestClassTest";

  private static final int THREAD_POOL_SIZE = 2;

  private Map<String, List<TestNgRunStateTracker.EventLog>> suiteEventLogsMap = new HashMap<>();
  private Map<String, Integer> expectedInvocationCounts = new HashMap<>();

  private Map<String, List<TestNgRunStateTracker.EventLog>> testEventLogsMap = new HashMap<>();

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

  private List<TestNgRunStateTracker.EventLog> suiteThreeSuiteAndTestLevelEventLogs;
  private List<TestNgRunStateTracker.EventLog> suiteThreeSuiteLevelEventLogs;
  private List<TestNgRunStateTracker.EventLog> suiteThreeTestLevelEventLogs;
  private List<TestNgRunStateTracker.EventLog> suiteThreeTestMethodLevelEventLogs;

  private List<TestNgRunStateTracker.EventLog> suiteOneTestOneTestMethodLevelEventLogs;

  private List<TestNgRunStateTracker.EventLog> suiteTwoTestOneTestMethodLevelEventLogs;
  private List<TestNgRunStateTracker.EventLog> suiteTwoTestTwoTestMethodLevelEventLogs;

  private List<TestNgRunStateTracker.EventLog> suiteThreeTestOneTestMethodLevelEventLogs;
  private List<TestNgRunStateTracker.EventLog> suiteThreeTestTwoTestMethodLevelEventLogs;
  private List<TestNgRunStateTracker.EventLog> suiteThreeTestThreeTestMethodLevelEventLogs;

  private TestNgRunStateTracker.EventLog suiteOneSuiteListenerOnStartEventLog;
  private TestNgRunStateTracker.EventLog suiteOneSuiteListenerOnFinishEventLog;

  private TestNgRunStateTracker.EventLog suiteTwoSuiteListenerOnStartEventLog;
  private TestNgRunStateTracker.EventLog suiteTwoSuiteListenerOnFinishEventLog;

  private TestNgRunStateTracker.EventLog suiteThreeSuiteListenerOnStartEventLog;
  private TestNgRunStateTracker.EventLog suiteThreeSuiteListenerOnFinishEventLog;

  private TestNgRunStateTracker.EventLog suiteOneTestOneListenerOnStartEventLog;
  private TestNgRunStateTracker.EventLog suiteOneTestOneListenerOnFinishEventLog;

  private TestNgRunStateTracker.EventLog suiteTwoTestOneListenerOnStartEventLog;
  private TestNgRunStateTracker.EventLog suiteTwoTestOneListenerOnFinishEventLog;
  private TestNgRunStateTracker.EventLog suiteTwoTestTwoListenerOnStartEventLog;
  private TestNgRunStateTracker.EventLog suiteTwoTestTwoListenerOnFinishEventLog;

  private TestNgRunStateTracker.EventLog suiteThreeTestOneListenerOnStartEventLog;
  private TestNgRunStateTracker.EventLog suiteThreeTestOneListenerOnFinishEventLog;
  private TestNgRunStateTracker.EventLog suiteThreeTestTwoListenerOnStartEventLog;
  private TestNgRunStateTracker.EventLog suiteThreeTestTwoListenerOnFinishEventLog;
  private TestNgRunStateTracker.EventLog suiteThreeTestThreeListenerOnStartEventLog;
  private TestNgRunStateTracker.EventLog suiteThreeTestThreeListenerOnFinishEventLog;

  @BeforeClass
  public void setUp() {
    reset();

    XmlSuite suiteOne = createXmlSuite(SUITE_A);
    XmlSuite suiteTwo = createXmlSuite(SUITE_B);
    XmlSuite suiteThree = createXmlSuite(SUITE_C);

    suiteOne.setParallel(XmlSuite.ParallelMode.METHODS);
    suiteOne.setThreadCount(3);

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

    suiteTwo.setParallel(XmlSuite.ParallelMode.METHODS);

    for (XmlTest test : suiteTwo.getTests()) {
      if (test.getName().equals(SUITE_B_TEST_A)) {
        test.setThreadCount(6);
      } else {
        test.setThreadCount(20);
      }
    }

    createXmlTest(
        suiteThree,
        SUITE_C_TEST_A,
        TestClassDThreeMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class,
        TestClassGFourMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class,
        TestClassAFiveMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class);
    createXmlTest(
        suiteThree,
        SUITE_C_TEST_B,
        TestClassBFourMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class,
        TestClassHFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class);
    createXmlTest(
        suiteThree,
        SUITE_C_TEST_C,
        TestClassIThreeMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class,
        TestClassJFourMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class,
        TestClassKFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class,
        TestClassBSixMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class);

    for (XmlTest test : suiteThree.getTests()) {
      test.setParallel(XmlSuite.ParallelMode.METHODS);

      switch (test.getName()) {
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

    addParams(suiteOne, SUITE_A, SUITE_A_TEST_A, "100", "paramOne,paramTwo,paramThree");

    addParams(suiteTwo, SUITE_B, SUITE_B_TEST_A, "100", "paramOne,paramTwo");
    addParams(suiteTwo, SUITE_B, SUITE_B_TEST_B, "100", "paramOne,paramTwo");

    addParams(suiteThree, SUITE_C, SUITE_C_TEST_A, "100", "paramOne,paramTwo,paramThree");
    addParams(suiteThree, SUITE_C, SUITE_C_TEST_B, "100", "paramOne,paramTwo");
    addParams(suiteThree, SUITE_C, SUITE_C_TEST_C, "100", "paramOne,paramTwo,paramThree");

    TestNG tng = create(suiteOne, suiteTwo, suiteThree);
    tng.setSuiteThreadPoolSize(2);
    tng.addListener((ITestNGListener) new TestNgRunStateListener());

    log.debug(
        "Beginning ParallelByMethodsTestCase4Scenario1. This test scenario consists of three "
            + "suites with 1, 2 and 3 tests respectively. The suites run in parallel and the thread pool size is "
            + "2. One test for a suite shall consist of a single test class while the rest shall consist of more "
            + "han one test class. Some test classes have a mixture of some methods that use a data provider and "
            + "some which do not. The data providers provide data sets of varying sizes. There are no "
            + "dependencies or factories.");

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
            + ". Thread count: 6");

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
            + ". Thread count: 20");

    log.debug(
        "Suite: "
            + SUITE_C
            + ", Test: "
            + SUITE_C_TEST_A
            + ", Test classes: "
            + TestClassDThreeMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class
                .getCanonicalName()
            + ", "
            + TestClassGFourMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class
                .getCanonicalName()
            + ", "
            + TestClassAFiveMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class
            + ". Thread count: 10");

    log.debug(
        "Suite: "
            + SUITE_C
            + ", Test: "
            + SUITE_C_TEST_B
            + ", Test classes: "
            + TestClassBFourMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class
                .getCanonicalName()
            + ", "
            + TestClassHFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class
            + ". Thread count: 5");

    log.debug(
        "Suite: "
            + SUITE_C
            + ", Test: "
            + SUITE_C_TEST_C
            + ", Test classes: "
            + TestClassIThreeMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class
                .getCanonicalName()
            + ", "
            + TestClassJFourMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class
                .getCanonicalName()
            + ", "
            + TestClassKFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class
                .getCanonicalName()
            + ", "
            + TestClassBSixMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class
                .getCanonicalName()
            + ". Thread count: 12.");

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

    expectedInvocationCounts.put(
        TestClassDThreeMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodA",
        3);
    expectedInvocationCounts.put(
        TestClassDThreeMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodB",
        3);
    expectedInvocationCounts.put(
        TestClassDThreeMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodC",
        3);

    expectedInvocationCounts.put(
        TestClassGFourMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodA",
        3);
    expectedInvocationCounts.put(
        TestClassGFourMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodB",
        3);
    expectedInvocationCounts.put(
        TestClassGFourMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodC",
        1);
    expectedInvocationCounts.put(
        TestClassGFourMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodD",
        1);

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

    expectedInvocationCounts.put(
        TestClassBFourMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodA",
        2);
    expectedInvocationCounts.put(
        TestClassBFourMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodB",
        2);
    expectedInvocationCounts.put(
        TestClassBFourMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodC",
        2);
    expectedInvocationCounts.put(
        TestClassBFourMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodD",
        2);

    expectedInvocationCounts.put(
        TestClassHFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodA",
        2);
    expectedInvocationCounts.put(
        TestClassHFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodB",
        1);
    expectedInvocationCounts.put(
        TestClassHFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodC",
        2);
    expectedInvocationCounts.put(
        TestClassHFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodD",
        1);
    expectedInvocationCounts.put(
        TestClassHFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodE",
        2);

    expectedInvocationCounts.put(
        TestClassIThreeMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodA",
        1);
    expectedInvocationCounts.put(
        TestClassIThreeMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodB",
        3);
    expectedInvocationCounts.put(
        TestClassIThreeMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodC",
        1);

    expectedInvocationCounts.put(
        TestClassJFourMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodA",
        3);
    expectedInvocationCounts.put(
        TestClassJFourMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodB",
        3);
    expectedInvocationCounts.put(
        TestClassJFourMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodC",
        1);
    expectedInvocationCounts.put(
        TestClassJFourMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodD",
        1);

    expectedInvocationCounts.put(
        TestClassKFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodA",
        3);
    expectedInvocationCounts.put(
        TestClassKFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodB",
        1);
    expectedInvocationCounts.put(
        TestClassKFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodC",
        3);
    expectedInvocationCounts.put(
        TestClassKFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodD",
        1);
    expectedInvocationCounts.put(
        TestClassKFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodE",
        3);

    expectedInvocationCounts.put(
        TestClassBSixMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodA",
        3);
    expectedInvocationCounts.put(
        TestClassBSixMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodB",
        3);
    expectedInvocationCounts.put(
        TestClassBSixMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodC",
        3);
    expectedInvocationCounts.put(
        TestClassBSixMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodD",
        3);
    expectedInvocationCounts.put(
        TestClassBSixMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodE",
        3);
    expectedInvocationCounts.put(
        TestClassBSixMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class.getCanonicalName()
            + ".testMethodF",
        3);

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

    suiteOneTestOneTestMethodLevelEventLogs =
        getTestMethodLevelEventLogsForTest(SUITE_A, SUITE_A_TEST_A);

    suiteTwoTestOneTestMethodLevelEventLogs =
        getTestMethodLevelEventLogsForTest(SUITE_B, SUITE_B_TEST_A);
    suiteTwoTestTwoTestMethodLevelEventLogs =
        getTestMethodLevelEventLogsForTest(SUITE_B, SUITE_B_TEST_B);

    suiteThreeTestOneTestMethodLevelEventLogs =
        getTestMethodLevelEventLogsForTest(SUITE_C, SUITE_C_TEST_A);
    suiteThreeTestTwoTestMethodLevelEventLogs =
        getTestMethodLevelEventLogsForTest(SUITE_C, SUITE_C_TEST_B);
    suiteThreeTestThreeTestMethodLevelEventLogs =
        getTestMethodLevelEventLogsForTest(SUITE_C, SUITE_C_TEST_C);

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
    suiteOneTestOneListenerOnFinishEventLog =
        getTestListenerFinishEventLog(SUITE_A, SUITE_A_TEST_A);

    suiteTwoTestOneListenerOnStartEventLog = getTestListenerStartEventLog(SUITE_B, SUITE_B_TEST_A);
    suiteTwoTestOneListenerOnFinishEventLog =
        getTestListenerFinishEventLog(SUITE_B, SUITE_B_TEST_A);

    suiteTwoTestTwoListenerOnStartEventLog = getTestListenerStartEventLog(SUITE_B, SUITE_B_TEST_B);
    suiteTwoTestTwoListenerOnFinishEventLog =
        getTestListenerFinishEventLog(SUITE_B, SUITE_B_TEST_B);

    suiteThreeTestOneListenerOnStartEventLog =
        getTestListenerStartEventLog(SUITE_C, SUITE_C_TEST_A);
    suiteThreeTestOneListenerOnFinishEventLog =
        getTestListenerFinishEventLog(SUITE_C, SUITE_C_TEST_A);
    suiteThreeTestTwoListenerOnStartEventLog =
        getTestListenerStartEventLog(SUITE_C, SUITE_C_TEST_B);
    suiteThreeTestTwoListenerOnFinishEventLog =
        getTestListenerFinishEventLog(SUITE_C, SUITE_C_TEST_B);
    suiteThreeTestThreeListenerOnStartEventLog =
        getTestListenerStartEventLog(SUITE_C, SUITE_C_TEST_C);
    suiteThreeTestThreeListenerOnFinishEventLog =
        getTestListenerFinishEventLog(SUITE_C, SUITE_C_TEST_C);
  }

  // Verifies that the expected number of suite, test and test method level events were logged for
  // each of the three
  // suites.
  @Test
  public void sanityCheck() {
    assertEquals(
        suiteLevelEventLogs.size(),
        6,
        "There should be 6 suite level events logged for "
            + SUITE_A
            + ", "
            + SUITE_B
            + " and "
            + SUITE_C
            + ": "
            + suiteLevelEventLogs);
    assertEquals(
        testLevelEventLogs.size(),
        12,
        "There should be 12 test level events logged for "
            + SUITE_A
            + ", "
            + SUITE_B
            + " and "
            + SUITE_C
            + ": "
            + testLevelEventLogs);

    assertEquals(
        testMethodLevelEventLogs.size(),
        420,
        "There should 420 test method level events logged for "
            + SUITE_A
            + ", "
            + SUITE_B
            + " and "
            + SUITE_C
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
        "There should be 81 test method level events "
            + "logged for "
            + SUITE_B
            + ": "
            + suiteTwoTestMethodLevelEventLogs);

    assertEquals(
        suiteThreeSuiteLevelEventLogs.size(),
        2,
        "There should be 2 suite level events logged for "
            + SUITE_C
            + ": "
            + suiteThreeSuiteLevelEventLogs);
    assertEquals(
        suiteThreeTestLevelEventLogs.size(),
        6,
        "There should be 6 test level events logged for "
            + SUITE_C
            + ": "
            + suiteThreeTestLevelEventLogs);
    assertEquals(
        suiteThreeTestMethodLevelEventLogs.size(),
        270,
        "There should be 270 test method level events "
            + "logged for "
            + SUITE_C
            + ": "
            + suiteThreeTestMethodLevelEventLogs);
  }

  // Verify that the suites run in parallel by checking that the suite and test level events for
  // both suites have
  // overlapping timestamps. Verify that there are two separate threads executing the suite-level
  // and test-level
  // events for each suite.
  @Test
  public void verifyThatSuitesRunInParallelThreads() {
    verifyParallelSuitesWithUnequalExecutionTimes(suiteLevelEventLogs, THREAD_POOL_SIZE);
  }

  @Test
  public void verifyTestLevelEventsRunInSequentialOrderForIndividualSuites() {
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

    verifySequentialTests(
        suiteThreeSuiteAndTestLevelEventLogs,
        suiteThreeTestLevelEventLogs,
        suiteThreeSuiteListenerOnStartEventLog,
        suiteThreeSuiteListenerOnFinishEventLog);
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

    verifyNumberOfInstancesOfTestClassesForMethods(
        SUITE_C,
        SUITE_C_TEST_A,
        Arrays.asList(
            TestClassDThreeMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class,
            TestClassGFourMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class,
            TestClassAFiveMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class),
        1);

    verifySameInstancesOfTestClassesAssociatedWithMethods(
        SUITE_C,
        SUITE_C_TEST_A,
        Arrays.asList(
            TestClassDThreeMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class,
            TestClassGFourMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class,
            TestClassAFiveMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class));

    verifyNumberOfInstancesOfTestClassesForMethods(
        SUITE_C,
        SUITE_C_TEST_B,
        Arrays.asList(
            TestClassBFourMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class,
            TestClassHFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class),
        1);

    verifySameInstancesOfTestClassesAssociatedWithMethods(
        SUITE_C,
        SUITE_C_TEST_B,
        Arrays.asList(
            TestClassBFourMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class,
            TestClassHFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class));

    verifyNumberOfInstancesOfTestClassesForMethods(
        SUITE_C,
        SUITE_C_TEST_C,
        Arrays.asList(
            TestClassIThreeMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class,
            TestClassJFourMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class,
            TestClassKFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class,
            TestClassBSixMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class),
        1);

    verifySameInstancesOfTestClassesAssociatedWithMethods(
        SUITE_C,
        SUITE_C_TEST_C,
        Arrays.asList(
            TestClassIThreeMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class,
            TestClassJFourMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class,
            TestClassKFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class,
            TestClassBSixMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class));
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

    verifyEventsOccurBetween(
        suiteThreeTestOneListenerOnStartEventLog,
        suiteThreeTestOneTestMethodLevelEventLogs,
        suiteThreeTestOneListenerOnFinishEventLog,
        "All of the test method level event logs for "
            + SUITE_C_TEST_A
            + " should have timestamps between the test listener's onStart and onFinish "
            + "event logs for "
            + SUITE_C_TEST_A
            + ". Test listener onStart event log: "
            + suiteThreeTestOneListenerOnStartEventLog
            + ". Test listener onFinish event log: "
            + suiteThreeTestOneListenerOnFinishEventLog
            + ". Test method level event logs: "
            + suiteThreeTestOneTestMethodLevelEventLogs);

    verifyEventsOccurBetween(
        suiteThreeTestTwoListenerOnStartEventLog,
        suiteThreeTestTwoTestMethodLevelEventLogs,
        suiteThreeTestTwoListenerOnFinishEventLog,
        "All of the test method level event logs for "
            + SUITE_C_TEST_B
            + " should have timestamps between the test listener's onStart and onFinish "
            + "event logs for "
            + SUITE_C_TEST_B
            + ". Test listener onStart event log: "
            + suiteThreeTestTwoListenerOnStartEventLog
            + ". Test listener onFinish event log: "
            + suiteThreeTestTwoListenerOnFinishEventLog
            + ". Test method level event logs: "
            + suiteThreeTestTwoTestMethodLevelEventLogs);

    verifyEventsOccurBetween(
        suiteThreeTestThreeListenerOnStartEventLog,
        suiteThreeTestThreeTestMethodLevelEventLogs,
        suiteThreeTestThreeListenerOnFinishEventLog,
        "All of the test method level event logs for "
            + SUITE_C_TEST_C
            + " should have timestamps between the test listener's onStart and onFinish "
            + "event logs for "
            + SUITE_C_TEST_C
            + ". Test listener onStart event log: "
            + suiteThreeTestThreeListenerOnStartEventLog
            + ". Test listener onFinish event log: "
            + suiteThreeTestThreeListenerOnFinishEventLog
            + ". Test method level event logs: "
            + suiteThreeTestThreeTestMethodLevelEventLogs);
  }

  @Test
  public void verifyThatMethodLevelEventsRunInDifferentThreadsFromSuiteAndTestLevelEvents() {

    verifyEventThreadsSpawnedAfter(
        getAllSuiteListenerStartEventLogs().get(0).getThreadId(),
        testMethodLevelEventLogs,
        "All the thread IDs for the test method level events should be greater "
            + "than the thread ID for the suite and test level events. The expectation is that since the "
            + "suite and test level events are running sequentially, and all the test methods are running "
            + "in parallel, new threads will be spawned after the thread executing the suite and test "
            + "level events when new methods begin executing. Suite and test level events thread ID: "
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

    verifyEventsForTestMethodsRunInTheSameThread(
        TestClassDThreeMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class,
        SUITE_C,
        SUITE_C_TEST_A);

    verifyEventsForTestMethodsRunInTheSameThread(
        TestClassGFourMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class,
        SUITE_C,
        SUITE_C_TEST_A);

    verifyEventsForTestMethodsRunInTheSameThread(
        TestClassBFourMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class,
        SUITE_C,
        SUITE_C_TEST_B);

    verifyEventsForTestMethodsRunInTheSameThread(
        TestClassHFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class,
        SUITE_C,
        SUITE_C_TEST_B);

    verifyEventsForTestMethodsRunInTheSameThread(
        TestClassIThreeMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class,
        SUITE_C,
        SUITE_C_TEST_C);

    verifyEventsForTestMethodsRunInTheSameThread(
        TestClassJFourMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class,
        SUITE_C,
        SUITE_C_TEST_C);

    verifyEventsForTestMethodsRunInTheSameThread(
        TestClassKFiveMethodsWithDataProviderOnSomeMethodsAndNoDepsSample.class,
        SUITE_C,
        SUITE_C_TEST_C);

    verifyEventsForTestMethodsRunInTheSameThread(
        TestClassBSixMethodsWithDataProviderOnAllMethodsAndNoDepsSample.class,
        SUITE_C,
        SUITE_C_TEST_C);
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
        6);

    verifyParallelTestMethodsWithNonParallelDataProvider(
        getTestMethodLevelEventLogsForTest(SUITE_B, SUITE_B_TEST_B),
        SUITE_B_TEST_B,
        expectedInvocationCounts,
        13,
        20);

    verifyParallelTestMethodsWithNonParallelDataProvider(
        getTestMethodLevelEventLogsForTest(SUITE_C, SUITE_C_TEST_A),
        SUITE_C_TEST_A,
        expectedInvocationCounts,
        12,
        10);

    verifyParallelTestMethodsWithNonParallelDataProvider(
        getTestMethodLevelEventLogsForTest(SUITE_C, SUITE_C_TEST_B),
        SUITE_C_TEST_B,
        expectedInvocationCounts,
        9,
        5);

    verifyParallelTestMethodsWithNonParallelDataProvider(
        getTestMethodLevelEventLogsForTest(SUITE_C, SUITE_C_TEST_C),
        SUITE_C_TEST_C,
        expectedInvocationCounts,
        18,
        12);
  }
}
