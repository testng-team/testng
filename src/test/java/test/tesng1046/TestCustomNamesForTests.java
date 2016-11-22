package test.tesng1046;

import org.testng.Assert;
import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.collections.Sets;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

import java.util.Set;

public class TestCustomNamesForTests extends SimpleBaseTest {
    @Test
    public void testCustomTestNames() {
        XmlSuite xmlSuite = createXmlSuite("Suite");
        XmlTest xmlTest = createXmlTest(xmlSuite, "Test");
        createXmlClass(xmlTest, TestClassSample.class);
        TestNG tng = create(xmlSuite);
        LocalTestNameGatherer reporter = new LocalTestNameGatherer();
        tng.addListener((ITestNGListener) reporter);
        tng.run();
        Set<String> expectedNames = Sets.newHashSet();
        for (String method : new String[] {"testSample1", "testSample2"}) {
            for (int i = 1; i <= 5; i++) {
                expectedNames.add(method + "_TestNG_TestCase_" + i);
            }
        }
        expectedNames.add("ordinaryTestMethod_TestNG_TestCase_999");
        Assert.assertEquals(reporter.getTestnames(), expectedNames);
    }
}
