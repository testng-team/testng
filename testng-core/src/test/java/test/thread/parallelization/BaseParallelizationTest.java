package test.thread.parallelization;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import static test.thread.parallelization.TestNgRunStateTracker.EventInfo.CLASS_NAME;
import static test.thread.parallelization.TestNgRunStateTracker.EventInfo.METHOD_NAME;
import static test.thread.parallelization.TestNgRunStateTracker.EventInfo.SUITE_NAME;
import static test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent.LISTENER_SUITE_FINISH;
import static test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent.LISTENER_SUITE_START;
import static test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent.LISTENER_TEST_FINISH;
import static test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent.LISTENER_TEST_METHOD_PASS;
import static test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent.LISTENER_TEST_METHOD_START;
import static test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent.LISTENER_TEST_START;
import static test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent.TEST_METHOD_EXECUTION;
import static test.thread.parallelization.TestNgRunStateTracker.getTestMethodEventLogsForMethod;

import com.google.common.collect.Multimap;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.testng.annotations.Test;
import org.testng.internal.collections.Pair;
import org.testng.log4testng.Logger;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;
import test.thread.parallelization.TestNgRunStateTracker.EventLog;
import test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent;

public class BaseParallelizationTest extends SimpleBaseTest {
  private static final Logger log = Logger.getLogger(BaseParallelizationTest.class);

  // Get a list of the names of declared methods with the @Test annotation from the specified class
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

  // Send the name of the suite and the name of the test as parameters so that the methods can
  // associate their
  // execution event logs with them. Specified the delay in seconds to apply for the method
  // execution. This delay
  // helps in determining the parallelism or lack thereof for method executions.
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

