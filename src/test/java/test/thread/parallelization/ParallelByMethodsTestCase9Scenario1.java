package test.thread.parallelization;

import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import test.thread.parallelization.sample.TestClassAFiveMethodsWithInterClassGroupDepsSample;
import test.thread.parallelization.sample.TestClassAFiveMethodsWithIntraClassGroupDepsSample;
import test.thread.parallelization.sample.TestClassAFiveMethodsWithMethodOnMethodDependenciesSample;
import test.thread.parallelization.sample.TestClassAFourMethodsWithIntraClassGroupDepsSample;
import test.thread.parallelization.sample.TestClassASevenMethodsWithIntraClassGroupDepsSample;
import test.thread.parallelization.sample.TestClassASixMethodsWithIntraClassGroupDepsSample;
import test.thread.parallelization.sample.TestClassAThreeMethodsWithIntraAndInterClassGroupDepsSample;
import test.thread.parallelization.sample.TestClassBSixMethodsWithMethodOnMethodDependenciesSample;
import test.thread.parallelization.sample.TestClassASevenMethodsWithMethodOnMethodDependenciesSample;
import test.thread.parallelization.sample.TestClassAThreeMethodsWithMethodOnMethodDependenciesSample;
import test.thread.parallelization.sample.TestClassASixMethodsWithMethodOnMethodDependenciesSample;
import test.thread.parallelization.sample.TestClassDThreeMethodsWithNoDepsSample;
import test.thread.parallelization.sample.TestClassGThreeMethodsWithNoDepsSample;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

/** This class covers PTP_TC_9, Scenario 1 in the Parallelization Test Plan.
 *
 * Test Case Summary: Parallel by methods mode with sequential test suites with no factories or data providers. There
 *                    are method-on-method, intra-class group and inter-class group dependencies.
 *
 * Scenario Description: Two suites with two tests each. One suite consists of only method-on-method dependencies and
 *                       one of its tests contains a test class with no dependencies at all. The other suite has a test
 *                       with both inter and intra-class group dependencies as well as a class with no dependencies at
 *                       all. Its other test consists of classes with intra-class group dependencies only.
 *
 * 1) For one suite, the thread count is specified at the test level and for the other, the thread count is specified
 *    at the suite level.
 * 2) One test has fewer methods available that can execute than its thread count due to dependencies when it starts,
 *    but the bottleneck clears and the maximum number of methods executes in a subsequent block of parallel methods
 * 3) One test has more methods available that can execute than its thread count when it starts, but in a subsequent
 *    block of parallel methods, a bottleneck develops due to dependencies so fewer methods execute in parallel than
 *    he thread count would allow
 * 4) There are NO configuration methods
 * 5) All test methods pass
 * 6) NO ordering is specified
 * 7) `group-by-instances is NOT set
 * 8) here are no method exclusions
 */
public class ParallelByMethodsTestCase9Scenario1 extends BaseParallelizationTest {
    private static final String SUITE_A = "TestSuiteA";
    private static final String SUITE_B = "TestSuiteB";

    private static final String SUITE_A_TEST_A = "TestSuiteA-TestA";
    private static final String SUITE_A_TEST_B = "TestSuiteA-TestB";

    private static final String SUITE_B_TEST_A = "TestSuiteB-TestA";
    private static final String SUITE_B_TEST_B = "TestSuiteB-TestB";

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
    private List<TestNgRunStateTracker.EventLog> suiteOneTestTwoTestMethodLevelEventLogs;

    private List<TestNgRunStateTracker.EventLog> suiteTwoTestOneTestMethodLevelEventLogs;
    private List<TestNgRunStateTracker.EventLog> suiteTwoTestTwoTestMethodLevelEventLogs;

    private TestNgRunStateTracker.EventLog suiteOneSuiteListenerOnStartEventLog;
    private TestNgRunStateTracker.EventLog suiteOneSuiteListenerOnFinishEventLog;

    private TestNgRunStateTracker.EventLog suiteTwoSuiteListenerOnStartEventLog;
    private TestNgRunStateTracker.EventLog suiteTwoSuiteListenerOnFinishEventLog;

    private TestNgRunStateTracker.EventLog suiteOneTestOneListenerOnStartEventLog;
    private TestNgRunStateTracker.EventLog suiteOneTestOneListenerOnFinishEventLog;
    private TestNgRunStateTracker.EventLog suiteOneTestTwoListenerOnStartEventLog;
    private TestNgRunStateTracker.EventLog suiteOneTestTwoListenerOnFinishEventLog;

