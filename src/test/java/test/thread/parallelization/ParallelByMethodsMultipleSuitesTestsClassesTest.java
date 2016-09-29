package test.thread.parallelization;

import com.google.common.collect.Multimap;
import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import test.thread.parallelization.TestNgRunStateTracker.EventLog;
import test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import static org.testng.Assert.fail;
import static test.thread.parallelization.TestNgRunStateTracker.EventInfo.METHOD_NAME;
import static test.thread.parallelization.TestNgRunStateTracker.getAllEventLogsForSuite;
import static test.thread.parallelization.TestNgRunStateTracker.getAllEventLogsForTest;

import static test.thread.parallelization.TestNgRunStateTracker.getSuiteListenerFinishThreadId;
import static test.thread.parallelization.TestNgRunStateTracker.getSuiteListenerFinishTimestamp;
import static test.thread.parallelization.TestNgRunStateTracker.getSuiteListenerStartEventLogs;
import static test.thread.parallelization.TestNgRunStateTracker.getSuiteListenerStartThreadId;
import static test.thread.parallelization.TestNgRunStateTracker.getSuiteListenerStartTimestamp;

import static test.thread.parallelization.TestNgRunStateTracker.getTestListenerFinishThreadId;
import static test.thread.parallelization.TestNgRunStateTracker.getTestListenerFinishTimestamp;
import static test.thread.parallelization.TestNgRunStateTracker.getTestListenerStartEventLogsForSuite;
import static test.thread.parallelization.TestNgRunStateTracker.getTestListenerStartThreadId;
import static test.thread.parallelization.TestNgRunStateTracker.getTestListenerStartTimestamp;

import static test.thread.parallelization.TestNgRunStateTracker.getTestMethodEventLogsForMethod;
import static test.thread.parallelization.TestNgRunStateTracker.getTestMethodLevelEventLogsForTest;
import static test.thread.parallelization.TestNgRunStateTracker.getTestMethodExecutionEventLogsForTest;
import static test.thread.parallelization.TestNgRunStateTracker.getTestMethodListenerPassEventLogsForTest;
import static test.thread.parallelization.TestNgRunStateTracker.getTestMethodListenerStartEventLogsForTest;
import static test.thread.parallelization.TestNgRunStateTracker.getTestMethodEventLogsForClass;

import static test.thread.parallelization.TestNgRunStateTracker.reset;

import static test.thread.parallelization.TestNgRunStateTracker.EventInfo.CLASS_NAME;
import static test.thread.parallelization.TestNgRunStateTracker.EventInfo.SUITE_NAME;
import static test.thread.parallelization.TestNgRunStateTracker.EventInfo.TEST_NAME;

import static test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent.LISTENER_TEST_METHOD_START;
import static test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent.TEST_METHOD_EXECUTION;
import static test.thread.parallelization.TestNgRunStateTracker.TestNgRunEvent.LISTENER_TEST_METHOD_PASS;

//Verify complex test run with multiple suites, tests and test classes and a thread count which is less than the number
//of test methods to be executed.
public class ParallelByMethodsMultipleSuitesTestsClassesTest extends BaseParallelizationTest {
    private static final String SUITE_A = "TestSuiteA";
    private static final String SUITE_B = "TestSuiteB";
    private static final String SUITE_C = "TestSuiteC";
    
    private static final String SUITE_A_TEST_A = "TestSuiteA-TwoTestClassTest";

    private static final String SUITE_B_TEST_A = "TestSuiteB-SingleTestClassTest";
    private static final String SUITE_B_TEST_B = "TestSuiteB-ThreeTestClassTest";
    
    private static final String SUITE_C_TEST_A = "TestSuiteC-ThreeTestClassTest";
    private static final String SUITE_C_TEST_B = "TestSuiteC-TwoTestClassTest";
    private static final String SUITE_C_TEST_C = "TestSuiteC-FourTestClassTest";
    
    private List<EventLog> suiteOneEventLogs;
    private List<EventLog> suiteTwoEventLogs;
    private List<EventLog> suiteThreeEventLogs;

    private List<EventLog> suiteTwoTestOneEventLogs;
    private List<EventLog> suiteTwoTestTwoEventLogs;

    private List<EventLog> suiteThreeTestOneEventLogs;
    private List<EventLog> suiteThreeTestTwoEventLogs;
    private List<EventLog> suiteThreeTestThreeEventLogs;

    private Long suiteOneListenerStartTimestamp;
    private Long suiteTwoListenerStartTimestamp;
    private Long suiteThreeListenerStartTimestamp;

    private Long suiteOneListenerFinishTimestamp;
    private Long suiteTwoListenerFinishTimestamp;
    private Long suiteThreeListenerFinishTimestamp;

    private Long suiteOneListenerStartThreadId;
    private Long suiteTwoListenerStartThreadId;
    private Long suiteThreeListenerStartThreadId;

    private Long suiteOneListenerFinishThreadId;
    private Long suiteTwoListenerFinishThreadId;
    private Long suiteThreeListenerFinishThreadId;

    private Long suiteOneTestOneListenerStartTimestamp;
    private Long suiteOneTestOneListenerFinishTimestamp;
    
    private Long suiteOneTestOneListenerStartThreadId;
    private Long suiteOneTestOneListenerFinishThreadId;

    private Long suiteTwoTestOneListenerStartTimestamp;
    private Long suiteTwoTestTwoListenerStartTimestamp;
    private Long suiteTwoTestOneListenerFinishTimestamp;
    private Long suiteTwoTestTwoListenerFinishTimestamp;

    private Long suiteTwoTestOneListenerStartThreadId;
    private Long suiteTwoTestTwoListenerStartThreadId;
    private Long suiteTwoTestOneListenerFinishThreadId;
    private Long suiteTwoTestTwoListenerFinishThreadId;
    
    private Long suiteThreeTestOneListenerStartTimestamp;
    private Long suiteThreeTestTwoListenerStartTimestamp;
    private Long suiteThreeTestThreeListenerStartTimestamp;
    private Long suiteThreeTestOneListenerFinishTimestamp;
    private Long suiteThreeTestTwoListenerFinishTimestamp;
    private Long suiteThreeTestThreeListenerFinishTimestamp;

    private Long suiteThreeTestOneListenerStartThreadId;
    private Long suiteThreeTestTwoListenerStartThreadId;
    private Long suiteThreeTestThreeListenerStartThreadId;
    private Long suiteThreeTestOneListenerFinishThreadId;
    private Long suiteThreeTestTwoListenerFinishThreadId;
    private Long suiteThreeTestThreeListenerFinishThreadId;

    private Multimap<Object,EventLog> suiteOneTestOneFiveMethodsClassEventLogMap;
    private Multimap<Object,EventLog> suiteOneTestOneSixMethodsClassEventLogMap;

    private Multimap<Object,EventLog> suiteTwoTestOneFiveMethodsClassEventLogMap;
    private Multimap<Object,EventLog> suiteTwoTestTwoThreeMethodsClassEventLogMap;
    private Multimap<Object,EventLog> suiteTwoTestTwoFourMethodsClassEventLogMap;
    private Multimap<Object,EventLog> suiteTwoTestTwoSixMethodsClassEventLogMap;

    private Multimap<Object,EventLog> suiteThreeTestOneThreeMethodsClassEventLogMap;
    private Multimap<Object,EventLog> suiteThreeTestOneFourMethodsClassEventLogMap;
    private Multimap<Object,EventLog> suiteThreeTestOneFiveMethodsClassEventLogMap;

    private Multimap<Object,EventLog> suiteThreeTestTwoFourMethodsClassEventLogMap;
    private Multimap<Object,EventLog> suiteThreeTestTwoFiveMethodsClassEventLogMap;

    private Multimap<Object,EventLog> suiteThreeTestThreeThreeMethodsClassEventLogMap;
    private Multimap<Object,EventLog> suiteThreeTestThreeFourMethodsClassEventLogMap;
    private Multimap<Object,EventLog> suiteThreeTestThreeFiveMethodsClassEventLogMap;
    private Multimap<Object,EventLog> suiteThreeTestThreeSixMethodsClassEventLogMap;

    private List<EventLog> suiteOneTestOneTestMethodListenerStartEventLogs;
    private List<EventLog> suiteOneTestOneTestMethodListenerPassEventLogs;
    private List<EventLog> suiteOneTestOneTestMethodExecutionEventLogs;

    private List<EventLog> suiteTwoTestOneTestMethodListenerStartEventLogs;
    private List<EventLog> suiteTwoTestTwoTestMethodListenerStartEventLogs;
    private List<EventLog> suiteTwoTestOneTestMethodListenerPassEventLogs;
    private List<EventLog> suiteTwoTestTwoTestMethodListenerPassEventLogs;
    private List<EventLog> suiteTwoTestOneTestMethodExecutionEventLogs;
    private List<EventLog> suiteTwoTestTwoTestMethodExecutionEventLogs;

    private List<EventLog> suiteThreeTestOneTestMethodListenerStartEventLogs;
    private List<EventLog> suiteThreeTestTwoTestMethodListenerStartEventLogs;
    private List<EventLog> suiteThreeTestThreeTestMethodListenerStartEventLogs;
    private List<EventLog> suiteThreeTestOneTestMethodListenerPassEventLogs;
    private List<EventLog> suiteThreeTestTwoTestMethodListenerPassEventLogs;
    private List<EventLog> suiteThreeTestThreeTestMethodListenerPassEventLogs;
    private List<EventLog> suiteThreeTestOneTestMethodExecutionEventLogs;
    private List<EventLog> suiteThreeTestTwoTestMethodExecutionEventLogs;
    private List<EventLog> suiteThreeTestThreeTestMethodExecutionEventLogs;

