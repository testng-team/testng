package test.listeners.github1602;

import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class IssueTest extends SimpleBaseTest {

    @Test(dataProvider = "dp")
    public void testListenerInvocation(Class<?> clazz, XmlSuite.FailurePolicy policy, List<String> expected) {
        TestNG tng = create(clazz);
        ListenerForIssue1602 listener = new ListenerForIssue1602();
        tng.setConfigFailurePolicy(policy);
        tng.setVerbose(2);
        tng.addListener(listener);
        tng.run();
        assertThat(listener.getLogs()).containsExactlyElementsOf(expected);
    }

    @DataProvider(name = "dp")
    public Object[][] getData() {
        List<String> passList = Arrays.asList("BeforeInvocation_beforeMethod_STARTED", "AfterInvocation_beforeMethod_SUCCESS",
                "BeforeInvocation_testMethod_STARTED", "AfterInvocation_testMethod_SUCCESS",
                "BeforeInvocation_afterMethod_STARTED", "AfterInvocation_afterMethod_SUCCESS");
        List<String> baseList = Arrays.asList("BeforeInvocation_beforeMethod_STARTED", "AfterInvocation_beforeMethod_FAILURE",
                "BeforeInvocation_testMethod_SKIP", "AfterInvocation_testMethod_SKIP",
                "BeforeInvocation_afterMethod_STARTED");
        List<String> skipList = Lists.newArrayList(baseList);
        skipList.add("AfterInvocation_afterMethod_SKIP");
        List<String> failList = Lists.newArrayList(baseList);
        failList.add("AfterInvocation_afterMethod_FAILURE");
        return new Object[][]{
                {TestClassWithPassingConfigsSample.class, XmlSuite.FailurePolicy.SKIP, passList},
                {TestClassWithFailingConfigsSample.class, XmlSuite.FailurePolicy.SKIP, skipList},
                {TestClassWithPassingConfigsSample.class, XmlSuite.FailurePolicy.CONTINUE, passList},
                {TestClassWithFailingConfigsSample.class, XmlSuite.FailurePolicy.CONTINUE, failList}
        };
    }
}
