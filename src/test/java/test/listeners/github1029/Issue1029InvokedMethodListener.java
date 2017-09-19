package test.listeners.github1029;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.xml.XmlTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Issue1029InvokedMethodListener implements IInvokedMethodListener {
    private List<String> beforeInvocation = Collections.synchronizedList(new ArrayList<String>());
    private List<String> afterInvocation = Collections.synchronizedList(new ArrayList<String>());

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        XmlTest xmlTest = method.getTestMethod().getXmlTest();
        beforeInvocation.add(xmlTest.getName());
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        XmlTest xmlTest = method.getTestMethod().getXmlTest();
        afterInvocation.add(xmlTest.getName());
    }

    public List<String> getAfterInvocation() {
        return afterInvocation;
    }

    public List<String> getBeforeInvocation() {
        return beforeInvocation;
    }
}
