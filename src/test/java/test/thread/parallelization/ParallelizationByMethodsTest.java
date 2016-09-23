package test.thread.parallelization;

import com.google.common.collect.Multimap;
import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static test.thread.parallelization.TestNgRunStateTracker.getSuiteListenerFinishThreadId;
import static test.thread.parallelization.TestNgRunStateTracker.getSuiteListenerFinishTimestamp;
import static test.thread.parallelization.TestNgRunStateTracker.getSuiteListenerStartThreadId;
import static test.thread.parallelization.TestNgRunStateTracker.getSuiteListenerStartTimestamp;
import static test.thread.parallelization.TestNgRunStateTracker.getTestListenerFinishThreadId;
import static test.thread.parallelization.TestNgRunStateTracker.getTestListenerFinishTimestamp;
import static test.thread.parallelization.TestNgRunStateTracker.getTestListenerStartThreadId;
import static test.thread.parallelization.TestNgRunStateTracker.getTestListenerStartTimestamp;
import static test.thread.parallelization.TestNgRunStateTracker.getTestMethodEventLogsForMethod;
import static test.thread.parallelization.TestNgRunStateTracker.getTestMethodExecutionTimestamps;
import static test.thread.parallelization.TestNgRunStateTracker.getTestMethodExeuctionThreadIds;
import static test.thread.parallelization.TestNgRunStateTracker.getTestMethodListenerPassThreadIds;
import static test.thread.parallelization.TestNgRunStateTracker.getTestMethodListenerPassTimestamps;
import static test.thread.parallelization.TestNgRunStateTracker.getTestMethodListenerStartThreadIds;
import static test.thread.parallelization.TestNgRunStateTracker.getTestMethodListenerStartTimestamps;
import static test.thread.parallelization.TestNgRunStateTracker.reset;

public class ParallelizationByMethodsTest extends SimpleBaseTest {

    private Long testListenerOnStartTimestamp;
    private Long testListenerOnFinishTimestamp;
    private Long testListenerOnStartThreadId;

    private Long testMethodAListenerOnStartTimestamp;
    private Long testMethodBListenerOnStartTimestamp;
    private Long testMethodCListenerOnStartTimestamp;
    private Long testMethodDListenerOnStartTimestamp;
    private Long testMethodEListenerOnStartTimestamp;

    private Long testMethodAListenerExecutionTimestamp;
    private Long testMethodBListenerExecutionTimestamp;
    private Long testMethodCListenerExecutionTimestamp;
    private Long testMethodDListenerExecutionTimestamp;
    private Long testMethodEListenerExecutionTimestamp;

    private Long testMethodAListenerOnPassTimestamp;
    private Long testMethodBListenerOnPassTimestamp;
    private Long testMethodCListenerOnPassTimestamp;
    private Long testMethodDListenerOnPassTimestamp;
    private Long testMethodEListenerOnPassTimestamp;

    private Long testMethodAListenerOnStartThreadId;
    private Long testMethodBListenerOnStartThreadId;
    private Long testMethodCListenerOnStartThreadId;
    private Long testMethodDListenerOnStartThreadId;
    private Long testMethodEListenerOnStartThreadId;

    private Long testMethodAExecutionThreadId;
    private Long testMethodBExecutionThreadId;
    private Long testMethodCExecutionThreadId;
    private Long testMethodDExecutionThreadId;
    private Long testMethodEExecutionThreadId;

    private Long testMethodAListenerOnPassThreadId;
    private Long testMethodBListenerOnPassThreadId;
    private Long testMethodCListenerOnPassThreadId;
    private Long testMethodDListenerOnPassThreadId;
    private Long testMethodEListenerOnPassThreadId;

