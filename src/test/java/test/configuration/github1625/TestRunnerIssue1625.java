package test.configuration.github1625;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

public class TestRunnerIssue1625 extends SimpleBaseTest {

    @Test(dataProvider = "dp")
    public void testMethod(Class<?> clazz) {
        TestNG testNG = create(clazz);
        testNG.setParallel(XmlSuite.ParallelMode.METHODS);
        testNG.run();
        assertThat(testNG.getStatus()).isEqualTo(0);
    }

    @DataProvider(name = "dp")
    public Object[][] getData() {
        return new Object[][]{
                {TestclassSampleUsingMocks.class},
                {TestclassSampleWithoutUsingMocks.class}
        };
    }
}
