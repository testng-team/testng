package test.listeners;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.Test;
import org.testng.xml.Parser;
import org.testng.xml.XmlSuite;
import org.xml.sax.SAXException;


public class ExecutionListenerAndSuiteListenerTest {

    @Test
    public void executionListenerAndSuiteListenerTest() {
        String suiteFile = "src/test/resources/executionlistenersingletoncheck/parent.xml";
        runTests(suiteFile);
    }

    private static void runTests(String suiteFile) {
        List<XmlSuite> suites;
        try {
            suites = new Parser(suiteFile).parseToList();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new TestNGException(e);
        }
        TestNG testng = new TestNG();
        testng.setXmlSuites(suites);
        testng.run();
        assertEquals(ExecutionListenerAndSuiteListener.getTmpString(), "INITIALIZED");
    }
}