    //Verify simple class with a single suite which consists of a single test with a single test class. The thread count
    //is sufficient to run all methods in parallel at once, so no methods should be queued.
    @BeforeClass
    public void singleSuiteSingleTestSingleTestClassNoMethodQueueing() {
        reset();

        XmlSuite suiteOne = createXmlSuite("SingleTestSuite");
        suiteOne.setParallel(XmlSuite.ParallelMode.METHODS);
        suiteOne.setThreadCount(5);

        createXmlTest(suiteOne, "SingleTestClassTest", TestClassSampleNoDeps.class);

        addParams(suiteOne, "SingleTestSuite", "SingleTestClassTest", "5");

        TestNG tng = create(suiteOne);
        tng.addListener((ITestNGListener)new TestNgRunStateListener());

        tng.run();

        testListenerOnStartTimestamp = getTestListenerStartTimestamp("SingleTestSuite", "SingleTestClassTest");
        testListenerOnFinishTimestamp = getTestListenerFinishTimestamp("SingleTestSuite", "SingleTestClassTest");
        testListenerOnStartThreadId = getTestListenerStartThreadId("SingleTestSuite", "SingleTestClassTest");

        Object instanceKey = getTestMethodEventLogsForMethod("SingleTestSuite", "SingleTestClassTest",
                TestClassSampleNoDeps.class.getCanonicalName(), "testMethodA").keySet().toArray()[0];

        testMethodAListenerOnStartTimestamp = getTestMethodListenerStartTimestamps("SingleTestSuite",
                "SingleTestClassTest", TestClassSampleNoDeps.class.getCanonicalName(), "testMethodA").get(instanceKey);
        testMethodBListenerOnStartTimestamp = getTestMethodListenerStartTimestamps("SingleTestSuite",
                "SingleTestClassTest", TestClassSampleNoDeps.class.getCanonicalName(), "testMethodB").get(instanceKey);
        testMethodCListenerOnStartTimestamp = getTestMethodListenerStartTimestamps("SingleTestSuite",
                "SingleTestClassTest", TestClassSampleNoDeps.class.getCanonicalName(), "testMethodC").get(instanceKey);
        testMethodDListenerOnStartTimestamp = getTestMethodListenerStartTimestamps("SingleTestSuite",
                "SingleTestClassTest", TestClassSampleNoDeps.class.getCanonicalName(), "testMethodD").get(instanceKey);
        testMethodEListenerOnStartTimestamp = getTestMethodListenerStartTimestamps("SingleTestSuite",
                "SingleTestClassTest", TestClassSampleNoDeps.class.getCanonicalName(), "testMethodE").get(instanceKey);

        testMethodAListenerExecutionTimestamp = getTestMethodExecutionTimestamps("SingleTestSuite",
                "SingleTestClassTest", TestClassSampleNoDeps.class.getCanonicalName(), "testMethodA").get(instanceKey);
        testMethodBListenerExecutionTimestamp = getTestMethodExecutionTimestamps("SingleTestSuite",
                "SingleTestClassTest", TestClassSampleNoDeps.class.getCanonicalName(), "testMethodB").get(instanceKey);
        testMethodCListenerExecutionTimestamp = getTestMethodExecutionTimestamps("SingleTestSuite",
                "SingleTestClassTest", TestClassSampleNoDeps.class.getCanonicalName(), "testMethodC").get(instanceKey);
        testMethodDListenerExecutionTimestamp = getTestMethodExecutionTimestamps("SingleTestSuite",
                "SingleTestClassTest", TestClassSampleNoDeps.class.getCanonicalName(), "testMethodD").get(instanceKey);
        testMethodEListenerExecutionTimestamp = getTestMethodExecutionTimestamps("SingleTestSuite",
                "SingleTestClassTest", TestClassSampleNoDeps.class.getCanonicalName(), "testMethodE").get(instanceKey);

        testMethodAListenerOnPassTimestamp = getTestMethodListenerPassTimestamps("SingleTestSuite",
                "SingleTestClassTest", TestClassSampleNoDeps.class.getCanonicalName(), "testMethodA").get(instanceKey);
        testMethodBListenerOnPassTimestamp = getTestMethodListenerPassTimestamps("SingleTestSuite",
                "SingleTestClassTest", TestClassSampleNoDeps.class.getCanonicalName(), "testMethodB").get(instanceKey);
        testMethodCListenerOnPassTimestamp = getTestMethodListenerPassTimestamps("SingleTestSuite",
                "SingleTestClassTest", TestClassSampleNoDeps.class.getCanonicalName(), "testMethodC").get(instanceKey);
        testMethodDListenerOnPassTimestamp = getTestMethodListenerPassTimestamps("SingleTestSuite",
                "SingleTestClassTest", TestClassSampleNoDeps.class.getCanonicalName(), "testMethodD").get(instanceKey);
        testMethodEListenerOnPassTimestamp = getTestMethodListenerPassTimestamps("SingleTestSuite",
                "SingleTestClassTest", TestClassSampleNoDeps.class.getCanonicalName(), "testMethodE").get(instanceKey);

        testMethodAListenerOnStartThreadId = getTestMethodListenerStartThreadIds("SingleTestSuite",
                "SingleTestClassTest", TestClassSampleNoDeps.class.getCanonicalName(), "testMethodA").get(instanceKey);
        testMethodBListenerOnStartThreadId = getTestMethodListenerStartThreadIds("SingleTestSuite",
                "SingleTestClassTest", TestClassSampleNoDeps.class.getCanonicalName(), "testMethodB").get(instanceKey);
        testMethodCListenerOnStartThreadId = getTestMethodListenerStartThreadIds("SingleTestSuite",
                "SingleTestClassTest", TestClassSampleNoDeps.class.getCanonicalName(), "testMethodC").get(instanceKey);
        testMethodDListenerOnStartThreadId = getTestMethodListenerStartThreadIds("SingleTestSuite",
                "SingleTestClassTest", TestClassSampleNoDeps.class.getCanonicalName(), "testMethodD").get(instanceKey);
        testMethodEListenerOnStartThreadId = getTestMethodListenerStartThreadIds("SingleTestSuite",
                "SingleTestClassTest", TestClassSampleNoDeps.class.getCanonicalName(), "testMethodE").get(instanceKey);

        testMethodAExecutionThreadId = getTestMethodExeuctionThreadIds("SingleTestSuite",
                "SingleTestClassTest", TestClassSampleNoDeps.class.getCanonicalName(), "testMethodA").get(instanceKey);
        testMethodBExecutionThreadId  = getTestMethodExeuctionThreadIds("SingleTestSuite",
                "SingleTestClassTest", TestClassSampleNoDeps.class.getCanonicalName(), "testMethodB").get(instanceKey);
        testMethodCExecutionThreadId  = getTestMethodExeuctionThreadIds("SingleTestSuite",
                "SingleTestClassTest", TestClassSampleNoDeps.class.getCanonicalName(), "testMethodC").get(instanceKey);
        testMethodDExecutionThreadId  = getTestMethodExeuctionThreadIds("SingleTestSuite",
                "SingleTestClassTest", TestClassSampleNoDeps.class.getCanonicalName(), "testMethodD").get(instanceKey);
        testMethodEExecutionThreadId  = getTestMethodExeuctionThreadIds("SingleTestSuite",
                "SingleTestClassTest", TestClassSampleNoDeps.class.getCanonicalName(), "testMethodE").get(instanceKey);

        testMethodAListenerOnPassThreadId = getTestMethodListenerPassThreadIds("SingleTestSuite",
                "SingleTestClassTest", TestClassSampleNoDeps.class.getCanonicalName(), "testMethodA").get(instanceKey);
        testMethodBListenerOnPassThreadId = getTestMethodListenerPassThreadIds("SingleTestSuite",
                "SingleTestClassTest", TestClassSampleNoDeps.class.getCanonicalName(), "testMethodB").get(instanceKey);
        testMethodCListenerOnPassThreadId = getTestMethodListenerPassThreadIds("SingleTestSuite",
                "SingleTestClassTest", TestClassSampleNoDeps.class.getCanonicalName(), "testMethodC").get(instanceKey);
        testMethodDListenerOnPassThreadId = getTestMethodListenerPassThreadIds("SingleTestSuite",
                "SingleTestClassTest", TestClassSampleNoDeps.class.getCanonicalName(), "testMethodD").get(instanceKey);
        testMethodEListenerOnPassThreadId = getTestMethodListenerPassThreadIds("SingleTestSuite",
                "SingleTestClassTest", TestClassSampleNoDeps.class.getCanonicalName(), "testMethodE").get(instanceKey);
    }