  public static void addParams(
      XmlSuite suite,
      String suiteName,
      String testName,
      String sleepFor,
      String dataProviderParam) {
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

  // Verify that the list of event logs have the specified event type. Print the specified failure
  // message if the
  // assertion on the event type fails.
  public static void verifyEventTypeForEventsLogs(
      List<EventLog> eventLogs, TestNgRunEvent event, String failMessage) {
    for (EventLog eventLog : eventLogs) {
      assertTrue(eventLog.getEvent() == event, failMessage);
    }
  }

  // Verify that the list of event logs all have different thread IDS. Print the specified failure
  // message if the
  // assertion on the thread IDs fails.
  public static void verifyDifferentThreadIdsForEvents(
      List<EventLog> eventLogs, String failMessage) {
    List<Long> threadIds = new ArrayList<>();

    for (EventLog eventLog : eventLogs) {
      assertFalse(threadIds.contains(eventLog.getThreadId()), failMessage);
      threadIds.add(eventLog.getThreadId());
    }
  }

  // Verify that the event logs in the first list all have different thread IDs from the event logs
  // in the second
  // list. Print the specified failure message if the assertion on the thread IDs fails.
  public static void verifyDifferentThreadIdsForEvents(
      List<EventLog> eventLogsOne, List<EventLog> eventLogsTwo, String failMessage) {
    List<Long> threadIds = new ArrayList<>();

    for (EventLog eventLog : eventLogsOne) {
      threadIds.add(eventLog.getThreadId());
    }

    for (EventLog eventLog : eventLogsTwo) {
      assertFalse(threadIds.contains(eventLog.getThreadId()), failMessage);
    }
  }

  // Verify that the list of event logs all have the same thread ID. Print the specified failure
  // message if the
  // assertion on the event type fails.
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

  // Verify that the threads from the specified list of event logs are all greater than the
  // specified thread ID. Print
  // the specified failure message if the assertion on the thread IDs fails.
  public static void verifyEventThreadsSpawnedAfter(
      Long earlierThreadId, List<EventLog> eventsFromLaterThread, String failMessage) {
    for (EventLog eventLog : eventsFromLaterThread) {
      assertTrue(eventLog.getThreadId() > earlierThreadId, failMessage);
    }
  }

  //    //Verify that the timestamps of the list of event logs are all within the specified range of
  // each other. Print
  //    //the specified failure message if the assertion on the timing range fails.
  //    public static void verifyTimingOfEvents(List<EventLog> eventLogs, long timingRange, String
  // failMessage) {
  //        if(!eventLogs.isEmpty()) {
  //            Pair<Long,Long> timestamps = getEarliestAndLatestTimestamps(eventLogs);
  //            verifyTimestampDifference(timestamps.second(), timestamps.first(), timingRange,
  // failMessage);
  //        }
  //    }

  //    public static void verifyTimingOfEvents(EventLog eventLogOne, EventLog eventLogTwo, long
  // timingRange, String
  //            failMessage) {
  //        verifyTimestampDifference(eventLogOne.getTimeOfEvent(), eventLogTwo.getTimeOfEvent(),
  // timingRange, failMessage);
  //    }
  //
  //    //Verify that the timestamps of the first list of events are all within the specified range
  // from the timestamps
  //    //of the second list of events. Print the specified failure message if the assertion on the
  // timing range fails.
  //    public static void verifyTimingOfEvents(List<EventLog> firstEventLogs, List<EventLog>
  // secondEventLogs,
  //            long lowerTimingRange, long upperTimingRange, String failMessage) {
  //
  //        if(!firstEventLogs.isEmpty() && !secondEventLogs.isEmpty()) {
  //            Pair<Long, Long> timestampsListOne = getEarliestAndLatestTimestamps(firstEventLogs);
  //            Pair<Long, Long> timestampsListTwo =
  // getEarliestAndLatestTimestamps(secondEventLogs);
  //
  //            verifyTimestampDifference(timestampsListTwo.first(), timestampsListOne.first(),
  // lowerTimingRange,
  //                    upperTimingRange, failMessage);
  //            verifyTimestampDifference(timestampsListTwo.first(), timestampsListOne.second(),
  // lowerTimingRange,
  //                    upperTimingRange, failMessage);
  //            verifyTimestampDifference(timestampsListTwo.second(), timestampsListOne.first(),
  // lowerTimingRange,
  //                    upperTimingRange, failMessage);
  //            verifyTimestampDifference(timestampsListTwo.second(), timestampsListOne.second(),
  // lowerTimingRange,
  //                    upperTimingRange, failMessage);
  //        }
  //    }

  //    //Verify that the difference between the two specified timestamps is within the specfied
  // upper and lower bound. The
  //    //difference is calculated as an absolute value.
  //    public static void verifyTimestampDifference(long timestampOne, long timestampTwo, long
  // lowerTimingRange,
  //            long upperTimingRange, String failMessage) {
  //        assertTrue(Math.abs(timestampOne - timestampTwo) <= upperTimingRange &&
  //                Math.abs(timestampOne - timestampTwo) >= lowerTimingRange, failMessage + ".
  // Difference: " +
  //                Math.abs(timestampOne - timestampTwo));
  //    }
  //
  //    public static void verifyTimestampDifference(long timestampOne, long timestampTwo, long
  // timingRange, String
  //            failMessage) {
  //        assertTrue(Math.abs(timestampOne - timestampTwo) <= timingRange, failMessage + ".
  // Difference: " +
  //                Math.abs(timestampOne - timestampTwo));
  //    }

  // Verify that the specified event logs all have timestamps between the specified earlier and
  // later event logs.
  // Print the specified failure message if the assertion on the timestamps fails.
  public static void verifyEventsOccurBetween(
      EventLog earlierEventLog,
      List<EventLog> inBetweenEventLogs,
      EventLog laterEventLog,
      String failMessage) {
    for (EventLog eventLog : inBetweenEventLogs) {
      assertTrue(
          eventLog.getTimeOfEvent() >= earlierEventLog.getTimeOfEvent()
              && eventLog.getTimeOfEvent() <= laterEventLog.getTimeOfEvent(),
          failMessage);
    }
  }

  // Verify that the timestamps for the events if the specified list are all increasing. That each
  // the timestamps of
  // a given event logger is later than the event logger immediately preceding it. Print the
  // specified failure message if
  // the assertion on the timestamps fails
  public static void verifySequentialTimingOfEvents(List<EventLog> eventLogs, String failMessage) {
    for (int i = 0; i + 1 < eventLogs.size(); i++) {
      assertTrue(
          eventLogs.get(i).getTimeOfEvent() < eventLogs.get(i + 1).getTimeOfEvent(), failMessage);
    }
  }

  // Verify that the first list of event logs all have timestamps earlier than all the events in the
  // second list of
  // event logs. Print the specified failure message if the assertion on the timestamps fails.
  public static void verifySequentialTimingOfEvents(
      List<EventLog> firstEventLogs, List<EventLog> secondEventLogs, String failMessage) {

    if (!firstEventLogs.isEmpty() && !secondEventLogs.isEmpty()) {
      Pair<Long, Long> timestampsListOne = getEarliestAndLatestTimestamps(firstEventLogs);
      Pair<Long, Long> timestampsListTwo = getEarliestAndLatestTimestamps(secondEventLogs);

      assertTrue(timestampsListTwo.first() > timestampsListOne.second(), failMessage);
    }
  }

  // Verify that the test methods declared in the specified list of classes have the specified
  // number of class
  // instances associated with them for the specified suite and test.
  public static void verifyNumberOfInstancesOfTestClassesForMethods(
      String suiteName, String testName, List<Class<?>> classes, int numInstances) {

    for (Class<?> clazz : classes) {
      verifyNumberOfInstancesOfTestClassForMethods(suiteName, testName, clazz, numInstances);
    }
  }

  // Verify that the test methods declared in the specified list of classes have the specified
  // number of class
  // instances associated with them for the specified suite and test.
  public static void verifyNumberOfInstancesOfTestClassesForMethods(
      String suiteName, String testName, List<Class<?>> classes, int... numInstances) {

    for (int i = 0; i < numInstances.length; i++) {
      verifyNumberOfInstancesOfTestClassForMethods(
          suiteName, testName, classes.get(i), numInstances[i]);
    }
  }

  // Verify that the test methods declared in the specified class have the specified number of class
  // instances
  // associated with them for the specified suite and test.
  public static void verifyNumberOfInstancesOfTestClassForMethods(
      String suiteName, String testName, Class<?> clazz, int numInstances) {

    for (String methodName : getDeclaredTestMethods(clazz)) {
      verifyNumberOfInstancesOfTestClassForMethod(
          suiteName, testName, clazz, methodName, numInstances);
    }
  }

  // Verify that the specified test method has the specified number of class instances associated
  // with it for the
  // specified suite and test.
  public static void verifyNumberOfInstancesOfTestClassForMethod(
      String suiteName, String testName, Class<?> clazz, String methodName, int numInstances) {

    Multimap<Object, EventLog> eventLogMap =
        getTestMethodEventLogsForMethod(suiteName, testName, clazz.getCanonicalName(), methodName);

    assertEquals(
        eventLogMap.keySet().size(),
        numInstances,
        "There should be "
            + numInstances
            + " instances "
            + "associated with the class "
            + clazz.getCanonicalName()
            + " for method "
            + methodName
            + " in the "
            + "test "
            + testName
            + " in the suite "
            + suiteName
            + ": "
            + eventLogMap);
  }

  // Verify that all the test methods declared in the specified list of classes have the same
  // instances of the classes
  // associated with them for the specified suite and test.
  public static void verifySameInstancesOfTestClassesAssociatedWithMethods(
      String suiteName, String testName, List<Class<?>> classes) {
    for (Class<?> clazz : classes) {
      verifySameInstancesOfTestClassAssociatedWithMethods(suiteName, testName, clazz);
    }
  }

  // Verify that all the test methods declared in the specified class have the same instances of the
  // class associated /
  // with them for the specified suite and test.
  public static void verifySameInstancesOfTestClassAssociatedWithMethods(
      String suiteName, String testName, Class<?> clazz) {

    Set<Object> instanceKeys = null;

    for (String methodName : getDeclaredTestMethods(clazz)) {
      Multimap<Object, EventLog> eventLogMap =
          getTestMethodEventLogsForMethod(
              suiteName, testName, clazz.getCanonicalName(), methodName);

      if (instanceKeys == null) {
        instanceKeys = eventLogMap.keySet();
      } else {
        assertTrue(
            instanceKeys.containsAll(eventLogMap.keySet())
                && eventLogMap.keySet().containsAll(instanceKeys),
            "The same instances of "
                + clazz.getCanonicalName()
                + " should be associated with its methods for the test "
                + testName
                + " in the suite "
                + suiteName);
      }
    }
  }

  // Verify that methods associated with the specified event logs execute simultaneously in parallel
  // fashion, in
  // accordance with the thread count. This verification is for blocks of parallel methods, none of
  // which use
  // data providers. This verification is for blocks of parallel methods that have the same sleep
  // delays for their
  // execution bodies and which do not have any BeforeMethod AfterMethod, BeforeGroup or AfterGroup
  // configuration methods.
  public static void verifySimultaneousTestMethods(
      List<EventLog> testMethodEventLogs, String testName, int threadCount) {

    if (log.isDebugEnabled()) {
      log.debug(
          "Verifying parallel execution of test methods for test named "
              + testName
              + " with "
              + "thread count "
              + threadCount);
      log.debug(
          testMethodEventLogs.size()
              + " test method event logs for "
              + testMethodEventLogs.size() / 3
              + " test methods");
    }

    // Keep track of the methods that have started, but not completed execution.
    Map<ClassInstanceMethodKey, EventLog> methodsExecuting = new HashMap<>();

    // Keep track of the methods whose start, execute and test method pass events have all been
    // found in the list
    // of logs
    Map<ClassInstanceMethodKey, EventLog> methodsCompleted = new HashMap<>();

    // Keep track of the thread IDs for all the methods that are executing and check that all a
    // method's events
    // run in the same thread.
    List<Long> executingMethodThreadIds = new ArrayList<>();

    // Make a list of the all the thread IDs of the first batch of start method events and check
    // that no other
    // thread IDs are found in the rest of the event logs.
    List<Long> allThreadIds = new ArrayList<>();

    // If number of test methods is more than the thread count, then the first block of methods will
    // be equal to
    // the thread count. If not, it will be equal to the total number of methods.
    int blockSize =
        testMethodEventLogs.size() / 3 >= threadCount
            ? threadCount
            : testMethodEventLogs.size() / 3;

    // Get the start events for the first batch of methods.
    List<EventLog> eventLogTestMethodListenerStartEvents =
        testMethodEventLogs.subList(0, blockSize);

    if (log.isDebugEnabled()) {
      log.debug(
          "First "
              + blockSize
              + " test method event logs should all be test method start events: "
              + getStringForEventLogList(eventLogTestMethodListenerStartEvents));
    }

    // Verify that all the events in the sublist extracted for the start events of the block of
    // methods expected
    // to execute in parallel all have the test method start event type and that they all executed
    // in different
    // threads.
    verifySimultaneousTestMethodListenerStartEvents(
        eventLogTestMethodListenerStartEvents, testName, blockSize);

    // Keep track of the current methods that are executing and their thread IDs
    for (EventLog eventLog : eventLogTestMethodListenerStartEvents) {
      ClassInstanceMethodKey classAndMethodNameAndInstanceHash =
          new ClassInstanceMethodKey(eventLog);

      assertNull(
          methodsExecuting.get(classAndMethodNameAndInstanceHash),
          "There should only be one start "
              + "event logged for a method in the first block of test method events");
      assertFalse(
          executingMethodThreadIds.contains(eventLog.getThreadId()),
          "The first block of test method " + "events should all have different thread IDs");

      methodsExecuting.put(classAndMethodNameAndInstanceHash, eventLog);
      executingMethodThreadIds.add(eventLog.getThreadId());
      allThreadIds.add(eventLog.getThreadId());
    }

    for (int i = blockSize; i < testMethodEventLogs.size(); i++) {
      EventLog eventLog = testMethodEventLogs.get(i);

      log.debug("Processing test method event log at index " + i + ": " + eventLog);

      ClassInstanceMethodKey classAndMethodNameAndInstanceHash =
          new ClassInstanceMethodKey(eventLog);

      if (eventLog.getEvent() == LISTENER_TEST_METHOD_START) {
        assertTrue(
            methodsExecuting.get(classAndMethodNameAndInstanceHash) == null
                && methodsCompleted.get(classAndMethodNameAndInstanceHash) == null,
            "There should only be one " + "execution of any given method");
        assertFalse(
            executingMethodThreadIds.contains(eventLog.getThreadId()),
            "Event logs for currently "
                + "executing test methods should have different thread IDs");
        assertTrue(
            allThreadIds.contains(eventLog.getThreadId()),
            "All of the test method event logs should "
                + "have the same "
                + threadCount
                + " thread IDs: "
                + allThreadIds.toString());
        assertTrue(
            methodsExecuting.size() < threadCount,
            "The current event log is a test method start "
                + "event. The list of currently executing methods should be less than the thread count. "
                + "Thread count: "
                + threadCount
                + ". Currently executing methods: "
                + getStringForEventLogList(methodsExecuting.values()));

        methodsExecuting.put(classAndMethodNameAndInstanceHash, eventLog);
        executingMethodThreadIds.add(eventLog.getThreadId());
      }

      if (eventLog.getEvent() == TEST_METHOD_EXECUTION) {
        assertTrue(
            methodsExecuting.get(classAndMethodNameAndInstanceHash) != null,
            "Found a test method "
                + "execution event log that does not have a corresponding test method start event log");
        assertTrue(
            methodsExecuting.get(classAndMethodNameAndInstanceHash).getThreadId()
                == eventLog.getThreadId(),
            "All the event logs for a given method should have the same thread ID");
      }

      if (eventLog.getEvent() == LISTENER_TEST_METHOD_PASS) {

        assertTrue(
            methodsExecuting.get(classAndMethodNameAndInstanceHash) != null,
            "Found a test method "
                + "pass event log that does not have a corresponding test method start event log");
        assertTrue(
            methodsExecuting.get(classAndMethodNameAndInstanceHash).getThreadId()
                == eventLog.getThreadId(),
            "All the event logs for a given method should have the same thread ID");
        methodsExecuting.remove(classAndMethodNameAndInstanceHash);
        executingMethodThreadIds.remove(eventLog.getThreadId());
        methodsCompleted.put(classAndMethodNameAndInstanceHash, eventLog);
      }
    }
  }

  // Verify that methods associated with the specified event logs execute simultaneously in parallel
  // fashion, in
  // accordance with the thread count. This verification is for blocks of parallel methods, some of
  // which use
  // non-parallel data providers without factories, so all the invocations of the methods run on the
  // same class
  // instances. This verification is for blocks of parallel methods that have the same sleep delays
  // for their
  // execution bodies and which do not have any BeforeMethod AfterMethod, BeforeGroup or AfterGroup
  // configuration methods.
  //
  // Some of the test methods use non-parallel data providers without factories. All the invocations
  // of those
  // test methods will occur serially within the same thread on the same class instances. There are
  // three test
  // method events expected per test method: a start event, an execution event, and a test method
  // pass event.
  public static void verifyParallelTestMethodsWithNonParallelDataProvider(
      List<EventLog> testMethodEventLogs,
      String testName,
      Map<String, Integer> expectedInvocationCounts,
      int numUniqueMethods,
      int threadCount) {

    log.debug(
        "Verifying parallel execution of test methods using non-parallel data providers for "
            + "test named "
            + testName
            + " with thread count "
            + threadCount);
    log.debug(
        testMethodEventLogs.size()
            + " test method event logs for "
            + numUniqueMethods
            + " unique methods");

    Map<String, EventLog> methodsExecuting = new HashMap<>();
    Map<String, EventLog> methodsCompleted = new HashMap<>();

    // Because this method verifies combination of parallel-by-methods mode and the use of
    // non-parallel data
    // providers without factories, it is necessary to track the number of times that test methods
    // are invoked in
    // order to check that this is consistent with the number of times they are expected to be
    // invoked based on
    // their use of non-parallel data providers.
    Map<String, Integer> methodInvocationsCounts = new HashMap<>();

    // In order to verify that all invocations of test methods which use non-parallel data providers
    // occur in the
    // same thread, it is necessary to keep track of the thread IDs of methods that are executing
    // within a
    // block of simultaneously executing methods.
    Map<String, Long> executingMethodThreadIds = new HashMap<>();

    // Make a list of the all the thread IDs of the first batch of start method events and check
    // that no other
    // thread IDs are found in the rest of the event logs.
    List<Long> allThreadIds = new ArrayList<>();

    // The logic for determining the block size of simultaneously executing parallel methods is
    // initially determined
    // by whether the total number of unique methods less than the thread count. If it is less than
    // the thread count,
    // then the block size is equal to the number of unique methods. Those methods will execute in
    // parallel
    // until all invocations of all the methods completes. Otherwise, there are more methods queued
    // up than the
    // thread count, so the block size is equal to the thread count.
    int blockSize = numUniqueMethods >= threadCount ? threadCount : numUniqueMethods;

    // Get the start events for the first batch of methods.
    List<EventLog> eventLogTestMethodListenerStartEvents =
        testMethodEventLogs.subList(0, blockSize);

    log.debug(
        "First "
            + blockSize
            + " test method event logs should all be test method start events: "
            + "\n"
            + getStringForEventLogList(eventLogTestMethodListenerStartEvents));
    log.debug(getStringForEventLogList(eventLogTestMethodListenerStartEvents));

    // Keep track of the current methods that are executing and their thread IDs
    for (EventLog eventLog : eventLogTestMethodListenerStartEvents) {
      String classAndMethodName =
          (String) eventLog.getData(CLASS_NAME) + "." + (String) eventLog.getData(METHOD_NAME);

      assertNull(
          methodsExecuting.get(classAndMethodName),
          "There should only be one start event logged for a "
              + "method in the first block of test method events");
      assertNull(
          executingMethodThreadIds.get(classAndMethodName),
          "The first block of test method events " + "should all have different thread IDs");

      methodsExecuting.put(classAndMethodName, eventLog);
      executingMethodThreadIds.put(classAndMethodName, eventLog.getThreadId());
      allThreadIds.add(eventLog.getThreadId());
      methodInvocationsCounts.put(classAndMethodName, 1);
    }

    // Verify that all the events in the sublist extracted for the test method execution events of
    // the block of
    // methods expected to execute in parallel all have the test method execution event type and
    // that they all
    // executed in different threads.
    verifySimultaneousTestMethodListenerStartEvents(
        eventLogTestMethodListenerStartEvents, testName, blockSize);

    for (int i = blockSize; i < testMethodEventLogs.size(); i++) {

      EventLog eventLog = testMethodEventLogs.get(i);

      log.debug("Processing test method event log at index " + i + ": " + eventLog);

      String classAndMethodName =
          (String) eventLog.getData(CLASS_NAME) + "." + (String) eventLog.getData(METHOD_NAME);

      if (i != blockSize) {
        // All methods that are in the list of currently executing methods should still have
        // invocations left.
        // Otherwise, they would have been removed from that list and added to the completed methods
        // list.
        allExecutingMethodsHaveMoreInvocations(
            methodsExecuting, methodInvocationsCounts, expectedInvocationCounts);
      }

      if (eventLog.getEvent() == LISTENER_TEST_METHOD_START) {
        assertTrue(
            executingMethodThreadIds.get(classAndMethodName) == null
                || executingMethodThreadIds.get(classAndMethodName) == eventLog.getThreadId(),
            "Event logs for all invocations of a method on a give class instance should have the same "
                + "thread ID ");

        for (String key : executingMethodThreadIds.keySet()) {
          if (!key.equals(classAndMethodName)) {
            assertFalse(
                executingMethodThreadIds.get(key) == eventLog.getThreadId(),
                "Events for " + "different methods should have different thread IDs");
          }
        }

        assertTrue(
            allThreadIds.contains(eventLog.getThreadId()),
            "All of the test method event logs should "
                + "have the same "
                + threadCount
                + " thread IDs: "
                + allThreadIds.toString());

        if (methodsExecuting.get(classAndMethodName) == null) {
          assertTrue(
              methodsExecuting.size() < threadCount,
              "The current event log is a test method start "
                  + "event for a method that has not yet been invoked. The list of currently executing "
                  + "methods should be less than the thread count. Thread count: "
                  + threadCount
                  + ". Currently executing methods: "
                  + getStringForEventLogList(methodsExecuting.values()));

          methodsExecuting.put(classAndMethodName, eventLog);
          executingMethodThreadIds.put(classAndMethodName, eventLog.getThreadId());
          methodInvocationsCounts.put(classAndMethodName, 1);
        } else {
          methodInvocationsCounts.put(
              classAndMethodName, methodInvocationsCounts.get(classAndMethodName) + 1);
          assertTrue(
              methodInvocationsCounts.get(classAndMethodName)
                  <= expectedInvocationCounts.get(classAndMethodName),
              "Method '"
                  + classAndMethodName
                  + "' is expected to execute only "
                  + expectedInvocationCounts.get(classAndMethodName)
                  + " times, but event logs show "
                  + "that it was execute at least "
                  + methodInvocationsCounts.get(classAndMethodName)
                  + " times");
        }
      }

      if (eventLog.getEvent() == TEST_METHOD_EXECUTION) {
        assertTrue(
            methodsExecuting.get(classAndMethodName) != null,
            "Found a test method execution event "
                + "log that does not have a corresponding test method start event log");
        assertTrue(
            methodsExecuting.get(classAndMethodName).getThreadId() == eventLog.getThreadId(),
            "All the event logs for a given method should have the same thread ID");
      }

      if (eventLog.getEvent() == LISTENER_TEST_METHOD_PASS) {

        assertTrue(
            methodsExecuting.get(classAndMethodName) != null,
            "Found a test method pass event log "
                + "that does not have a corresponding test method start event log");
        assertTrue(
            methodsExecuting.get(classAndMethodName).getThreadId() == eventLog.getThreadId(),
            "All the event logs for a given method should have the same thread ID");

        if (methodInvocationsCounts
            .get(classAndMethodName)
            .equals(expectedInvocationCounts.get(classAndMethodName))) {
          methodsExecuting.remove(classAndMethodName);
          executingMethodThreadIds.remove(classAndMethodName);
          methodsCompleted.put(classAndMethodName, eventLog);
        }

        log.debug(
            classAndMethodName
                + " has executed "
                + methodInvocationsCounts.get(classAndMethodName)
                + " times. Expected to execute "
                + (expectedInvocationCounts.get(classAndMethodName)
                    - methodInvocationsCounts.get(classAndMethodName))
                + " more times");
      }
    }
  }

  // Verify that the specified test method listener onTestStart event logs execute simultaneously in
  // parallel fashion
  // according to the expected maximum number of simultaneous executions. Verifies that each of them
  // has the same
  // event type and all have different thread IDs.
  public static void verifySimultaneousTestMethodListenerStartEvents(
      List<EventLog> listenerStartEventLogs, String testName, int blockSize) {

    verifyEventTypeForEventsLogs(
        listenerStartEventLogs,
        LISTENER_TEST_METHOD_START,
        "Expected "
            + blockSize
            + " test method start event logs to be in a block of methods executing in parallel. Found an event log "
            + "of a different type in the block being processed: "
            + listenerStartEventLogs);

    verifyDifferentThreadIdsForEvents(
        listenerStartEventLogs,
        "Expected "
            + blockSize
            + " test method start "
            + "event logs to be in a block of methods executing in parallel. Each one of these event logs should "
            + "be associated with a different thread ID, but found that at two event logs share the same thread "
            + "ID: "
            + listenerStartEventLogs);
  }

  // Verify that the test method level events for the test methods declared in the specified class
  // run in the same
  // thread for each instance of the test class for the specified suite and test
  public static void verifyEventsForTestMethodsRunInTheSameThread(
      Class<?> testClass, String suiteName, String testName) {

    for (Method method : testClass.getMethods()) {
      boolean isTestMethod = false;

      Annotation[] annotations = method.getDeclaredAnnotations();

      for (Annotation a : annotations) {
        if (Test.class.isAssignableFrom(a.getClass())) {
          isTestMethod = true;
        }
      }

      if (method.getDeclaringClass().equals(testClass) && isTestMethod) {
        Multimap<Object, EventLog> testMethodEventLogs =
            getTestMethodEventLogsForMethod(
                suiteName, testName, testClass.getCanonicalName(), method.getName());

        for (Object instanceKey : testMethodEventLogs.keySet()) {

          long threadId = -1;

          for (EventLog eventLog : testMethodEventLogs.get(instanceKey)) {

            if (threadId == -1) {
              threadId = eventLog.getThreadId();
            } else {
              assertEquals(
                  eventLog.getThreadId(),
                  threadId,
                  "All of the method level events for the test "
                      + "method "
                      + method.getName()
                      + " in the test class "
                      + testClass.getCanonicalName()
                      + " for the test "
                      + suiteName
                      + " should be run in the same thread");
            }
          }
        }
      }
    }
  }

  public static void verifySequentialSuites(
      List<EventLog> suiteLevelEventLogs, Map<String, List<EventLog>> suiteEventLogsMap) {

    verifySameThreadIdForAllEvents(
        suiteLevelEventLogs,
        "Because the suites execute sequentially, the event logs "
            + "suite level events should have the same thread ID: "
            + suiteLevelEventLogs);

    List<EventLog> suiteListenerStartEventLogs = new ArrayList<>();

    for (int i = 0; i < suiteLevelEventLogs.size(); i = i + 2) {
      assertTrue(
          suiteLevelEventLogs.get(i).getEvent() == LISTENER_SUITE_START
              && suiteLevelEventLogs.get(i + 1).getEvent() == LISTENER_SUITE_FINISH,
          "Because the suites are "
              + "expected to execute sequentially, the suite level event logs should consist of a series of "
              + "pairs of a suite listener onStart event logger followed by a suite listener onFinish event logger: "
              + suiteLevelEventLogs);
      suiteListenerStartEventLogs.add((suiteLevelEventLogs.get(i)));
    }

    for (int i = 0; i < suiteListenerStartEventLogs.size() - 1; i++) {
      String firstSuite = (String) suiteListenerStartEventLogs.get(i).getData(SUITE_NAME);
      String secondSuite = (String) suiteListenerStartEventLogs.get(i + 1).getData(SUITE_NAME);

      List<EventLog> firstSuiteEventLogs = suiteEventLogsMap.get(firstSuite);
      List<EventLog> secondSuiteEventLogs = suiteEventLogsMap.get(secondSuite);

      verifySequentialTimingOfEvents(
          firstSuiteEventLogs,
          secondSuiteEventLogs,
          "The first suite listener "
              + "onStart event logger is for "
              + firstSuite
              + " and the second suite listener onStart event logger is "
              + "for "
              + secondSuite
              + ". Because the suites are supposed to execute sequentially, all of the "
              + "event logs for "
              + firstSuite
              + " should have timestamps earlier than all of the event logs for "
              + secondSuite
              + ". First suite event logs: "
              + firstSuiteEventLogs
              + ". Second suite event logs: "
              + secondSuiteEventLogs);
    }
  }

  public static void verifySequentialTests(
      List<EventLog> suiteAndTestLevelEventLogs,
      List<EventLog> testLevelEventLogs,
      EventLog suiteListenerOnStartEventLog,
      EventLog suiteListenerOnFinishEventLog) {

    verifySameThreadIdForAllEvents(
        suiteAndTestLevelEventLogs,
        "All suite level and test level event logs "
            + "should have the same thread ID because there is no parallelism specified at the suite or test "
            + "level: "
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
            + "onFinish event logs. Suite listener onStart event logger: "
            + suiteListenerOnStartEventLog
            + ". Suite listener onFinish event logger: "
            + suiteListenerOnFinishEventLog
            + ". Test level "
            + "event logs: "
            + testLevelEventLogs);

    for (int i = 0; i < testLevelEventLogs.size(); i = i + 2) {
      assertTrue(
          testLevelEventLogs.get(i).getEvent() == LISTENER_TEST_START
              && testLevelEventLogs.get(i + 1).getEvent() == LISTENER_TEST_FINISH,
          "Because the tests are "
              + "expected to execute sequentially, the test level event logs should consist of a series of "
              + "pairs of a test listener onStart event logger followed by a test listener onFinish event logger: "
              + testLevelEventLogs);
    }
  }

  public static void verifyParallelSuitesWithUnequalExecutionTimes(
      List<EventLog> suiteLevelEventLogs, int threadPoolSize) {

    Map<String, EventLog> suitesExecuting = new HashMap<>();
    Map<String, EventLog> suitesCompleted = new HashMap<>();

    List<Long> executingSuiteThreadIds = new ArrayList<>();

    if (suiteLevelEventLogs.size() > 2) {
      int offset =
          suiteLevelEventLogs.size() >= 2 * threadPoolSize
              ? threadPoolSize
              : suiteLevelEventLogs.size() / 2;

      List<EventLog> suiteListenerStartEventLogs = suiteLevelEventLogs.subList(0, offset);

      verifyFirstBlockOfSimultaneouslyExecutingSuites(suiteListenerStartEventLogs, threadPoolSize);

      for (EventLog eventLog : suiteListenerStartEventLogs) {
        suitesExecuting.put((String) eventLog.getData(SUITE_NAME), eventLog);
        executingSuiteThreadIds.add(eventLog.getThreadId());
      }

      for (int i = offset; i < suiteLevelEventLogs.size(); i++) {

        EventLog eventLog = suiteLevelEventLogs.get(i);
        String suiteName = (String) eventLog.getData(SUITE_NAME);

        if (eventLog.getEvent() == LISTENER_SUITE_START) {
          if (suitesExecuting.keySet().size() == threadPoolSize) {
            fail(
                "The thread pool size is "
                    + threadPoolSize
                    + ", so there should be no more than "
                    + threadPoolSize
                    + " suites executing at the same time: "
                    + suiteLevelEventLogs);
          }

          assertFalse(
              suitesExecuting.get(suiteName) != null || suitesCompleted.get(suiteName) != null,
              "There should only be one execution of any given suite");
          assertFalse(
              executingSuiteThreadIds.contains(eventLog.getThreadId()),
              "Event logs for currently " + "executing suites should have different thread IDs");

          suitesExecuting.put(suiteName, eventLog);
          executingSuiteThreadIds.add(eventLog.getThreadId());

          if (suitesCompleted.size() > 0) {
            EventLog priorEventLog = suiteLevelEventLogs.get(i - 1);

            assertEquals(
                priorEventLog.getEvent(),
                LISTENER_SUITE_FINISH,
                "When suites are executing in "
                    + "parallel and a new suite begins execution when the active thread count was last "
                    + "known to be equal to the maximum thread pool size, the previously logged suite "
                    + "level event should be a suite listener onFinish event.");
          }
        }

        if (suitesExecuting.keySet().size() < threadPoolSize
            && suiteLevelEventLogs.size() - i + 1 > threadPoolSize) {
          fail(
              "The thread pool size is "
                  + threadPoolSize
                  + ", so there should be at least "
                  + threadPoolSize
                  + " suites executing at the same time unless there are no suites left to "
                  + "queue and the final block of suites is currently in execution: "
                  + suiteLevelEventLogs);
        }

        if (eventLog.getEvent() == LISTENER_SUITE_FINISH) {

          assertTrue(
              suitesExecuting.get(suiteName) != null,
              "Found an event logger for a suite listener "
                  + "onFinish event that does not have a corresponding event logger for a suite listener "
                  + "onStart event");
          assertTrue(
              suitesExecuting.get(suiteName).getThreadId() == eventLog.getThreadId(),
              "All the "
                  + "suite level event logs for a given suite should have the same thread ID");

          suitesExecuting.remove(suiteName);
          executingSuiteThreadIds.remove(eventLog.getThreadId());
          suitesCompleted.put((String) eventLog.getData(SUITE_NAME), eventLog);
        }
      }
    }
  }

  public static void verifySimultaneousSuiteListenerStartEvents(
      List<EventLog> listenerStartEventLogs, int threadPoolSize) {

    verifyEventTypeForEventsLogs(
        listenerStartEventLogs,
        LISTENER_SUITE_START,
        "The suite thread pool size is "
            + threadPoolSize
            + ", so no more than "
            + threadPoolSize
            + " suites should start running at the "
            + "same time if there are more than "
            + threadPoolSize
            + " suites remaining to execute.");
    verifyDifferentThreadIdsForEvents(
        listenerStartEventLogs,
        "The suite thread pool size is "
            + threadPoolSize
            + ", so the thread IDs for all the suite listener's onStart method for the "
            + threadPoolSize
            + " currently executing suites should be different");
  }

  private static void verifyFirstBlockOfSimultaneouslyExecutingSuites(
      List<EventLog> suiteListenerStartEventLogs, int threadPoolSize) {

    verifySimultaneousSuiteListenerStartEvents(suiteListenerStartEventLogs, threadPoolSize);
    verifyDifferentThreadIdsForEvents(
        suiteListenerStartEventLogs,
        "The thread count is "
            + threadPoolSize
            + " so the thread IDs for the suite listener onStart events for simultaneously executing suites "
            + "should be different. Event logs: "
            + suiteListenerStartEventLogs);
  }

  // Helper method that retrieves the earliest and latest timestamps for the specified list of event
  // logs
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

  private static boolean allExecutingMethodsHaveMoreInvocations(
      Map<String, EventLog> methodsExecuting,
      Map<String, Integer> methodInvocationsCounts,
      Map<String, Integer> expectedInvocationCounts) {

    for (String methodAndClassName : methodsExecuting.keySet()) {
      if (Objects.equals(
          methodInvocationsCounts.get(methodAndClassName),
          expectedInvocationCounts.get(methodAndClassName))) {
        return false;
      }
    }

    return true;
  }

  private static String getStringForEventLogList(Collection<EventLog> list) {
    StringBuilder sb = new StringBuilder();
    ArrayList<EventLog> eventLogs = new ArrayList<>(list);

    for (int i = 0; i < eventLogs.size(); i++) {
      sb.append(eventLogs.get(i));
      if (i != eventLogs.size() - 1) {
        sb.append("\n");
      }
    }

    return sb.toString();
  }
}
