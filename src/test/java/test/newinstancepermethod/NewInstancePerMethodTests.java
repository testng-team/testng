package test.newinstancepermethod;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.Arrays;

public class NewInstancePerMethodTests {
    @Test
    public void testParallelAttributes() {
        XmlSuite suite = new XmlSuite();
        suite.setName("TestParallelAttributes");
        XmlTest test = new XmlTest(suite);
        test.setParallel(XmlSuite.ParallelMode.METHODS);
        test.setThreadCount(6);
        test.getXmlClasses().add(new XmlClass(ParallelAttributeTest.class.getName(), 0, true));
        TestNG result = new TestNG();
        result.setUseDefaultListeners(false);
        result.setVerbose(0);
        result.setXmlSuites(Arrays.asList(suite));
        TestListenerAdapter tla = new TestListenerAdapter();
        result.addListener(tla);
        result.run();
        Assert.assertEquals(tla.getFailedTests().size(), 0, "Each test method did not recieve it's own instance. ");
    }

    @Test
    public void testSerialModificationWithSystemProperty() {
        boolean modified = false;
        if(!System.getProperties().containsKey("testng.newInstancePerMethod")) {
            System.getProperties().put("testng.newInstancePerMethod", "true");
            modified = true;
        }
        XmlSuite suite = new XmlSuite();
        suite.setName("TestSerialModification");
        XmlTest test = new XmlTest(suite);
        test.getXmlClasses().add(new XmlClass(SerialMemberModificationTest.class.getName(), 0, true));
        TestNG result = new TestNG();
        result.setUseDefaultListeners(false);
        result.setVerbose(0);
        result.setXmlSuites(Arrays.asList(suite));
        TestListenerAdapter tla = new TestListenerAdapter();
        result.addListener(tla);
        result.run();
        try {
            Assert.assertEquals(tla.getFailedTests().size(), 0, "Each test method did not receive it's own instance. ");
        } finally {
            if(modified) {
                System.getProperties().remove("testng.newInstancePerMethod");
            }
        }
    }

    @Test
    public void testClonable() {
        XmlSuite suite = new XmlSuite();
        suite.setName("CloneableTest");
        XmlTest test = new XmlTest(suite);
        test.getXmlClasses().add(new XmlClass(CloneableTest.class.getName(), 0, true));
        TestNG result = new TestNG();
        result.setUseDefaultListeners(false);
        result.setVerbose(0);
        result.setXmlSuites(Arrays.asList(suite));
        TestListenerAdapter tla = new TestListenerAdapter();
        result.addListener(tla);
        result.run();
        Assert.assertEquals(tla.getFailedTests().size(), 0, "Each test method did not receive it's own instance. ");
    }
}