    //Verify that the suite listener and test listener events have timestamps in the following order: suite start,
    //test start, test finish, suite finish. Verify that all of these events run in the same thread because the
    //parallelization mode is by methods only.
    @Test
    public void verifySuiteAndTestLevelEventsRunInSequentialOrderInSameThread() {

        assertTrue(getSuiteListenerStartTimestamp("SingleTestSuite") < testListenerOnStartTimestamp, "The timestamp " +
                "for the onStart method for the suite listener should be before the timestamp for the onStart method " +
                "for the test listener");
        assertEquals(getSuiteListenerStartThreadId("SingleTestSuite"), testListenerOnStartThreadId, "The thread IDs " +
                "for the suite listener's onStart method and the test listener's onStart method should be the same");
        assertTrue(getSuiteListenerFinishTimestamp("SingleTestSuite") > testListenerOnFinishTimestamp, "The " +
                "timestamp for the onFinish method for the suite listener should be after the timestamp for the " +
                "onFinish method for the test listener");
        assertEquals(getSuiteListenerFinishThreadId("SingleTestSuite"),getTestListenerFinishThreadId("SingleTestSuite",
                "SingleTestClassTest"), "The thread IDs for the suite listener's onFinish method and the test " +
                "listener's onFinish method should be the same");
        assertEquals(testListenerOnStartThreadId, getTestListenerFinishThreadId("SingleTestSuite",
                "SingleTestClassTest"), "The thread ID for the onFinish methods for the suite and test listeners " +
                "should be the same as the thread ID for their onStart methods");
    }

