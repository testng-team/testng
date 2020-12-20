package test.github2440;

import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

import java.util.Collections;

public class TimeoutWithNoExecutorTest extends SimpleBaseTest {

    @Test
    public void testTimeout() {
        TestNG testNG = new TestNG();
        XmlSuite xmlSuite = new XmlSuite();
        XmlTest xmlTest = new XmlTest(xmlSuite);
        xmlTest.setClasses(Collections.singletonList(new XmlClass(TimeoutTest.class)));
        xmlSuite.setParallel(XmlSuite.ParallelMode.CLASSES);
        testNG.setXmlSuites(Collections.singletonList(xmlSuite));
        testNG.run();
    }

}
