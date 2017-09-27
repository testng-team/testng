package test.thread.parallelization;

import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import test.thread.parallelization.sample.FactoryForTestClassAFiveMethodsWithNoDepsTwoInstancesSample;
import test.thread.parallelization.sample.FactoryForTestClassBFourMethodsWithNoDepsFiveInstancesSample;
import test.thread.parallelization.sample.FactoryForTestClassCSixMethodsWithNoDepsThreeInstancesSample;
import test.thread.parallelization.sample.FactoryForTestClassDThreeMethodsWithNoDepsFourInstancesSample;
import test.thread.parallelization.sample.FactoryForTestClassFSixMethodsWithNoDepsSixInstancesSample;

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

/** This class covers PTP_TC_5, Scenario 2 in the Parallelization Test Plan.
 *
 * Test Case Summary: Parallel by methods mode with sequential test suites using a factory but no dependencies and no
 *                    data providers.
 *
 * Scenario Description: Two suites with 1 and 2 tests respectively. One suite with two tests has a test consisting of
 *                       a single test class without a factory while the other consists of factories which provide
 *                       multiple instances of multiple test classes. One suite shall consist of a single test with
 *                       multiple test classes which uses factories.
 *
 * 1) For both suites, the thread count and parallel mode are specified at the suite level
 * 2) The thread count is less than the number of test methods for all tests in both suites, so methods will have to
 *    wait for the active thread count to drop below the maximum thread count before they can begin execution. The
 *    expectation is that threads will be spawned for each test method that executes just as they would if there were
 *    no factories and test suites simply used the default mechanism for instantiating single instances of the test
 *    classes.
 * 3) There are NO configuration methods
 * 4) All test methods pass
 * 5) NO ordering is specified
 * 6) group-by-instances is NOT set
 * 7) There are no method exclusions
 */
public class ParallelByMethodsTestCase5Scenario2 extends BaseParallelizationTest {

    private static final String SUITE_A = "TestSuiteA";
    private static final String SUITE_B = "TestSuiteB";

    private static final String SUITE_A_TEST_A = "TestSuiteA-TwoTestClassTest";

    private static final String SUITE_B_TEST_A = "TestSuiteB-SingleTestClassTest";
    private static final String SUITE_B_TEST_B = "TestSuiteB-ThreeTestClassTest";

    private Map<String, List<TestNgRunStateTracker.EventLog>> suiteEventLogsMap = new HashMap<>();
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

        suiteOne.setParallel(XmlSuite.ParallelMode.METHODS);
        suiteOne.setThreadCount(10);

        createXmlTest(suiteOne, SUITE_A_TEST_A, FactoryForTestClassAFiveMethodsWithNoDepsTwoInstancesSample.class,
                FactoryForTestClassCSixMethodsWithNoDepsThreeInstancesSample.class);
        createXmlTest(suiteTwo, SUITE_B_TEST_A, TestClassEFiveMethodsWithNoDepsSample.class);
        createXmlTest(suiteTwo, SUITE_B_TEST_B, FactoryForTestClassDThreeMethodsWithNoDepsFourInstancesSample.class,
                FactoryForTestClassBFourMethodsWithNoDepsFiveInstancesSample.class,
                FactoryForTestClassFSixMethodsWithNoDepsSixInstancesSample.class);

        suiteTwo.setParallel(XmlSuite.ParallelMode.METHODS);

        for(XmlTest test : suiteTwo.getTests()) {
            if(test.getName().equals(SUITE_B_TEST_A)) {
                test.setThreadCount(3);
            } else {
                test.setThreadCount(20);
            }
        }

        addParams(suiteOne, SUITE_A, SUITE_A_TEST_A, "100");

        addParams(suiteTwo, SUITE_B, SUITE_B_TEST_A, "100");
        addParams(suiteTwo, SUITE_B, SUITE_B_TEST_B, "100");

        TestNG tng = create(suiteOne, suiteTwo);
        tng.addListener((ITestNGListener) new TestNgRunStateListener());

        System.out.println("Beginning ParallelByMethodsTestCase5Scenario2. This test scenario consists of two " +
                "suites with 1 and 2 tests respectively. One suite with two tests has a test consisting of a single " +
                "test class without a factory while the other consists of factories which provide multiple instances " +
                "of multiple test classes. One suite shall consist of a single test with multiple test classes which " +
                "uses factories. There are no dependencies or data providers.");

