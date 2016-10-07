package test.thread.parallelization;

import com.google.common.collect.Multimap;
import org.testng.internal.collections.Pair;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import test.thread.parallelization.TestNgRunStateTracker.EventLog;
import test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import static org.testng.Assert.fail;
import static test.thread.parallelization.TestNgRunStateTracker.EventInfo.CLASS_NAME;
import static test.thread.parallelization.TestNgRunStateTracker.EventInfo.METHOD_NAME;
import static test.thread.parallelization.TestNgRunStateTracker.EventInfo.SUITE_NAME;
import static test.thread.parallelization.TestNgRunStateTracker.EventInfo.TEST_NAME;
import static test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent.LISTENER_SUITE_START;
import static test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent.LISTENER_SUITE_FINISH;
import static test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent.LISTENER_TEST_FINISH;
import static test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent.LISTENER_TEST_METHOD_PASS;
import static test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent.LISTENER_TEST_METHOD_START;
import static test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent.LISTENER_TEST_START;
import static test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent.TEST_METHOD_EXECUTION;
import static test.thread.parallelization.TestNgRunStateTracker.getAllEventLogsForSuite;
import static test.thread.parallelization.TestNgRunStateTracker.getTestMethodEventLogsForMethod;

public class BaseParallelizationTest extends SimpleBaseTest {

    //Get a list of the names of declared methods with the @Test annotation from the specified class
    public static List<String> getDeclaredTestMethods(Class<?> clazz) {
        List<String> methodNames = new ArrayList<>();

        for (Method method : clazz.getMethods()) {
            List<Annotation> declaredAnnotations = Arrays.asList(method.getDeclaredAnnotations());

            for (Annotation a : declaredAnnotations) {
                if (a.annotationType().isAssignableFrom(org.testng.annotations.Test.class)) {
                    methodNames.add(method.getName());
                }
            }
        }

        return methodNames;
    }

    //Send the name of the suite and the name of the test as parameters so that the methods can associate their
    //execution event logs with them. Specified the delay in seconds to apply for the method execution. This delay
    //helps in determining the parallelism or lack thereof for method executions.
    public static void addParams(XmlSuite suite, String suiteName, String testName, String sleepFor) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("suiteName", suiteName);
        parameters.put("testName", testName);
        parameters.put("sleepFor", sleepFor);

