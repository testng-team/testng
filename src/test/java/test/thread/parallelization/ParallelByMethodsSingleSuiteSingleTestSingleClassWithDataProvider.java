package test.thread.parallelization;

import com.google.common.collect.Multimap;

import org.testng.ITestNGListener;
import org.testng.TestNG;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.testng.xml.XmlSuite;

import test.thread.parallelization.TestNgRunStateTracker.EventLog;
import test.thread.parallelization.sample.TestClassAFiveMethodsWithDataProviderAndNoDepsSample;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static test.thread.parallelization.TestNgRunStateTracker.getAllEventLogs;
import static test.thread.parallelization.TestNgRunStateTracker.getAllSuiteAndTestLevelEventLogs;
import static test.thread.parallelization.TestNgRunStateTracker.getAllSuiteLevelEventLogs;
import static test.thread.parallelization.TestNgRunStateTracker.getAllTestLevelEventLogs;
import static test.thread.parallelization.TestNgRunStateTracker.getAllTestMethodLevelEventLogs;
import static test.thread.parallelization.TestNgRunStateTracker.getSuiteListenerFinishEventLog;
import static test.thread.parallelization.TestNgRunStateTracker.getSuiteListenerStartEventLog;
import static test.thread.parallelization.TestNgRunStateTracker.getTestListenerFinishEventLog;
import static test.thread.parallelization.TestNgRunStateTracker.getTestListenerStartEventLog;
import static test.thread.parallelization.TestNgRunStateTracker.getTestListenerStartThreadId;
import static test.thread.parallelization.TestNgRunStateTracker.getTestMethodEventLogsForMethod;
import static test.thread.parallelization.TestNgRunStateTracker.reset;

public class ParallelByMethodsSingleSuiteSingleTestSingleClassWithDataProvider extends BaseParallelizationTest {
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

    private Multimap<Object, EventLog> testMethodAEventLogs;
    private Multimap<Object, EventLog> testMethodBEventLogs;
    private Multimap<Object, EventLog> testMethodCEventLogs;
    private Multimap<Object, EventLog> testMethodDEventLogs;
    private Multimap<Object, EventLog> testMethodEEventLogs;

    @BeforeClass
    public void singleSuiteSingleTestSingleTestClassWithDataProvider() {
        reset();

        XmlSuite suite = createXmlSuite(SUITE);
        suite.setParallel(XmlSuite.ParallelMode.METHODS);
        suite.setThreadCount(15);

        createXmlTest(suite, TEST, TestClassAFiveMethodsWithDataProviderAndNoDepsSample.class);

        addParams(suite, SUITE, TEST, "1", "paramOne,paramTwo,paramThree");

        TestNG tng = create(suite);

        tng.addListener((ITestNGListener)new TestNgRunStateListener());

        tng.run();

        suiteLevelEventLogs = getAllSuiteLevelEventLogs();
        testLevelEventLogs = getAllTestLevelEventLogs();
        suiteAndTestLevelEventLogs = getAllSuiteAndTestLevelEventLogs();
        testMethodLevelEventLogs = getAllTestMethodLevelEventLogs();

        testMethodAEventLogs = getTestMethodEventLogsForMethod(SUITE, TEST,
                TestClassAFiveMethodsWithDataProviderAndNoDepsSample.class.getCanonicalName(), "testMethodA");
        testMethodBEventLogs = getTestMethodEventLogsForMethod(SUITE, TEST,
                TestClassAFiveMethodsWithDataProviderAndNoDepsSample.class.getCanonicalName(), "testMethodB");
        testMethodCEventLogs = getTestMethodEventLogsForMethod(SUITE, TEST,
                TestClassAFiveMethodsWithDataProviderAndNoDepsSample.class.getCanonicalName(), "testMethodC");
        testMethodDEventLogs = getTestMethodEventLogsForMethod(SUITE, TEST,
                TestClassAFiveMethodsWithDataProviderAndNoDepsSample.class.getCanonicalName(), "testMethodD");
        testMethodEEventLogs = getTestMethodEventLogsForMethod(SUITE, TEST,
                TestClassAFiveMethodsWithDataProviderAndNoDepsSample.class.getCanonicalName(), "testMethodE");

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
        assertEquals(testMethodLevelEventLogs.size(), 45, "There should be 15 test method level event logged for " +
                SUITE + ": " + testMethodLevelEventLogs);

        for(TestNgRunStateTracker.EventLog eventLog: getAllEventLogs()) {
            System.out.println(eventLog);
        }
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
                TestClassAFiveMethodsWithDataProviderAndNoDepsSample.class,
                1);

        verifySameInstancesOfTestClassAssociatedWithMethods(
                SUITE,
                TEST,
                TestClassAFiveMethodsWithDataProviderAndNoDepsSample.class);
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
        verifySameThreadIdForAllEvents(
                new ArrayList<>(testMethodAEventLogs.get(testMethodAEventLogs.keySet().toArray()[0])), "The event " +
                        "for testMethodA should all be run in the same thread: " +
                        testMethodAEventLogs.get(testMethodAEventLogs.keySet().toArray()[0])
        );

        verifySameThreadIdForAllEvents(
                new ArrayList<>(testMethodBEventLogs.get(testMethodBEventLogs.keySet().toArray()[0])), "The event " +
                        "for testMethodB should all be run in the same thread: " +
                        testMethodBEventLogs.get(testMethodBEventLogs.keySet().toArray()[0])
        );

        verifySameThreadIdForAllEvents(
                new ArrayList<>(testMethodCEventLogs.get(testMethodCEventLogs.keySet().toArray()[0])), "The event " +
                        "for testMethodC should all be run in the same thread: " +
                        testMethodCEventLogs.get(testMethodCEventLogs.keySet().toArray()[0])
        );

        verifySameThreadIdForAllEvents(
                new ArrayList<>(testMethodDEventLogs.get(testMethodDEventLogs.keySet().toArray()[0])), "The event " +
                        "for testMethodD should all be run in the same thread: " +
                        testMethodDEventLogs.get(testMethodDEventLogs.keySet().toArray()[0])
        );

        verifySameThreadIdForAllEvents(
                new ArrayList<>(testMethodEEventLogs.get(testMethodEEventLogs.keySet().toArray()[0])), "The event " +
                        "for testMethodE should all be run in the same thread: " +
                        testMethodEEventLogs.get(testMethodEEventLogs.keySet().toArray()[0])
        );
    }

}
