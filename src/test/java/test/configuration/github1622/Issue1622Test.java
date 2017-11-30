package test.configuration.github1622;

import org.assertj.core.api.Assertions;
import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

public class Issue1622Test extends SimpleBaseTest {
    @Test(dataProvider = "dp")
    public void testWithoutGroups(XmlSuite.FailurePolicy failurePolicy, String ...expected) {
        TestNG testNG = create(TestClassWithNoGroups.class);
        testNG.setConfigFailurePolicy(failurePolicy);
        InvokedMethodNameListener listener = new InvokedMethodNameListener();
        testNG.addListener((ITestNGListener) listener);
        testNG.run();
        Assertions.assertThat(listener.getInvokedMethodNames()).contains(expected);
    }

    @DataProvider(name = "dp")
    public Object[][] getData() {
        return new Object[][] {
                {XmlSuite.FailurePolicy.SKIP, "failedBeforeSuite"},
                {XmlSuite.FailurePolicy.CONTINUE, "failedBeforeSuite", "beforeTest", "beforeClass", "beforeMethod"}
        };
    }
}
