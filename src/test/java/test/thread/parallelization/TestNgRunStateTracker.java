package test.thread.parallelization;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.Thread.State.TERMINATED;

/**
 * {@code TestNgRunStateTracker} tracks state information for a TestNG run: suite listener start, suite listener end,
 * test listener start, test listener end, method listener start, method execution, and method listener completion.
 * {@code TestNgRunStateListener} can be used in conjunction with {@code TestNgRunStateTracker} to log the time and
 * thread IDs for each of these events.
 */
public class TestNgRunStateTracker {

    private static List<EventLog> eventLogs = new ArrayList<>();
    private static Set<Thread> threads = new HashSet<Thread>();

    public static void logEvent(EventLog eventLog, Thread thread) {
        synchronized(eventLogs) {
            int count = 0;

            synchronized (threads) {
                List<Thread> deadThreads = new ArrayList<>();

                for(Thread t : threads) {
                    if(t.getState() == TERMINATED) {
                        deadThreads.add(t);
                    }
                }

                for(Thread t : deadThreads) {
                    threads.remove(t);
                }

                threads.add(thread);

                for (Thread t : threads) {
                    count++;
                }
            }

            eventLog.setActiveThreadCount(count);
            eventLogs.add(eventLog);
        }
    }

    //Get all event logs for all suites
    public static List<EventLog> getAllEventLogs() {
        return eventLogs;
    }

    //Get all suite level event logs
    public static List<EventLog> getAllSuiteLevelEventLogs() {
        List<EventLog> suiteEventLogs = new ArrayList<>();

        for(EventLog eventLog : eventLogs) {
            if(isSuiteLevelEventLog(eventLog)) {
                suiteEventLogs.add(eventLog);
            }
        }
        return suiteEventLogs;
    }

    public static List<EventLog> getSuiteListenerStartEventLogs() {
        List<EventLog> suiteStartEventLogs = new ArrayList<>();

        for(EventLog eventLog : eventLogs) {
            if(eventLog.getEvent() == TestNgRunEvent.LISTENER_SUITE_START) {
                suiteStartEventLogs.add(eventLog);
            }
        }

        return suiteStartEventLogs;
    }

    public static List<EventLog> getSuiteListenerFinishEventLogs() {
        List<EventLog> suiteFinishEventLogs = new ArrayList<>();

        for(EventLog eventLog : eventLogs) {
            if(eventLog.getEvent() == TestNgRunEvent.LISTENER_SUITE_FINISH) {
                suiteFinishEventLogs.add(eventLog);
            }
        }

        return suiteFinishEventLogs;
    }

    //Get all suite level event logs associated with the specified suite
    public static List<EventLog> getSuiteLevelEventLogsForSuite(String suiteName) {
        List<EventLog> suiteEventLogs = new ArrayList<>();

        for(EventLog eventLog : eventLogs) {
            if(isSuiteLevelEventLog(eventLog) && belongsToSuite(suiteName, eventLog)) {
                suiteEventLogs.add(eventLog);
            }
        }
        return suiteEventLogs;
    }

    //Get all event logs associated with the specified suite
    public static List<EventLog> getAllEventLogsForSuite(String suiteName) {
        List<EventLog> suiteEventLogs = new ArrayList<>();

        for(EventLog eventLog : eventLogs) {
            if(belongsToSuite(suiteName, eventLog)) {
                suiteEventLogs.add(eventLog);
            }
        }
        return suiteEventLogs;
    }

    //Get event logs for the specified suite and event
    public static List<EventLog> getEventLogsByEventTypeForSuite(String suiteName, TestNgRunEvent event) {
        List<EventLog> suiteEventLogs = new ArrayList<>();

        for(EventLog eventLog : eventLogs) {
            if(eventLog.getEvent() == event && belongsToSuite(suiteName, eventLog)) {
                suiteEventLogs.add(eventLog);
            }
        }

        return suiteEventLogs ;
    }

    //Get the event log for the suite listener's onStart method for the specified suite
    public static EventLog getSuiteListenerStartEventLog(String suiteName) {
        for(EventLog eventLog : eventLogs) {
            if(eventLog.getEvent() == TestNgRunEvent.LISTENER_SUITE_START && belongsToSuite(suiteName, eventLog)) {
                return eventLog;
            }
        }

        return null;
    }

    //Get the event log for the suite listener's onStart method for the specified suite
    public static EventLog getSuiteListenerFinishEventLog(String suiteName) {
        for(EventLog eventLog : eventLogs) {
            if(eventLog.getEvent() == TestNgRunEvent.LISTENER_SUITE_FINISH && belongsToSuite(suiteName, eventLog)) {
                return eventLog;
            }
        }

        return null;
    }