        System.out.println("Suite: " + SUITE_A + ", Test: " + SUITE_A_TEST_A + ", Test classes: " +
                TestClassAFiveMethodsWithNoDepsSample.class.getCanonicalName() +
                ", " + TestClassCSixMethodsWithNoDepsSample.class.getCanonicalName() + ". Thread count: 10");

        System.out.println("Suite: " + SUITE_B + ", Test: " + SUITE_B_TEST_A + ", Test class: " +
                TestClassEFiveMethodsWithNoDepsSample.class.getCanonicalName() + ". Thread count: 3");

        System.out.println("Suite " + SUITE_B + ", Test: " + SUITE_B_TEST_B + ", Test classes: " +
                TestClassDThreeMethodsWithNoDepsSample.class + ", " +
                TestClassBFourMethodsWithNoDepsSample.class + ", " +
                TestClassFSixMethodsWithNoDepsSample.class + ". Thread count: 20");

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

        suiteEventLogsMap.put(SUITE_A, getAllEventLogsForSuite(SUITE_A));
        suiteEventLogsMap.put(SUITE_B, getAllEventLogsForSuite(SUITE_B));

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
                " and " + SUITE_B + ": " + suiteLevelEventLogs);
        assertEquals(testLevelEventLogs.size(), 6, "There should be 6 test level events logged for " + SUITE_A +
                " and " + SUITE_B + ": " + testLevelEventLogs);

        assertEquals(testMethodLevelEventLogs.size(), 303, "There should 303 test method level events logged for " +
                SUITE_A + " and " + SUITE_B + ": " + testMethodLevelEventLogs);

        assertEquals(suiteOneSuiteLevelEventLogs.size(), 2, "There should be 2 suite level events logged for " +
                SUITE_A + ": " + suiteOneSuiteLevelEventLogs);
        assertEquals(suiteOneTestLevelEventLogs.size(), 2, "There should be 2 test level events logged for " + SUITE_A +
                ": " + suiteOneTestLevelEventLogs);
        assertEquals(suiteOneTestMethodLevelEventLogs.size(), 84, "There should be 84 test method level events " +
                "logged for " + SUITE_A + ": " + suiteOneTestMethodLevelEventLogs);

        assertEquals(suiteTwoSuiteLevelEventLogs.size(), 2, "There should be 2 suite level events logged for " +
                SUITE_B + ": " + suiteTwoSuiteLevelEventLogs);
        assertEquals(suiteTwoTestLevelEventLogs.size(), 4, "There should be 4 test level events logged for " + SUITE_B +
                ": " + suiteTwoTestLevelEventLogs);
        assertEquals(suiteTwoTestMethodLevelEventLogs.size(), 219, "There should be 219 test method level events " +
                "logged for " + SUITE_B + ": " + suiteTwoTestMethodLevelEventLogs);
    }

    //Verify that all the events in the second suite run have timestamps later than the suite listener's onFinish event
    //for the first suite run.
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
    }

    //Verify the expected number of test class instances for the test method events.
    //Verify that the same test class instances are associated with each of the test methods from the sample test class
    @Test
    public void verifyNumberOfInstanceOfTestClassForAllTestMethodsForAllSuites() {

        verifyNumberOfInstancesOfTestClassesForMethods(
                SUITE_A,
                SUITE_A_TEST_A,
                Arrays.asList(TestClassAFiveMethodsWithNoDepsSample.class, TestClassCSixMethodsWithNoDepsSample.class),
                2, 3);

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
                4, 5, 6
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

    //Verify that the methods are run in separate threads.
    @Test
    public void verifyThatTestMethodsRunInParallelThreads() {
        verifySimultaneousTestMethods(getTestMethodLevelEventLogsForTest(SUITE_A, SUITE_A_TEST_A), SUITE_A_TEST_A, 10);
        verifySimultaneousTestMethods(getTestMethodLevelEventLogsForTest(SUITE_B, SUITE_B_TEST_A), SUITE_B_TEST_A, 3);
        verifySimultaneousTestMethods(getTestMethodLevelEventLogsForTest(SUITE_B, SUITE_B_TEST_B), SUITE_B_TEST_B, 20);
    }
}
