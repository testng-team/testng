package test.inject;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestContext;
import org.testng.ITestNGListener;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class Github1649Test extends SimpleBaseTest {
    @Test
    public void testHappyFlowForNativeInjectionOnTestMethods() {
        Map<String, List<String>> mapping = Maps.newHashMap();
        mapping.put("m1", Collections.singletonList(Method.class.getName()));
        mapping.put("m2", Collections.singletonList(ITestContext.class.getName()));
        mapping.put("m3", Collections.singletonList(XmlTest.class.getName()));

        mapping.put("m4", Arrays.asList(ITestContext.class.getName(), XmlTest.class.getName()));
        mapping.put("m5", Arrays.asList(XmlTest.class.getName(), ITestContext.class.getName()));
        mapping.put("m6", Arrays.asList(Method.class.getName(), ITestContext.class.getName()));
        mapping.put("m7", Arrays.asList(ITestContext.class.getName(), Method.class.getName()));
        mapping.put("m8", Arrays.asList(Method.class.getName(), XmlTest.class.getName()));
        mapping.put("m9", Arrays.asList(XmlTest.class.getName(), Method.class.getName()));

        mapping.put("m10", Arrays.asList(Method.class.getName(), XmlTest.class.getName(), ITestContext.class.getName()));
        mapping.put("m11", Arrays.asList(Method.class.getName(), ITestContext.class.getName(), XmlTest.class.getName()));
        mapping.put("m12", Arrays.asList(XmlTest.class.getName(), Method.class.getName(), ITestContext.class.getName()));
        mapping.put("m13", Arrays.asList(XmlTest.class.getName(), ITestContext.class.getName(), Method.class.getName()));
        mapping.put("m14", Arrays.asList(ITestContext.class.getName(), Method.class.getName(), XmlTest.class.getName()));
        mapping.put("m15", Arrays.asList(ITestContext.class.getName(), XmlTest.class.getName(), Method.class.getName()));
        TestNG testng = create(HappyPathNativeInjectionTestSample.class);
        Github1649TestListener listener = new Github1649TestListener();
        testng.addListener((ITestNGListener) listener);
        testng.run();
        assertThat(listener.mapping).containsAllEntriesOf(mapping);
    }

    @Test
    public void testNegativeFlowForNativeInjectionOnTestMethods() {
        Map<String, String> failures = Maps.newHashMap();
        failures.put("m1", "Cannot inject @Test annotated Method [m1] with [interface org.testng.ITestResult].");
        failures.put("m2", "Cannot inject @Test annotated Method [m2] with [int].");
        TestNG testng = create(NegativeNativeInjectionTestSample.class);
        Github1649TestListener listener = new Github1649TestListener();
        testng.addListener((ITestNGListener) listener);
        testng.run();
        assertThat(listener.failures).containsAllEntriesOf(failures);
    }

    public static class Github1649TestListener extends TestListenerAdapter implements IInvokedMethodListener {
        Map<String, List<String>> mapping = Maps.newHashMap();
        Map<String, String> failures = Maps.newHashMap();

        @Override
        public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {

        }

        @Override
        public void onTestFailure(ITestResult testResult) {
            String methodName = testResult.getMethod().getMethodName();
            String raw = testResult.getThrowable().getMessage();
            String actual = raw.split("\n")[1];
            failures.put(methodName, actual);
        }

        @Override
        public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
            String methodName = testResult.getMethod().getMethodName();
            List<String> paramTypes = Lists.newArrayList();
            for (Object parameter : testResult.getParameters()) {
                String value = parameter.getClass().getName();
                if (parameter instanceof ITestContext) {
                    value = ITestContext.class.getName();
                }
                paramTypes.add(value);
            }
            mapping.put(methodName, paramTypes);
        }
    }
}