    private TestNgRunStateTracker.EventLog suiteTwoTestOneListenerOnStartEventLog;
    private TestNgRunStateTracker.EventLog suiteTwoTestOneListenerOnFinishEventLog;
    private TestNgRunStateTracker.EventLog suiteTwoTestTwoListenerOnStartEventLog;
    private TestNgRunStateTracker.EventLog suiteTwoTestTwoListenerOnFinishEventLog;

    @BeforeClass
    public void setUp() {
        reset();

        XmlSuite suiteOne = createXmlSuite(SUITE_A);
        XmlSuite suiteTwo = createXmlSuite(SUITE_B);

        createXmlTest(suiteOne, SUITE_A_TEST_A, TestClassBSixMethodsWithMethodOnMethodDependenciesSample.class,
                TestClassAThreeMethodsWithMethodOnMethodDependenciesSample.class);
        createXmlTest(suiteOne, SUITE_A_TEST_B, TestClassDThreeMethodsWithNoDepsSample.class,
                TestClassAFiveMethodsWithMethodOnMethodDependenciesSample.class,
                TestClassASixMethodsWithMethodOnMethodDependenciesSample.class,
                TestClassASevenMethodsWithMethodOnMethodDependenciesSample.class);

        suiteOne.setParallel(XmlSuite.ParallelMode.METHODS);

        for(XmlTest test : suiteOne.getTests()) {
            if(test.getName().equals(SUITE_A_TEST_A)) {
                test.setThreadCount(3);
            } else {
                test.setThreadCount(7);
            }
        }

        createXmlTest(suiteTwo, SUITE_B_TEST_A, TestClassAFiveMethodsWithIntraClassGroupDepsSample.class,
                TestClassAFiveMethodsWithInterClassGroupDepsSample.class, TestClassGThreeMethodsWithNoDepsSample.class);
        createXmlTest(suiteTwo, SUITE_B_TEST_B, TestClassASevenMethodsWithIntraClassGroupDepsSample.class,
                TestClassAFourMethodsWithIntraClassGroupDepsSample.class,
                TestClassAThreeMethodsWithIntraAndInterClassGroupDepsSample.class,
                TestClassASixMethodsWithIntraClassGroupDepsSample.class);

        suiteTwo.setParallel(XmlSuite.ParallelMode.METHODS);

        suiteTwo.setThreadCount(7);

        addParams(suiteOne, SUITE_A, SUITE_A_TEST_A, "100");
        addParams(suiteOne, SUITE_A, SUITE_A_TEST_B, "100");

        addParams(suiteTwo, SUITE_B, SUITE_B_TEST_A, "100");
        addParams(suiteTwo, SUITE_B, SUITE_B_TEST_B, "100");

        TestNG tng = create(suiteOne, suiteTwo);
        tng.addListener((ITestNGListener) new TestNgRunStateListener());

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
        suiteOneTestTwoTestMethodLevelEventLogs = getTestMethodLevelEventLogsForTest(SUITE_A, SUITE_A_TEST_B);

        suiteTwoTestOneTestMethodLevelEventLogs = getTestMethodLevelEventLogsForTest(SUITE_B, SUITE_B_TEST_A);
        suiteTwoTestTwoTestMethodLevelEventLogs = getTestMethodLevelEventLogsForTest(SUITE_B, SUITE_B_TEST_B);

        testEventLogsMap.put(SUITE_A_TEST_A, getTestLevelEventLogsForTest(SUITE_A, SUITE_A_TEST_A));
        testEventLogsMap.put(SUITE_A_TEST_B, getTestLevelEventLogsForTest(SUITE_A, SUITE_A_TEST_B));

        testEventLogsMap.put(SUITE_B_TEST_A, getTestLevelEventLogsForTest(SUITE_B, SUITE_B_TEST_A));
        testEventLogsMap.put(SUITE_B_TEST_B, getTestLevelEventLogsForTest(SUITE_B, SUITE_B_TEST_B));

        suiteOneSuiteListenerOnStartEventLog = getSuiteListenerStartEventLog(SUITE_A);
        suiteOneSuiteListenerOnFinishEventLog = getSuiteListenerFinishEventLog(SUITE_A);

        suiteTwoSuiteListenerOnStartEventLog = getSuiteListenerStartEventLog(SUITE_B);
        suiteTwoSuiteListenerOnFinishEventLog = getSuiteListenerFinishEventLog(SUITE_B);

        suiteOneTestOneListenerOnStartEventLog = getTestListenerStartEventLog(SUITE_A, SUITE_A_TEST_A);
        suiteOneTestOneListenerOnFinishEventLog = getTestListenerFinishEventLog(SUITE_A, SUITE_A_TEST_A);

        suiteOneTestTwoListenerOnStartEventLog = getTestListenerStartEventLog(SUITE_A, SUITE_A_TEST_B);
        suiteOneTestTwoListenerOnFinishEventLog = getTestListenerFinishEventLog(SUITE_A, SUITE_A_TEST_B);

        suiteTwoTestOneListenerOnStartEventLog = getTestListenerStartEventLog(SUITE_B, SUITE_B_TEST_A);
        suiteTwoTestOneListenerOnFinishEventLog = getTestListenerFinishEventLog(SUITE_B, SUITE_B_TEST_A);

        suiteTwoTestTwoListenerOnStartEventLog = getTestListenerStartEventLog(SUITE_B, SUITE_B_TEST_B);
        suiteTwoTestTwoListenerOnFinishEventLog = getTestListenerFinishEventLog(SUITE_B, SUITE_B_TEST_B);
    }

