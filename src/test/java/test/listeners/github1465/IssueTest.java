package test.listeners.github1465;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

    @Test(dataProvider = "dp")
    public void reproduceIssue(XmlSuite.FailurePolicy policy, String[] expectedMsgs) {
        TestNG testNG = create(ExampleClassSample.class);
        testNG.setConfigFailurePolicy(policy);
        ExampleClassListener listener = new ExampleClassListener();
        testNG.addListener((ITestNGListener) listener);
        testNG.run();
        String[] expected = new String[]{
                "beforeInvocation:_test_method: test1", "afterInvocation:_test_method: test1",
                "beforeInvocation:_test_method: test2", "afterInvocation:_test_method: test2"
        };
        assertThat(listener.messages).containsExactly(expected);
        assertThat(listener.configMsgs).containsExactly(expectedMsgs);
    }

    @DataProvider(name = "dp")
    public Object[][] getdata() {
        String[] continueFailurePolicyExpected = new String[]{
                "beforeInvocation:_before_method: beforeMethod[test1]",
                "afterInvocation:_before_method: beforeMethod[test1]",
                "beforeInvocation:_after_method: afterMethod[test1]",
                "afterInvocation:_after_method: afterMethod[test1]",
                "beforeInvocation:_before_method: beforeMethod[test2]",
                "afterInvocation:_before_method: beforeMethod[test2]",
                "beforeInvocation:_after_method: afterMethod[test2]",
                "afterInvocation:_after_method: afterMethod[test2]"
        };
        String[] skipFailurePolicyExpected = new String[]{
                "beforeInvocation:_before_method: beforeMethod[test1]",
                "afterInvocation:_before_method: beforeMethod[test1]"
        };
        return new Object[][]{
                {XmlSuite.FailurePolicy.CONTINUE, continueFailurePolicyExpected},
                {XmlSuite.FailurePolicy.SKIP, skipFailurePolicyExpected}
        };
    }
}
