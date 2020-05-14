package test.listeners;

import org.testng.Assert;
import org.testng.IAlterSuiteListener;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

import java.util.Arrays;
import java.util.List;

public class AlterSuiteListenerTest extends SimpleBaseTest {

    public static final String ALTER_SUITE_LISTENER = "AlterSuiteListener";

    @Test
    public void executionListenerWithXml() {
        XmlSuite suite = runTest(AlterSuiteListener1SampleTest.class, AlterSuiteNameListener.class.getName());
        Assert.assertEquals(suite.getName(), AlterSuiteNameListener.class.getSimpleName());
    }

    @Test
    public void executionListenerWithoutListener() {
        XmlSuite suite = runTest(AlterSuiteListener1SampleTest.class, null/*Donot add the listener*/);
        Assert.assertEquals(suite.getName(), ALTER_SUITE_LISTENER);
    }

    @Test
    public void executionListenerWithXml2() {
        XmlSuite suite = runTest(AlterSuiteListener1SampleTest.class, AlterXmlTestsInSuiteListener.class.getName());
        Assert.assertEquals(suite.getTests().size(), 2);
    }


    private XmlSuite runTest(Class<?> listenerClass, String listenerName) {
        XmlSuite s = createXmlSuite(ALTER_SUITE_LISTENER);
        createXmlTest(s, "Test", listenerClass.getName());
        boolean addListener = (listenerName != null);

        if (addListener) {
            s.addListener(listenerName);
        }
        TestNG tng = create();
        tng.setXmlSuites(Arrays.asList(s));
        tng.run();
        return s;
    }

    public static class AlterSuiteListener1SampleTest {
        @Test
        public void foo() {
        }
    }


    public static class AlterSuiteNameListener implements IAlterSuiteListener {

        @Override
        public void alter(List<XmlSuite> suites) {
            XmlSuite suite = suites.get(0);
            suite.setName(getClass().getSimpleName());
        }
    }


    public static class AlterXmlTestsInSuiteListener implements IAlterSuiteListener {

        @Override
        public void alter(List<XmlSuite> suites) {
            XmlSuite suite = suites.get(0);
            List<XmlTest> tests = suite.getTests();
            XmlTest test = tests.get(0);
            XmlTest anotherTest = new XmlTest(suite);
            anotherTest.setName("foo");
            anotherTest.setClasses(test.getClasses());
        }
    }

}