    //Verify that there is only a single test class instance associated with each of the test methods from the
    //sample test class
    @Test
    public void verifyOnlyOneInstanceOfTestClassForAllTestMethods() {
        Multimap<Object,TestNgRunStateTracker.EventLog> testMethodAEventLogMap =
                getTestMethodEventLogsForMethod("SingleTestSuite", "SingleTestClassTest", TestClassSampleNoDeps.class
                        .getCanonicalName(), "testMethodA");

        Multimap<Object,TestNgRunStateTracker.EventLog> testMethodBEventLogMap =
                getTestMethodEventLogsForMethod("SingleTestSuite", "SingleTestClassTest", TestClassSampleNoDeps.class
                        .getCanonicalName(), "testMethodB");

        Multimap<Object,TestNgRunStateTracker.EventLog> testMethodCEventLogMap =
                getTestMethodEventLogsForMethod("SingleTestSuite", "SingleTestClassTest", TestClassSampleNoDeps.class
                        .getCanonicalName(), "testMethodC");

        Multimap<Object,TestNgRunStateTracker.EventLog> testMethodDEventLogMap =
                getTestMethodEventLogsForMethod("SingleTestSuite", "SingleTestClassTest", TestClassSampleNoDeps.class
                        .getCanonicalName(), "testMethodD");

        Multimap<Object,TestNgRunStateTracker.EventLog> testMethodEEventLogMap =
                getTestMethodEventLogsForMethod("SingleTestSuite", "SingleTestClassTest", TestClassSampleNoDeps.class
                        .getCanonicalName(), "testMethodE");

        assertEquals(testMethodAEventLogMap.keySet().size(), 1, "There should be only one test class instance " +
                "associated with testMethodA from " + TestClassSampleNoDeps.class.getCanonicalName());
        assertEquals(testMethodBEventLogMap.keySet().size(), 1, "There should be only one test class instance " +
                "associated with testMethodB from " + TestClassSampleNoDeps.class.getCanonicalName());
        assertEquals(testMethodCEventLogMap.keySet().size(), 1, "There should be only one test class instance " +
                "associated with testMethodC from " + TestClassSampleNoDeps.class.getCanonicalName());
        assertEquals(testMethodDEventLogMap.keySet().size(), 1, "There should be only one test class instance " +
                "associated with testMethodD from " + TestClassSampleNoDeps.class.getCanonicalName());
        assertEquals(testMethodEEventLogMap.keySet().size(), 1, "There should be only one test class instance " +
                "associated with testMethodE from " + TestClassSampleNoDeps.class.getCanonicalName());

        Object methodAInstanceKey = getTestMethodEventLogsForMethod("SingleTestSuite", "SingleTestClassTest",
                TestClassSampleNoDeps.class.getCanonicalName(), "testMethodA").keySet().toArray()[0];
        Object methodBInstanceKey = getTestMethodEventLogsForMethod("SingleTestSuite", "SingleTestClassTest",
                TestClassSampleNoDeps.class.getCanonicalName(), "testMethodB").keySet().toArray()[0];
        Object methodCInstanceKey = getTestMethodEventLogsForMethod("SingleTestSuite", "SingleTestClassTest",
                TestClassSampleNoDeps.class.getCanonicalName(), "testMethodC").keySet().toArray()[0];
        Object methodDInstanceKey = getTestMethodEventLogsForMethod("SingleTestSuite", "SingleTestClassTest",
                TestClassSampleNoDeps.class.getCanonicalName(), "testMethodD").keySet().toArray()[0];
        Object methodEInstanceKey = getTestMethodEventLogsForMethod("SingleTestSuite", "SingleTestClassTest",
                TestClassSampleNoDeps.class.getCanonicalName(), "testMethodE").keySet().toArray()[0];

        assertTrue(
                methodAInstanceKey == methodBInstanceKey &&
                methodBInstanceKey == methodCInstanceKey &&
                methodCInstanceKey == methodDInstanceKey &&
                methodDInstanceKey == methodEInstanceKey,
                "The test methods should all have the same single test class instance"
        );
    }