    //Get the timestamp for the suite listener's onStart method for the specified suite
    public static Long getSuiteListenerStartTimestamp(String suiteName) {
        EventLog eventLog = getSuiteListenerStartEventLog(suiteName);

        return eventLog == null ? null : eventLog.getTimeOfEvent();
    }

    //Get the timestamp for suite listener's onFinish method for the specified suite
    public static Long getSuiteListenerFinishTimestamp(String suiteName) {
        EventLog eventLog = getSuiteListenerFinishEventLog(suiteName);

        return eventLog == null ? null : eventLog.getTimeOfEvent();
    }

    //Get the threadId for suite listener's onStart method for the specified suite
    public static Long getSuiteListenerStartThreadId(String suiteName) {
        EventLog eventLog = getSuiteListenerStartEventLog(suiteName);

        return eventLog == null ? null : eventLog.getThreadId();
    }

    //Get the threadId for suite listener's onFinish method for the specified suite
    public static Long getSuiteListenerFinishThreadId(String suiteName) {
        EventLog eventLog = getSuiteListenerFinishEventLog(suiteName);

        return eventLog == null ? null : eventLog.getThreadId();
    }

    public static Integer getSuiteListenerStartActiveThreadCount(String suiteName) {
        EventLog eventLog = getSuiteListenerStartEventLog(suiteName);

        return eventLog == null ? null : eventLog.getActiveThreadCount();
    }

    public static Integer getSuiteListenerFinishActiveThreadCount(String suiteName) {
        EventLog eventLog = getSuiteListenerFinishEventLog(suiteName);

        return eventLog == null ? null : eventLog.getActiveThreadCount();
    }

    //Get all test level event logs
    public static List<EventLog> getAllTestLevelEventLogs() {
        List<EventLog> testEventLogs = new ArrayList<>();

        for(EventLog eventLog : eventLogs) {
            if(isTestLevelEventLog(eventLog)) {
                testEventLogs.add(eventLog);
            }
        }
        return testEventLogs;
    }

    //Get all test level event logs for the specified suite
    public static List<EventLog> getTestLevelEventLogsForSuite(String suiteName) {
        List<EventLog> testEventLogs = new ArrayList<>();

        for(EventLog eventLog : eventLogs) {
            if(isTestLevelEventLog(eventLog) && belongsToSuite(suiteName, eventLog)) {
                testEventLogs.add(eventLog);
            }
        }
        return testEventLogs;
    }

    public static List<EventLog> getTestListenerStartEventLogsForSuite(String suiteName) {
        List<EventLog> testStartEventLogs = new ArrayList<>();

        for(EventLog eventLog : eventLogs) {
            if(eventLog.getEvent() == TestNgRunEvent.LISTENER_TEST_START && belongsToSuite(suiteName, eventLog)) {
                testStartEventLogs.add(eventLog);
            }
        }

        return testStartEventLogs;
    }

    public static List<EventLog> getTestListenerFinishEventLogsForSuite(String suiteName) {
        List<EventLog> testFinishEventLogs = new ArrayList<>();

        for(EventLog eventLog : eventLogs) {
            if(eventLog.getEvent() == TestNgRunEvent.LISTENER_TEST_FINISH && belongsToSuite(suiteName, eventLog)) {
                testFinishEventLogs.add(eventLog);
            }
        }

        return testFinishEventLogs;
    }

    //Get all test level event logs for with the specified test
    public static List<EventLog> getTestLevelEventLogsForTest(String suiteName, String testName) {
        List<EventLog> testEventLogs = new ArrayList<>();

        for(EventLog eventLog : eventLogs) {
            if(isTestLevelEventLog(eventLog) && belongsToTest(suiteName, testName, eventLog)) {
                testEventLogs.add(eventLog);
            }
        }
        return testEventLogs;
    }

    //Get all event logs associated with the specified test
    public static List<EventLog> getAllEventLogsForTest(String suiteName, String testName) {
        List<EventLog> testEventLogs = new ArrayList<>();

        for(EventLog eventLog : eventLogs) {
            if(belongsToTest(suiteName, testName, eventLog)) {
                testEventLogs.add(eventLog);
            }
        }
        return testEventLogs;
    }

