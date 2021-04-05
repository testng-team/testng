package test.priority.parallel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import static org.testng.Assert.assertEquals;
import static test.thread.parallelization.TestNgRunStateTracker.*;

import test.thread.parallelization.BaseParallelizationTest;
import test.thread.parallelization.TestNgRunStateListener;
import test.thread.parallelization.TestNgRunStateTracker.EventLog;

public class EfficientPriorityParallelizationTest extends BaseParallelizationTest {
    private static final String SUITE_A = "TestSuiteA";

    private static final String SUITE_A_TEST_A = "TestSuiteA-HighPriorityTestClassTest";
    private static final String SUITE_A_TEST_B = "TestSuiteA-LowPriorityTestClassTest";

    private static final int THREAD_POOL_SIZE = 2;

    private Map<String, List<EventLog>> testEventLogsMap = new HashMap<>();

    private List<EventLog> suiteLevelEventLogs;
    private List<EventLog> testLevelEventLogs;
    private List<EventLog> testMethodLevelEventLogs;

    private List<EventLog> suiteOneSuiteAndTestLevelEventLogs;
    private List<EventLog> suiteOneSuiteLevelEventLogs;
    private List<EventLog> suiteOneTestLevelEventLogs;
    private List<EventLog> suiteOneTestMethodLevelEventLogs;

    private List<EventLog> suiteOneTestOneTestMethodLevelEventLogs;
    private List<EventLog> suiteOneTestTwoTestMethodLevelEventLogs;

    private EventLog suiteOneSuiteListenerOnStartEventLog;
    private EventLog suiteOneSuiteListenerOnFinishEventLog;

    private EventLog suiteOneTestOneListenerOnStartEventLog;
    private EventLog suiteOneTestOneListenerOnFinishEventLog;

    private EventLog suiteOneTestTwoListenerOnStartEventLog;
    private EventLog suiteOneTestTwoListenerOnFinishEventLog;

    @BeforeClass
    public void setUp() {
        reset();

        XmlSuite suiteOne = createXmlSuite(SUITE_A);

        XmlTest testA = createXmlTest(suiteOne, SUITE_A_TEST_A, HighPriorityTestSample.class);
        XmlTest testB = createXmlTest(suiteOne, SUITE_A_TEST_B, LowPriorityTestSample.class);

        suiteOne.setParallel(XmlSuite.ParallelMode.TESTS);
        suiteOne.setThreadCount(THREAD_POOL_SIZE);
        
        testA.setParallel(XmlSuite.ParallelMode.METHODS);
        testA.setThreadCount(THREAD_POOL_SIZE);
        testB.setParallel(XmlSuite.ParallelMode.METHODS);
        testB.setThreadCount(THREAD_POOL_SIZE);

        // High priority but slow test does slowness on its own.
        addParams(suiteOne, SUITE_A, SUITE_A_TEST_A, "50");
        // Low priority but quick
        addParams(suiteOne, SUITE_A, SUITE_A_TEST_B, "50");

        TestNG tng = create(suiteOne);
        tng.setSuiteThreadPoolSize(THREAD_POOL_SIZE);
        tng.addListener((ITestNGListener) new TestNgRunStateListener());

        System.out.println("Beginning EfficientPriorityParallelizationTest. This test scenario consists of 1 suite " + 
                "with 2 tests. Parallel is set to TESTS at the suite level, METHODS at the test level, and the thread pool size is 2. One test shall consist of a " + 
                "single test class with a single, high-priority, slow-running method, while the other test shall " + 
                "consist of a single test class with several low-priority, quick-running methods. There are no " + 
                "dependencies, data providers or factories.");

        System.out.println("Suite: " + SUITE_A + ", Test: " + SUITE_A_TEST_A + ", Test classes: " +
                HighPriorityTestSample.class.getCanonicalName() + ". Thread count: 2");

        System.out.println("Suite: " + SUITE_A + ", Test: " + SUITE_A_TEST_B + ", Test class: " +
                LowPriorityTestSample.class.getCanonicalName() + ". Thread count: 2");

        tng.run();

        suiteLevelEventLogs = getAllSuiteLevelEventLogs();
        testLevelEventLogs = getAllTestLevelEventLogs();
        testMethodLevelEventLogs = getAllTestMethodLevelEventLogs();

        suiteOneSuiteAndTestLevelEventLogs = getSuiteAndTestLevelEventLogsForSuite(SUITE_A);
        suiteOneSuiteLevelEventLogs = getSuiteLevelEventLogsForSuite(SUITE_A);
        suiteOneTestLevelEventLogs = getTestLevelEventLogsForSuite(SUITE_A);

        suiteOneTestMethodLevelEventLogs = getTestMethodLevelEventLogsForSuite(SUITE_A);

        suiteOneTestOneTestMethodLevelEventLogs = getTestMethodLevelEventLogsForTest(SUITE_A, SUITE_A_TEST_A);
        suiteOneTestTwoTestMethodLevelEventLogs = getTestMethodLevelEventLogsForTest(SUITE_A, SUITE_A_TEST_B);

        testEventLogsMap.put(SUITE_A_TEST_A, getTestLevelEventLogsForTest(SUITE_A, SUITE_A_TEST_A));
        testEventLogsMap.put(SUITE_A_TEST_B, getTestLevelEventLogsForTest(SUITE_A, SUITE_A_TEST_B));

        suiteOneSuiteListenerOnStartEventLog = getSuiteListenerStartEventLog(SUITE_A);
        suiteOneSuiteListenerOnFinishEventLog = getSuiteListenerFinishEventLog(SUITE_A);

        suiteOneTestOneListenerOnStartEventLog = getTestListenerStartEventLog(SUITE_A, SUITE_A_TEST_A);
        suiteOneTestOneListenerOnFinishEventLog = getTestListenerFinishEventLog(SUITE_A, SUITE_A_TEST_A);

        suiteOneTestTwoListenerOnStartEventLog = getTestListenerStartEventLog(SUITE_A, SUITE_A_TEST_B);
        suiteOneTestTwoListenerOnFinishEventLog = getTestListenerFinishEventLog(SUITE_A, SUITE_A_TEST_B);
    }