    //Verify that the test method listener's onTestStart method runs after the test listener's onStart method for
    //all the test methods from the test class.
    @Test
    public void verifyTestMethodLevelListenerOnStartOccursAfterTestListenerStart() {

        assertTrue(testMethodAListenerOnStartTimestamp > testListenerOnStartTimestamp, "The timestamp for the test " +
                "method listener's onTestStart method for testMethodA should be after the timestamp for the test " +
                "listener's onStart method");
        assertTrue(testMethodBListenerOnStartTimestamp > testListenerOnStartTimestamp, "The timestamp for the test " +
                "method listener's onTestStart method for testMethodB should be after the timestamp for the test " +
                "listener's onStart method");
        assertTrue(testMethodCListenerOnStartTimestamp > testListenerOnStartTimestamp, "The timestamp for the test " +
                "method listener's onTestStart method for testMethodC should be after the timestamp for the test " +
                "listener's onStart method");
        assertTrue(testMethodDListenerOnStartTimestamp > testListenerOnStartTimestamp, "The timestamp for the test " +
                "method listener's onTestStart method for testMethodD should be after the timestamp for the test " +
                "listener's onStart method");
        assertTrue(testMethodEListenerOnStartTimestamp > testListenerOnStartTimestamp, "The timestamp for the test " +
                "method listener's onTestStart method for testMethodE should be after the timestamp for the test " +
                "listener's onStart method");
    }

    //Verify that the test method listener's onTestSuccess method runs before the test listener's onFinish method
    //for all the test methods from the test class.
    @Test
    public void verifyTestMethodLevelListenerOnSuccessRunsBeforeTestListenerOnFinish() {

        assertTrue(testListenerOnFinishTimestamp > testMethodAListenerOnPassTimestamp, "The timestamp for the test " +
                "method listener's onTestSuccess method for testMethodA should be before the timestamp for the test " +
                "listener's onFinish method");
        assertTrue(testListenerOnFinishTimestamp > testMethodBListenerOnPassTimestamp, "The timestamp for the test " +
                "method listener's onTestSuccess method for testMethodB should be before the timestamp for the test " +
                "listener's onFinish method");
        assertTrue(testListenerOnFinishTimestamp > testMethodCListenerOnPassTimestamp, "The timestamp for the test " +
                "method listener's onTestSuccess method for testMethodC should be before the timestamp for the test " +
                "listener's onFinish method");
        assertTrue(testListenerOnFinishTimestamp > testMethodDListenerOnPassTimestamp, "The timestamp for the test " +
                "method listener's onTestSuccess method for testMethodD should be before the timestamp for the test " +
                "listener's onFinish method");
        assertTrue(testListenerOnFinishTimestamp > testMethodEListenerOnPassTimestamp, "The timestamp for the test " +
                "method listener's onTestSuccess method for testMethodE should be before the timestamp for the test " +
                "listener's onFinish method");

    }