    //Get event logs for the specified test and event
    public static List<EventLog> getEventLogsByEventTypeForTest(String suiteName, String testName, TestNgRunEvent
            event) {
        List<EventLog> testEventLogs = new ArrayList<>();

        for(EventLog eventLog : eventLogs) {
            if(eventLog.getEvent() == event && belongsToTest(suiteName, testName, eventLog)) {
                testEventLogs.add(eventLog);
            }
        }

        return testEventLogs ;
    }

    //Get event log for test listener's onStart method for the specified test
    public static EventLog getTestListenerStartEventLog(String suiteName, String testName) {

        for(EventLog eventLog : eventLogs) {
            if(eventLog.getEvent() == TestNgRunEvent.LISTENER_TEST_START &&
                    belongsToTest(suiteName, testName, eventLog)) {
                return eventLog;
            }
        }

        return null;
    }

    //Get event log for test listener's onFinish method for the specified test
    public static EventLog getTestListenerFinishEventLog(String suiteName, String testName) {

        for(EventLog eventLog : eventLogs) {
            if(eventLog.getEvent() == TestNgRunEvent.LISTENER_TEST_FINISH &&
                    belongsToTest(suiteName, testName, eventLog)) {
                return eventLog;
            }
        }

        return null;
    }

    //Get the timestamp for test listener's onStart method for the specified test
    public static Long getTestListenerStartTimestamp(String suiteName, String testName) {
        EventLog eventLog = getTestListenerStartEventLog(suiteName, testName);

        return eventLog == null ? null : eventLog.getTimeOfEvent();
    }

    //Get the timestamp for test listener's onFinish method for the specified test
    public static Long getTestListenerFinishTimestamp(String suiteName, String testName) {
        EventLog eventLog = getTestListenerFinishEventLog(suiteName, testName);

        return eventLog == null ? null : eventLog.getTimeOfEvent();
    }

    //Get the threadId for test listener's onStart method for the specified test
    public static Long getTestListenerStartThreadId(String suiteName, String testName) {
        EventLog eventLog = getTestListenerStartEventLog(suiteName, testName);

        return eventLog == null ? null : eventLog.getThreadId();
    }

    //Get the threadId for test listener's onFinish method for the specified test
    public static Long getTestListenerFinishThreadId(String suiteName, String testName) {
        EventLog eventLog = getTestListenerFinishEventLog(suiteName, testName);

        return eventLog == null ? null : eventLog.getThreadId();
    }

    public static Integer getTestListenerStartActiveThreadCount(String suiteName, String testName) {
        EventLog eventLog = getTestListenerStartEventLog(suiteName, testName);

        return eventLog == null ? null : eventLog.getActiveThreadCount();
    }

    public static Integer getTestListenerFinishActiveThreadCount(String suiteName, String testName) {
        EventLog eventLog = getTestListenerFinishEventLog(suiteName, testName);

        return eventLog == null ? null : eventLog.getActiveThreadCount();
    }

    //Get all test method level event logs
    public static List<EventLog> getAllTestMethodLevelEventLogs() {
        List<EventLog> testMethodEventLogs = new ArrayList<>();

        for(EventLog eventLog : eventLogs) {
            if(isTestMethodLevelEventLog(eventLog)) {
                testMethodEventLogs.add(eventLog);
            }
        }
        return testMethodEventLogs;
    }

    //Get all test method level event logs for the specified suite
    public static List<EventLog> getTestMethodLevelEventLogsForSuite(String suiteName) {
        List<EventLog> testMethodEventLogs = new ArrayList<>();

        for(EventLog eventLog : eventLogs) {
            if(isTestMethodLevelEventLog(eventLog) && belongsToSuite(suiteName, eventLog)) {
                testMethodEventLogs.add(eventLog);
            }
        }
        return testMethodEventLogs;
    }

    //Get all test method level event logs for the specified test
    public static List<EventLog> getTestMethodLevelEventLogsForTest(String suiteName, String testName) {
        List<EventLog> testMethodEventLogs = new ArrayList<>();

        for(EventLog eventLog : eventLogs) {
            if(isTestMethodLevelEventLog(eventLog) && belongsToTest(suiteName, testName, eventLog)) {
                testMethodEventLogs.add(eventLog);
            }
        }
        return testMethodEventLogs;
    }

    public static List<EventLog> getTestMethodListenerStartEventLogsForTest(String suiteName, String testName) {
        List<EventLog> testMethodStartEventLogs = new ArrayList<>();

        for(EventLog eventLog : eventLogs) {
            if(eventLog.getEvent() == TestNgRunEvent.LISTENER_TEST_METHOD_START && belongsToTest(suiteName, testName,
                    eventLog)) {
                testMethodStartEventLogs.add(eventLog);
            }
        }

        return testMethodStartEventLogs;
    }

