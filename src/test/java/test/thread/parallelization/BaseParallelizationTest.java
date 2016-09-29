package test.thread.parallelization;

import com.google.common.collect.Multimap;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import test.thread.parallelization.TestNgRunStateTracker.EventLog;

import static org.testng.Assert.assertEquals;
import static test.thread.parallelization.TestNgRunStateTracker.getTestMethodEventLogsForMethod;

public class BaseParallelizationTest extends SimpleBaseTest {
    public static void addParams(XmlSuite suite, String suiteName, String testName, String sleepFor) {
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
