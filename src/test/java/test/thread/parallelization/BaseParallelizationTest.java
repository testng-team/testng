package test.thread.parallelization;

import com.google.common.collect.Multimap;
import org.testng.annotations.Test;
import org.testng.internal.collections.Pair;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import test.thread.parallelization.TestNgRunStateTracker.EventLog;
import test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import static org.testng.Assert.fail;
import static test.thread.parallelization.TestNgRunStateTracker.EventInfo.CLASS_NAME;
import static test.thread.parallelization.TestNgRunStateTracker.EventInfo.GROUPS_BELONGING_TO;
import static test.thread.parallelization.TestNgRunStateTracker.EventInfo.GROUPS_DEPENDED_ON;
import static test.thread.parallelization.TestNgRunStateTracker.EventInfo.METHODS_DEPENDED_ON;
import static test.thread.parallelization.TestNgRunStateTracker.EventInfo.METHOD_NAME;
import static test.thread.parallelization.TestNgRunStateTracker.EventInfo.SUITE_NAME;
import static test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent.LISTENER_SUITE_START;
import static test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent.LISTENER_SUITE_FINISH;
import static test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent.LISTENER_TEST_FINISH;
import static test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent.LISTENER_TEST_METHOD_PASS;
import static test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent.LISTENER_TEST_METHOD_START;
import static test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent.LISTENER_TEST_START;
import static test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent.TEST_METHOD_EXECUTION;
import static test.thread.parallelization.TestNgRunStateTracker.getTestMethodEventLogsForMethod;
import static test.thread.parallelization.TestNgRunStateTracker.getTestMethodEventLogsForMethodsBelongingToGroupsDependedOn;
import static test.thread.parallelization.TestNgRunStateTracker.getTestMethodEventLogsForMethodsDependedOn;

public class BaseParallelizationTest extends SimpleBaseTest {

    private static final Logger logger = Logger.getLogger(BaseParallelizationTest.class.getCanonicalName());

    {
        System.setProperty("java.util.logging.SimpleFormatter.format","%n [%4$s] %2$s - %5$s");
        logger.setLevel(Level.INFO);
    }

//    String x = "%1$tF %1$tT";

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

    public static void addParams(XmlSuite suite, String suiteName, String testName, String sleepFor, String
            dataProviderParam) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("suiteName", suiteName);
        parameters.put("testName", testName);
        parameters.put("sleepFor", sleepFor);
        parameters.put("dataProviderParam", dataProviderParam);

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
        for (EventLog eventLog : eventsFromLaterThread) {
            assertTrue(eventLog.getThreadId() > earlierThreadId, failMessage);
        }
    }

//    //Verify that the timestamps of the list of event logs are all within the specified range of each other. Print
//    //the specified failure message if the assertion on the timing range fails.
//    public static void verifyTimingOfEvents(List<EventLog> eventLogs, long timingRange, String failMessage) {
//        if(!eventLogs.isEmpty()) {
//            Pair<Long,Long> timestamps = getEarliestAndLatestTimestamps(eventLogs);
//            verifyTimestampDifference(timestamps.second(), timestamps.first(), timingRange, failMessage);
//        }
//    }


//    public static void verifyTimingOfEvents(EventLog eventLogOne, EventLog eventLogTwo, long timingRange, String
//            failMessage) {
//        verifyTimestampDifference(eventLogOne.getTimeOfEvent(), eventLogTwo.getTimeOfEvent(), timingRange, failMessage);
//    }
//
//    //Verify that the timestamps of the first list of events are all within the specified range from the timestamps
//    //of the second list of events. Print the specified failure message if the assertion on the timing range fails.
//    public static void verifyTimingOfEvents(List<EventLog> firstEventLogs, List<EventLog> secondEventLogs,
//            long lowerTimingRange, long upperTimingRange, String failMessage) {
//
//        if(!firstEventLogs.isEmpty() && !secondEventLogs.isEmpty()) {
//            Pair<Long, Long> timestampsListOne = getEarliestAndLatestTimestamps(firstEventLogs);
//            Pair<Long, Long> timestampsListTwo = getEarliestAndLatestTimestamps(secondEventLogs);
//
//            verifyTimestampDifference(timestampsListTwo.first(), timestampsListOne.first(), lowerTimingRange,
//                    upperTimingRange, failMessage);
//            verifyTimestampDifference(timestampsListTwo.first(), timestampsListOne.second(), lowerTimingRange,
//                    upperTimingRange, failMessage);
//            verifyTimestampDifference(timestampsListTwo.second(), timestampsListOne.first(), lowerTimingRange,
//                    upperTimingRange, failMessage);
//            verifyTimestampDifference(timestampsListTwo.second(), timestampsListOne.second(), lowerTimingRange,
//                    upperTimingRange, failMessage);
//        }
//    }