    public static List<EventLog> getTestMethodListenerPassEventLogsForTest(String suiteName, String testName) {
        List<EventLog> testMethodPassEventLogs = new ArrayList<>();

        for(EventLog eventLog : eventLogs) {
            if(eventLog.getEvent() == TestNgRunEvent.LISTENER_TEST_METHOD_PASS && belongsToTest(suiteName, testName,
                    eventLog)) {
                testMethodPassEventLogs.add(eventLog);
            }
        }

        return testMethodPassEventLogs;
    }

    public static List<EventLog> getTestMethodExecutionEventLogsForTest(String suiteName, String testName) {
        List<EventLog> testMethodExecuteEventLogs = new ArrayList<>();

        for(EventLog eventLog : eventLogs) {
            if(eventLog.getEvent() == TestNgRunEvent.TEST_METHOD_EXECUTION && belongsToTest(suiteName, testName,
                    eventLog)) {
                testMethodExecuteEventLogs.add(eventLog);
            }
        }

        return testMethodExecuteEventLogs;
    }

    //Get the test method level event logs for the test methods from the specified suite, test and test class, separated
    //out in a map where the keys are the class instances on which the methods were run.
    public static Multimap<Object, EventLog> getTestMethodEventLogsForClass(String suiteName, String testName,
            String className) {
        Multimap<Object,EventLog> testMethodEventLogs = ArrayListMultimap.create();

        for(EventLog eventLog : eventLogs) {
            if(isTestMethodLevelEventLog(eventLog) && belongsToClass(suiteName, testName, className, eventLog)) {
                testMethodEventLogs.put(eventLog.getData(EventInfo.CLASS_INSTANCE), eventLog);
            }
        }

        return testMethodEventLogs;
    }

    //Get the test method level event logs for the test method from the specified suite, test and test class, separated
    //out in a map where the keys are the class instances on which the method was run.
    public static Multimap<Object, EventLog> getTestMethodEventLogsForMethod(String suiteName, String testName, String
            className, String methodName) {
        Multimap<Object,EventLog> testMethodEventLogs = ArrayListMultimap.create();

        for(EventLog eventLog : eventLogs) {
            if(belongsToMethod(suiteName, testName, className, methodName, eventLog)) {
                testMethodEventLogs.put(eventLog.getData(EventInfo.CLASS_INSTANCE), eventLog);
            }
        }
        return testMethodEventLogs;
    }

    //Get the test method event logs of the specified type for the specified test class from the specified suite and
    //test in a multimap where the keys are the class instances for the test class
    public static Multimap<Object, EventLog> getTestMethodEventLogsByEventTypeForClass(String suiteName, String
            testName, String className, TestNgRunEvent event) {
        Multimap<Object,EventLog> testMethodEventLogs = ArrayListMultimap.create();

        for(EventLog eventLog : eventLogs) {
            if(eventLog.getEvent() == event && belongsToClass(suiteName, testName, className, eventLog)) {
                testMethodEventLogs.put(eventLog.getData(EventInfo.CLASS_INSTANCE), eventLog);
            }
        }

        return testMethodEventLogs ;
    }

    //Get the test method level event logs for the test method from the specified suite, test, test class, and event
    //separated out in a map where the keys are the class instances on which the method was run.
    public static Map<Object, EventLog> getTestMethodEventLogsByEventTypeForMethod(String suiteName, String testName,
            String className, String methodName, TestNgRunEvent event) {

        Map<Object,EventLog> testMethodEventLogs = new HashMap<>();

        for(EventLog eventLog : eventLogs) {
            if(eventLog.getEvent() == event && belongsToMethod(suiteName, testName, className, methodName, eventLog)) {
                testMethodEventLogs.put(eventLog.getData(EventInfo.CLASS_INSTANCE), eventLog);
            }
        }

        return testMethodEventLogs ;
    }

    //Get the event logs for test method listener's onTestStart method for the specified test method from the
    //specified suite, test and test class as a map where the keys are the test class instances on which the method was
    //run
    public static Map<Object,EventLog> getTestMethodListenerStartEventLogsForMethod(String suiteName, String testName,
            String className, String methodName) {
        return getTestMethodEventLogsByEventTypeForMethod(suiteName, testName, className, methodName,
                TestNgRunEvent.LISTENER_TEST_METHOD_START);
    }