    @Test
    public void sanityCheck() {
        assertEquals(suiteLevelEventLogs.size(), 4, "There should be 4 suite level events logged for " + SUITE_A +
                " and " + SUITE_B + ": " + suiteLevelEventLogs);
        assertEquals(testLevelEventLogs.size(), 8, "There should be 8 test level events logged for " + SUITE_A +
                " and " + SUITE_B + ": " + testLevelEventLogs);

        assertEquals(testMethodLevelEventLogs.size(), 189, "There should 189 test method level events logged for " +
                SUITE_A + " and " + SUITE_B + ": " + testMethodLevelEventLogs);

        assertEquals(suiteOneSuiteLevelEventLogs.size(), 2, "There should be 2 suite level events logged for " +
                SUITE_A + ": " + suiteOneSuiteLevelEventLogs);
        assertEquals(suiteOneTestLevelEventLogs.size(), 4, "There should be 4 test level events logged for " + SUITE_A +
                ": " + suiteOneTestLevelEventLogs);
        assertEquals(suiteOneTestMethodLevelEventLogs.size(), 90, "There should be 90 test method level events " +
                "logged for " + SUITE_A + ": " + suiteOneTestMethodLevelEventLogs);

        assertEquals(suiteTwoSuiteLevelEventLogs.size(), 2, "There should be 2 suite level events logged for " +
                SUITE_B + ": " + suiteTwoSuiteLevelEventLogs);
        assertEquals(suiteTwoTestLevelEventLogs.size(), 4, "There should be 4 test level events logged for " + SUITE_B +
                ": " + suiteTwoTestLevelEventLogs);
        assertEquals(suiteTwoTestMethodLevelEventLogs.size(), 99, "There should be 99 test method level events " +
                "logged for " + SUITE_B + ": " + suiteTwoTestMethodLevelEventLogs);

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
    }

