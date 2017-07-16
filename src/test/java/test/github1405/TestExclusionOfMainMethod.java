package test.github1405;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

import java.util.Collections;

public class TestExclusionOfMainMethod extends SimpleBaseTest {
    @Test
    public void testMainMethodExclusion() {
        TestNG tng = create(TestClassSample.class);
        tng.run();
        Assert.assertEquals(tng.getStatus(), 0);
    }

    @Test
    public void testMainMethodExclusionForJunit() {
        XmlSuite xmlSuite = createXmlSuite("suite");
        xmlSuite.setJunit(true);
        createXmlTest(xmlSuite, "test", JUnitTestClassSample.class);
        TestNG tng = create(xmlSuite);
        tng.run();
        Assert.assertEquals(tng.getStatus(), 0);
    }
}