    //Get the event logs for test method listener's onTestSuccess method for the specified test method from the
    //specified suite, test and test class as a map where the keys are the test class instances on which the method was
    //run
    public static Map<Object,EventLog> getTestMethodListenerPassEventLogsForMethod(String suiteName, String testName,
            String className, String methodName) {

        Map<Object, EventLog> testMethodEventLogs = new HashMap<>();

        for(EventLog eventLog : eventLogs) {
            if(belongsToMethod(suiteName, testName, className, methodName, eventLog) &&
                    eventLog.getEvent() == TestNgRunEvent.LISTENER_TEST_METHOD_PASS) {
                testMethodEventLogs.put(eventLog.getData(EventInfo.CLASS_INSTANCE), eventLog);
            }
        }

        return testMethodEventLogs;
    }

    //Get the event logs for test method listener's onTestFailure method for the specified test method from the
    //specified suite, test and test class as a map where the keys are the test class instances on which the method was
    //run
    public static Map<Object,EventLog> getTestMethodListenerFailEventLogsForMethod(String suiteName, String testName,
            String className, String methodName) {

        Map<Object, EventLog> testMethodEventLogs = new HashMap<>();

        for(EventLog eventLog : eventLogs) {
            if(belongsToMethod(suiteName, testName, className, methodName, eventLog) &&
                    eventLog.getEvent() == TestNgRunEvent.LISTENER_TEST_METHOD_FAIL) {
                testMethodEventLogs.put(eventLog.getData(EventInfo.CLASS_INSTANCE), eventLog);
            }
        }

        return testMethodEventLogs;
    }

    //Get the event logs for test method listener's onTestFailedButWithinSuccessPercentage method for the specified
    //test method from the specified suite, test and test class as a map where the keys are the test class instances on
    //which the method was run
    public static Map<Object,EventLog> getTestMethodListenerFailWithinSuccessPercentageEventLogsForMethod(String
            suiteName, String testName, String className, String methodName) {

        Map<Object, EventLog> testMethodEventLogs = new HashMap<>();

        for(EventLog eventLog : eventLogs) {
            if(belongsToMethod(suiteName, testName, className, methodName, eventLog) &&
                    eventLog.getEvent() == TestNgRunEvent.LISTENER_TEST_METHOD_FAIL_PERCENTAGE) {
                testMethodEventLogs.put(eventLog.getData(EventInfo.CLASS_INSTANCE), eventLog);
            }
        }

        return testMethodEventLogs;
    }

    //Get the event logs for test method listener's nTestSkipped method for the specified test method from the
    //specified suite, test and test class as a map where the keys are the test class instances on
    //which the method was run
    public static Map<Object,EventLog> getTestMethodListenerSkippedEventLogsForMethod(String suiteName, String
            testName, String className, String methodName) {

        Map<Object, EventLog> testMethodEventLogs = new HashMap<>();

        for(EventLog eventLog : eventLogs) {
            if(belongsToMethod(suiteName, testName, className, methodName, eventLog) &&
                    eventLog.getEvent() == TestNgRunEvent.LISTENER_TEST_METHOD_SKIPPED) {
                testMethodEventLogs.put(eventLog.getData(EventInfo.CLASS_INSTANCE), eventLog);
            }
        }

        return testMethodEventLogs;
    }

    //Get the event logs for execution of the test method's body for the specified test method from the
    //specified suite, test and test class as a map where the keys are the test class instances on
    //which the method was run
    public static Map<Object,EventLog> getTestMethodExecutionEventLogsForMethod(String suiteName, String
            testName, String className, String methodName) {

        Map<Object, EventLog> testMethodEventLogs = new HashMap<>();

        for(EventLog eventLog : eventLogs) {
            if(belongsToMethod(suiteName, testName, className, methodName, eventLog) &&
                    eventLog.getEvent() == TestNgRunEvent.TEST_METHOD_EXECUTION) {
                testMethodEventLogs.put(eventLog.getData(EventInfo.CLASS_INSTANCE), eventLog);
            }
        }

        return testMethodEventLogs;
    }

    //Get the timestamps for test method listener's onTestStart method for the specified test method from the specified
    //suite, test and test class separated out in a map where the keys are the class instances on which the method was
    // run.
    public static Map<Object, Long> getTestMethodListenerStartTimestamps(String suiteName, String testName, String
            className, String methodName) {
        Map<Object,Long> testMethodEventTimes = new HashMap<>();
        Map<Object,EventLog> testMethodEventLogs = getTestMethodListenerStartEventLogsForMethod(suiteName, testName,
                className, methodName);

        for(Object instance : testMethodEventLogs.keySet()) {
            testMethodEventTimes.put(instance, testMethodEventLogs.get(instance).getTimeOfEvent());
        }

        return testMethodEventTimes;
    }