    //Verify that the test method listener's onTestStart method runs before the test method begins execution
    //Verify that the test method listener's onTestSuccess method runs after the test method executes and passes
    @Test
    public void verifyTestExecutionTimestampIsAfterTestListenerOnStartAndBeforeTestListenerOnTestSuccess() {

        assertTrue(testMethodAListenerExecutionTimestamp > testMethodAListenerOnStartTimestamp, "The timestamp for " +
                "the test method listener's onTestStart method for testMethodA should be before the timestamp for " +
                "the method's execution.");
        assertTrue(testMethodBListenerExecutionTimestamp > testMethodBListenerOnStartTimestamp, "The timestamp for " +
                "the test method listener's onTestStart method for testMethodB should be before the timestamp for " +
                "the method's execution.");
        assertTrue(testMethodCListenerExecutionTimestamp > testMethodCListenerOnStartTimestamp, "The timestamp for " +
                "the test method listener's onTestStart method for testMethodC should be before the timestamp for " +
                "the method's execution.");
        assertTrue(testMethodDListenerExecutionTimestamp > testMethodDListenerOnStartTimestamp, "The timestamp for " +
                "the test method listener's onTestStart method for testMethodD should be before the timestamp for " +
                "the method's execution.");
        assertTrue(testMethodEListenerExecutionTimestamp > testMethodEListenerOnStartTimestamp, "The timestamp for " +
                "the test method listener's onTestStart method for testMethodE should be before the timestamp for " +
                "the method's execution.");

        assertTrue(testMethodAListenerExecutionTimestamp < testMethodAListenerOnPassTimestamp, "The timestamp for " +
                "the test method listener's onTestSuccess method for testMethodA should be after the timestamp for " +
                "the method's execution.");
        assertTrue(testMethodBListenerExecutionTimestamp < testMethodBListenerOnPassTimestamp, "The timestamp for " +
                "the test method listener's onTestSuccess method for testMethodB should be after the timestamp for " +
                "the method's execution");
        assertTrue(testMethodCListenerExecutionTimestamp < testMethodCListenerOnPassTimestamp, "The timestamp for " +
                "the test method listener's onTestSuccess method for testMethodC should be after the timestamp for " +
                "the method's execution");
        assertTrue(testMethodDListenerExecutionTimestamp < testMethodDListenerOnPassTimestamp, "The timestamp for " +
                "the test method listener's onTestSuccess method for testMethodE should be after the timestamp for " +
                "the method's execution");
        assertTrue(testMethodEListenerExecutionTimestamp < testMethodEListenerOnPassTimestamp, "The timestamp for " +
                "the test method listener's onTestSuccess method for testMethodE should be after the timestamp for " +
                "the method's execution");
    }

    //Verifies that the method level events all run in different threads from the test and suite level events.
    //Verifies that the test method listener and execution events for a given test method all run in the same thread.
    @Test
    public void verifyThatMethodLevelEventsRunInDifferentThreadsFromSuiteAndTestLevelEvents() {

        assertTrue(testMethodAListenerOnStartThreadId > testListenerOnStartThreadId, "The thread for the test method " +
                "listener for testMethodA should be spawned after the thread in which the suite and test level " +
                "listener events run");
        assertTrue(testMethodBListenerOnStartThreadId > testListenerOnStartThreadId, "The thread for the test method " +
                "listener for testMethodB should be spawned after the thread in which the suite and test level " +
                "listener events run");
        assertTrue(testMethodCListenerOnStartThreadId > testListenerOnStartThreadId, "The thread for the test method " +
                "listener for testMethodB should be spawned after the thread in which the suite and test level " +
                "listener events run");
        assertTrue(testMethodDListenerOnStartThreadId > testListenerOnStartThreadId, "The thread for the test method " +
                "listener for testMethodB should be spawned after the thread in which the suite and test level " +
                "listener events run");
        assertTrue(testMethodEListenerOnStartThreadId > testListenerOnStartThreadId, "The thread for the test method " +
                "listener for testMethodC should be spawned after the thread in which the suite and test level " +
                "listener events run");

        assertTrue(
                testMethodAListenerOnStartThreadId.equals(testMethodAExecutionThreadId) &&
                testMethodAListenerOnStartThreadId.equals(testMethodAListenerOnPassThreadId),
                "The listener and execution events for testMethodA should run in the same thread"
        );

        assertTrue(
                testMethodBListenerOnStartThreadId.equals(testMethodBExecutionThreadId) &&
                        testMethodBListenerOnStartThreadId.equals(testMethodBListenerOnPassThreadId),
                "The listener and execution events for testMethodB should run in the same thread"
        );

        assertTrue(
                testMethodCListenerOnStartThreadId.equals(testMethodCExecutionThreadId) &&
                        testMethodCListenerOnStartThreadId.equals(testMethodCListenerOnPassThreadId),
                "The listener and execution events for testMethodC should run in the same thread"
        );

        assertTrue(
                testMethodDListenerOnStartThreadId.equals(testMethodDExecutionThreadId) &&
                        testMethodDListenerOnStartThreadId.equals(testMethodDListenerOnPassThreadId),
                "The listener and execution events for testMethodD should run in the same thread"
        );

        assertTrue(
                testMethodEListenerOnStartThreadId.equals(testMethodEExecutionThreadId) &&
                        testMethodEListenerOnStartThreadId.equals(testMethodEListenerOnPassThreadId),
                "The listener and execution events for testMethodE should run in the same thread"
        );
    }

