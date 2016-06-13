package test.dataprovider;

import org.testng.Assert;
import org.testng.ITestNGListener;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.Arrays;
import java.util.Collections;

public class IndicesTest {

    @Test
    public void test() {
        TestNG testng = new TestNG(false);

        testng.setTestClasses(new Class[]{IndicesSample.class});

        TestListenerAdapter tla = new TestListenerAdapter();
        testng.addListener((ITestNGListener) tla);
        testng.setVerbose(0);
        try {
            testng.run();
        } catch (RuntimeException e) {
            Assert.fail("Exceptions thrown during tests should always be caught!", e);
        }

        Assert.assertTrue(tla.getFailedTests().isEmpty(),
                "Should have 0 failure: bad data-provider iteration should be ignored");
        Assert.assertEquals(tla.getPassedTests().size(), 2,
                "Should have 2 passed test");
    }

    @Test
    public void test2() {
        TestNG testng = new TestNG(false);

        XmlSuite suite = new XmlSuite();
        XmlTest test = new XmlTest(suite);
        XmlClass clazz = new XmlClass(IndicesSample.class);
        clazz.setXmlTest(test);
        test.getClasses().add(clazz);
        XmlInclude include = new XmlInclude("indicesShouldWork", Arrays.asList(0), 0);
        include.setXmlClass(clazz);
        clazz.getIncludedMethods().add(include);
        XmlInclude include2 = new XmlInclude("indicesShouldWorkWithIterator", Arrays.asList(0), 0);
        include2.setXmlClass(clazz);
        clazz.getIncludedMethods().add(include2);
        testng.setXmlSuites(Collections.singletonList(suite));

        TestListenerAdapter tla = new TestListenerAdapter();
        testng.addListener((ITestNGListener) tla);
        testng.setVerbose(0);
        try {
            testng.run();
        } catch (RuntimeException e) {
            Assert.fail("Exceptions thrown during tests should always be caught!", e);
        }

        Assert.assertTrue(tla.getFailedTests().isEmpty(),
                "Should have 0 failure: bad data-provider iteration should be ignored");
        Assert.assertEquals(tla.getPassedTests().size(), 4,
                "Should have 4 passed test");
    }
}