    //Get the timestamps for test listener's onTestSuccess method for the specified test method from the specified
    //suite, test and test class separated out in a map where the keys are the class instances on which the method was
    //run.
    public static Map<Object, Long> getTestMethodListenerPassTimestamps(String suiteName, String testName, String
            className, String methodName) {
        Map<Object,Long> testMethodEventTimes = new HashMap<>();
        Map<Object,EventLog> testMethodEventLogs = getTestMethodListenerPassEventLogsForMethod(suiteName, testName,
                className, methodName);

        for(Object instance : testMethodEventLogs.keySet()) {
            testMethodEventTimes.put(instance, testMethodEventLogs.get(instance).getTimeOfEvent());
        }

        return testMethodEventTimes;
    }

    //Get the timestamps for test listener's onTestFailure method for the specified test method from the specified
    //suite, test and test class separated out in a map where the keys are the class instances on which the method was
    //run.
    public static Map<Object, Long> getTestMethodListenerFailTimestamps(String suiteName, String testName, String
            className, String methodName) {
        Map<Object,Long> testMethodEventTimes = new HashMap<>();
        Map<Object,EventLog> testMethodEventLogs = getTestMethodListenerFailEventLogsForMethod(suiteName, testName,
                className, methodName);

        for(Object instance : testMethodEventLogs.keySet()) {
            testMethodEventTimes.put(instance, testMethodEventLogs.get(instance).getTimeOfEvent());
        }

        return testMethodEventTimes;
    }

    //Get the timestamps for test listener's onTestFailedButWithinSuccessPercentage method for the specified test method
    //from the specified suite, test and test class separated out in a map where the keys are the class instances on
    //which the method was run.
    public static Map<Object, Long> getTestMethodListenerFailWithinSuccessPercentageTimestamps(String suiteName, String
            testName, String className, String methodName) {
        Map<Object,Long> testMethodEventTimes = new HashMap<>();
        Map<Object,EventLog> testMethodEventLogs =
                getTestMethodListenerFailWithinSuccessPercentageEventLogsForMethod(suiteName, testName, className,
                        methodName);

        for(Object instance : testMethodEventLogs.keySet()) {
            testMethodEventTimes.put(instance, testMethodEventLogs.get(instance).getTimeOfEvent());
        }

        return testMethodEventTimes;
    }

    public static Map<Object, Long> getTestMethodListenerSkipTimestamps(String suiteName, String testName, String
            className, String methodName) {
        Map<Object,Long> testMethodEventTimes = new HashMap<>();
        Map<Object,EventLog> testMethodEventLogs = getTestMethodListenerSkippedEventLogsForMethod(suiteName, testName,
                className, methodName);

        for(Object instance : testMethodEventLogs.keySet()) {
            testMethodEventTimes.put(instance, testMethodEventLogs.get(instance).getTimeOfEvent());
        }

        return testMethodEventTimes;
    }

    //Get the timestamps for execution of the test method's body for the specified test method from the specified
    //suite, test and test class separated out in a map where the keys are the class instances on which the method was
    //run.
    public static Map<Object, Long> getTestMethodExecutionTimestamps(String suiteName, String testName, String
            className, String methodName) {
        Map<Object,Long> testMethodEventTimes = new HashMap<>();
        Map<Object,EventLog> testMethodEventLogs = getTestMethodExecutionEventLogsForMethod(suiteName, testName,
                className, methodName);

        for(Object instance : testMethodEventLogs.keySet()) {
            testMethodEventTimes.put(instance, testMethodEventLogs.get(instance).getTimeOfEvent());
        }

        return testMethodEventTimes;
    }

    //Get the thread IDs for test method listener's onTestStart method for the specified test method from the specified
    //suite, test and test class separated out in a map where the keys are the class instances on which the method was
    //run.
    public static Map<Object, Long> getTestMethodListenerStartThreadIds(String suiteName, String testName,
            String className, String methodName) {
        Map<Object,Long> testMethodEventThreadIds = new HashMap<>();
        Map<Object,EventLog> testMethodEventLogs = getTestMethodListenerStartEventLogsForMethod(suiteName, testName,
                className, methodName);

        for(Object instance : testMethodEventLogs.keySet()) {
            testMethodEventThreadIds.put(instance, testMethodEventLogs.get(instance).getThreadId());
        }

        return testMethodEventThreadIds;
    }