    //Verifies that the expected number of suite, test and test method level events were logged for each of the three
    //suites.
    @Test
    public void sanityCheck() {
        assertEquals(suiteLevelEventLogs.size(), 2, "There should be 2 suite level events logged for " + SUITE_A +
                ": " + suiteLevelEventLogs);
        assertEquals(testLevelEventLogs.size(), 4, "There should be 4 test level events logged for " + SUITE_A +
                ": " + testLevelEventLogs);

        assertEquals(testMethodLevelEventLogs.size(), 12, "There should 12 test method level events logged for " +
                SUITE_A + ": " + testMethodLevelEventLogs);

        assertEquals(suiteOneSuiteLevelEventLogs.size(), 2, "There should be 2 suite level events logged for " +
                SUITE_A + ": " + suiteOneSuiteLevelEventLogs);
        assertEquals(suiteOneTestLevelEventLogs.size(), 4, "There should be 4 test level events logged for " + SUITE_A +
                ": " + suiteOneTestLevelEventLogs);
        assertEquals(suiteOneTestMethodLevelEventLogs.size(), 12, "There should be 12 test method level events " +
                "logged for " + SUITE_A + ": " + suiteOneTestMethodLevelEventLogs);
    }

    //Verify that the suites run in parallel by checking that the suite and test level events for both suites have
    //overlapping timestamps. Verify that there are two separate threads executing the suite-level and test-level
    //events for each suite.
    @Test
    public void verifyThatSuitesRunInParallelThreads() {
        verifyParallelSuitesWithUnequalExecutionTimes(suiteLevelEventLogs, THREAD_POOL_SIZE);
    }

    @Test
    public void verifyOnlyOneInstanceOfTestClassForAllTestMethodsForAllSuites() {

        verifyNumberOfInstancesOfTestClassForMethods(
                SUITE_A,
                SUITE_A_TEST_A,
                HighPriorityTestSample.class,
                1);

        verifySameInstancesOfTestClassAssociatedWithMethods(
                SUITE_A,
                SUITE_A_TEST_A,
                HighPriorityTestSample.class
        );

        verifyNumberOfInstancesOfTestClassForMethods(
                SUITE_A,
                SUITE_A_TEST_B,
                LowPriorityTestSample.class,
                1);

        verifySameInstancesOfTestClassAssociatedWithMethods(
                SUITE_A,
                SUITE_A_TEST_B,
                LowPriorityTestSample.class
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

        verifyEventsForTestMethodsRunInTheSameThread(HighPriorityTestSample.class, SUITE_A,
                SUITE_A_TEST_A);
        verifyEventsForTestMethodsRunInTheSameThread(LowPriorityTestSample.class, SUITE_A,
                SUITE_A_TEST_B);
    }
    
    // Verifies that the slow, high-priority method started before and ended after the faster, low-priority methods.
    @Test
    public void verifyThatSlowMethodStartedFirstAndEndedLast() {
        verifyEventsOccurBetween(suiteOneTestOneListenerOnStartEventLog, suiteOneTestTwoTestMethodLevelEventLogs, suiteOneTestOneListenerOnFinishEventLog,
                "All the test two methods should run between when test one starts and ends. Start Event: " + 
                        suiteOneTestOneListenerOnStartEventLog + ". In between events: " + 
                        suiteOneTestTwoTestMethodLevelEventLogs + ". Final event: " + 
                        suiteOneTestOneListenerOnFinishEventLog);
    }
}