        for (XmlTest test : suite.getTests()) {
            if (test.getName().equals(testName)) {
                test.setParameters(parameters);
            }
        }
    }

    //Verify that the list of event logs have the specified event type. Print the specified failure message if the
    //assertion on the event type fails.
    public static void verifyEventTypeForEventsLogs(List<EventLog> eventLogs, TestNgRunEvent event, String
            failMessage) {
        for (EventLog eventLog : eventLogs) {
            assertTrue(eventLog.getEvent() == event, failMessage);
        }
    }

    //Verify that the list of event logs all have different thread IDS. Print the specified failure message if the
    //assertion on the thread IDs fails.
    public static void verifyDifferentThreadIdsForEvents(List<EventLog> eventLogs, String failMessage) {
        List<Long> threadIds = new ArrayList<>();

        for (EventLog eventLog : eventLogs) {
            assertFalse(threadIds.contains(eventLog.getThreadId()), failMessage);
            threadIds.add(eventLog.getThreadId());
        }
    }

    //Verify that the event logs in the first list all have different thread IDs from the event logs in the second
    //list. Print the specified failure message if the assertion on the thread IDs fails.
    public static void verifyDifferentThreadIdsForEvents(List<EventLog> eventLogsOne, List<EventLog> eventLogsTwo,
            String failMessage) {
        List<Long> threadIds = new ArrayList<>();

        for (EventLog eventLog : eventLogsOne) {
            threadIds.add(eventLog.getThreadId());
        }

        for (EventLog eventLog : eventLogsTwo) {
            assertFalse(threadIds.contains(eventLog.getThreadId()), failMessage);
        }
    }

    //Verify that the list of event logs all have the same thread ID. Print the specified failure message if the
    //assertion on the event type fails.
    public static void verifySameThreadIdForAllEvents(List<EventLog> eventLogs, String failMessage) {
        long threadId = -1;
        for (EventLog eventLog : eventLogs) {
            if (threadId == -1) {
                threadId = eventLog.getThreadId();
            } else {
                assertEquals(eventLog.getThreadId(), threadId, failMessage);
            }
        }
    }

    //Verify that the threads from the specified list of event logs are all greater than the specified thread ID. Print
    //the specified failure message if the assertion on the thread IDs fails.
    public static void verifyEventThreadsSpawnedAfter(Long earlierThreadId, List<EventLog> eventsFromLaterThread, String
            failMessage) {
        for(EventLog eventLog : eventsFromLaterThread) {
            assertTrue(eventLog.getThreadId() > earlierThreadId, failMessage);
        }
    }

    //Verify that the timestamps of the list of event logs are all within the specified range of each other. Print
    //the specified failure message if the assertion on the timing range fails.
    public static void verifyTimingOfEvents(List<EventLog> eventLogs, long timingRange, String failMessage) {
        if(!eventLogs.isEmpty()) {
            Pair<Long,Long> timestamps = getEarliestAndLatestTimestamps(eventLogs);
            verifyTimestampDifference(timestamps.second(), timestamps.first(), timingRange, failMessage);
//            assertTrue(Math.abs(timestamps.second() - timestamps.first()) <= timingRange, failMessage);
        }
    }


    public static void verifyTimingOfEvents(EventLog eventLogOne, EventLog eventLogTwo, long timingRange, String
            failMessage) {
        verifyTimestampDifference(eventLogOne.getTimeOfEvent(), eventLogTwo.getTimeOfEvent(), timingRange, failMessage);
    }

    //Verify that the timestamps of the first list of events are all within the specified range from the timestamps
    //of the second list of events. Print the specified failure message if the assertion on the timing range fails.
    public static void verifyTimingOfEvents(List<EventLog> firstEventLogs, List<EventLog> secondEventLogs,
            long lowerTimingRange, long upperTimingRange, String failMessage) {

        if(!firstEventLogs.isEmpty() && !secondEventLogs.isEmpty()) {
            Pair<Long, Long> timestampsListOne = getEarliestAndLatestTimestamps(firstEventLogs);
            Pair<Long, Long> timestampsListTwo = getEarliestAndLatestTimestamps(secondEventLogs);

            System.out.println("Difference earliest and earliest: " +
                    Math.abs((timestampsListTwo.first() - timestampsListOne.first())));
            System.out.println("Difference earliest and latest: " +
                    Math.abs((timestampsListTwo.first() - timestampsListOne.second())));
            System.out.println("Difference latest and earliest: " +
                    Math.abs((timestampsListTwo.second() - timestampsListOne.first())));
            System.out.println("Difference latest and latest: " +
                    Math.abs((timestampsListTwo.second() - timestampsListOne.second())));

            verifyTimestampDifference(timestampsListTwo.first(), timestampsListOne.first(), lowerTimingRange,
                    upperTimingRange, failMessage);
            verifyTimestampDifference(timestampsListTwo.first(), timestampsListOne.second(), lowerTimingRange,
                    upperTimingRange, failMessage);
            verifyTimestampDifference(timestampsListTwo.second(), timestampsListOne.first(), lowerTimingRange,
                    upperTimingRange, failMessage);
            verifyTimestampDifference(timestampsListTwo.second(), timestampsListOne.second(), lowerTimingRange,
                    upperTimingRange, failMessage);
        }
    }

    //Verify that the difference between the two specified timestamps is within the specfied upper and lower bound. The
    //difference is calculated as an absolute value.
    public static void verifyTimestampDifference(long timestampOne, long timestampTwo, long lowerTimingRange,
            long upperTimingRange, String failMessage) {
        assertTrue(Math.abs(timestampOne - timestampTwo) <= upperTimingRange &&
                Math.abs(timestampOne - timestampTwo) >= lowerTimingRange, failMessage);
    }

    public static void verifyTimestampDifference(long timestampOne, long timestampTwo, long timingRange, String
            failMessage) {
        assertTrue(Math.abs(timestampOne - timestampTwo) <= timingRange, failMessage + " Difference: " +
                Math.abs(timestampOne - timestampTwo));
    }

    //Verify that the specified event logs all have timestamps between the specified earlier and later event logs.
    //Print the specified failure message if the assertion on the timestamps fails.
    public static void verifyEventsOccurBetween(EventLog earlierEventLog, List<EventLog> inBetweenEventLogs, EventLog
            laterEventLog, String failMessage) {
        for(EventLog eventLog : inBetweenEventLogs) {
            assertTrue(eventLog.getTimeOfEvent() > earlierEventLog.getTimeOfEvent() &&
                    eventLog.getTimeOfEvent() < laterEventLog.getTimeOfEvent(), failMessage);
        }
    }

    //Verify that the timestamps for the events if the specified list are all increasing. That each the timestamps of
    //a given event log is later than the event log immediately preceding it. Print the specified failure message if
    //the assertion on the timestamps fails
    public static void verifySequentialTimingOfEvents(List<EventLog> eventLogs, String failMessage) {
        for(int i = 0; i + 1 < eventLogs.size(); i++) {
            assertTrue(eventLogs.get(i).getTimeOfEvent() < eventLogs.get(i + 1).getTimeOfEvent(), failMessage);
        }
    }

    //Verify that the first list of event logs all have timestamps earlier than all the events in the second list of
    //event logs. Print the specified failure message if the assertion on the timestamps fails.
    public static void verifySequentialTimingOfEvents(List<EventLog> firstEventLogs, List<EventLog> secondEventLogs,
            String failMessage) {

        if(!firstEventLogs.isEmpty() && !secondEventLogs.isEmpty()) {
            Pair<Long, Long> timestampsListOne = getEarliestAndLatestTimestamps(firstEventLogs);
            Pair<Long, Long> timestampsListTwo = getEarliestAndLatestTimestamps(secondEventLogs);

            assertTrue(timestampsListTwo.first() > timestampsListOne.second(), failMessage);
        }
    }

    //Verify that the test methods declared in the specified list of classes have the specified number of class
    //instances associated with them for the specified suite and test.
    public static void verifyNumberOfInstancesOfTestClassesForMethods(String suiteName, String testName, List<Class<?>>
            classes, int numInstances) {

        for (Class<?> clazz : classes) {
            verifyNumberOfInstancesOfTestClassForMethods(suiteName, testName, clazz, numInstances);
        }

    }

    //Verify that the test methods declared in the specified class have the specified number of class instances
    //associated with them for the specified suite and test.
    public static void verifyNumberOfInstancesOfTestClassForMethods(String suiteName, String testName, Class<?>
            clazz, int numInstances) {

        for (String methodName :  getDeclaredTestMethods(clazz)) {
            verifyNumberOfInstancesOfTestClassForMethod(suiteName, testName, clazz, methodName, numInstances);
        }
    }

    //Verify that the specified test method has the specified number of class instances associated with it for the
    //specified suite and test.
    public static void verifyNumberOfInstancesOfTestClassForMethod(String suiteName, String testName, Class<?>
            clazz, String methodName, int numInstances) {

        Multimap<Object, EventLog> eventLogMap = getTestMethodEventLogsForMethod(suiteName, testName,
                clazz.getCanonicalName(), methodName);

        assertEquals(eventLogMap.keySet().size(), numInstances, "There should be " + numInstances + " instances " +
                "associated with the class " + clazz.getCanonicalName() + " for method " + methodName + " in the " +
                "test " + testName + " in the suite " + suiteName + ": " + eventLogMap);
    }

    //Verify that all the test methods declared in the specified list of classes have the same instances of the classes
    //associated with them for the specified suite and test.
    public static void verifySameInstancesOfTestClassesAssociatedWithMethods(String suiteName, String testName,
            List<Class<?>> classes) {
        for(Class<?> clazz : classes) {
            verifySameInstancesOfTestClassAssociatedWithMethods(suiteName, testName, clazz);
        }

    }

    //Verify that all the test methods declared in the specified class have the same instances of the class associated /
    //with them for the specified suite and test.
    public static void verifySameInstancesOfTestClassAssociatedWithMethods(String suiteName, String testName, Class<?>
            clazz) {

        Set<Object> instanceKeys = null;

        for (String methodName :  getDeclaredTestMethods(clazz)) {
            Multimap<Object, EventLog> eventLogMap = getTestMethodEventLogsForMethod(suiteName, testName,
                    clazz.getCanonicalName(), methodName);

            if (instanceKeys == null) {
                instanceKeys = eventLogMap.keySet();
            } else {
                assertTrue(instanceKeys.containsAll(eventLogMap.keySet()) &&
                        eventLogMap.keySet().containsAll(instanceKeys), "The same instances of " +
                        clazz.getCanonicalName() + " should be associated with its methods for the test " + testName +
                        " in the suite " + suiteName);
            }
        }
    }

    //Verify that methods associated with the specified event logs execute simultaneously in parallel fashion, in
    //accordance with the specified thread count.
    public static void verifySimultaneousTestMethods(List<EventLog> testMethodEventLogs, String testName, int
            threadCount) {

        int remainder = testMethodEventLogs.size() % (threadCount * 3);

        for (int i = 1; i < testMethodEventLogs.size(); i = i + threadCount * 3) {
            int blockSize = (remainder != 0 && testMethodEventLogs.size() - i < threadCount * 3) ?
                    remainder / 3 :
                    threadCount;

            int offsetOne = (remainder != 0 && testMethodEventLogs.size() - i < threadCount * 3) ?
                    testMethodEventLogs.size() - remainder :
                    i - 1;

            int offsetTwo = (remainder != 0 && testMethodEventLogs.size() - i < threadCount * 3) ?
                    testMethodEventLogs.size() - remainder + blockSize :
                    i + threadCount - 1;

            List<EventLog> eventLogMethodListenerStartSublist = testMethodEventLogs.subList(offsetOne, offsetTwo);
            List<EventLog> eventLogMethodExecuteSublist = testMethodEventLogs.subList(offsetTwo, offsetTwo + blockSize);
            List<EventLog> eventLogMethodListenerPassSublist = testMethodEventLogs.subList(offsetTwo + blockSize,
                    offsetTwo + 2 * blockSize);

            verifySimultaneousTestMethodListenerStartEvents(eventLogMethodListenerStartSublist, testName, threadCount);

            verifySimultaneousTestMethodExecutionEvents(eventLogMethodExecuteSublist, testName, threadCount);
            verifyEventsBelongToSameMethods(eventLogMethodListenerStartSublist, eventLogMethodExecuteSublist, "The " +
                    "thread count is " + threadCount + " for " + testName + " so no more than " + threadCount +
                    " methods should be running at the same time. The test execution event logs for a block of " +
                    "simultaneously running test methods should all belong to the same methods as the test method " +
                    "listener onTestStart event logs immediately preceding");
            verifyTimingOfEvents(eventLogMethodListenerStartSublist, eventLogMethodExecuteSublist, 950, 1050, "The " +
                    "test method execution event logs for a block of simultaneously executing methods should be " +
                    "within 950 and 1050 milliseconds of their test method listener's onTestStart events");

            verifySimultaneousTestMethodListenerPassEvents(eventLogMethodListenerPassSublist, testName, threadCount);
            verifyEventsBelongToSameMethods(eventLogMethodExecuteSublist, eventLogMethodListenerPassSublist, "The " +
                    "thread count is " + threadCount + " for " + testName + " so no more than " + threadCount +
                    " methods should be running at the same time. The test method listener on onTestSuccess event " +
                    "logs for a block of simultaneously running test methods should all belong to the same methods " +
                    "as the test method execution event logs immediately preceding");
            verifyTimingOfEvents(eventLogMethodExecuteSublist, eventLogMethodListenerPassSublist, 4950, 5050, "The " +
                    "test method listener's onTestSuccess events for a block of simultaneously executing methods " +
                    "should be within 4950 and 5050 milliseconds of their execution events");
        }
    }

    //Verify that the specified test method listener onTestStart event logs execute simultaneously in parallel fashion
    //according to the specified thread count. Verifies that each of them has the same event type and all have
    //different thread IDs and that they all execute within 50 milliseconds of each other.
    public static void verifySimultaneousTestMethodListenerStartEvents(List<EventLog> listenerStartEventLogs, String
            testName, int threadCount) {

        verifyEventTypeForEventsLogs(listenerStartEventLogs, LISTENER_TEST_METHOD_START, "The thread count is " +
                threadCount + " for " + testName + " so more more than " + threadCount + " methods should start " +
                "running at the same time if there are more than " + threadCount + " methods remaining to execute. " +
                "Event logs: " + listenerStartEventLogs);
        verifyDifferentThreadIdsForEvents(listenerStartEventLogs, "The thread count is " + threadCount + " for " +
                testName + " so the thread IDs for all the test method listener's onTestStart method " + "the " +
                threadCount + "currently executing test methods should be different. Event logs: " +
                listenerStartEventLogs);
        verifyTimingOfEvents(listenerStartEventLogs, 50, "The test method listener's onTestStart method " +
                "for a block of simultaneously executing methods should be within 50 milliseconds of each other. " +
                "Event logs: " + listenerStartEventLogs);
    }

    //Verify that the specified test method execution event logs execute simultaneously in parallel fashion according
    //to the specified thread count. Verifies that each of them has the same event type and all have different thread
    //IDs and that they all execute within 50 milliseconds of each other. This verification is for blocks of parallel
    //methods that have the same sleep delays for their execution bodies and which do not have any BeforeMethod,
    //AfterMethod, BeforeGroup or AfterGroup configuration methods.
    public static void verifySimultaneousTestMethodExecutionEvents(List<EventLog> testMethodExecutionEventLogs,
            String testName, int threadCount) {

        verifyEventTypeForEventsLogs(testMethodExecutionEventLogs, TEST_METHOD_EXECUTION, "The thread count " +
                "is " + threadCount + " for " + testName + " so no more than " + threadCount + " methods should be " +
                "executing at the same time. Event logs: " + testMethodExecutionEventLogs);
        verifyDifferentThreadIdsForEvents(testMethodExecutionEventLogs, "The thread count is " + threadCount + " for " +
                testName + " so the thread IDs for the test method execution events for the " + threadCount +
                "currently executing test methods should be different. Event logs: " + testMethodExecutionEventLogs);
        verifyTimingOfEvents(testMethodExecutionEventLogs, 50, "The test method execution events for a block of " +
                "simultaneously executing methods should be within 50 milliseconds of each other. Event logs: " +
                testMethodExecutionEventLogs);
    }

    //Verify that the specified test method listener onTestSuccess event logs execute simultaneously in parallel
    //fashion according to the specified thread count. Verifies that each of them has the same event type and all have
    //different thread IDs and that they all execute within 50 milliseconds of each other.
    public static void verifySimultaneousTestMethodListenerPassEvents(List<EventLog> testMethodListenerPassEventLogs,
            String testName, int threadCount) {
        verifyEventTypeForEventsLogs(testMethodListenerPassEventLogs, LISTENER_TEST_METHOD_PASS, "The thread " +
                "count is " + threadCount + " for " + testName + " so no more than " + threadCount + " test listener " +
                "onTestSuccess methods should be executing at the same time. Event logs: " +
                testMethodListenerPassEventLogs);
        verifyDifferentThreadIdsForEvents(testMethodListenerPassEventLogs, "The thread count is " + threadCount +
                " for " + testName + " so the thread IDs for the test method listener onTestSuccess events for the " +
                threadCount + "currently executing test methods should be different. Event logs: " +
                testMethodListenerPassEventLogs);
        verifyTimingOfEvents(testMethodListenerPassEventLogs, 50, "The test method listener's onTestSuccess events " +
                "for a block of simultaneously executing methods should be within 50 milliseconds of each other. Event logs: " +
                testMethodListenerPassEventLogs);
    }

    //Verify that the test method level events for the test methods declared in the specified class run in the same
    //thread for each instance of the test class for the specified suite and test
    public static void verifyEventsForTestMethodsRunInTheSameThread(Class<?> testClass, String suiteName, String
            testName) {

        for(Method method : testClass.getMethods()) {
            if (method.getDeclaringClass().equals(testClass)) {
                Multimap<Object, EventLog> testMethodEventLogs = getTestMethodEventLogsForMethod(suiteName, testName,
                        testClass.getCanonicalName(), method.getName());

                for(Object instanceKey : testMethodEventLogs.keySet()) {
                    long threadId = -1;

                    for (EventLog eventLog : testMethodEventLogs.get(instanceKey)) {
                        if (threadId == -1) {
                            threadId = eventLog.getThreadId();
                        } else {
                            assertEquals(eventLog.getThreadId(), threadId, "All of the method level events for the test " +
                                    "method " + method.getName() + " in the test class " + testClass.getCanonicalName() +
                                    " for the test " + suiteName + " should be run in the same thread");
                        }
                    }
                }
            }
        }
    }

    public static void verifySequentialSuites(List<EventLog> suiteLevelEventLogs, Map<String,List<EventLog>>
            suiteEventLogsMap, Map<String, Long> expectedExecutionTimes) {

        verifySameThreadIdForAllEvents(suiteLevelEventLogs, "Because the suites execute sequentially, the event logs " +
                "suite level events should have the same thread ID: " + suiteLevelEventLogs);

        List<EventLog> suiteListenerStartEventLogs = new ArrayList<>();

        for(int i = 0; i < suiteLevelEventLogs.size(); i = i + 2) {
            assertTrue(suiteLevelEventLogs.get(i).getEvent() == LISTENER_SUITE_START &&
                    suiteLevelEventLogs.get(i + 1).getEvent() == LISTENER_SUITE_FINISH, "Because the suites are " +
                    "expected to execute sequentially, the suite level event logs should consist of a series of " +
                    "pairs of a suite listener onStart event log followed by a suite listener onFinish event log: " +
                    suiteLevelEventLogs);
            suiteListenerStartEventLogs.add((suiteLevelEventLogs.get(i)));
        }

        for(int i = 0; i < suiteListenerStartEventLogs.size() - 1; i++) {
            String firstSuite = (String)suiteListenerStartEventLogs.get(i).getData(SUITE_NAME);
            String secondSuite = (String)suiteListenerStartEventLogs.get(i + 1).getData(SUITE_NAME);

            System.out.println("Comparing " + firstSuite + " and " + secondSuite);

            List<EventLog> firstSuiteEventLogs = suiteEventLogsMap.get(firstSuite);
            List<EventLog> secondSuiteEventLogs = suiteEventLogsMap.get(secondSuite);

            verifySequentialTimingOfEvents(firstSuiteEventLogs, secondSuiteEventLogs, "The first suite listener " +
                    "onStart event log is for " + firstSuite + " and the second suite listener onStart event log is " +
                    "for " + secondSuite + ". Because the suites are supposed to execute sequentially, all of the " +
                    "event logs for " + firstSuite + " should have timestamps earlier than all of the event logs for " +
                    secondSuite + ". First suite event logs: " + firstSuiteEventLogs + ". Second suite event logs: " +
                    secondSuiteEventLogs);

            verifyTimingOfEvents(firstSuiteEventLogs, secondSuiteEventLogs, 950,
                    expectedExecutionTimes.get(firstSuite) + expectedExecutionTimes.get(secondSuite) - 800,
                    "The expected execution time of " + firstSuite + " is " + expectedExecutionTimes.get(firstSuite) +
                            " and the expected execution time of " + secondSuite + " is " +
                            expectedExecutionTimes.get(secondSuite) + ". The delay for the suite listener's onFinish " +
                            "event is 1 second. Because there is no parallelism specified at the suite or test " +
                            "level and all tests for all suites are supposed to execute in parallel by methods mode, " +
                            "the suites should all execute sequentially. Therefore the minimum difference between " +
                            "the timestamps for the first event log for " + secondSuite + " and the last event log " +
                            "for " + firstSuite + " is 950 milliseconds and the maximum difference in the timestamps " +
                            "for the first event log for " + firstSuite + " and the last event log for " +
                            secondSuite + " is " + (expectedExecutionTimes.get(firstSuite) +
                            expectedExecutionTimes.get(secondSuite) - 800));
        }
    }

    public static void verifySequentialTests(List<EventLog> suiteAndTestLevelEventLogs, List<EventLog>
            testLevelEventLogs, EventLog suiteListenerOnStartEventLog, EventLog suiteListenerOnFinishEventLog,
            Map<String, List<EventLog>> testEventLogsMap, Map<String, Long> expectedTestExecutionTimes) {

        verifySameThreadIdForAllEvents(suiteAndTestLevelEventLogs, "All suite level and test level event logs " +
                "should have the same thread ID because there is no parallelism specified at the suite or test " +
                "level: " + suiteAndTestLevelEventLogs);

        verifySequentialTimingOfEvents(suiteAndTestLevelEventLogs, "The timestamps of suite and test level events " +
                "logged first should be earlier than those which are logged afterwards because there is no " +
                "parallelism specified at the suite or test level: " + suiteAndTestLevelEventLogs);

        verifyEventsOccurBetween(suiteListenerOnStartEventLog, testLevelEventLogs, suiteListenerOnFinishEventLog,
                "All of the test level event logs should have timestamps between the suite listener's onStart and " +
                        "onFinish event logs. Suite listener onStart event log: " + suiteListenerOnStartEventLog +
                        ". Suite listener onFinish event log: " + suiteListenerOnFinishEventLog + ". Test level " +
                        "event logs: " + testLevelEventLogs);

        List<EventLog> testListenerStartEventLogs = new ArrayList<>();

        for(int i = 0; i < testLevelEventLogs.size(); i = i + 2) {
            assertTrue(testLevelEventLogs.get(i).getEvent() == LISTENER_TEST_START &&
                    testLevelEventLogs.get(i + 1).getEvent() == LISTENER_TEST_FINISH, "Because the tests are " +
                    "expected to execute sequentially, the test level event logs should consist of a series of " +
                    "pairs of a test listener onStart event log followed by a test listener onFinish event log: " +
                    testLevelEventLogs);
            testListenerStartEventLogs.add((testLevelEventLogs.get(i)));
        }

        for(int i = 0; i < testListenerStartEventLogs.size() - 1; i++) {
            String firstTest = (String) testListenerStartEventLogs.get(i).getData(TEST_NAME);
            String secondTest = (String) testListenerStartEventLogs.get(i + 1).getData(TEST_NAME);

            System.out.println("Comparing " + firstTest + " and " + secondTest);

            List<EventLog> firstTestEventLogs = testEventLogsMap.get(firstTest);
            List<EventLog> secondTestEventLogs = testEventLogsMap.get(secondTest);

            verifyTimingOfEvents(firstTestEventLogs,secondTestEventLogs, 950,
                    expectedTestExecutionTimes.get(firstTest) + expectedTestExecutionTimes.get(secondTest) - 800,
                    "The expected execution time of " + firstTest + " is " + expectedTestExecutionTimes.get(firstTest) +
                            " and the expected execution time of " + secondTest + " is " +
                            expectedTestExecutionTimes.get(secondTest) + ". The delay for the test listener's " +
                            "onFinish event is 1 second. Because there is no parallelism specified at the suite or " +
                            "test level and all tests for all suites are supposed to execute in parallel by methods " +
                            "mode, tests in the suites should all execute sequentially. Therefore the minimum " +
                            "difference between the timestamps for the first event log for " + secondTest + " and " +
                            "the last event log for " + firstTest + " is 950 milliseconds and the maximum difference " +
                            "in the timestamps for the first event log for " + firstTest + " and the last event log " +
                            "for " + secondTest  + " is " + (expectedTestExecutionTimes.get(firstTest) +
                            expectedTestExecutionTimes.get(secondTest) - 800));

        }
    }

    public static void verifyParallelSuitesWithUnequalExecutionTimes(List<EventLog> suiteLevelEventLogs,
            Map<String, Long> expectedExecutionTimes, int threadPoolSize) {

        for(EventLog eventLog : suiteLevelEventLogs) {
            System.out.println(eventLog);
        }
        Map<String, EventLog> suitesExecuting = new HashMap<>();
        Map<String, EventLog> suitesCompleted = new HashMap<>();

        List<Long> executingSuiteThreadIds = new ArrayList<>();

        if(expectedExecutionTimes.keySet().size() > 1) {
            System.out.println("Verifying first block");
            int offset = expectedExecutionTimes.keySet().size() >= threadPoolSize ? threadPoolSize :
                    expectedExecutionTimes.keySet().size();

            List<EventLog> suiteListenerStartEventLogs = suiteLevelEventLogs.subList(0, offset);

            verifyFirstBlockOfSimultaneouslyExecutingSuites(suiteListenerStartEventLogs, expectedExecutionTimes,
                    threadPoolSize);

            for(EventLog eventLog : suiteListenerStartEventLogs) {
                suitesExecuting.put((String)eventLog.getData(SUITE_NAME), eventLog);
                executingSuiteThreadIds.add(eventLog.getThreadId());
            }

            for (int i = offset; i < suiteLevelEventLogs.size(); i++) {
                System.out.println("Processing #: " + (i+1) + ": " + suiteLevelEventLogs.get(i));

                EventLog eventLog = suiteLevelEventLogs.get(i);
                String suiteName = (String)eventLog.getData(SUITE_NAME);

                if(eventLog.getEvent() == LISTENER_SUITE_START) {
                    if (suitesExecuting.keySet().size() == threadPoolSize) {
                        fail("The thread pool size is " + threadPoolSize + ", so there should be no more than " +
                                threadPoolSize + " suites executing at the same time: " + suiteLevelEventLogs);
                    }

                    assertFalse(suitesExecuting.get(suiteName) != null || suitesCompleted.get(suiteName) != null,
                            "There should only be one execution of any given suite");
                    assertFalse(executingSuiteThreadIds.contains(eventLog.getThreadId()), "Event logs for currently " +
                            "executing suites should have different thread IDs");

                    suitesExecuting.put(suiteName, eventLog);
                    executingSuiteThreadIds.add(eventLog.getThreadId());

                    if (suitesCompleted.size() > 0) {
                        EventLog priorEventLog = suiteLevelEventLogs.get(i - 1);

                        assertEquals(priorEventLog.getEvent(), LISTENER_SUITE_FINISH, "When suites are executing in " +
                                "parallel and a new suite begins execution when the active thread count was last " +
                                "known to be equal to the maximum thread pool size, the previously logged suite " +
                                "level event should be a suite listener onFinish event.");

                        System.out.println("Processing a start event: " + eventLog);

                        verifyTimingOfEvents(priorEventLog, eventLog, 1050, "When suites are executing in parallel, " +
                                "and a new suite begins executing because a prior suite completed execution, causing " +
                                "the active thread count to drop below the thread pool size, the event log for " +
                                "suite listener onStart associated with the new suite should have a timestamp " +
                                "within 1050 milliseconds of the timestamp for the event log for suite listener's " +
                                "onFinish event associated with the suite which completed immediately prior. The " +
                                "delay for a suite listener onFinish event is 1 second.");

                    }
                }

                if(suitesExecuting.keySet().size() < threadPoolSize && suiteLevelEventLogs.size() - i + 1 >
                        threadPoolSize) {
                    fail("The thread pool size is " + threadPoolSize + ", so there should be at least " +
                            threadPoolSize + " suites executing at the same time unless there are no suites left to " +
                            "queue and the final block of suites is currently in execution: " + suiteLevelEventLogs);
                }

                if(eventLog.getEvent() == LISTENER_SUITE_FINISH) {

                    System.out.println("Processing a finish event: " + eventLog);

                    assertTrue(suitesExecuting.get(suiteName) != null, "Found an event log for a suite listener " +
                            "onFinish event that does not have a corresponding event log for a suite listener " +
                            "onStart event");
                    assertTrue(suitesExecuting.get(suiteName).getThreadId() == eventLog.getThreadId(), "All the " +
                            "suite level event logs for a given suite should have the same thread ID");

                    suitesExecuting.remove(suiteName);
                    executingSuiteThreadIds.remove(eventLog.getThreadId());
                    suitesCompleted.put((String)eventLog.getData(SUITE_NAME),eventLog);
                }
            }
        }
    }

    public static void verifySimultaneousSuiteListenerStartEvents(List<EventLog> listenerStartEventLogs, int
            threadPoolSize) {

        verifyEventTypeForEventsLogs(listenerStartEventLogs, LISTENER_SUITE_START, "The suite thread pool size is " +
                threadPoolSize + ", so no more more than " + threadPoolSize + " suites should start running at the " +
                "the same time if there are more than " + threadPoolSize + " suites remaining to execute.");
        verifyDifferentThreadIdsForEvents(listenerStartEventLogs, "The suite thread pool size is " + threadPoolSize +
                ", so the thread IDs for all the suite listener's onStart method for the " + threadPoolSize +
                " currently executing suites should be different");
        verifyTimingOfEvents(listenerStartEventLogs, 50, "The suite listener's onTestStart method for a block of " +
                "simultaneously executing methods should be within 50 milliseconds of each other");
    }

    private static void verifyFirstBlockOfSimultaneouslyExecutingSuites(List<EventLog> suiteListenerStartEventLogs,
                Map<String, Long> expectedExecutionTimes, int threadPoolSize) {

        //verify different thread Ids for their test level event logs too.
        verifySimultaneousSuiteListenerStartEvents(suiteListenerStartEventLogs, threadPoolSize);
        verifyDifferentThreadIdsForEvents(suiteListenerStartEventLogs, "The thread count is " + threadPoolSize +
                " so the thread IDs for the suite listener onStart events for simultaneously executing suites " +
                "should be different. Event logs: " + suiteListenerStartEventLogs);

        for(int i = 0; i < suiteListenerStartEventLogs.size(); i ++) {
            String suiteOneName = (String)suiteListenerStartEventLogs.get(i).getData(SUITE_NAME);
            long suiteOneTime =  expectedExecutionTimes.get(suiteOneName);
            List<EventLog> suiteOneEventLogs = getAllEventLogsForSuite(suiteOneName);

            for(int j = i + 1; j < suiteListenerStartEventLogs.size(); j++) {

                String suiteTwoName = (String)suiteListenerStartEventLogs.get(i).getData(SUITE_NAME);
                long suiteTwoTime =  expectedExecutionTimes.get(suiteTwoName);
                List<EventLog> suiteTwoEventLogs = getAllEventLogsForSuite(suiteTwoName);

                long upperTimingRange = suiteOneTime > suiteTwoTime ? suiteOneTime : suiteTwoTime;

                verifyTimingOfEvents(suiteOneEventLogs, suiteTwoEventLogs, 0, upperTimingRange - 800, "The " +
                        "expected execution time of " + suiteOneName + " is " + suiteOneTime + " and the " +
                        "expected execution time of " + suiteTwoName + " is " + suiteTwoTime + ". The delay for " +
                        "the suite listener's onFinish event is 1 second. Because the suites should execute in " +
                        "parallel and started at the same time the minimum difference between the timestamps for " +
                        "the event logs for " + suiteOneName + " and " + suiteTwoName + " is 0 milliseconds and " +
                        "the maximum difference in the timestamps for their event logs is " +
                        (upperTimingRange - 800));
            }
        }
    }
    //Helper which verifies that the specified lists of test method level events are associated with the same methods
    private static void verifyEventsBelongToSameMethods(List<EventLog> firstEventLogs, List<EventLog> secondEventLogs,
            String faiMessage) {

        List<String> methodNames = new ArrayList<>();

        for(EventLog eventLog : firstEventLogs) {
            methodNames.add((String)eventLog.getData(CLASS_NAME) + (String)eventLog.getData(METHOD_NAME));
        }

        for(EventLog eventLog : secondEventLogs) {

            assertTrue(methodNames.contains((String)eventLog.getData(CLASS_NAME) +
                    (String)eventLog.getData(METHOD_NAME)), faiMessage);
        }
    }

    //Helper method that retrieves the earliest and latest timestamps for the specified list of event logs
    private static Pair<Long, Long> getEarliestAndLatestTimestamps(List<EventLog> eventLogs) {

        if(!eventLogs.isEmpty()) {
            long earliestTimestamp = eventLogs.get(0).getTimeOfEvent();
            long latestTimestamp = eventLogs.get(0).getTimeOfEvent();

            for (int i = 1; i < eventLogs.size(); i++) {
                long timestamp = eventLogs.get(i).getTimeOfEvent();

                if (timestamp < earliestTimestamp) {
                    earliestTimestamp = timestamp;
                }

                if (timestamp > latestTimestamp) {
                    latestTimestamp = timestamp;
                }
            }

            return new Pair<>(earliestTimestamp, latestTimestamp);
        }

        return null;
    }
}