    //Get the thread IDs for test listener's onTestSuccess method for the specified test method from the specified
    //suite, test and test class separated out in a map where the keys are the class instances on which the method was
    //run.
    public static Map<Object, Long> getTestMethodListenerPassThreadIds(String suiteName, String testName, String
            className, String methodName) {
        Map<Object,Long> testMethodEventThreadIds = new HashMap<>();
        Map<Object,EventLog> testMethodEventLogs = getTestMethodListenerPassEventLogsForMethod(suiteName, testName,
                className, methodName);

        for(Object instance : testMethodEventLogs.keySet()) {
            testMethodEventThreadIds.put(instance, testMethodEventLogs.get(instance).getThreadId());
        }

        return testMethodEventThreadIds;
    }

    //Get the thread IDs for test listener's onTestFailure method for the specified test method from the specified
    //suite, test and test class separated out in a map where the keys are the class instances on which the method was
    //run.
    public static Map<Object, Long> getTestMethodListenerFailThreadIds(String suiteName, String testName, String
            className, String methodName) {
        Map<Object,Long> testMethodEventThreadIds = new HashMap<>();
        Map<Object,EventLog> testMethodEventLogs = getTestMethodListenerFailEventLogsForMethod(suiteName, testName,
                className, methodName);

        for(Object instance : testMethodEventLogs.keySet()) {
            testMethodEventThreadIds.put(instance, testMethodEventLogs.get(instance).getThreadId());
        }

        return testMethodEventThreadIds;
    }

    //Get the thread IDs for test listener's onTestFailedButWithinSuccessPercentage method for the specified test
    //method from the specified suite, test and test class separated out in a map where the keys are the class
    //instances on which the method was run.
    public static Map<Object, Long> getTestMethodListenerFailWithSuccessPercentageThreadIds(String suiteName, String
            testName, String className, String methodName) {
        Map<Object,Long> testMethodEventThreadIds = new HashMap<>();
        Map<Object,EventLog> testMethodEventLogs =
                getTestMethodListenerFailWithinSuccessPercentageEventLogsForMethod(suiteName, testName, className,
                        methodName);

        for(Object instance : testMethodEventLogs.keySet()) {
            testMethodEventThreadIds.put(instance, testMethodEventLogs.get(instance).getThreadId());
        }

        return testMethodEventThreadIds;
    }

    //Get the thread IDs for test listener's onTestSkipped method for the specified test method from the specified
    //suite, test and test class separated out in a map where the keys are the class instances on which the method was
    //run.
    public static Map<Object, Long> getTestMethodListenerSkipThreadIds(String suiteName, String testName, String
            className, String methodName) {
        Map<Object,Long> testMethodEventThreadIds = new HashMap<>();
        Map<Object,EventLog> testMethodEventLogs = getTestMethodListenerSkippedEventLogsForMethod(suiteName, testName,
                className, methodName);

        for(Object instance : testMethodEventLogs.keySet()) {
            testMethodEventThreadIds.put(instance, testMethodEventLogs.get(instance).getThreadId());
        }

        return testMethodEventThreadIds;
    }

    //Get the thread IDs for execution of the test method's body for the specified test method from the specified
    //suite, test and test class separated out in a map where the keys are the class instances on which the method was
    //run.
    public static Map<Object, Long> getTestMethodExecutionThreadIds(String suiteName, String testName, String
            className, String methodName) {
        Map<Object,Long> testMethodEventThreadIds = new HashMap<>();
        Map<Object,EventLog> testMethodEventLogs = getTestMethodExecutionEventLogsForMethod(suiteName, testName,
                className, methodName);

        for(Object instance : testMethodEventLogs.keySet()) {
            testMethodEventThreadIds.put(instance, testMethodEventLogs.get(instance).getThreadId());
        }

        return testMethodEventThreadIds;
    }

    public static void reset() {
        eventLogs = new ArrayList<>();
    }

    private static boolean isSuiteLevelEventLog(EventLog eventLog) {
        return eventLog.getEvent() == TestNgRunEvent.LISTENER_SUITE_START ||
                eventLog.getEvent() == TestNgRunEvent.LISTENER_SUITE_FINISH;
    }

    private static boolean isTestLevelEventLog(EventLog eventLog) {
        return eventLog.getEvent() == TestNgRunEvent.LISTENER_TEST_START ||
                eventLog.getEvent() == TestNgRunEvent.LISTENER_TEST_FINISH;
    }