    @BeforeClass
    public void complexMultipleSuites() {
        reset();

        XmlSuite suiteOne = createXmlSuite(SUITE_A);
        XmlSuite suiteTwo = createXmlSuite(SUITE_B);
        XmlSuite suiteThree = createXmlSuite(SUITE_C);

        suiteOne.setParallel(XmlSuite.ParallelMode.METHODS);
        suiteOne.setThreadCount(3);

        createXmlTest(suiteOne, SUITE_A_TEST_A, TestClassAWithNoDepsSample.class, TestClassCWithNoDepsSample.class);
        createXmlTest(suiteTwo, SUITE_B_TEST_A, TestClassEWithNoDepsSample.class);
        createXmlTest(suiteTwo, SUITE_B_TEST_B, TestClassDWithNoDepsSample.class, TestClassBWithNoDepsSample.class,
                TestClassFWithNoDepsSample.class);

        suiteTwo.setParallel(XmlSuite.ParallelMode.METHODS);

        for(XmlTest test : suiteTwo.getTests()) {
            if(test.getName().equals(SUITE_B_TEST_A)) {
                test.setThreadCount(6);
            } else {
                test.setThreadCount(20);
            }
        }

        createXmlTest(suiteThree, SUITE_C_TEST_A, TestClassGWithNoDepsSample.class, TestClassHWithNoDepsSample.class,
                TestClassIWithNoDepsSample.class);
        createXmlTest(suiteThree, SUITE_C_TEST_B, TestClassJWithNoDepsSample.class, TestClassKWithNoDepsSample.class);
        createXmlTest(suiteThree, SUITE_C_TEST_C, TestClassLWithNoDepsSample.class, TestClassMWithNoDepsSample.class,
                TestClassNWithNoDepsSample.class, TestClassOWithNoDepsSample.class);

        for(XmlTest test : suiteThree.getTests()) {
            test.setParallel(XmlSuite.ParallelMode.METHODS);

            switch(test.getName()) {
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

        addParams(suiteOne, SUITE_A, SUITE_A_TEST_A, "5");

        addParams(suiteTwo, SUITE_B, SUITE_B_TEST_A, "5");
        addParams(suiteTwo, SUITE_B, SUITE_B_TEST_B, "5");

        addParams(suiteThree, SUITE_C, SUITE_C_TEST_A, "5");
        addParams(suiteThree, SUITE_C, SUITE_C_TEST_B, "5");
        addParams(suiteThree, SUITE_C, SUITE_C_TEST_C, "5");

        TestNG tng = create(suiteOne, suiteTwo, suiteThree);
        tng.addListener((ITestNGListener) new TestNgRunStateListener());

        tng.run();

        suiteOneEventLogs = getAllEventLogsForSuite(SUITE_A);
        suiteTwoEventLogs = getAllEventLogsForSuite(SUITE_B);
        suiteThreeEventLogs = getAllEventLogsForSuite(SUITE_C);

        suiteTwoTestOneEventLogs = getAllEventLogsForTest(SUITE_B, SUITE_B_TEST_A);
        suiteTwoTestTwoEventLogs = getAllEventLogsForTest(SUITE_B, SUITE_B_TEST_B);

        suiteThreeTestOneEventLogs = getAllEventLogsForTest(SUITE_C, SUITE_C_TEST_A);
        suiteThreeTestTwoEventLogs = getAllEventLogsForTest(SUITE_C, SUITE_C_TEST_B);
        suiteThreeTestThreeEventLogs = getAllEventLogsForTest(SUITE_C, SUITE_C_TEST_C);

        suiteOneListenerStartTimestamp = getSuiteListenerStartTimestamp(SUITE_A);
        suiteTwoListenerStartTimestamp = getSuiteListenerStartTimestamp(SUITE_B);
        suiteThreeListenerStartTimestamp = getSuiteListenerStartTimestamp(SUITE_C);

        suiteOneListenerFinishTimestamp = getSuiteListenerFinishTimestamp(SUITE_A);
        suiteTwoListenerFinishTimestamp = getSuiteListenerFinishTimestamp(SUITE_B);
        suiteThreeListenerFinishTimestamp = getSuiteListenerFinishTimestamp(SUITE_C);

        suiteOneListenerStartThreadId = getSuiteListenerStartThreadId(SUITE_A);
        suiteTwoListenerStartThreadId = getSuiteListenerStartThreadId(SUITE_B);
        suiteThreeListenerStartThreadId = getSuiteListenerStartThreadId(SUITE_C);

        suiteOneListenerFinishThreadId = getSuiteListenerFinishThreadId(SUITE_A);
        suiteTwoListenerFinishThreadId = getSuiteListenerFinishThreadId(SUITE_B);
        suiteThreeListenerFinishThreadId= getSuiteListenerFinishThreadId(SUITE_C);

        suiteOneTestOneListenerStartTimestamp = getTestListenerStartTimestamp(SUITE_A, SUITE_A_TEST_A);
        suiteOneTestOneListenerFinishTimestamp = getTestListenerFinishTimestamp(SUITE_A, SUITE_A_TEST_A);

        suiteOneTestOneListenerStartThreadId = getTestListenerStartThreadId(SUITE_A, SUITE_A_TEST_A);
        suiteOneTestOneListenerFinishThreadId = getTestListenerFinishThreadId(SUITE_A, SUITE_A_TEST_A);

        suiteTwoTestOneListenerStartTimestamp = getTestListenerStartTimestamp(SUITE_B, SUITE_B_TEST_A);
        suiteTwoTestTwoListenerStartTimestamp = getTestListenerStartTimestamp(SUITE_B, SUITE_B_TEST_B);
        suiteTwoTestOneListenerFinishTimestamp = getTestListenerFinishTimestamp(SUITE_B, SUITE_B_TEST_A);
        suiteTwoTestTwoListenerFinishTimestamp = getTestListenerFinishTimestamp(SUITE_B, SUITE_B_TEST_B);

        suiteTwoTestOneListenerStartThreadId = getTestListenerStartThreadId(SUITE_B, SUITE_B_TEST_A);
        suiteTwoTestTwoListenerStartThreadId = getTestListenerStartThreadId(SUITE_B, SUITE_B_TEST_B);
        suiteTwoTestOneListenerFinishThreadId = getTestListenerFinishThreadId(SUITE_B, SUITE_B_TEST_A);
        suiteTwoTestTwoListenerFinishThreadId = getTestListenerFinishThreadId(SUITE_B, SUITE_B_TEST_B);

        suiteThreeTestOneListenerStartTimestamp = getTestListenerStartTimestamp(SUITE_C, SUITE_C_TEST_A);
        suiteThreeTestTwoListenerStartTimestamp = getTestListenerStartTimestamp(SUITE_C, SUITE_C_TEST_B);
        suiteThreeTestThreeListenerStartTimestamp = getTestListenerStartTimestamp(SUITE_C, SUITE_C_TEST_C);

        suiteThreeTestOneListenerFinishTimestamp = getTestListenerFinishTimestamp(SUITE_C, SUITE_C_TEST_A);
        suiteThreeTestTwoListenerFinishTimestamp = getTestListenerFinishTimestamp(SUITE_C, SUITE_C_TEST_B);
        suiteThreeTestThreeListenerFinishTimestamp = getTestListenerFinishTimestamp(SUITE_C, SUITE_C_TEST_C);

        suiteThreeTestOneListenerStartThreadId = getTestListenerStartThreadId(SUITE_C, SUITE_C_TEST_A);
        suiteThreeTestTwoListenerStartThreadId = getTestListenerStartThreadId(SUITE_C, SUITE_C_TEST_B);
        suiteThreeTestThreeListenerStartThreadId = getTestListenerStartThreadId(SUITE_C, SUITE_C_TEST_C);
        suiteThreeTestOneListenerFinishThreadId = getTestListenerFinishThreadId(SUITE_C, SUITE_C_TEST_A);
        suiteThreeTestTwoListenerFinishThreadId = getTestListenerFinishThreadId(SUITE_C, SUITE_C_TEST_B);
        suiteThreeTestThreeListenerFinishThreadId = getTestListenerFinishThreadId(SUITE_C, SUITE_C_TEST_C);

        suiteOneTestOneFiveMethodsClassEventLogMap = getTestMethodEventLogsForClass(SUITE_A, SUITE_A_TEST_A,
                TestClassAWithNoDepsSample.class.getCanonicalName());
        suiteOneTestOneSixMethodsClassEventLogMap = getTestMethodEventLogsForClass(SUITE_A, SUITE_A_TEST_A,
                TestClassCWithNoDepsSample.class.getCanonicalName());

        suiteTwoTestOneFiveMethodsClassEventLogMap = getTestMethodEventLogsForClass(SUITE_B, SUITE_B_TEST_A,
                TestClassEWithNoDepsSample.class.getCanonicalName());

        suiteTwoTestTwoThreeMethodsClassEventLogMap = getTestMethodEventLogsForClass(SUITE_B, SUITE_B_TEST_B,
                TestClassDWithNoDepsSample.class.getCanonicalName());
        suiteTwoTestTwoFourMethodsClassEventLogMap = getTestMethodEventLogsForClass(SUITE_B, SUITE_B_TEST_B,
                TestClassBWithNoDepsSample.class.getCanonicalName());
        suiteTwoTestTwoSixMethodsClassEventLogMap = getTestMethodEventLogsForClass(SUITE_B, SUITE_B_TEST_B,
                TestClassFWithNoDepsSample.class.getCanonicalName());

        suiteThreeTestOneThreeMethodsClassEventLogMap = getTestMethodEventLogsForClass(SUITE_C, SUITE_C_TEST_A,
                TestClassGWithNoDepsSample.class.getCanonicalName());
        suiteThreeTestOneFourMethodsClassEventLogMap = getTestMethodEventLogsForClass(SUITE_C, SUITE_C_TEST_A,
                TestClassHWithNoDepsSample.class.getCanonicalName());
        suiteThreeTestOneFiveMethodsClassEventLogMap = getTestMethodEventLogsForClass(SUITE_C, SUITE_C_TEST_A,
                TestClassIWithNoDepsSample.class.getCanonicalName());

        suiteThreeTestTwoFourMethodsClassEventLogMap = getTestMethodEventLogsForClass(SUITE_C, SUITE_C_TEST_B,
                TestClassJWithNoDepsSample.class.getCanonicalName());
        suiteThreeTestTwoFiveMethodsClassEventLogMap = getTestMethodEventLogsForClass(SUITE_C, SUITE_C_TEST_B,
                TestClassKWithNoDepsSample.class.getCanonicalName());

        suiteThreeTestThreeThreeMethodsClassEventLogMap = getTestMethodEventLogsForClass(SUITE_C, SUITE_C_TEST_C,
                TestClassLWithNoDepsSample.class.getCanonicalName());
        suiteThreeTestThreeFourMethodsClassEventLogMap = getTestMethodEventLogsForClass(SUITE_C, SUITE_C_TEST_C,
                TestClassMWithNoDepsSample.class.getCanonicalName());
        suiteThreeTestThreeFiveMethodsClassEventLogMap = getTestMethodEventLogsForClass(SUITE_C, SUITE_C_TEST_C,
                TestClassNWithNoDepsSample.class.getCanonicalName());
        suiteThreeTestThreeSixMethodsClassEventLogMap = getTestMethodEventLogsForClass(SUITE_C, SUITE_C_TEST_C,
                TestClassOWithNoDepsSample.class.getCanonicalName());

        suiteOneTestOneTestMethodListenerStartEventLogs = getTestMethodListenerStartEventLogsForTest(SUITE_A,
                SUITE_A_TEST_A);
        suiteOneTestOneTestMethodListenerPassEventLogs = getTestMethodListenerPassEventLogsForTest(SUITE_A,
                SUITE_A_TEST_A);

        suiteTwoTestOneTestMethodListenerStartEventLogs = getTestMethodListenerStartEventLogsForTest(SUITE_B,
                SUITE_B_TEST_A);
        suiteTwoTestTwoTestMethodListenerStartEventLogs = getTestMethodListenerStartEventLogsForTest(SUITE_B,
                SUITE_B_TEST_B);
        suiteTwoTestOneTestMethodListenerPassEventLogs = getTestMethodListenerPassEventLogsForTest(SUITE_B,
                SUITE_B_TEST_A);
        suiteTwoTestTwoTestMethodListenerPassEventLogs = getTestMethodListenerPassEventLogsForTest(SUITE_B,
                SUITE_B_TEST_B);

        suiteThreeTestOneTestMethodListenerStartEventLogs = getTestMethodListenerStartEventLogsForTest(SUITE_C,
                SUITE_C_TEST_A);
        suiteThreeTestTwoTestMethodListenerStartEventLogs = getTestMethodListenerStartEventLogsForTest(SUITE_C,
                SUITE_C_TEST_B);
        suiteThreeTestThreeTestMethodListenerStartEventLogs = getTestMethodListenerStartEventLogsForTest(SUITE_C,
                SUITE_C_TEST_C);
        suiteThreeTestOneTestMethodListenerPassEventLogs = getTestMethodListenerPassEventLogsForTest(SUITE_C,
                SUITE_C_TEST_A);
        suiteThreeTestTwoTestMethodListenerPassEventLogs = getTestMethodListenerPassEventLogsForTest(SUITE_C,
                SUITE_C_TEST_B);
        suiteThreeTestThreeTestMethodListenerPassEventLogs = getTestMethodListenerPassEventLogsForTest(SUITE_C,
                SUITE_C_TEST_C);

        suiteOneTestOneTestMethodExecutionEventLogs = getTestMethodExecutionEventLogsForTest(SUITE_A, SUITE_A_TEST_A);
        suiteTwoTestOneTestMethodExecutionEventLogs = getTestMethodExecutionEventLogsForTest(SUITE_B, SUITE_B_TEST_A);
        suiteTwoTestTwoTestMethodExecutionEventLogs = getTestMethodExecutionEventLogsForTest(SUITE_B, SUITE_B_TEST_B);
        suiteThreeTestOneTestMethodExecutionEventLogs = getTestMethodExecutionEventLogsForTest(SUITE_C, SUITE_C_TEST_A);
        suiteThreeTestTwoTestMethodExecutionEventLogs = getTestMethodExecutionEventLogsForTest(SUITE_C, SUITE_C_TEST_B);
        suiteThreeTestThreeTestMethodExecutionEventLogs = getTestMethodExecutionEventLogsForTest(SUITE_C,
                SUITE_C_TEST_C);
    }

    //Verify that all the events in the second suite and third suites run have timestamps later than the suite
    //listener's onFinish event for the first suite run. Verify that all the events in the third suite run have
    //timestamps later than the suite listener's onFinish event for the second suite run.
    @Test
    public void verifySuitesRunSequentially() {
        List<EventLog> suiteListenerStartEventLogs = getSuiteListenerStartEventLogs();

        assertEquals(suiteListenerStartEventLogs.size(),  3, "There should be three suite listener onStart events " +
                "logged");

        String firstSuite = (String)suiteListenerStartEventLogs.get(0).getData(SUITE_NAME);
        String secondSuite = (String)suiteListenerStartEventLogs.get(1).getData(SUITE_NAME);
        String thirdSuite = (String)suiteListenerStartEventLogs.get(2).getData(SUITE_NAME);

        List<String> suitesRun = new ArrayList<>();
        suitesRun.add(firstSuite);
        suitesRun.add(secondSuite);
        suitesRun.add(thirdSuite);

        assertTrue(suitesRun.contains(SUITE_A), "There should be an event log for the suite listener onStart event " +
                "for " + SUITE_A);
        assertTrue(suitesRun.contains(SUITE_B), "There should be an event log for the suite listener onStart event " +
                "for " + SUITE_B);
        assertTrue(suitesRun.contains(SUITE_C), "There should be an event log for the suite listener onStart event " +
                "for " + SUITE_C);

        switch(firstSuite) {
            case SUITE_A:
                for(EventLog eventLog : suiteTwoEventLogs) {
                    assertTrue(eventLog.getTimeOfEvent() > suiteOneListenerFinishTimestamp, "If " + SUITE_A + " is " +
                            "the first test suite that is executed, all events logged for " + SUITE_B + " should" +
                            "have a timestamp later the suite listener's onFinish event log for " + SUITE_A);
                }

                for(EventLog eventLog : suiteThreeEventLogs) {
                    assertTrue(eventLog.getTimeOfEvent() > suiteOneListenerFinishTimestamp, "If " + SUITE_A + " is " +
                            "the first test suite that is executed, all events logged for " + SUITE_C + " should" +
                            "have a timestamp later the suite listener's onFinish event log for " + SUITE_A);
                }
                break;
            case SUITE_B:
                for(EventLog eventLog : suiteOneEventLogs) {
                    assertTrue(eventLog.getTimeOfEvent() > suiteTwoListenerFinishTimestamp, "If " + SUITE_B + " is " +
                            "the first test suite that is executed, all events logged for " + SUITE_A + " should" +
                            "have a timestamp later the suite listener's onFinish event log for " + SUITE_B);
                }

                for(EventLog eventLog : suiteThreeEventLogs) {
                    assertTrue(eventLog.getTimeOfEvent() > suiteTwoListenerFinishTimestamp, "If " + SUITE_B + " is " +
                            "the first test suite that is executed, all events logged for " + SUITE_C + " should" +
                            "have a timestamp later the suite listener's onFinish event log for " + SUITE_B);
                }
                break;
            default:
                for(EventLog eventLog : suiteOneEventLogs) {
                    assertTrue(eventLog.getTimeOfEvent() > suiteThreeListenerFinishTimestamp, "If " + SUITE_C +
                            " is the first test suite that is executed, all events logged for " + SUITE_A + " should" +
                            " have a timestamp later the suite listener's onFinish event log for " + SUITE_C);
                }

                for(EventLog eventLog : suiteTwoEventLogs) {
                    assertTrue(eventLog.getTimeOfEvent() > suiteThreeListenerFinishTimestamp, "If " + SUITE_C +
                            " is the first test suite that is executed, all events logged for " + SUITE_B + " should" +
                            " have a timestamp later the suite listener's onFinish event log for " + SUITE_C);
                }
                break;
        }

        switch(secondSuite) {
            case SUITE_A:
                if(firstSuite.equals(SUITE_B)) {
                    for(EventLog eventLog : suiteThreeEventLogs) {
                        assertTrue(eventLog.getTimeOfEvent() > suiteOneListenerFinishTimestamp, "If " + SUITE_B +
                                " is the first suite run and " + SUITE_A + " is the second suite run, all events " +
                                "logged for " + SUITE_C + " should have a timestamp later than the suite listener's " +
                                "onFinish event for " + SUITE_A);
                    }
                } else if(firstSuite.equals(SUITE_C)) {
                    for(EventLog eventLog : suiteTwoEventLogs) {
                        assertTrue(eventLog.getTimeOfEvent() > suiteOneListenerFinishTimestamp, "If " + SUITE_C +
                                " is the first suite run and " + SUITE_A + " is the second suite run, all events " +
                                "logged for " + SUITE_B + " should have a timestamp later than the suite listener's " +
                                "onFinish event for " + SUITE_A);
                    }
                }
                break;
            case SUITE_B:
                if(firstSuite.equals(SUITE_A)) {
                    for(EventLog eventLog : suiteThreeEventLogs) {
                        assertTrue(eventLog.getTimeOfEvent() > suiteTwoListenerFinishTimestamp, "If " + SUITE_A +
                                " is the first suite run and " + SUITE_B + " is the second suite run, all events " +
                                "logged for " + SUITE_C + " should have a timestamp later than the suite listener's " +
                                "onFinish event for " + SUITE_B);
                    }
                } else if(firstSuite.equals(SUITE_C)) {
                    for(EventLog eventLog : suiteOneEventLogs) {
                        assertTrue(eventLog.getTimeOfEvent() > suiteTwoListenerFinishTimestamp, "If " + SUITE_C +
                                " is the first suite run and " + SUITE_B + " is the second suite run, all events " +
                                "logged for " + SUITE_A + " should have a timestamp later than the suite listener's " +
                                "onFinish event for " + SUITE_B);
                    }
                }
                break;
            default:
                if(firstSuite.equals(SUITE_A)) {
                    for(EventLog eventLog : suiteTwoEventLogs) {
                        assertTrue(eventLog.getTimeOfEvent() > suiteThreeListenerFinishTimestamp, "If " + SUITE_A +
                                " is the first suite run and " + SUITE_C + "  is the second suite run, all events " +
                                "logged for " + SUITE_B + " should have a timestamp later than the suite listener's " +
                                "onFinish event for " + SUITE_C);
                    }
                } else if(firstSuite.equals(SUITE_B)) {
                    for(EventLog eventLog : suiteOneEventLogs) {
                        assertTrue(eventLog.getTimeOfEvent() > suiteThreeListenerFinishTimestamp, "If " + SUITE_B +
                                " is the first suite run and " + SUITE_C + " is the second suite run, all events " +
                                "logged for " + SUITE_A + " should have a timestamp later than the suite listener's " +
                                "onFinish event for " + SUITE_C);
                    }
                }
                break;
        }
    }

    //For all suites, verify that the suite listener and test listener events have timestamps in the following order:
    //suite start, then for each test in the suite, test start and test finish, followed by suite finish. For all
    //suites, verify that all of these events run in the same thread because the parallelization mode is by methods
    //only.
    @Test
    public void verifySuiteAndTestLevelEventsRunInSequentialOrderInSameThreadForAllSuites() {
        assertTrue(suiteOneListenerStartTimestamp < suiteOneTestOneListenerStartTimestamp, "The timestamp for the " +
                "suite listener's onStart method for " + SUITE_A + " should be before the timestamp for the test " +
                "listener's onStart method for " + SUITE_A_TEST_A);
        assertTrue(suiteOneListenerFinishTimestamp > suiteOneTestOneListenerFinishTimestamp, "The timestamp for the " +
                "onFinish method for the suite listener's onFinish method for " + SUITE_A + " should be after the " +
                "timestamp for the test listener's onFinish method for " + SUITE_A_TEST_A);

        assertTrue(suiteTwoListenerStartTimestamp < suiteTwoTestOneListenerStartTimestamp, "The timestamp for the " +
                "suite listener's onStart method for " + SUITE_B + " should be before the timestamp for the test " +
                "listener's onStart method for " + SUITE_B_TEST_A);
        assertTrue(suiteTwoListenerStartTimestamp < suiteTwoTestTwoListenerStartTimestamp, "The timestamp for the " +
                "suite listener's onStart method for " + SUITE_B + " should be before the timestamp for the test " +
                "listener's onStart method for " + SUITE_B_TEST_B);
        assertTrue(suiteTwoListenerFinishTimestamp > suiteTwoTestOneListenerFinishTimestamp, "The timestamp for the " +
                "onFinish method for the suite listener's onFinish method for " + SUITE_B + " should be after the " +
                "timestamp for the test listener's onFinish method for " + SUITE_B_TEST_A);
        assertTrue(suiteTwoListenerFinishTimestamp > suiteTwoTestTwoListenerFinishTimestamp, "The timestamp for the " +
                "onFinish method for the suite listener's onFinish method for " + SUITE_B + " should be after the " +
                "timestamp for the test listener's onFinish method for " + SUITE_B_TEST_B);
        
        assertTrue(suiteThreeListenerStartTimestamp < suiteThreeTestOneListenerStartTimestamp, "The timestamp for " +
                "the suite listener's onStart method for " + SUITE_C + " should be before the timestamp for the test " +
                "listener's onStart method for " + SUITE_C_TEST_A);
        assertTrue(suiteThreeListenerStartTimestamp < suiteThreeTestTwoListenerStartTimestamp, "The timestamp for " +
                "the suite listener's onStart method for " + SUITE_C + " should be before the timestamp for the test " +
                "listener's onStart method for " + SUITE_C_TEST_B);
        assertTrue(suiteThreeListenerStartTimestamp < suiteThreeTestThreeListenerStartTimestamp, "The timestamp for " +
                "the suite listener's onStart method for " + SUITE_C + " should be before the timestamp for the test " +
                "listener's onStart method for " + SUITE_C_TEST_C);
        assertTrue(suiteThreeListenerFinishTimestamp > suiteThreeTestOneListenerFinishTimestamp, "The timestamp for " +
                "the onFinish method for the suite listener's onFinish method for " + SUITE_C + " should be after " +
                "the timestamp for the test listener's onFinish method for " + SUITE_C_TEST_A);
        assertTrue(suiteThreeListenerFinishTimestamp > suiteThreeTestTwoListenerFinishTimestamp, "The timestamp for " +
                "the onFinish method for the suite listener's onFinish method for " + SUITE_C + " should be after " +
                "the timestamp for the test listener's onFinish method for " + SUITE_C_TEST_B);
        assertTrue(suiteThreeListenerFinishTimestamp > suiteThreeTestThreeListenerFinishTimestamp, "The timestamp " +
                "for the onFinish method for the suite listener's onFinish method for " + SUITE_C + " should be " +
                "after the timestamp for the test listener's onFinish method for " + SUITE_C_TEST_C);
        

        List<EventLog> testListenerStartEventLogs = getTestListenerStartEventLogsForSuite(SUITE_B);

        assertEquals(testListenerStartEventLogs.size(),  2, "There should be two test listener onStart events " +
                "logged for " + SUITE_B);

        String firstTest = (String)testListenerStartEventLogs.get(0).getData(TEST_NAME);
        String secondTest = (String)testListenerStartEventLogs.get(1).getData(TEST_NAME);

        List<String> testsRun = new ArrayList<>();
        testsRun.add(firstTest);
        testsRun.add(secondTest);

        assertTrue(testsRun.contains(SUITE_B_TEST_A), "There should be an event log for the test listener onStart " +
                "event for " + SUITE_B_TEST_A);
        assertTrue(testsRun.contains(SUITE_B_TEST_B), "There should be an event log for the test listener onStart " +
                "event for " + SUITE_B_TEST_B);

        if(firstTest.equals(SUITE_B_TEST_A)) {
            for(EventLog eventLog : suiteTwoTestTwoEventLogs) {
                assertTrue(eventLog.getTimeOfEvent() > suiteTwoTestOneListenerFinishTimestamp, "If " + SUITE_B_TEST_A +
                        " is the first test run in " + SUITE_B + ", then all the events logged for " + SUITE_B_TEST_B +
                        " should have a timestamp after the test listener's onFinish event for " + SUITE_B_TEST_A);
            }
        } else {
            for(EventLog eventLog : suiteTwoTestOneEventLogs) {
                assertTrue(eventLog.getTimeOfEvent() > suiteTwoTestTwoListenerFinishTimestamp, "If " + SUITE_B_TEST_B +
                        " is the first test run in " + SUITE_C + ", then all the events logged for " + SUITE_B_TEST_A +
                        " should have a timestamp after the test listener's onFinish event for " + SUITE_B_TEST_A);
            }
        }

        testListenerStartEventLogs = getTestListenerStartEventLogsForSuite(SUITE_C);

        assertEquals(testListenerStartEventLogs.size(),  3, "There should be two test listener onStart events logged " +
                "for " + SUITE_C);

        firstTest = (String)testListenerStartEventLogs.get(0).getData(TEST_NAME);
        secondTest = (String)testListenerStartEventLogs.get(1).getData(TEST_NAME);
        String thirdTest =  (String)testListenerStartEventLogs.get(2).getData(TEST_NAME);

        testsRun = new ArrayList<>();
        testsRun.add(firstTest);
        testsRun.add(secondTest);
        testsRun.add(thirdTest);

        assertTrue(testsRun.contains(SUITE_C_TEST_A), "There should be an event log for the test listener onStart " +
                "event for " + SUITE_C_TEST_A);
        assertTrue(testsRun.contains(SUITE_C_TEST_B), "There should be an event log for the test listener onStart " +
                "event for " + SUITE_C_TEST_B);
        assertTrue(testsRun.contains(SUITE_C_TEST_C), "There should be an event log for the test listener onStart " +
                "event for " + SUITE_C_TEST_C);

        switch (firstTest) {
            case SUITE_C_TEST_A:
                for (EventLog eventLog : suiteThreeTestTwoEventLogs) {
                    assertTrue(eventLog.getTimeOfEvent() > suiteThreeTestOneListenerFinishTimestamp, "If " +
                            SUITE_C_TEST_A + " is the first test run in " + SUITE_B + " , then all the events logged " +
                            "for " + SUITE_C_TEST_B + "  should have a timestamp after the test listener's onFinish " +
                            "event for " + SUITE_C_TEST_A);
                }

                for (EventLog eventLog : suiteThreeTestThreeEventLogs) {
                    assertTrue(eventLog.getTimeOfEvent() > suiteThreeTestOneListenerFinishTimestamp, "If " +
                            SUITE_C_TEST_A + " is the first test run in " + SUITE_B + ", then all the events logged " +
                            "for " + SUITE_C_TEST_C + " should have a timestamp after the test listener's onFinish " +
                            "event for " + SUITE_C_TEST_A);
                }
                break;
            case SUITE_C_TEST_B:
                for (EventLog eventLog : suiteThreeTestOneEventLogs) {
                    assertTrue(eventLog.getTimeOfEvent() > suiteThreeTestTwoListenerFinishTimestamp, "If " +
                            SUITE_C_TEST_B + " is the first test run in " + SUITE_B + ", then all the events logged " +
                            "for " + SUITE_C_TEST_A + " should have a timestamp after the test listener's onFinish " +
                            "event for " + SUITE_C_TEST_B);
                }

                for (EventLog eventLog : suiteThreeTestThreeEventLogs) {
                    assertTrue(eventLog.getTimeOfEvent() > suiteThreeTestTwoListenerFinishTimestamp, "If " +
                            SUITE_C_TEST_A + " is the first test run in " + SUITE_B + ", then all the events logged " +
                            "for " + SUITE_C_TEST_C + " should have a timestamp after the test listener's onFinish " +
                            "event for " + SUITE_C_TEST_A);
                }
                break;
            default:
                for (EventLog eventLog : suiteThreeTestOneEventLogs) {
                    assertTrue(eventLog.getTimeOfEvent() > suiteThreeTestThreeListenerFinishTimestamp, "If " +
                            SUITE_C_TEST_C + " is the first test run in " + SUITE_B + ", then all the events logged " +
                            "for " + SUITE_C_TEST_A + " should have a timestamp after the test listener's onFinish " +
                            "event for " + SUITE_C_TEST_C);
                }

                for (EventLog eventLog : suiteThreeTestTwoEventLogs) {
                    assertTrue(eventLog.getTimeOfEvent() > suiteThreeTestThreeListenerFinishTimestamp, "If " +
                            SUITE_C_TEST_C + " is the first test run in " + SUITE_B + ", then all the events logged " +
                            "for " + SUITE_C_TEST_B + " should have a timestamp after the test listener's onFinish " +
                            "event for " + SUITE_C_TEST_C);
                }
                break;
        }

        switch(secondTest) {
            case SUITE_C_TEST_A:
                if(firstTest.equals(SUITE_C_TEST_B)) {
                    for(EventLog eventLog : suiteThreeTestThreeEventLogs) {
                        assertTrue(eventLog.getTimeOfEvent() > suiteThreeTestOneListenerFinishTimestamp, "If " +
                                SUITE_C_TEST_B + " is the first test run in " + SUITE_C + " and " + SUITE_C_TEST_A +
                                " is the second test run, all events logged for " + SUITE_C_TEST_C + " should have a " +
                                "timestamp after the test listener's onFinish event for " + SUITE_C_TEST_A);
                    }
                } else if(firstTest.equals(SUITE_C_TEST_C)) {
                    for(EventLog eventLog : suiteThreeTestTwoEventLogs) {
                        assertTrue(eventLog.getTimeOfEvent() > suiteThreeTestOneListenerFinishTimestamp, "If " +
                                SUITE_C_TEST_C + " is the first test run in " + SUITE_C + " and " + SUITE_C_TEST_A +
                                " is the second test run, all events logged for " + SUITE_C_TEST_B + " should have a " +
                                "timestamp after the test listener's onFinish event for " + SUITE_C_TEST_A);
                    }
                }
                break;
            case SUITE_C_TEST_B:
                if(firstTest.equals(SUITE_C_TEST_A)) {
                    for(EventLog eventLog : suiteThreeTestThreeEventLogs) {
                        assertTrue(eventLog.getTimeOfEvent() > suiteThreeTestTwoListenerFinishTimestamp, "If " +
                                SUITE_C_TEST_A + " is the first test run in " + SUITE_C + " and " + SUITE_C_TEST_B +
                                " is the second test run, all events logged for " + SUITE_C_TEST_C + " should have a " +
                                "timestamp after the test listener's onFinish event for " + SUITE_C_TEST_B);
                    }
                } else if(firstTest.equals(SUITE_C_TEST_C)) {
                    for(EventLog eventLog : suiteThreeTestOneEventLogs) {
                        assertTrue(eventLog.getTimeOfEvent() > suiteThreeTestTwoListenerFinishTimestamp, "If " +
                                SUITE_C_TEST_C + " is the first test run in " + SUITE_C + " and " + SUITE_C_TEST_B +
                                " is the second test run, all events logged for " + SUITE_C_TEST_A + " should have a " +
                                "timestamp after the test listener's onFinish event for " + SUITE_C_TEST_B);
                    }
                }
                break;
            default:
                if(firstTest.equals(SUITE_C_TEST_A)) {
                    for(EventLog eventLog : suiteThreeTestTwoEventLogs) {
                        assertTrue(eventLog.getTimeOfEvent() > suiteThreeTestThreeListenerFinishTimestamp, "If " +
                                SUITE_C_TEST_A + " is the first test run in " + SUITE_C + " and " + SUITE_C_TEST_C +
                                " is the second test run, all events logged for " + SUITE_C_TEST_B + " should have a " +
                                "timestamp after the test listener's onFinish event for " + SUITE_C_TEST_C);
                    }
                } else if(firstTest.equals(SUITE_C_TEST_B)) {
                    for(EventLog eventLog : suiteThreeTestOneEventLogs) {
                        assertTrue(eventLog.getTimeOfEvent() > suiteThreeTestThreeListenerFinishTimestamp, "If " +
                                SUITE_C_TEST_B + " is the first test run in " + SUITE_C + " and " + SUITE_C_TEST_C +
                                " is the second test run, all events logged for " + SUITE_C_TEST_A + " should have a " +
                                "timestamp after the test listener's onFinish event for " + SUITE_C_TEST_C);
                    }
                }
                break;
        }

        assertEquals(suiteOneListenerStartThreadId, suiteOneListenerFinishThreadId, "The thread ID for the suite " + 
                "listener's onStart method should be the same as the thread ID for the suite listener's onFinish " + 
                "method for " + SUITE_A);
        assertEquals(suiteOneTestOneListenerStartThreadId, suiteOneTestOneListenerFinishThreadId, "The thread ID for " +
                "the test listener's onStart method should be the same as the thread ID for the test listener's " +
                "onFinish method for " + SUITE_A_TEST_A);


        assertEquals(suiteTwoListenerStartThreadId, suiteTwoListenerFinishThreadId, "The thread ID for the suite " +
                "listener's onStart method should be the same as the thread ID for the suite listener's onFinish " +
                "method for " + SUITE_B);
        assertEquals(suiteTwoTestOneListenerStartThreadId, suiteTwoTestOneListenerFinishThreadId, "The thread ID for " +
                "the test listener's onStart method should be the same as the thread ID for the test listener's " +
                "onFinish method for " + SUITE_B_TEST_A);
        assertEquals(suiteTwoTestTwoListenerStartThreadId, suiteTwoTestTwoListenerFinishThreadId, "The thread ID for " +
                "the test listener's onStart method should be the same as the thread ID for the test listener's " +
                "onFinish method for " + SUITE_B_TEST_B);

        assertEquals(suiteThreeListenerStartThreadId, suiteThreeListenerFinishThreadId, "The thread ID for the suite " +
                "listener's onStart method should be the same as the thread ID for the suite listener's onFinish " +
                "method for " + SUITE_C);
        assertEquals(suiteThreeTestOneListenerStartThreadId, suiteThreeTestOneListenerFinishThreadId, "The thread ID " +
                "for the test listener's onStart method should be the same as the thread ID for the test listener's " +
                "onFinish method for " + SUITE_C_TEST_A);
        assertEquals(suiteThreeTestTwoListenerStartThreadId, suiteThreeTestTwoListenerFinishThreadId, "The thread ID " +
                "for the test listener's onStart method should be the same as the thread ID for the test listener's " +
                "onFinish method for " + SUITE_C_TEST_B);
        assertEquals(suiteThreeTestThreeListenerStartThreadId, suiteThreeTestThreeListenerFinishThreadId, "The " +
                "thread ID for the test listener's onStart method should be the same as the thread ID for the test " +
                "listener's onFinish method for " + SUITE_C_TEST_C);

        assertEquals(suiteOneListenerStartThreadId, suiteTwoListenerStartThreadId, "The thread IDs for the " + 
                "suite level events for all the test suites should be the same");
        assertEquals(suiteOneListenerStartThreadId, suiteThreeListenerStartThreadId, "The thread IDs for the " +
                "suite level events for all the test suites should be the same");

        assertEquals(suiteOneListenerStartThreadId, suiteOneTestOneListenerStartThreadId, "The thread IDs for the " +
                "suite level and the test level events for all the test suites should be the same");
        assertEquals(suiteOneListenerStartThreadId, suiteTwoTestOneListenerStartThreadId, "The thread IDs for the " +
                "suite level and the test level events for all the test suites should be the same");
        assertEquals(suiteOneListenerStartThreadId, suiteTwoTestTwoListenerStartThreadId, "The thread IDs for the " +
                "suite level and the test level events for all the test suites should be the same");
        assertEquals(suiteOneListenerStartThreadId, suiteThreeTestOneListenerStartThreadId, "The thread IDs for the " +
                "suite level and the test level events for all the test suites should be the same");
        assertEquals(suiteOneListenerStartThreadId, suiteThreeTestTwoListenerStartThreadId, "The thread IDs for the " +
                "suite level and the test level events for all the test suites should be the same");
        assertEquals(suiteOneListenerStartThreadId, suiteThreeTestThreeListenerStartThreadId, "The thread IDs for " +
                "the suite level and the test level events for all the test suites should be the same");
    }

    //Verify that there is only a single test class instance associated with each of the test methods from the sample
    //classes for every test in all the suites. Verify that instances are unique and separate if the same test class
    //is included in different tests in different suites or in different tests within the same suite
    @Test
    public void verifyOnlyOneInstanceOfTestClassForAllTestMethodsForAllSuites() {

        assertEquals(suiteOneTestOneFiveMethodsClassEventLogMap.keySet().size(), 1, "There should be only one test " +
                "class instance associated with " + TestClassAWithNoDepsSample.class.getCanonicalName() +
                " for " + SUITE_A_TEST_A);



        assertEquals(suiteOneTestOneSixMethodsClassEventLogMap.keySet().size(), 1, "There should be only one test " +
                "class instance associated with " + TestClassCWithNoDepsSample.class.getCanonicalName() +
                " for " + SUITE_A_TEST_A);


        assertEquals(suiteTwoTestOneFiveMethodsClassEventLogMap.keySet().size(), 1, "There should be only one test " +
                "class instance associated with " + TestClassEWithNoDepsSample.class.getCanonicalName() +
                " for " + SUITE_B_TEST_A);

        assertEquals(suiteTwoTestTwoThreeMethodsClassEventLogMap.keySet().size(), 1, "There should be only one test " +
                "class instance associated with " + TestClassDWithNoDepsSample.class.getCanonicalName() +
                " for " + SUITE_B_TEST_B);
        assertEquals(suiteTwoTestTwoFourMethodsClassEventLogMap.keySet().size(), 1, "There should be only one test " +
                "class instance associated with " + TestClassBWithNoDepsSample.class.getCanonicalName() +
                " for " + SUITE_B_TEST_B);
        assertEquals(suiteTwoTestTwoSixMethodsClassEventLogMap.keySet().size(), 1, "There should be only one test " +
                "class instance associated with " + TestClassFWithNoDepsSample.class.getCanonicalName() +
                " for " + SUITE_B_TEST_B);

        assertEquals(suiteThreeTestOneThreeMethodsClassEventLogMap.keySet().size(), 1, "There should be only one " +
                "test class instance associated with " + TestClassGWithNoDepsSample.class.getCanonicalName() +
                " for " + SUITE_C_TEST_A);
        assertEquals(suiteThreeTestOneFourMethodsClassEventLogMap.keySet().size(), 1, "There should be only one " +
                "test class instance associated with " + TestClassHWithNoDepsSample.class.getCanonicalName() +
                " for " + SUITE_C_TEST_A);
        assertEquals(suiteThreeTestOneFiveMethodsClassEventLogMap.keySet().size(), 1, "There should be only one " +
                "test class instance associated with " + TestClassIWithNoDepsSample.class.getCanonicalName() +
                " for " + SUITE_C_TEST_A);

        assertEquals(suiteThreeTestTwoFourMethodsClassEventLogMap.keySet().size(), 1, "There should be only one " +
                "test class instance associated with " + TestClassJWithNoDepsSample.class.getCanonicalName() +
                " for " + SUITE_C_TEST_B);
        assertEquals(suiteThreeTestTwoFiveMethodsClassEventLogMap.keySet().size(), 1, "There should be only one " +
                "test class instance associated with " + TestClassKWithNoDepsSample.class.getCanonicalName() +
                " for " + SUITE_C_TEST_B);

        assertEquals(suiteThreeTestThreeThreeMethodsClassEventLogMap.keySet().size(), 1, "There should be only one " +
                "test class instance associated with " + TestClassLWithNoDepsSample.class.getCanonicalName() +
                " for " + SUITE_C_TEST_C);
        assertEquals(suiteThreeTestThreeFourMethodsClassEventLogMap.keySet().size(), 1, "There should be only one " +
                "test class instance associated with " + TestClassMWithNoDepsSample.class.getCanonicalName() +
                " for " + SUITE_C_TEST_C);
        assertEquals(suiteThreeTestThreeFiveMethodsClassEventLogMap.keySet().size(), 1, "There should be only one " +
                "test class instance associated with " + TestClassNWithNoDepsSample.class.getCanonicalName() +
                " for " + SUITE_C_TEST_C);
        assertEquals(suiteThreeTestThreeSixMethodsClassEventLogMap.keySet().size(), 1, "There should be only one " +
                "test class instance associated with " + TestClassOWithNoDepsSample.class.getCanonicalName() +
                " for " + SUITE_C_TEST_C);
    }

    //Verify that the test method listener's onTestStart method runs after the test listener's onStart method for
    //all the test methods in all tests and suites.
    @Test
    public void verifyTestMethodLevelListenerOnStartOccursAfterTestListenerStart() {

        for(EventLog eventLog : suiteOneTestOneTestMethodListenerStartEventLogs) {
            assertTrue(eventLog.getTimeOfEvent() > suiteOneTestOneListenerStartTimestamp, "The timestamps for all " +
                    "the test method listener's onTestStart methods for test methods associated with " +
                    SUITE_A_TEST_A + " should be later than the test listener's onStart method for " + SUITE_A_TEST_A);
        }


        for(EventLog eventLog : suiteTwoTestOneTestMethodListenerStartEventLogs) {
            assertTrue(eventLog.getTimeOfEvent() > suiteTwoTestOneListenerStartTimestamp, "The timestamps for all " +
                    "the test method listener's onTestStart methods for test methods associated with " +
                    SUITE_B_TEST_A + " should be later than the test listener's onStart method for " + SUITE_B_TEST_A);
        }

        for(EventLog eventLog : suiteTwoTestTwoTestMethodListenerStartEventLogs) {
            assertTrue(eventLog.getTimeOfEvent() > suiteTwoTestTwoListenerStartTimestamp, "The timestamps for all " +
                    "the test method listener's onTestStart methods for test methods associated with " +
                    SUITE_B_TEST_B + " should be later than the test listener's onStart method for " + SUITE_B_TEST_B);
        }


        for(EventLog eventLog : suiteThreeTestOneTestMethodListenerStartEventLogs) {
            assertTrue(eventLog.getTimeOfEvent() > suiteThreeTestOneListenerStartTimestamp, "The timestamps for all " +
                    "the test method listener's onTestStart methods for test methods associated with " +
                    SUITE_C_TEST_A + " should be later than the test listener's onStart method for " + SUITE_C_TEST_A);
        }

        for(EventLog eventLog : suiteThreeTestTwoTestMethodListenerStartEventLogs) {
            assertTrue(eventLog.getTimeOfEvent() > suiteThreeTestTwoListenerStartTimestamp, "The timestamps for all " +
                    "the test method listener's onTestStart methods for test methods associated with " +
                    SUITE_C_TEST_B + " should be later than the test listener's onStart method for " + SUITE_C_TEST_B);
        }

        for(EventLog eventLog : suiteThreeTestThreeTestMethodListenerStartEventLogs) {
            assertTrue(eventLog.getTimeOfEvent() > suiteThreeTestThreeListenerStartTimestamp, "The timestamps for " +
                    "all the test method listener's onTestStart methods for test methods associated with " +
                    SUITE_C_TEST_C + " should be later than the test listener's onStart method for " + SUITE_C_TEST_C);
        }

    }

    //Verify that the test method listener's onTestSuccess method runs before the test listener's onFinish method
    //for all the test methods in all tests and suites.
    @Test
    public void verifyTestMethodLevelListenerOnSuccessRunsBeforeTestListenerOnFinish() {
        for(EventLog eventLog : suiteOneTestOneTestMethodListenerPassEventLogs) {
            assertTrue(eventLog.getTimeOfEvent() < suiteOneTestOneListenerFinishTimestamp, "The timestamps for all " +
                    "the test method listener's onTestSuccess methods for test methods associated with " +
                    SUITE_A_TEST_A + " should be earlier than the test listener's onFinish method for " +
                    SUITE_A_TEST_A);
        }


        for(EventLog eventLog : suiteTwoTestOneTestMethodListenerPassEventLogs) {
            assertTrue(eventLog.getTimeOfEvent() < suiteTwoTestOneListenerFinishTimestamp, "The timestamps for all " +
                    "the test method listener's onTestSuccess methods for test methods associated with " +
                    SUITE_B_TEST_A + " should be earlier than the test listener's onFinish method for " +
                    SUITE_B_TEST_A);
        }

        for(EventLog eventLog : suiteTwoTestTwoTestMethodListenerPassEventLogs) {
            assertTrue(eventLog.getTimeOfEvent() < suiteTwoTestTwoListenerFinishTimestamp, "The timestamps for all " +
                    "the test method listener's onTestSuccess methods for test methods associated with " +
                    SUITE_B_TEST_B + " should be earlier than the test listener's onFinish method for " +
                    SUITE_B_TEST_B);
        }


        for(EventLog eventLog : suiteThreeTestOneTestMethodListenerPassEventLogs) {
            assertTrue(eventLog.getTimeOfEvent() < suiteThreeTestOneListenerFinishTimestamp, "The timestamps for all " +
                    "the test method listener's onTestSuccess methods for test methods associated with " +
                    SUITE_C_TEST_A + " should be earlier than the test listener's onFinish method for " +
                    SUITE_C_TEST_A);
        }

        for(EventLog eventLog : suiteThreeTestTwoTestMethodListenerPassEventLogs) {
            assertTrue(eventLog.getTimeOfEvent() < suiteThreeTestTwoListenerFinishTimestamp, "The timestamps for all " +
                    "the test method listener's onTestSuccess methods for test methods associated with " +
                    SUITE_C_TEST_B + " should be earlier than the test listener's onFinish method for " +
                    SUITE_C_TEST_B);
        }

        for(EventLog eventLog : suiteThreeTestThreeTestMethodListenerPassEventLogs) {
            assertTrue(eventLog.getTimeOfEvent() < suiteThreeTestThreeListenerFinishTimestamp,"The timestamps for all " +
                    "the test method listener's onTestSuccess methods for test methods associated with " +
                    SUITE_C_TEST_C + " should be earlier than the test listener's onFinish method for " +
                    SUITE_C_TEST_C);
        }
    }

    //Verify that the test method listener's onTestStart method runs before the test method begins execution
    //Verify that the test method listener's onTestSuccess method runs after the test method executes and passes
    @Test
    public void verifyTestExecutionTimestampIsAfterTestListenerOnStartAndBeforeTestListenerOnTestSuccess() {
        for(EventLog eventLog : suiteOneTestOneTestMethodExecutionEventLogs) {
            assertTrue(eventLog.getTimeOfEvent() > suiteOneTestOneListenerStartTimestamp, "All test method level " +
                    "events should have timestamps between the timestamps for the test listener's onStart event and " +
                    "the test listener's onFinish event");
            assertTrue(eventLog.getTimeOfEvent() < suiteOneTestOneListenerFinishTimestamp, "All test method level " +
                    "events should have timestamps between the timestamps for the test listener's onStart event and " +
                    "the test listener's onFinish event");
        }


        for(EventLog eventLog : suiteTwoTestOneTestMethodExecutionEventLogs) {
            assertTrue(eventLog.getTimeOfEvent() > suiteTwoTestOneListenerStartTimestamp, "All test method level " +
                    "events should have timestamps between the timestamps for the test listener's onStart event and " +
                    "the test listener's onFinish event");
            assertTrue(eventLog.getTimeOfEvent() < suiteTwoTestOneListenerFinishTimestamp, "All test method level " +
                    "events should have timestamps between the timestamps for the test listener's onStart event and " +
                    "the test listener's onFinish event");
        }

        for(EventLog eventLog : suiteTwoTestTwoTestMethodExecutionEventLogs) {
            assertTrue(eventLog.getTimeOfEvent() > suiteTwoTestTwoListenerStartTimestamp, "All test method level " +
                    "events should have timestamps between the timestamps for the test listener's onStart event and " +
                    "the test listener's onFinish event");
            assertTrue(eventLog.getTimeOfEvent() < suiteTwoTestTwoListenerFinishTimestamp, "All test method level " +
                    "events should have timestamps between the timestamps for the test listener's onStart event and " +
                    "the test listener's onFinish event");
        }

        for(EventLog eventLog : suiteThreeTestOneTestMethodExecutionEventLogs) {
            assertTrue(eventLog.getTimeOfEvent() > suiteThreeTestOneListenerStartTimestamp, "All test method level " +
                    "events should have timestamps between the timestamps for the test listener's onStart event and " +
                    "the test listener's onFinish event");
            assertTrue(eventLog.getTimeOfEvent() < suiteThreeTestOneListenerFinishTimestamp, "All test method level " +
                    "events should have timestamps between the timestamps for the test listener's onStart event and " +
                    "the test listener's onFinish event");
        }

        for(EventLog eventLog : suiteThreeTestTwoTestMethodExecutionEventLogs) {
            assertTrue(eventLog.getTimeOfEvent() > suiteThreeTestTwoListenerStartTimestamp, "All test method level " +
                    "events should have timestamps between the timestamps for the test listener's onStart event and " +
                    "the test listener's onFinish event");
            assertTrue(eventLog.getTimeOfEvent() < suiteThreeTestTwoListenerFinishTimestamp, "All test method level " +
                    "events should have timestamps between the timestamps for the test listener's onStart event and " +
                    "the test listener's onFinish event");
        }

        for(EventLog eventLog : suiteThreeTestThreeTestMethodExecutionEventLogs) {
            assertTrue(eventLog.getTimeOfEvent() > suiteThreeTestThreeListenerStartTimestamp, "All test method level " +
                    "events should have timestamps between the timestamps for the test listener's onStart event and " +
                    "the test listener's onFinish event");
            assertTrue(eventLog.getTimeOfEvent() < suiteThreeTestThreeListenerFinishTimestamp, "All test method " +
                    "level events should have timestamps between the timestamps for the test listener's onStart " +
                    "event and the test listener's onFinish event");
        }
    }

    //Verifies that the method level events all run in different threads from the test and suite level events.
    //Verifies that the test method listener and execution events for a given test method all run in the same thread.
    @Test
    public void verifyThatMethodLevelEventsRunInDifferentThreadsFromSuiteAndTestLevelEvents() {
        for(EventLog eventLog : suiteOneTestOneTestMethodExecutionEventLogs) {
            assertTrue(eventLog.getThreadId() > suiteOneTestOneListenerStartThreadId, "The threads in which the test " +
                    "method level events for " + SUITE_A_TEST_A + " run should be spawned after the thread in which " +
                    "the suite and test level events listener events run");
        }

        for(EventLog eventLog : suiteTwoTestOneTestMethodExecutionEventLogs) {
            assertTrue(eventLog.getThreadId() > suiteTwoTestOneListenerStartThreadId, "The threads in which the test " +
                    "method level events for " + SUITE_B_TEST_A + " run should be spawned after the thread in which " +
                    "the suite and test level events listener events run");
        }

        for(EventLog eventLog : suiteTwoTestTwoTestMethodExecutionEventLogs) {
            assertTrue(eventLog.getThreadId() > suiteTwoTestTwoListenerStartThreadId, "The threads in which the test " +
                    "method level events for " + SUITE_B_TEST_B + " run should be spawned after the thread in which " +
                    "the suite and test level events listener events run");
        }

        for(EventLog eventLog : suiteThreeTestOneTestMethodExecutionEventLogs) {
            assertTrue(eventLog.getThreadId() > suiteThreeTestOneListenerStartThreadId, "The threads in which the " +
                    "test method level events for " + SUITE_C_TEST_A + " run should be spawned after the thread in " +
                    "which the suite and test level events listener events run");
        }

        for(EventLog eventLog : suiteThreeTestTwoTestMethodExecutionEventLogs) {
            assertTrue(eventLog.getThreadId() > suiteThreeTestTwoListenerStartThreadId, "The threads in which the " +
                    "test method level events for " + SUITE_C_TEST_B + " run should be spawned after the thread in " +
                    "which the suite and test level events listener events run");
        }

        for(EventLog eventLog : suiteThreeTestThreeTestMethodExecutionEventLogs) {
            assertTrue(eventLog.getThreadId() > suiteThreeTestThreeListenerStartThreadId, "The threads in which the " +
                    "test method level events for " + SUITE_C_TEST_C + " run should be spawned after the thread in " +
                    "which the suite and test level events listener events run");
        }

        verifyEventsForTestMethodRunInSameThread(TestClassAWithNoDepsSample.class, SUITE_A, SUITE_A_TEST_A);
        verifyEventsForTestMethodRunInSameThread(TestClassCWithNoDepsSample.class, SUITE_A, SUITE_A_TEST_A);

        verifyEventsForTestMethodRunInSameThread(TestClassEWithNoDepsSample.class, SUITE_B, SUITE_B_TEST_A);

        verifyEventsForTestMethodRunInSameThread(TestClassDWithNoDepsSample.class, SUITE_B, SUITE_B_TEST_B);
        verifyEventsForTestMethodRunInSameThread(TestClassBWithNoDepsSample.class, SUITE_B, SUITE_B_TEST_B);
        verifyEventsForTestMethodRunInSameThread(TestClassFWithNoDepsSample.class, SUITE_B, SUITE_B_TEST_B);

        verifyEventsForTestMethodRunInSameThread(TestClassGWithNoDepsSample.class, SUITE_C, SUITE_C_TEST_A);
        verifyEventsForTestMethodRunInSameThread(TestClassHWithNoDepsSample.class, SUITE_C, SUITE_C_TEST_A);
        verifyEventsForTestMethodRunInSameThread(TestClassIWithNoDepsSample.class, SUITE_C, SUITE_C_TEST_A);

        verifyEventsForTestMethodRunInSameThread(TestClassJWithNoDepsSample.class, SUITE_C, SUITE_C_TEST_B);
        verifyEventsForTestMethodRunInSameThread(TestClassKWithNoDepsSample.class, SUITE_C, SUITE_C_TEST_B);

        verifyEventsForTestMethodRunInSameThread(TestClassLWithNoDepsSample.class, SUITE_C, SUITE_C_TEST_C);
        verifyEventsForTestMethodRunInSameThread(TestClassMWithNoDepsSample.class, SUITE_C, SUITE_C_TEST_C);
        verifyEventsForTestMethodRunInSameThread(TestClassNWithNoDepsSample.class, SUITE_C, SUITE_C_TEST_C);
        verifyEventsForTestMethodRunInSameThread(TestClassOWithNoDepsSample.class, SUITE_C, SUITE_C_TEST_C);
    }

    //Verify that the methods are run in separate threads in true parallel fashion by checking that the start and run
    //times of events that should be run simultaneously start basically at the same time using the timestamps and the
    //known values of the wait time specified for the event. Verify that the thread IDs of parallel events are
    //different.
    @Test
    public void verifyThatTestMethodsRunInParallelThreads() {
        verifyParallelismForTestMethodEvents(SUITE_A, SUITE_A_TEST_A, 3);
        verifyParallelismForTestMethodEvents(SUITE_B, SUITE_B_TEST_A, 6);
        verifyParallelismForTestMethodEvents(SUITE_B, SUITE_B_TEST_B, 20);
        verifyParallelismForTestMethodEvents(SUITE_C, SUITE_C_TEST_A, 10);
        verifyParallelismForTestMethodEvents(SUITE_C, SUITE_C_TEST_B, 5);
        verifyParallelismForTestMethodEvents(SUITE_C, SUITE_C_TEST_C, 12);
    }

    private static void verifyEventsForTestMethodRunInSameThread(Class<?> testClass, String suiteName, String
            testName) {

        for(Method method : testClass.getMethods()) {
            if (method.getDeclaringClass().equals(testClass)) {
                Multimap<Object, EventLog> testMethodEventLogs = getTestMethodEventLogsForMethod(suiteName, testName,
                        testClass.getCanonicalName(), method.getName());

                assertTrue(testMethodEventLogs.keySet().size() > 0, "There should be event logs for the method " +
                        method.getName() + " in an instance of " + testClass.getCanonicalName() + " for test " +
                        testName + " in suite " + suiteName);

                long threadId = -1;

                for (EventLog eventLog : testMethodEventLogs.get(testMethodEventLogs.keySet().toArray()[0])) {
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

    private static void verifyParallelismForTestMethodEvents(String suiteName, String testName, int
            threadCount) {
        List<EventLog> testMethodEventLogs = getTestMethodLevelEventLogsForTest(suiteName, testName);

        List<String> methodsAlreadyExecuted = new ArrayList<>();
        List<Long> activeMethodEventThreadIds = new ArrayList<>();

        for(int i = 1; i < testMethodEventLogs.size(); i = i + threadCount * 3) {
            List<EventLog> eventLogListenerStartSublist;

            if(testMethodEventLogs.size() - i < threadCount * 3) {

                int remainder = testMethodEventLogs.size() % (threadCount * 3);
                int blockSize = remainder / 3;

                eventLogListenerStartSublist = testMethodEventLogs.subList(testMethodEventLogs.size() - remainder,
                        testMethodEventLogs.size() - remainder + blockSize);

            } else {
                eventLogListenerStartSublist = testMethodEventLogs.subList(i - 1, i + threadCount - 1);
            }


            if(!methodsAlreadyExecuted.isEmpty()) {
                verifyTestMethodListenerStartEventLogBlock(eventLogListenerStartSublist, testName, threadCount,
                        methodsAlreadyExecuted, activeMethodEventThreadIds);
            } else {
                verifyTestMethodListenerStartEventLogBlock(eventLogListenerStartSublist, testName, threadCount);

                for(EventLog eventLog : eventLogListenerStartSublist) {
                    activeMethodEventThreadIds.add(eventLog.getThreadId());
                }

            }

            List<String> methodsExecuting = new ArrayList<>();

            for(EventLog eventLog : eventLogListenerStartSublist) {
                methodsExecuting.add(eventLog.getData(CLASS_NAME) + (String) eventLog.getData(METHOD_NAME));
            }

            List<EventLog> eventLogMethodExecuteSublist;

            if(testMethodEventLogs.size() - i < threadCount * 3) {
                int remainder = testMethodEventLogs.size() % (threadCount * 3);
                int blockSize = remainder / 3;

                eventLogMethodExecuteSublist = testMethodEventLogs.subList(testMethodEventLogs.size() - remainder +
                                blockSize, testMethodEventLogs.size() - remainder + 2 * blockSize);
            }
            else {
                eventLogMethodExecuteSublist = testMethodEventLogs.subList(i + threadCount - 1,
                        i + 2 * threadCount - 1);
            }

            verifyTestMethodExecutionEventLogBlock(eventLogMethodExecuteSublist, testName, threadCount,
                    methodsExecuting, activeMethodEventThreadIds);
            verifyTimingOfEvents(eventLogListenerStartSublist, eventLogMethodExecuteSublist, 1050, "The test method " +
                    "execution events for a block of simultaneously executing methods should be within 1050 " +
                    "milliseconds of their test method listener's onTestStart events");

            List<EventLog> eventLogMethodListenerPassSublist;

            if(testMethodEventLogs.size() - i < threadCount * 3) {
                int remainder = testMethodEventLogs.size() % (threadCount * 3);
                int blockSize = remainder / 3;

                eventLogMethodListenerPassSublist = testMethodEventLogs.subList(testMethodEventLogs.size() - remainder +
                        2 * blockSize, testMethodEventLogs.size() - remainder + 3 * blockSize );
            } else {
                eventLogMethodListenerPassSublist = testMethodEventLogs.subList(i + 2 * threadCount - 1,
                        i + 3 * threadCount - 1);
            }

            verifyTestMethodListenerPassEventLogBlock(eventLogMethodListenerPassSublist, testName, threadCount,
                    methodsExecuting, activeMethodEventThreadIds);
            verifyTimingOfEvents(eventLogMethodExecuteSublist, eventLogMethodListenerPassSublist, 5050, "The test " +
                    "method listener's onTestSuccess events for a block of simultaneously executing methods should " +
                    "be within 5050 milliseconds of their execution events");

            methodsAlreadyExecuted.addAll(methodsExecuting);
        }
    }

    private static void verifyTestMethodListenerStartEventLogBlock(List<EventLog> listenerStartEventLogs, String
            testName, int threadCount) {

        verifyEventTypeForMethodLevelEvents(listenerStartEventLogs, LISTENER_TEST_METHOD_START, "The thread count is " +
                threadCount + " for " + testName + " so more more than " + threadCount + " methods should start " +
                "running at the same time if there are more than " + threadCount + " methods remaining to execute.");
        verifyDifferentThreadIdsForEvents(listenerStartEventLogs, "The thread count is " + threadCount + " for " +
                testName + " so the thread IDs for all the test method listener's onTestStart method " + "the " +
                threadCount + "currently executing test methods should be different");
        verifyTimingOfEvents(listenerStartEventLogs, 50,  "The test method listener's onTestStart method " +
                "for a block of simultaneously executing methods should be within 50 milliseconds of each other");
    }

    private static void verifyTestMethodListenerStartEventLogBlock(List<EventLog> listenerStartEventLogs, String
            testName, int threadCount, List<String> methodsAlreadyExecuted, List<Long> activeThreadIds) {

        for(EventLog eventLog : listenerStartEventLogs) {
            assertTrue(activeThreadIds.contains(eventLog.getThreadId()), "The thread count is " + threadCount +
                    " for " + testName + " so the thread IDs should be recycled when a brand new block of methods " +
                    "begins to execute");
            assertFalse(methodsAlreadyExecuted.contains(eventLog.getData(CLASS_NAME) +
                    (String)eventLog.getData(METHOD_NAME)), "After the test method listener's onTestSuccess event " +
                    "is logged for a test method, no subsequent test method level event should belong to that method");
        }

        verifyTestMethodListenerStartEventLogBlock(listenerStartEventLogs, testName, threadCount);
    }

    private static void verifyTestMethodExecutionEventLogBlock(List<EventLog> testMethodExecutionEventLogs, String
            testName, int threadCount, List<String> methodsExecuting, List<Long> activeThreadIds) {
        verifyEventTypeForMethodLevelEvents(testMethodExecutionEventLogs, TEST_METHOD_EXECUTION, "The thread count " +
                "is " + threadCount + " for " + testName + " so no more than " + threadCount + " methods should be " +
                "executing at the same time.");
        verifyDifferentThreadIdsForEvents(testMethodExecutionEventLogs, "The thread count is " + threadCount + " for " +
                testName + " so the thread IDs for the test method execution events for the " + threadCount +
                "currently executing test methods should be different");

        for(EventLog eventLog : testMethodExecutionEventLogs) {
            assertTrue(activeThreadIds.contains(eventLog.getThreadId()), "The thread count is " + threadCount +
                    " for " + testName + " so the thread IDs for the " + threadCount + " methods' execution events " +
                    "should be the same as the thread IDs for their test method listener's onTestStart events");
            assertTrue(methodsExecuting.contains(eventLog.getData(CLASS_NAME) + (String)eventLog.getData(METHOD_NAME)),
                    "The thread count is " + threadCount + " for " + testName + " so " + threadCount + " methods " +
                            "should be running at the same time. The test execution events following the test method " +
                            "listener's onTestStart events for a block of simultaneously running test methods should " +
                            "all belong to the same " + threadCount + " methods");
        }

        verifyTimingOfEvents(testMethodExecutionEventLogs, 50, "The test method execution events for a block of " +
                "simultaneously executing methods should be within 50 milliseconds of each other");
    }

    private static void verifyTestMethodListenerPassEventLogBlock(List<EventLog> testMethodListenerPassEventLogs,
            String testName, int threadCount, List<String> methodsExecuting, List<Long> activeThreadIds) {
        verifyEventTypeForMethodLevelEvents(testMethodListenerPassEventLogs,  LISTENER_TEST_METHOD_PASS, "The thread " +
                "count is " + threadCount + " for " + testName + " so no more than " + threadCount + " test listener " +
                "onTestSuccess methods should be executing at the same time.");
        verifyDifferentThreadIdsForEvents(testMethodListenerPassEventLogs, "The thread count is " + threadCount +
                " for " + testName + " so the thread IDs for the test method listener onTestSuccess events for the " +
                threadCount + "currently executing test methods should be different");


        for(EventLog eventLog : testMethodListenerPassEventLogs) {
            assertTrue(activeThreadIds.contains(eventLog.getThreadId()), "The thread count is " + threadCount +
                    " for " + testName + " so the thread IDs for the " + threadCount + " methods' test listener " +
                    "onTestSuccess events should be the same as the thread IDs for their execution events");
            assertTrue(methodsExecuting.contains(eventLog.getData(CLASS_NAME) + (String)eventLog.getData(METHOD_NAME)),
                    "The thread count is " + threadCount + " for " + testName + " so no more than " + threadCount +
                            " methods should be running at the same time. The test method listener's onTestSuccess " +
                            "events following the execution events for a block of simultaneously running test " +
                            "methods should all belong to the same " + threadCount + " methods");
        }

        verifyTimingOfEvents(testMethodListenerPassEventLogs, 50, "The test method listener's onTestSuccess events " +
                "for a block of simultaneously executing methods should be within 50 milliseconds of each other");
    }

    private static void verifyEventTypeForMethodLevelEvents(List<EventLog> eventLogs, TestNgRunEvent event, String
            failMessage) {
        for(EventLog eventLog : eventLogs) {
            assertTrue(eventLog.getEvent() == event, failMessage);
        }
    }

    private static void verifyDifferentThreadIdsForEvents(List<EventLog> eventLogs, String failMessage) {
        List<Long> threadIds = new ArrayList<>();

        for(EventLog eventLog : eventLogs) {
            if(threadIds.contains(eventLog.getThreadId())) {
                fail(failMessage);
            }

            threadIds.add(eventLog.getThreadId());
        }
    }

    private static void verifyTimingOfEvents(List<EventLog> eventLogs, int timingRange, String failMessage) {
        for(int i = 0; i < eventLogs.size() - 1; i++) {
            for(int j = i + 1; j < eventLogs.size(); j++) {
                assertTrue(Math.abs(eventLogs.get(i).getTimeOfEvent() - eventLogs.get(j).getTimeOfEvent()) <=
                        timingRange, failMessage);
            }
        }
    }

    private static void verifyTimingOfEvents(List<EventLog> firstEventLogBlock, List<EventLog> secondEventLogBlock,
            int timingRange, String failMessage) {
        for(int i = 0; i < firstEventLogBlock.size() - 1; i++) {
            for(int j = 0; j < secondEventLogBlock.size() - 1; j++) {
                assertTrue(Math.abs(firstEventLogBlock.get(i).getTimeOfEvent() -
                        secondEventLogBlock.get(j).getTimeOfEvent()) <= timingRange, failMessage);
            }
        }
    }
}
