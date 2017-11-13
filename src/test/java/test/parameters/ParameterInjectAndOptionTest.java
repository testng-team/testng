package test.parameters;

import org.testng.Assert;
import org.testng.ITestNGListener;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

public class ParameterInjectAndOptionTest extends SimpleBaseTest {

    @Test
    public void test() {
        TestNG tng = create(ParameterInjectAndOptionSample.class);
        TestListenerAdapter tla = new TestListenerAdapter();
        tng.addListener((ITestNGListener) tla);
        tng.run();
        Assert.assertEquals(tla.getPassedTests().size(), 1);
    }

}