    private static boolean isTestMethodLevelEventLog(EventLog eventLog) {
        return eventLog.getEvent() == TestNgRunEvent.LISTENER_TEST_METHOD_START ||
                eventLog.getEvent() == TestNgRunEvent.LISTENER_TEST_METHOD_PASS ||
                eventLog.getEvent() == TestNgRunEvent.LISTENER_TEST_METHOD_FAIL ||
                eventLog.getEvent() == TestNgRunEvent.LISTENER_TEST_METHOD_FAIL_PERCENTAGE ||
                eventLog.getEvent() == TestNgRunEvent.LISTENER_TEST_METHOD_SKIPPED ||
                eventLog.getEvent() == TestNgRunEvent.TEST_METHOD_EXECUTION;
    }

    private static boolean belongsToSuite(String suiteName, EventLog eventLog) {
        return suiteName.equals(eventLog.getData(EventInfo.SUITE_NAME));
    }

    private static boolean belongsToTest(String suiteName, String testName, EventLog eventLog) {
        return suiteName.equals(eventLog.getData(EventInfo.SUITE_NAME)) &&
                testName.equals(eventLog.getData(EventInfo.TEST_NAME));
    }

    private static boolean belongsToClass(String suiteName, String testName, String className, EventLog eventLog) {
        return suiteName.equals(eventLog.getData(EventInfo.SUITE_NAME)) &&
                testName.equals(eventLog.getData(EventInfo.TEST_NAME)) &&
                className.equals(eventLog.getData(EventInfo.CLASS_NAME));
    }

    private static boolean belongsToMethod(String suiteName, String testName, String className, String methodName,
            EventLog eventLog) {
        return suiteName.equals(eventLog.getData(EventInfo.SUITE_NAME)) &&
                testName.equals(eventLog.getData(EventInfo.TEST_NAME)) &&
                className.equals(eventLog.getData(EventInfo.CLASS_NAME)) &&
                methodName.equals(eventLog.getData(EventInfo.METHOD_NAME));
    }

    public enum TestNgRunEvent {
        LISTENER_SUITE_START,
        LISTENER_SUITE_FINISH,
        LISTENER_TEST_START,
        LISTENER_TEST_FINISH,
        LISTENER_TEST_METHOD_START,
        TEST_METHOD_EXECUTION,
        LISTENER_TEST_METHOD_PASS,
        LISTENER_TEST_METHOD_FAIL,
        LISTENER_TEST_METHOD_FAIL_PERCENTAGE,
        LISTENER_TEST_METHOD_SKIPPED,
    }

    public enum EventInfo {
        SUITE_NAME,
        TEST_NAME,
        CLASS_NAME,
        METHOD_NAME,
        CLASS_INSTANCE
    }

    public static class EventLog {
        private TestNgRunEvent event;
        private long timeOfEvent;
        private long threadId;
        private int activeThreadCount;

        private Map<EventInfo, Object> data = new HashMap<>();

        public void setEvent(TestNgRunEvent event) {
            this.event = event;
        }

        public TestNgRunEvent getEvent() {
            return event;
        }

        public void setTimeOfEvent(long timeOfEvent) {
            this.timeOfEvent = timeOfEvent;
        }

        public long getTimeOfEvent() {
            return timeOfEvent;
        }

        public void setThreadId(long threadId) {
            this.threadId = threadId;
        }

        public int getActiveThreadCount() { return activeThreadCount; }

        public long getThreadId() {
            return threadId;
        }

        public void addData(EventInfo key, Object value) {
            data.put(key, value);
        }

        public Object getData(EventInfo key) {
            return data.get(key);
        }

        private void setActiveThreadCount(int count) {
            this.activeThreadCount = count;
        }

        public static EventLogBuilder builder() {
            return new EventLogBuilder();
        }

        @Override
        public String toString() {
            return "EventLog{" +
                    "event=" + event +
                    ", timeOfEvent=" + timeOfEvent +
                    ", threadId=" + threadId +
                    ", activeThreadCount=" + activeThreadCount +
                    ", data=" + data +
                    '}';
        }
    }

    public static class EventLogBuilder {
        private EventLog eventLog = new EventLog();

        EventLogBuilder setEvent(TestNgRunEvent event) {
            eventLog.setEvent(event);
            return this;
        }

        EventLogBuilder setTimeOfEvent(long timeOfEvent) {
            eventLog.setTimeOfEvent(timeOfEvent);
            return this;
        }

        EventLogBuilder setThreadId(long threadId) {
            eventLog.setThreadId(threadId);
            return this;
        }

        EventLogBuilder addData(EventInfo key, Object value) {
            eventLog.addData(key, value);
            return this;
        }

        EventLog build() {
            return eventLog;
        }
    }
}