    //Verify that there is only a single test class instance associated with each of the test methods from the sample
    //classes for every test in all the suites.
    //Verify that the same test class instance is associated with each of the test methods from the sample test class
    @Test
    public void verifyOnlyOneInstanceOfTestClassForAllTestMethodsForAllSuites() {

        verifyNumberOfInstancesOfTestClassesForMethods(
                SUITE_A,
                SUITE_A_TEST_A,
                Arrays.asList(TestClassBSixMethodsWithMethodOnMethodDependenciesSample.class,
                        TestClassAThreeMethodsWithMethodOnMethodDependenciesSample.class),
                1);

        verifySameInstancesOfTestClassesAssociatedWithMethods(
                SUITE_A,
                SUITE_A_TEST_A,
                Arrays.asList(TestClassBSixMethodsWithMethodOnMethodDependenciesSample.class,
                        TestClassAThreeMethodsWithMethodOnMethodDependenciesSample.class)
        );

        verifyNumberOfInstancesOfTestClassesForMethods(
                SUITE_A,
                SUITE_A_TEST_B,
                Arrays.asList(TestClassDThreeMethodsWithNoDepsSample.class,
                        TestClassAFiveMethodsWithMethodOnMethodDependenciesSample.class,
                        TestClassASixMethodsWithMethodOnMethodDependenciesSample.class,
                        TestClassASevenMethodsWithMethodOnMethodDependenciesSample.class),
                1);

        verifySameInstancesOfTestClassesAssociatedWithMethods(
                SUITE_A,
                SUITE_A_TEST_B,
                Arrays.asList(TestClassDThreeMethodsWithNoDepsSample.class,
                        TestClassAFiveMethodsWithMethodOnMethodDependenciesSample.class,
                        TestClassASixMethodsWithMethodOnMethodDependenciesSample.class,
                        TestClassASevenMethodsWithMethodOnMethodDependenciesSample.class)
        );

        verifyNumberOfInstancesOfTestClassesForMethods(
                SUITE_B,
                SUITE_B_TEST_A,
                Arrays.asList(
                        TestClassAFiveMethodsWithIntraClassGroupDepsSample.class,
                        TestClassAFiveMethodsWithInterClassGroupDepsSample.class,
                        TestClassGThreeMethodsWithNoDepsSample.class
                ),
                1
        );

        verifySameInstancesOfTestClassesAssociatedWithMethods(
                SUITE_B,
                SUITE_B_TEST_A,
                Arrays.asList(
                        TestClassAFiveMethodsWithIntraClassGroupDepsSample.class,
                        TestClassAFiveMethodsWithInterClassGroupDepsSample.class,
                        TestClassGThreeMethodsWithNoDepsSample.class
                )
        );

        verifyNumberOfInstancesOfTestClassesForMethods(
                SUITE_B,
                SUITE_B_TEST_B,
                Arrays.asList(
                        TestClassASevenMethodsWithIntraClassGroupDepsSample.class,
                        TestClassAFourMethodsWithIntraClassGroupDepsSample.class,
                        TestClassAThreeMethodsWithIntraAndInterClassGroupDepsSample.class,
                        TestClassASixMethodsWithIntraClassGroupDepsSample.class
                ),
                1
        );

        verifySameInstancesOfTestClassesAssociatedWithMethods(
                SUITE_B,
                SUITE_B_TEST_B,
                Arrays.asList(
                        TestClassASevenMethodsWithIntraClassGroupDepsSample.class,
                        TestClassAFourMethodsWithIntraClassGroupDepsSample.class,
                        TestClassAThreeMethodsWithIntraAndInterClassGroupDepsSample.class,
                        TestClassASixMethodsWithIntraClassGroupDepsSample.class
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

        verifyEventsOccurBetween(suiteOneTestTwoListenerOnStartEventLog, suiteOneTestTwoTestMethodLevelEventLogs,
                suiteOneTestTwoListenerOnFinishEventLog,  "All of the test method level event logs for " +
                        SUITE_A_TEST_B + " should have timestamps between the test listener's onStart and onFinish " +
                        "event logs for " + SUITE_A_TEST_B + ". Test listener onStart event log: " +
                        suiteOneTestTwoListenerOnStartEventLog + ". Test listener onFinish event log: " +
                        suiteOneTestTwoListenerOnFinishEventLog + ". Test method level event logs: " +
                        suiteOneTestTwoTestMethodLevelEventLogs);

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

        verifyEventThreadsSpawnedAfter(getAllSuiteListenerStartEventLogs().get(0).getThreadId(),
                testMethodLevelEventLogs, "All the thread IDs for the test method level events should be greater " +
                        "than the thread ID for the suite and test level events. The expectation is that since the " +
                        "suite and test level events are running sequentially, and all the test methods are running " +
                        "in parallel, new threads will be spawned after the thread executing the suite and test " +
                        "level events when new methods begin executing. Suite and test level events thread ID: " +
                        getAllSuiteListenerStartEventLogs().get(0).getThreadId() + ". Test method level event logs: " +
                        testMethodLevelEventLogs);


        verifyEventsForTestMethodsRunInTheSameThread(TestClassBSixMethodsWithMethodOnMethodDependenciesSample.class,
                SUITE_A, SUITE_A_TEST_A);
        verifyEventsForTestMethodsRunInTheSameThread(TestClassAThreeMethodsWithMethodOnMethodDependenciesSample.class,
                SUITE_A, SUITE_A_TEST_A);

        verifyEventsForTestMethodsRunInTheSameThread(TestClassDThreeMethodsWithNoDepsSample.class, SUITE_A,
                SUITE_A_TEST_B);
        verifyEventsForTestMethodsRunInTheSameThread(TestClassAFiveMethodsWithMethodOnMethodDependenciesSample.class,
                SUITE_A, SUITE_A_TEST_B);
        verifyEventsForTestMethodsRunInTheSameThread(TestClassASixMethodsWithMethodOnMethodDependenciesSample.class,
                SUITE_A, SUITE_A_TEST_B);
        verifyEventsForTestMethodsRunInTheSameThread(TestClassASevenMethodsWithMethodOnMethodDependenciesSample.class,
                SUITE_A, SUITE_A_TEST_B);

        verifyEventsForTestMethodsRunInTheSameThread(TestClassAFiveMethodsWithIntraClassGroupDepsSample.class, SUITE_B,
                SUITE_B_TEST_A);
        verifyEventsForTestMethodsRunInTheSameThread(TestClassAFiveMethodsWithInterClassGroupDepsSample.class, SUITE_B,
                SUITE_B_TEST_A);
        verifyEventsForTestMethodsRunInTheSameThread(TestClassGThreeMethodsWithNoDepsSample.class, SUITE_B,
                SUITE_B_TEST_A);

        verifyEventsForTestMethodsRunInTheSameThread(TestClassASevenMethodsWithIntraClassGroupDepsSample.class, SUITE_B,
                SUITE_B_TEST_B);
        verifyEventsForTestMethodsRunInTheSameThread(TestClassAFourMethodsWithIntraClassGroupDepsSample.class, SUITE_B,
                SUITE_B_TEST_B);
        verifyEventsForTestMethodsRunInTheSameThread(TestClassAThreeMethodsWithIntraAndInterClassGroupDepsSample.class,
                SUITE_B, SUITE_B_TEST_B);
        verifyEventsForTestMethodsRunInTheSameThread(TestClassASixMethodsWithIntraClassGroupDepsSample.class, SUITE_B,
                SUITE_B_TEST_B);
    }

    @Test
    public void verifyThatTestMethodsAreaExecutedBeforeMethodsThatDependOnThem() {
        verifyThatTestMethodsRunAfterMethodsTheyDependOn(SUITE_A, SUITE_A_TEST_A,
                Arrays.asList(
                        TestClassBSixMethodsWithMethodOnMethodDependenciesSample.class,
                        TestClassAThreeMethodsWithMethodOnMethodDependenciesSample.class
                ));

        verifyThatTestMethodsRunAfterMethodsTheyDependOn(SUITE_A, SUITE_A_TEST_B,
                Arrays.asList(
                        TestClassDThreeMethodsWithNoDepsSample.class,
                        TestClassAFiveMethodsWithMethodOnMethodDependenciesSample.class,
                        TestClassASixMethodsWithMethodOnMethodDependenciesSample.class,
                        TestClassASevenMethodsWithMethodOnMethodDependenciesSample.class
                ));

        verifyThatTestMethodsRunAfterMethodsTheyDependOn(SUITE_B, SUITE_B_TEST_A,
                Arrays.asList(
                        TestClassAFiveMethodsWithIntraClassGroupDepsSample.class,
                        TestClassAFiveMethodsWithInterClassGroupDepsSample.class,
                        TestClassGThreeMethodsWithNoDepsSample.class
                ));

        verifyThatTestMethodsRunAfterMethodsTheyDependOn(SUITE_B, SUITE_B_TEST_B,
                Arrays.asList(
                        TestClassASevenMethodsWithIntraClassGroupDepsSample.class,
                        TestClassAFourMethodsWithIntraClassGroupDepsSample.class,
                        TestClassAThreeMethodsWithIntraAndInterClassGroupDepsSample.class,
                        TestClassASixMethodsWithIntraClassGroupDepsSample.class
                ));
    }

    @Test
    public void verifyThatTestMethodsRunInParallelThreads() {
        verifyParallelMethodsWithDependencies(getTestMethodLevelEventLogsForTest(SUITE_A, SUITE_A_TEST_A), SUITE_A_TEST_A, 3);
        verifyParallelMethodsWithDependencies(getTestMethodLevelEventLogsForTest(SUITE_A, SUITE_A_TEST_B), SUITE_A_TEST_B, 7);
        verifyParallelMethodsWithDependencies(getTestMethodLevelEventLogsForTest(SUITE_B, SUITE_B_TEST_A), SUITE_B_TEST_A, 7);
        verifyParallelMethodsWithDependencies(getTestMethodLevelEventLogsForTest(SUITE_B, SUITE_B_TEST_B), SUITE_B_TEST_B, 7);
    }
}
