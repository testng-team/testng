package test.methodselectors;

import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlMethodSelector;
import org.testng.xml.XmlScript;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class ScriptNegativeTest extends SimpleBaseTest {

    private static final String LANGUAGE_NAME = "MissingLanguage";

    @BeforeMethod
    public void setup() {
        System.setProperty("skip.caller.clsLoader", Boolean.TRUE.toString());
    }

    @AfterMethod
    public void cleanup() {
        System.setProperty("skip.caller.clsLoader", Boolean.FALSE.toString());
    }

    @Test (expectedExceptions = TestNGException.class,
        expectedExceptionsMessageRegExp = ".*No engine found for language: " + LANGUAGE_NAME + ".*")
    public void testNegativeScenario() {
        XmlSuite suite = createXmlSuite("suite");
        XmlTest test = createXmlTest(suite, "test", "test.methodselectors.SampleTest");
        XmlScript script = new XmlScript();
        script.setLanguage(LANGUAGE_NAME);
        script.setExpression("expression");
        XmlMethodSelector selector = new XmlMethodSelector();
        selector.setScript(script);
        test.setMethodSelectors(Collections.singletonList(selector));
        TestNG tng = create(suite);
        tng.run();
    }
}