//    //Verify that the difference between the two specified timestamps is within the specfied upper and lower bound. The
//    //difference is calculated as an absolute value.
//    public static void verifyTimestampDifference(long timestampOne, long timestampTwo, long lowerTimingRange,
//            long upperTimingRange, String failMessage) {
//        assertTrue(Math.abs(timestampOne - timestampTwo) <= upperTimingRange &&
//                Math.abs(timestampOne - timestampTwo) >= lowerTimingRange, failMessage + ". Difference: " +
//                Math.abs(timestampOne - timestampTwo));
//    }
//
//    public static void verifyTimestampDifference(long timestampOne, long timestampTwo, long timingRange, String
//            failMessage) {
//        assertTrue(Math.abs(timestampOne - timestampTwo) <= timingRange, failMessage + ". Difference: " +
//                Math.abs(timestampOne - timestampTwo));
//    }

    //Verify that the specified event logs all have timestamps between the specified earlier and later event logs.
    //Print the specified failure message if the assertion on the timestamps fails.
    public static void verifyEventsOccurBetween(EventLog earlierEventLog, List<EventLog> inBetweenEventLogs, EventLog
            laterEventLog, String failMessage) {
        for (EventLog eventLog : inBetweenEventLogs) {
            assertTrue(eventLog.getTimeOfEvent() > earlierEventLog.getTimeOfEvent() &&
                    eventLog.getTimeOfEvent() < laterEventLog.getTimeOfEvent(), failMessage);
        }
    }

    //Verify that the timestamps for the events if the specified list are all increasing. That each the timestamps of
    //a given event logger is later than the event logger immediately preceding it. Print the specified failure message if
    //the assertion on the timestamps fails
    public static void verifySequentialTimingOfEvents(List<EventLog> eventLogs, String failMessage) {
        for (int i = 0; i + 1 < eventLogs.size(); i++) {
            assertTrue(eventLogs.get(i).getTimeOfEvent() < eventLogs.get(i + 1).getTimeOfEvent(), failMessage);
        }
    }

    //Verify that the first list of event logs all have timestamps earlier than all the events in the second list of
    //event logs. Print the specified failure message if the assertion on the timestamps fails.
    public static void verifySequentialTimingOfEvents(List<EventLog> firstEventLogs, List<EventLog> secondEventLogs,
            String failMessage) {

        if (!firstEventLogs.isEmpty() && !secondEventLogs.isEmpty()) {
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

    //Verify that the test methods declared in the specified list of classes have the specified number of class
    //instances associated with them for the specified suite and test.
    public static void verifyNumberOfInstancesOfTestClassesForMethods(String suiteName, String testName, List<Class<?>>
            classes, int... numInstances) {

        for(int i = 0; i < numInstances.length; i++) {
            verifyNumberOfInstancesOfTestClassForMethods(suiteName, testName, classes.get(i), numInstances[i]);
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
        for (Class<?> clazz : classes) {
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
    //accordance with the thread count. This verification is for blocks of parallel methods that have the same sleep
    //delays for their execution bodies and which do not have any BeforeMethod AfterMethod, BeforeGroup or AfterGroup
    //configuration methods.
    public static void verifySimultaneousTestMethods(List<EventLog> testMethodEventLogs, String testName, int
            maxSimultaneousTestMethods) {

        logger.log(Level.INFO,"Verifying parallel execution of test methods for test named {0} with thread count {1}",
                new Object[] {testName, maxSimultaneousTestMethods});

        logger.log(Level.INFO, "{0} test method event logs for {1} test methods: ",
                new Object[]{ testMethodEventLogs.size(), testMethodEventLogs.size()/3} );

        //There are three test method events expected per test method: a start event, an execution event, and a test
        //method pass event. All methods take exactly the same amount of time to execute. Each one of their events
        //takes exactly the same time to execute. The reason for this is that it makes it possible to assume that blocks
        //of methods should execute in parallel in lockstep, starting and finishing at the same time.
        //
        //The TestNgRunStateListener logs the start and pass events. The test method execution event is logged by the
        //test method itself. See the sample test classes for examples. This test method verifies the parallel
        //execution of test methods for parallelization tests involving parallel-by-methods mode. Therefore, the
        //expectation is that there are simultaneously executing blocks of methods. The'size' of the block is either
        //equal to the thread count or less in the event that the total number of methods is not a multiple of the
        //thread count and we are processing the final block of methods to execute.
        //
        //This smaller, last block size is calculated using the number of events logged, the number of events logged
        // per method (3) and the thread count to find the number of events expected for that remainder block.
        int remainder = (testMethodEventLogs.size() / 3) % maxSimultaneousTestMethods;

        int numBlocks = testMethodEventLogs.size() / 3 < maxSimultaneousTestMethods ? 1 :
                (testMethodEventLogs.size() / 3) / maxSimultaneousTestMethods + (remainder > 0 ? 1 : 0);

        log(testMethodEventLogs.size(), maxSimultaneousTestMethods, remainder);

        int loopNum = 1;

        //Loop over the event logs. The increment is equal the thread count times the number of events logged for each
        //test method.
        for (int i = 1; i < testMethodEventLogs.size(); i = i + maxSimultaneousTestMethods * 3) {

            logger.log(Level.INFO, "Processing block {0} of {1}", new Object[] {loopNum, numBlocks});

            //The size of the block is equal to the thread count or the number of methods left over in the last block
            //if the total number of methods is not a multiple of the thread count. Example: For a test run with 19
            //methods in total and a thread count of 7, the remainder is 5 methods. The last block of methods to execute
            //would have 5 methods executing simultaneously. If the remainder is non-zero, and we are processing the
            //last block of methods then the block size is less than the thread count. Otherwise, the block size is
            //equal to the thread count.
            int blockSize = (remainder != 0 && testMethodEventLogs.size() - i < maxSimultaneousTestMethods * 3) ?
                    remainder :
                    maxSimultaneousTestMethods;

            //The expectation for the block of methods executing in parallel is that the test method start events are
            //logged first, then the test method execution events, followed by the test method pass events. These
            //offset values are used to extract the sublists of start events, execution events and pass events for the
            //block of event logs to process in the current loop execution.
            int offsetOne = (remainder != 0 && testMethodEventLogs.size() - i < maxSimultaneousTestMethods * 3) ?
                    testMethodEventLogs.size() - (remainder * 3) :
                    i - 1;

            int offsetTwo = (remainder != 0 && testMethodEventLogs.size() - i < maxSimultaneousTestMethods * 3) ?
                    testMethodEventLogs.size() - (remainder * 3) + blockSize :
                    i + maxSimultaneousTestMethods - 1;

            logger.log(Level.INFO, "Expecting {0} test method start events, followed by {0} test method execution " +
                    "events, followed by {0} test method pass events", blockSize);

            List<EventLog> eventLogMethodListenerStartSublist = testMethodEventLogs.subList(offsetOne, offsetTwo);
            List<EventLog> eventLogMethodExecuteSublist = testMethodEventLogs.subList(offsetTwo, offsetTwo + blockSize);
            List<EventLog> eventLogMethodListenerPassSublist = testMethodEventLogs.subList(offsetTwo + blockSize,
                    offsetTwo + 2 * blockSize);

            log(offsetOne, offsetTwo, blockSize, eventLogMethodListenerStartSublist, eventLogMethodExecuteSublist,
                    eventLogMethodListenerPassSublist);

            //Verify that all the events in the sublist extracted for the start events of the block of methods expected
            //to execute in parallel all have the test method start event type and that they all executed in different
            //threads.
            verifySimultaneousTestMethodListenerStartEvents(eventLogMethodListenerStartSublist, testName, blockSize);

            //Verify that all the events in the sublist extracted for the test method execution events of the block of
            //methods expected to execute in parallel all have the test method execution event type and that they all
            //executed in different threads.
            verifySimultaneousTestMethodExecutionEvents(eventLogMethodExecuteSublist, testName, blockSize);

            //Verify that the test method start events and the test method execution events in the two sublists belong
            //to the same methods. This is done by verifying that the test class names and method names are the same for
            //for both sublists.
            verifyEventsBelongToSameMethods(eventLogMethodListenerStartSublist, eventLogMethodExecuteSublist, "The " +
                    "expected maximum number of methods to execute simultaneously is " + maxSimultaneousTestMethods +
                    " for " + testName + " so no more than " + maxSimultaneousTestMethods + " methods should be " +
                    "running at the same time. The test execution event logs for a block of simultaneously running " +
                    "test methods should all belong to the same methods as the test method listener onTestStart " +
                    "event logs immediately preceding");

            //Verify that all the events in the sublist extracted for the test method pass events of the block of
            //methods expected to execute in parallel all have the test method pass event type and that they all
            //executed in different threads.
            verifySimultaneousTestMethodListenerPassEvents(eventLogMethodListenerPassSublist, testName, blockSize);

            //Verify that the test method execution events and the test method pass events in the two sublists belong
            //to the same methods. This is done by verifying that the test class names and method names are the same for
            //for both sublists.
            verifyEventsBelongToSameMethods(eventLogMethodExecuteSublist, eventLogMethodListenerPassSublist, "The " +
                    "expected maximum number of methods to execute simultaneously is " + maxSimultaneousTestMethods +
                    " for " + testName + " so no more than " + maxSimultaneousTestMethods + " methods should be " +
                    "running at the same time. The test method listener on onTestSuccess event logs for a block of " +
                    "simultaneously running test methods should all belong to the same methods as the test method " +
                    "execution event logs immediately preceding");

            loopNum++;
        }
    }

    //Verify that methods associated with the specified event logs execute simultaneously in parallel fashion, in
    //accordance with the thread count. This verification is for blocks of parallel methods, some of which use
    //non-parallel data providers without factories, so all the invocations of the methods run on the same class
    //instances. This verification is for blocks of parallel methods that have the same sleep delays for their
    //execution bodies and which do not have any BeforeMethod AfterMethod, BeforeGroup or AfterGroup
    //configuration methods.
    public static void verifyParallelTestMethodsWithNonParallelDataProvider(List<EventLog> testMethodEventLogs, String
            testName, Map<String, Integer> expectedInvocationCounts, int numUniqueMethods, int
            maxSimultaneousTestMethods) {

        logger.log(Level.INFO,"Verifying parallel execution of test methods using non-parallel data providers for " +
                "test named {0} with thread count {1}", new Object[] {testName, maxSimultaneousTestMethods});

        logger.log(Level.INFO, "{0} test method event logs for {1} unique methods: ",
                new Object[] {testMethodEventLogs.size(), numUniqueMethods});

        //Some of the test methods use non-parallel data providers without factories. All the invocations of those
        //test methods will occur serially within the same thread on the same class instances. In order to ensure that
        //the loop logic below works properly, it is necessary to keep state information about which methods are
        //supposed to be executing within a block of parallel methods that are running simultaneously. Unlike the
        //logic in verifySimultaneousTestMethods, it is not possible to assume that methods within a block of
        //simultaneously executing methods start and finish at the same time because the methods will frequently be
        //invoked a varying number of times, depending on their use of data providers.
        //
        //However, each _invocation_ of a test method should take exactly the same amount of time. There are three test
        //method events expected per test method: a start event, an execution event, and a test method pass event. All
        //test method events of the same time take the same amount of time to execute. The reason for this is that it
        //makes it possible to assume that blocks of method invocations should execute in parallel in lockstep,
        //starting and finishing at the same time.
        Map<String, EventLog> methodsExecuting = new HashMap<>();

        //This isn't actually used for any verification logic. I may remove it in the future.
        Map<String, EventLog> methodsCompleted = new HashMap<>();

        //Because this method verifies combination of parallel-by-methods mode and the use of non-parallel data
        //providers without factories, it is necessary to track the number of times that test methods are invoked in
        //order to check that this is consistent with the number of times they are expected to be invoked based on
        //their use of non-parallel data providers.
        Map<String, Integer> methodInvocationsCounts = new HashMap<>();

        //In order to verify that all invocations of test methods which use non-parallel data providers occur in the
        //same thread, it is necessary to keep track of the thread IDs of methods that are executing within a
        //block of simultaneously executing methods.
        Map<String, Long> executingMethodThreadIds = new HashMap<>();

        //The logic for determining the block size of simultaneously executing parallel methods is initially determined
        //by whether the total number of unique methods less than the thread count. If it is less than the thread count,
        //then the block size is equal to the number of unique methods. Those methods will execute in parallel
        //until all invocations of all the methods completes. Otherwise, there are more methods queued up than the
        //thread count, so the block size is equal to the thread count.
        int blockSize = numUniqueMethods >= maxSimultaneousTestMethods ? maxSimultaneousTestMethods :
                numUniqueMethods;

        int loopNum = 1;

        for (int i = 1; i < testMethodEventLogs.size(); i = i + blockSize * 3) {

            logger.log(Level.INFO, "Processing block {0}", loopNum);

            //If the loop is executing more than once, then the block size needs to be updated. The number of remaining
            //unique methods to execute determines the block size of parallel methods expected to execute
            //simultaneously.
            if(i != 1) {

                //All methods that are in the list of currently executing methods should still have invocations left.
                //Otherwise, they would have been removed from that list and added to the completed methods list.
                allExecutingMethodsHaveMoreInvocations(methodsExecuting, methodInvocationsCounts,
                        expectedInvocationCounts);

                //If there are no remaining unique methods, the block size of methods expected to be executing in
                //parallel is equal to the number of methods using data providers that are already executing. The only
                //test method event logs to verify in this loop should belong to test methods which use data providers
                //and are executed multiple times as a result.
                if(numUniqueMethods == 0) {
                    blockSize = methodsExecuting.keySet().size();
                } else {
                    //Otherwise, if the number of unique methods left is non-zero, but less than the thread count,
                    //the block size is dependent on whether the number of currently executing methods is equal to the
                    //thread count. If so, then the block size is equal to the thread count and no new unique methods
                    //will begin executing this block of methods. If the number of methods already executing is less
                    //than the thread count and the sum of the number of unique methods left and the number of currently
                    //executing methods is equal to or greater than the thread count, the block size is equal to the
                    //thread count. If the sum is less than the thread count, the block size is equal to the sum.
                    if(numUniqueMethods < maxSimultaneousTestMethods) {

                        if (methodsExecuting.keySet().size() == maxSimultaneousTestMethods ||
                                numUniqueMethods + methodsExecuting.keySet().size() >= maxSimultaneousTestMethods) {
                            blockSize = maxSimultaneousTestMethods;
                        } else {
                            blockSize = numUniqueMethods + methodsExecuting.keySet().size();
                        }
                        //If the number of unique methods left is more than or equal to the thread count, the block size
                        //is equal to the thread count.
                    } else {
                        blockSize = maxSimultaneousTestMethods;
                    }
                }
            }

            int offsetOne = i - 1;

            int offsetTwo = i + blockSize - 1;

            logger.log(Level.INFO, "Expecting {0} test method start events, followed by {0} test method execution " +
                    "events, followed by {0} test method pass events", blockSize);

            List<EventLog> eventLogMethodListenerStartSublist = testMethodEventLogs.subList(offsetOne, offsetTwo);
            List<EventLog> eventLogMethodExecuteSublist = testMethodEventLogs.subList(offsetTwo, offsetTwo + blockSize);
            List<EventLog> eventLogMethodListenerPassSublist = testMethodEventLogs.subList(offsetTwo + blockSize,
                    offsetTwo + 2 * blockSize);

            log(offsetOne, offsetTwo, blockSize, eventLogMethodListenerStartSublist, eventLogMethodExecuteSublist,
                    eventLogMethodListenerPassSublist);

            //Verify that all the events in the sublist extracted for the start events of the block of methods expected
            //to execute in parallel all have the test method start event type and that they all executed in different
            //threads. The method should return the total number of new unique methods that began executing in the
            //current block of parallel methods executing in parallel.
            int decrementUniqueMethods = verifySimultaneousTestMethodListenerStartEvents(
                    eventLogMethodListenerStartSublist, testName, blockSize, methodsExecuting,
                    executingMethodThreadIds, methodInvocationsCounts, expectedInvocationCounts
            );

            //Decrement the unique of unique methods left to execute by the number of new unique methods that began
            //execution in this block of parallel methods.
            numUniqueMethods = numUniqueMethods - decrementUniqueMethods;

            //Verify that all the events in the sublist extracted for the test method execution events of the block of
            //methods expected to execute in parallel all have the test method execution event type and that they all
            //executed in different threads.
            verifySimultaneousTestMethodExecutionEvents(eventLogMethodExecuteSublist, testName,
                    executingMethodThreadIds, blockSize);

            //Verify that the test method start events and the test method execution events in the two sublists belong
            //to the same methods. This is done by verifying that the test class names and method names are the same for
            //for both sublists.
            verifyEventsBelongToSameMethods(eventLogMethodListenerStartSublist, eventLogMethodExecuteSublist, "The " +
                    "expected maximum number of methods to execute simultaneously is " + maxSimultaneousTestMethods +
                    " for " + testName + " so no more than " + maxSimultaneousTestMethods + " methods should be " +
                    "running at the same time. The test execution event logs for a block of simultaneously running " +
                    "test methods should all belong to the same methods as the test method listener onTestStart " +
                    "event logs immediately preceding");

            for(String method : methodsExecuting.keySet()) {
                logger.log(Level.INFO, "{0} has executed {1} times. Expected to execute {2} more times.",
                        new Object[]{method, methodInvocationsCounts.get(method),
                                expectedInvocationCounts.get(method) - methodInvocationsCounts.get(method)});
            }

            //Verify that all the events in the sublist extracted for the test method pass events of the block of
            //methods expected to execute in parallel all have the test method pass event type and that they all
            //executed in different threads.
            verifySimultaneousTestMethodListenerPassEvents(eventLogMethodListenerPassSublist, testName,
                    blockSize, methodsExecuting, methodsCompleted, executingMethodThreadIds,
                    methodInvocationsCounts, expectedInvocationCounts);

            //Verify that the test method execution events and the test method pass events in the two sublists belong
            //to the same methods. This is done by verifying that the test class names and method names are the same for
            //for both sublists.
            verifyEventsBelongToSameMethods(eventLogMethodExecuteSublist, eventLogMethodListenerPassSublist, "The " +
                    "expected maximum number of methods to execute simultaneously is " + maxSimultaneousTestMethods +
                    " for " + testName + " so no more than " + maxSimultaneousTestMethods + " methods should be " +
                    "running at the same time. The test method listener on onTestSuccess event logs for a block of " +
                    "simultaneously running test methods should all belong to the same methods as the test method " +
                    "execution event logs immediately preceding");

            loopNum++;
        }
    }

    public void verifyParallelMethodsWithDependencies(List<EventLog> testMethodEventLogs, String testName, int
            maxSimultaneousTestMethods) {

        Map<String, EventLog> freeMethods = new HashMap<>();
        Map<String, EventLog> methodsCompleted = new HashMap<>();

        for(EventLog log : testMethodEventLogs) {
            String classAndMethodName = (String)log.getData(CLASS_NAME) + "." +
                    (String)log.getData(METHOD_NAME);

            if(freeMethods.get(classAndMethodName) == null) {
                if (((String[]) log.getData(METHODS_DEPENDED_ON)).length == 0 &&
                        ((String[]) log.getData(GROUPS_DEPENDED_ON)).length == 0) {
                    freeMethods.put(classAndMethodName, log);
                }
            }
        }

        int blockSize = freeMethods.size() < maxSimultaneousTestMethods ? freeMethods.size() :
                maxSimultaneousTestMethods;

        for (int i = 1; i < testMethodEventLogs.size(); i = i + blockSize * 3) {

            if(i != 1) {
                freeMethods = new HashMap<>();

                for(int j = i - 1; j < testMethodEventLogs.size(); j++) {
                    EventLog log = testMethodEventLogs.get(j);
                    String classAndMethodName = (String)log.getData(CLASS_NAME) + "." +
                            (String)log.getData(METHOD_NAME);

                    if(freeMethods.get(classAndMethodName) == null) {
                        List<String> methodsDependedOn =
                                new ArrayList<>(Arrays.asList((String[]) log.getData(METHODS_DEPENDED_ON)));
                        List<String> groupsDependedOn =
                                new ArrayList<>(Arrays.asList((String[]) log.getData(GROUPS_DEPENDED_ON)));

                        if (methodsDependedOn.size() == 0 && groupsDependedOn.size() == 0) {
                            freeMethods.put(classAndMethodName, log);
                        } else {
                            methodsDependedOn.addAll(getMethodsBelongingToGroups(groupsDependedOn, testMethodEventLogs));

                            if (methodsCompleted.keySet().containsAll(methodsDependedOn)) {
                                freeMethods.put(classAndMethodName, log);
                            }
                        }
                    }
                }

                blockSize = freeMethods.size() < maxSimultaneousTestMethods ? freeMethods.size() :
                        maxSimultaneousTestMethods;
            }

            int offsetOne = i - 1;

            int offsetTwo = i + blockSize - 1;

            List<EventLog> eventLogMethodListenerStartSublist = testMethodEventLogs.subList(offsetOne, offsetTwo);
            List<EventLog> eventLogMethodExecuteSublist = testMethodEventLogs.subList(offsetTwo, offsetTwo + blockSize);
            List<EventLog> eventLogMethodListenerPassSublist = testMethodEventLogs.subList(offsetTwo + blockSize,
                    offsetTwo + 2 * blockSize);

            verifySimultaneousTestMethodListenerStartEvents(eventLogMethodListenerStartSublist, testName, freeMethods,
                    blockSize);

            verifySimultaneousTestMethodExecutionEvents(eventLogMethodExecuteSublist, testName, blockSize);

            verifyEventsBelongToSameMethods(eventLogMethodListenerStartSublist, eventLogMethodExecuteSublist, "The " +
                    "expected maximum number of methods to execute simultaneously is " + maxSimultaneousTestMethods +
                    " for " + testName + " so no more than " + maxSimultaneousTestMethods + " methods should be " +
                    "running at the same time. The test execution event logs for a block of simultaneously running " +
                    "test methods should all belong to the same methods as the test method listener onTestStart " +
                    "event logs immediately preceding");

            verifySimultaneousTestMethodListenerPassEvents(eventLogMethodListenerPassSublist, testName,
                    methodsCompleted, blockSize);

            verifyEventsBelongToSameMethods(eventLogMethodExecuteSublist, eventLogMethodListenerPassSublist, "The " +
                    "expected maximum number of methods to execute simultaneously is " + maxSimultaneousTestMethods +
                    " for " + testName + " so no more than " + maxSimultaneousTestMethods + " methods should be " +
                    "running at the same time. The test method listener on onTestSuccess event logs for a block of " +
                    "simultaneously running test methods should all belong to the same methods as the test method " +
                    "execution event logs immediately preceding");

        }


    }

    public static void verifySimultaneousTestMethodListenerStartEvents(List<EventLog> listenerStartEventLogs, String
            testName, Map<String, EventLog> freeMethods, int blockSize) {

        verifySimultaneousTestMethodListenerStartEvents(listenerStartEventLogs, testName, blockSize);

        for (EventLog eventLog : listenerStartEventLogs) {
            String classAndMethodName = (String)eventLog.getData(CLASS_NAME) + "." +
                    (String)eventLog.getData(METHOD_NAME);

            assertTrue(freeMethods.get(classAndMethodName) != null, "Currently executing methods should have no " +
                    "dependencies or all methods they depend on should have already executed and passed.");
        }
    }

    public static int verifySimultaneousTestMethodListenerStartEvents(List<EventLog> listenerStartEventLogs, String
            testName, int blockSize, Map<String, EventLog> methodsExecuting, Map<String, Long>
            executingMethodThreadIds, Map<String, Integer> methodInvocationsCounts, Map<String, Integer>
            expectedInvocationCounts) {

        verifySimultaneousTestMethodListenerStartEvents(listenerStartEventLogs, testName, blockSize);

        int decrement = 0;

        for (EventLog eventLog : listenerStartEventLogs) {

            String classAndMethodName = (String)eventLog.getData(CLASS_NAME) + "." +
                    (String)eventLog.getData(METHOD_NAME);

            if(methodInvocationsCounts.get(classAndMethodName) == null) {
                methodInvocationsCounts.put(classAndMethodName, 1);
                decrement++;
            } else {
                methodInvocationsCounts.put(classAndMethodName,
                        methodInvocationsCounts.get(classAndMethodName) + 1);
            }

            assertFalse(methodInvocationsCounts.get(classAndMethodName) >
                    expectedInvocationCounts.get(classAndMethodName), "Method '" + classAndMethodName +
                    "' is expected to execute only " +  expectedInvocationCounts.get(classAndMethodName) +
                    " times, but event logs show that it was execute at least " +
                    methodInvocationsCounts.get(classAndMethodName) + " times");

            if (methodsExecuting.keySet().contains(classAndMethodName)) {
                assertTrue(eventLog.getThreadId() == executingMethodThreadIds.get(classAndMethodName), "All " +
                        "invocations of method '" + classAndMethodName + "' should execute in the same " +
                        "thread, but some event logs have different thread IDs");
            } else {
                assertFalse(executingMethodThreadIds.values().contains(eventLog.getThreadId()), "Event logs " +
                        "for different methods currently executing should have different thread IDs, but some event " +
                        "logs for different methods in the current block being processed have the same thread ID: " +
                        classAndMethodName);
            }

            if(methodsExecuting.get(classAndMethodName) == null) {
                methodsExecuting.put(classAndMethodName, eventLog);
                executingMethodThreadIds.put(classAndMethodName, eventLog.getThreadId());
            }
        }

        return decrement;
    }

    public static void verifySimultaneousTestMethodExecutionEvents(List<EventLog> testMethodExecutionEventLogs, String
            testName, Map<String, Long> executingMethodThreadIds, int blockSize) {

        verifySimultaneousTestMethodExecutionEvents(testMethodExecutionEventLogs, testName, blockSize);

        for(EventLog eventLog : testMethodExecutionEventLogs) {
            String classAndMethodName = (String)eventLog.getData(CLASS_NAME) + "." +
                    (String)eventLog.getData(METHOD_NAME);

            assertTrue(eventLog.getThreadId() == executingMethodThreadIds.get(classAndMethodName), "All the " +
                    "test method event logs for a given method using a non-parallel data provider should have the " +
                    "same thread ID, but some event logs for a method have different thread IDs: " +
                    classAndMethodName);
        }
    }

    public static void verifySimultaneousTestMethodListenerPassEvents(List<EventLog> testMethodListenerPassEventLogs,
            String testName,  Map<String, EventLog> methodsCompleted, int blockSize) {

        verifySimultaneousTestMethodListenerPassEvents(testMethodListenerPassEventLogs, testName,
                blockSize);

        for(EventLog eventLog : testMethodListenerPassEventLogs) {
            String classAndMethodName = (String)eventLog.getData(CLASS_NAME) + "." +
                    (String)eventLog.getData(METHOD_NAME);

                methodsCompleted.put(classAndMethodName, eventLog);
        }
    }

    public static void verifySimultaneousTestMethodListenerPassEvents(List<EventLog> testMethodListenerPassEventLogs,
            String testName, int blockSize, Map<String, EventLog> methodsExecuting,
            Map<String, EventLog> methodsCompleted, Map<String, Long> executingMethodThreadIds, Map<String, Integer>
            methodInvocationsCounts, Map<String, Integer> expectedInvocationCounts) {

        verifySimultaneousTestMethodListenerPassEvents(testMethodListenerPassEventLogs, testName, blockSize);

        for(EventLog eventLog : testMethodListenerPassEventLogs) {
            String classAndMethodName = (String)eventLog.getData(CLASS_NAME) + "." +
                    (String)eventLog.getData(METHOD_NAME);

            assertTrue(eventLog.getThreadId() == executingMethodThreadIds.get(classAndMethodName), "All the " +
                    "test method event logs for a given method using a non-parallel data provider should have the " +
                    "same thread ID, but some event logs for a method have different thread IDs: " +
                    classAndMethodName);

            if(methodInvocationsCounts.get(classAndMethodName)
                    .equals(expectedInvocationCounts.get(classAndMethodName))) {
                methodsExecuting.remove(classAndMethodName);
                executingMethodThreadIds.remove(classAndMethodName);
                methodsCompleted.put(classAndMethodName, eventLog);
            }
        }

    }

    //Verify that the specified test method listener onTestStart event logs execute simultaneously in parallel fashion
    //according to the expected maximum number of simultaneous executions. Verifies that each of them has the same
    //event type and all have different thread IDs.
    public static void verifySimultaneousTestMethodListenerStartEvents(List<EventLog> listenerStartEventLogs, String
            testName, int blockSize) {

        verifyEventTypeForEventsLogs(listenerStartEventLogs, LISTENER_TEST_METHOD_START, "Expected " + blockSize +
                " test method start event logs to be in a block of methods executing in parallel. Found an event log " +
                "of a different type in the block being processed: " + listenerStartEventLogs);

        verifyDifferentThreadIdsForEvents(listenerStartEventLogs, "Expected " + blockSize + " test method start " +
                "event logs to be in a block of methods executing in parallel. Each one of these event logs should " +
                "be associated with a different thread ID, but found that at two event logs share the same thread " +
                "ID: " + listenerStartEventLogs);
    }

    //Verify that the specified test method execution event logs execute simultaneously in parallel fashion according
    //to the specified thread count. Verifies that each of them has the same event type and all have different thread
    //IDs.
    public static void verifySimultaneousTestMethodExecutionEvents(List<EventLog> testMethodExecutionEventLogs,
            String testName, int blockSize) {

        verifyEventTypeForEventsLogs(testMethodExecutionEventLogs, TEST_METHOD_EXECUTION, "Expected " + blockSize +
                " test method execution event logs to be in a block of methods executing in parallel. Found an event " +
                "log of a different type in the block being processed: " + testMethodExecutionEventLogs);
        verifyDifferentThreadIdsForEvents(testMethodExecutionEventLogs, "Expected " + blockSize + " test method " +
                "execution event logs to be in a block of methods executing in parallel. Each one of these event " +
                "logs should be associated with a different thread ID, but found that at two event logs share the " +
                "same thread ID: " + testMethodExecutionEventLogs);
    }

    //Verify that the specified test method listener onTestSuccess event logs execute simultaneously in parallel
    //fashion according to the specified thread count. Verifies that each of them has the same event type and all have
    //different thread IDs.
    public static void verifySimultaneousTestMethodListenerPassEvents(List<EventLog> testMethodListenerPassEventLogs,
            String testName, int blockSize) {
        verifyEventTypeForEventsLogs(testMethodListenerPassEventLogs, LISTENER_TEST_METHOD_PASS, "Expected " +
                blockSize + " test method pass event logs to be in a block of methods executing in parallel. Found " +
                "an event log of a different type in the block being processed: " + testMethodListenerPassEventLogs);
        verifyDifferentThreadIdsForEvents(testMethodListenerPassEventLogs, "Expected " + blockSize + " test method " +
                "pass event logs to be in a block of methods executing in parallel. Each one of these event " +
                "logs should be associated with a different thread ID, but found that at two event logs share the " +
                "same thread ID: " + testMethodListenerPassEventLogs);
    }

    //Verify that the test method level events for the test methods declared in the specified class run in the same
    //thread for each instance of the test class for the specified suite and test
    public static void verifyEventsForTestMethodsRunInTheSameThread(Class<?> testClass, String suiteName, String
            testName) {

        for (Method method : getTestMethods(testClass)) {
            Multimap<Object, EventLog> testMethodEventLogs = getTestMethodEventLogsForMethod(suiteName, testName,
                    testClass.getCanonicalName(), method.getName());

            for (Object instanceKey : testMethodEventLogs.keySet()) {

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

    //Verify that the test method level events for the test methods declared in the specified class run in the same
    //thread for each instance of the test class for the specified suite and test
    public static void verifyEventsForTestMethodsInDifferentInstancesRunInDifferentThreads(Class<?> testClass, String
            suiteName, String testName) {

        for (Method method : testClass.getMethods()) {
            if (method.getDeclaringClass().equals(testClass)) {
                Multimap<Object, EventLog> testMethodEventLogs = getTestMethodEventLogsForMethod(suiteName, testName,
                        testClass.getCanonicalName(), method.getName());

                for(int i = 0; i <= testMethodEventLogs.keySet().size() - 2; i++) {
                    verifyDifferentThreadIdsForEvents(
                            new ArrayList<>(testMethodEventLogs.get(testMethodEventLogs.keySet().toArray()[i])),
                            new ArrayList<>(testMethodEventLogs.get(testMethodEventLogs.keySet().toArray()[i + 1])),
                            "The test method event logs for " + method.getName() + " for different test class " +
                                    "instances should run in different threads");
                }
            }
        }
    }

    public static void verifySequentialSuites(List<EventLog> suiteLevelEventLogs, Map<String,List<EventLog>>
            suiteEventLogsMap) {

        verifySameThreadIdForAllEvents(suiteLevelEventLogs, "Because the suites execute sequentially, the event logs " +
                "suite level events should have the same thread ID: " + suiteLevelEventLogs);

        List<EventLog> suiteListenerStartEventLogs = new ArrayList<>();

        for (int i = 0; i < suiteLevelEventLogs.size(); i = i + 2) {
            assertTrue(suiteLevelEventLogs.get(i).getEvent() == LISTENER_SUITE_START &&
                    suiteLevelEventLogs.get(i + 1).getEvent() == LISTENER_SUITE_FINISH, "Because the suites are " +
                    "expected to execute sequentially, the suite level event logs should consist of a series of " +
                    "pairs of a suite listener onStart event logger followed by a suite listener onFinish event logger: " +
                    suiteLevelEventLogs);
            suiteListenerStartEventLogs.add((suiteLevelEventLogs.get(i)));
        }

        for (int i = 0; i < suiteListenerStartEventLogs.size() - 1; i++) {
            String firstSuite = (String)suiteListenerStartEventLogs.get(i).getData(SUITE_NAME);
            String secondSuite = (String)suiteListenerStartEventLogs.get(i + 1).getData(SUITE_NAME);

            List<EventLog> firstSuiteEventLogs = suiteEventLogsMap.get(firstSuite);
            List<EventLog> secondSuiteEventLogs = suiteEventLogsMap.get(secondSuite);

            verifySequentialTimingOfEvents(firstSuiteEventLogs, secondSuiteEventLogs, "The first suite listener " +
                    "onStart event logger is for " + firstSuite + " and the second suite listener onStart event logger is " +
                    "for " + secondSuite + ". Because the suites are supposed to execute sequentially, all of the " +
                    "event logs for " + firstSuite + " should have timestamps earlier than all of the event logs for " +
                    secondSuite + ". First suite event logs: " + firstSuiteEventLogs + ". Second suite event logs: " +
                    secondSuiteEventLogs);
        }
    }

    public static void verifySequentialTests(List<EventLog> suiteAndTestLevelEventLogs, List<EventLog>
            testLevelEventLogs, EventLog suiteListenerOnStartEventLog, EventLog suiteListenerOnFinishEventLog) {

        verifySameThreadIdForAllEvents(suiteAndTestLevelEventLogs, "All suite level and test level event logs " +
                "should have the same thread ID because there is no parallelism specified at the suite or test " +
                "level: " + suiteAndTestLevelEventLogs);

        verifySequentialTimingOfEvents(suiteAndTestLevelEventLogs, "The timestamps of suite and test level events " +
                "logged first should be earlier than those which are logged afterwards because there is no " +
                "parallelism specified at the suite or test level: " + suiteAndTestLevelEventLogs);

        verifyEventsOccurBetween(suiteListenerOnStartEventLog, testLevelEventLogs, suiteListenerOnFinishEventLog,
                "All of the test level event logs should have timestamps between the suite listener's onStart and " +
                        "onFinish event logs. Suite listener onStart event logger: " + suiteListenerOnStartEventLog +
                        ". Suite listener onFinish event logger: " + suiteListenerOnFinishEventLog + ". Test level " +
                        "event logs: " + testLevelEventLogs);

        for (int i = 0; i < testLevelEventLogs.size(); i = i + 2) {
            assertTrue(testLevelEventLogs.get(i).getEvent() == LISTENER_TEST_START &&
                    testLevelEventLogs.get(i + 1).getEvent() == LISTENER_TEST_FINISH, "Because the tests are " +
                    "expected to execute sequentially, the test level event logs should consist of a series of " +
                    "pairs of a test listener onStart event logger followed by a test listener onFinish event logger: " +
                    testLevelEventLogs);
        }
    }

    public static void verifyParallelSuitesWithUnequalExecutionTimes(List<EventLog> suiteLevelEventLogs, int
            threadPoolSize) {

        Map<String, EventLog> suitesExecuting = new HashMap<>();
        Map<String, EventLog> suitesCompleted = new HashMap<>();

        List<Long> executingSuiteThreadIds = new ArrayList<>();

        if (suiteLevelEventLogs.size() > 2) {
            int offset = suiteLevelEventLogs.size() >= 2 * threadPoolSize ? threadPoolSize :
                    suiteLevelEventLogs.size() / 2;

            List<EventLog> suiteListenerStartEventLogs = suiteLevelEventLogs.subList(0, offset);

            verifyFirstBlockOfSimultaneouslyExecutingSuites(suiteListenerStartEventLogs, threadPoolSize);

            for (EventLog eventLog : suiteListenerStartEventLogs) {
                suitesExecuting.put((String)eventLog.getData(SUITE_NAME), eventLog);
                executingSuiteThreadIds.add(eventLog.getThreadId());
            }

            for (int i = offset; i < suiteLevelEventLogs.size(); i++) {

                EventLog eventLog = suiteLevelEventLogs.get(i);
                String suiteName = (String)eventLog.getData(SUITE_NAME);

                if (eventLog.getEvent() == LISTENER_SUITE_START) {
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
                    }
                }

                if (suitesExecuting.keySet().size() < threadPoolSize && suiteLevelEventLogs.size() - i + 1 >
                        threadPoolSize) {
                    fail("The thread pool size is " + threadPoolSize + ", so there should be at least " +
                            threadPoolSize + " suites executing at the same time unless there are no suites left to " +
                            "queue and the final block of suites is currently in execution: " + suiteLevelEventLogs);
                }

                if (eventLog.getEvent() == LISTENER_SUITE_FINISH) {

                    assertTrue(suitesExecuting.get(suiteName) != null, "Found an event logger for a suite listener " +
                            "onFinish event that does not have a corresponding event logger for a suite listener " +
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
    }

    public static void verifyThatTestMethodsRunAfterMethodsTheyDependOn(String suiteName, String testName,
            List<Class<?>> classes) {

        for(Class<?> clazz : classes) {
            for(Method method : getTestMethods(clazz)) {
                Multimap<Object, EventLog> eventLogsForMethod = getTestMethodEventLogsForMethod(suiteName, testName,
                        clazz.getCanonicalName(), method.getName());

                Multimap<Object, EventLog> eventLogsForMethodsDependedOn =
                        getTestMethodEventLogsForMethodsDependedOn(suiteName, testName, clazz.getCanonicalName(),
                                method.getName());

                Multimap<Object, EventLog> eventLogsForMethodsBelongingToGroupsDependedOn =
                        getTestMethodEventLogsForMethodsBelongingToGroupsDependedOn(suiteName, testName,
                                clazz.getCanonicalName(), method.getName());

                for(Object instance : eventLogsForMethod.keySet()) {
                    EventLog startEventOfDependingMethod = (EventLog)eventLogsForMethod.get(instance).toArray()[0];

                    for(EventLog eventLog : eventLogsForMethodsDependedOn.get(instance)) {
                        assertTrue(eventLog.getTimeOfEvent() < startEventOfDependingMethod.getTimeOfEvent(), "All " +
                                "methods that a method depends on should execute before it: " + eventLog + ", " +
                                startEventOfDependingMethod);
                    }

                    for(EventLog eventLog : eventLogsForMethodsBelongingToGroupsDependedOn.get(instance)) {
                        assertTrue(eventLog.getTimeOfEvent() < startEventOfDependingMethod.getTimeOfEvent(), "All " +
                                "methods belonging to groups that a method depends on should execute before it: " +
                                eventLog + ", " + startEventOfDependingMethod);
                    }
                }
            }
        }
    }

    private static void verifyFirstBlockOfSimultaneouslyExecutingSuites(List<EventLog> suiteListenerStartEventLogs,
            int threadPoolSize) {

        verifySimultaneousSuiteListenerStartEvents(suiteListenerStartEventLogs, threadPoolSize);
        verifyDifferentThreadIdsForEvents(suiteListenerStartEventLogs, "The thread count is " + threadPoolSize +
                " so the thread IDs for the suite listener onStart events for simultaneously executing suites " +
                "should be different. Event logs: " + suiteListenerStartEventLogs);
    }

    //Helper which verifies that the specified lists of test method level events are associated with the same methods
    private static void verifyEventsBelongToSameMethods(List<EventLog> firstEventLogs, List<EventLog> secondEventLogs,
            String faiMessage) {

        List<String> methodNames = new ArrayList<>();

        for (EventLog eventLog : firstEventLogs) {
            methodNames.add((String)eventLog.getData(CLASS_NAME) + (String)eventLog.getData(METHOD_NAME));
        }

        for (EventLog eventLog : secondEventLogs) {

            assertTrue(methodNames.contains((String)eventLog.getData(CLASS_NAME) +
                    (String)eventLog.getData(METHOD_NAME)), faiMessage);
        }
    }

    //Helper method that retrieves the earliest and latest timestamps for the specified list of event logs
    private static Pair<Long, Long> getEarliestAndLatestTimestamps(List<EventLog> eventLogs) {

        if (eventLogs.isEmpty()) {
            return null;
        }

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

    private static boolean allExecutingMethodsHaveMoreInvocations(Map<String, EventLog> methodsExecuting,
            Map<String, Integer> methodInvocationsCounts, Map<String, Integer> expectedInvocationCounts) {

        for(String methodAndClassName : methodsExecuting.keySet()) {
            if(Objects.equals(methodInvocationsCounts.get(methodAndClassName),
                    expectedInvocationCounts.get(methodAndClassName))) {
                return false;
            }
        }

        return true;
    }

    private static List<Method> getTestMethods(Class<?> testClass) {
        List<Method> methods = new ArrayList<>();

        for (Method method : testClass.getMethods()) {

            Annotation[] annotations = method.getDeclaredAnnotations();

            for (Annotation a : annotations) {
                if (Test.class.isAssignableFrom(a.getClass())) {
                    methods.add(method);
                }
            }
        }

        return methods;
    }

    private static List<String> getMethodsBelongingToGroups(List<String> groups, List<EventLog> logs) {
        List<String> methods = new ArrayList<>();

        for (EventLog log : logs) {
            String classAndMethodName = (String) log.getData(CLASS_NAME) + "." +
                    (String) log.getData(METHOD_NAME);

            if (!methods.contains(classAndMethodName)) {
                for (String group : (String[]) log.getData(GROUPS_BELONGING_TO)) {
                    if (groups.contains(group)) {

                        if (!methods.contains(classAndMethodName)) {
                            methods.add(classAndMethodName);
                        }
                    }
                }
            }
        }

        return methods;
    }

    private static void log(int offsetOne, int offsetTwo, int blockSize, List<EventLog> eventLogMethodListenerStartSublist,
                List<EventLog> eventLogMethodExecuteSublist, List<EventLog> eventLogMethodListenerPassSublist) {
        logger.log(Level.INFO, "Event logs extracted from event log list between index {0} and index {1} should " +
                        "be the test method start event logs for a block of {2} simultaneously executing methods",
                new Object[] {offsetOne, offsetTwo - 1, blockSize});

        int j = offsetOne;

        for(EventLog eventLog : eventLogMethodListenerStartSublist) {
            logger.log(Level.INFO, "Event logged at index {0}: {1}", new Object[] {j, eventLog.toString()});
            j++;
        }

        logger.log(Level.INFO, "Event logs extracted from event log list between index {0} and index {1} should " +
                        "be the test method execution event logs for a block of {2} simultaneously executing methods",
                new Object[] {offsetTwo, offsetTwo + blockSize - 1, blockSize});

        j = offsetTwo;

        for(EventLog eventLog : eventLogMethodExecuteSublist) {
            logger.log(Level.INFO, "Event logged at index {0}: {1}", new Object[] {j, eventLog.toString()});
            j++;
        }

        logger.log(Level.INFO, "Event logs extracted from event log list between index {0} and index {1} should " +
                        "be the test method pass event logs for a block of {2} simultaneously executing methods",
                new Object[] {offsetTwo + blockSize, offsetTwo + 2 * blockSize - 1, blockSize});

        j = offsetTwo + blockSize;

        for(EventLog eventLog : eventLogMethodListenerPassSublist) {
            logger.log(Level.INFO, "Event logged at index {0}: {1}", new Object[] {j, eventLog.toString()});
            j++;
        }
    }

    private static void log(int listSize, int threadCount, int remainder) {
        if(listSize / 3 < threadCount) {
            logger.log(Level.INFO, "Expecting there to be a single block of {0} parallel methods", listSize / 3);
        } else {

            if(remainder > 0) {
                logger.log(Level.INFO, "Expecting there to be a series of {0} blocks of {1} parallel methods with a " +
                                "final block of {2} parallel methods",

                        new Object[]
                                {
                                        (listSize / 3) / threadCount,
                                        threadCount,
                                        remainder
                                }
                );
            } else {
                logger.log(Level.INFO, "Expecting there to be a series of {0} blocks of {1} parallel methods",
                        new Object[]
                                {
                                        (listSize / 3) / threadCount,
                                        threadCount
                                }
                        );
            }
        }
    }
}