    @Test
    public void verifyThatTestMethodsRunInParallelThreads() {

        assertTrue(
                !testMethodAListenerOnStartThreadId.equals(testMethodBListenerOnStartThreadId) &&
                        !testMethodAListenerOnStartThreadId.equals(testMethodCListenerOnStartThreadId) &&
                        !testMethodAListenerOnStartThreadId.equals(testMethodDListenerOnStartThreadId) &&
                        !testMethodAListenerOnStartThreadId.equals(testMethodEListenerOnStartThreadId) &&
                        !testMethodBListenerOnStartThreadId.equals(testMethodCListenerOnStartThreadId) &&
                        !testMethodBListenerOnStartThreadId.equals(testMethodDListenerOnStartThreadId) &&
                        !testMethodBListenerOnStartThreadId.equals(testMethodEListenerOnStartThreadId) &&
                        !testMethodCListenerOnStartThreadId.equals(testMethodDListenerOnStartThreadId) &&
                        !testMethodCListenerOnStartThreadId.equals(testMethodEListenerOnStartThreadId) &&
                        !testMethodDListenerOnStartThreadId.equals(testMethodEListenerOnStartThreadId),
                "The test method listener and execution events for each of the test methods should execute in " +
                        "different threads"
        );

        assertTrue(
                Math.abs(testMethodAListenerOnStartTimestamp - testMethodBListenerOnStartTimestamp) <= 50 &&
                        Math.abs(testMethodAListenerOnStartTimestamp - testMethodCListenerOnStartTimestamp) <= 50 &&
                        Math.abs(testMethodAListenerOnStartTimestamp - testMethodDListenerOnStartTimestamp) <= 50 &&
                        Math.abs(testMethodAListenerOnStartTimestamp - testMethodEListenerOnStartTimestamp) <= 50,
                "The test listener's onStart event for all five methods should run within 50 milliseconds of each " +
                        "other");

        assertTrue(
                Math.abs(testMethodAListenerExecutionTimestamp - testMethodBListenerExecutionTimestamp) <= 50 &&
                        Math.abs(testMethodAListenerExecutionTimestamp - testMethodCListenerExecutionTimestamp) <= 50 &&
                        Math.abs(testMethodAListenerExecutionTimestamp - testMethodDListenerExecutionTimestamp) <= 50 &&
                        Math.abs(testMethodAListenerExecutionTimestamp - testMethodEListenerExecutionTimestamp) <= 50,
                "The execution timestamps for all five methods should be within 50 milliseconds of each " +
                        "other");

        assertTrue(
                Math.abs(testMethodAListenerOnPassTimestamp - testMethodBListenerOnPassTimestamp) <= 50 &&
                        Math.abs(testMethodAListenerOnPassTimestamp - testMethodCListenerOnPassTimestamp) <= 50 &&
                        Math.abs(testMethodAListenerOnPassTimestamp - testMethodDListenerOnPassTimestamp) <= 50 &&
                        Math.abs(testMethodAListenerOnPassTimestamp - testMethodEListenerOnPassTimestamp) <= 50,
                "The test listener's onTestSuccess event for all five methods should run within 50 milliseconds of " +
                        "each other");

        assertTrue(
                Math.abs(testMethodAListenerExecutionTimestamp - testMethodBListenerOnPassTimestamp) < 5050 &&
                        Math.abs(testMethodAListenerExecutionTimestamp - testMethodCListenerOnPassTimestamp) < 5050 &&
                        Math.abs(testMethodAListenerExecutionTimestamp - testMethodDListenerOnPassTimestamp) < 5050 &&
                        Math.abs(testMethodAListenerExecutionTimestamp - testMethodEListenerOnPassTimestamp) < 5050,
                "The difference between the execution timestamps of all five methods should be within 5050 " +
                        "milliseconds of the timestamps for the test listener's onTestSuccess events for all the " +
                        "other methods."
        );

    }

    private void addParams(XmlSuite suite, String suiteName, String testName, String sleepFor) {
        Map<String,String> parameters = new HashMap<>();
        parameters.put("suiteName", suiteName);
        parameters.put("testName", testName);
        parameters.put("sleepFor", sleepFor);

        for(XmlTest test : suite.getTests()) {
            if(test.getName().equals(testName)) {
                test.setParameters(parameters);
            }
        }
    }
}
