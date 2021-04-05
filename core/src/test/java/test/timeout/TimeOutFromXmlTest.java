package test.timeout;

import org.testng.annotations.Test;
import org.testng.xml.SuiteXmlParser;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import test.BaseTest;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TimeOutFromXmlTest extends BaseTest {

    private void timeOutTest(boolean onSuite) {
        addClass(TestTimeOutSampleTest.class);
        if (onSuite) {
            setSuiteTimeOut(1_000);
        } else {
            setTestTimeOut(1_000);
        }
        run();

        verifyPassedTests();
        verifyFailedTests("timeoutTest");
    }

    @Test
    public void timeOutOnSuiteTag() {
        timeOutTest(true /* on suite */);
    }

    @Test
    public void timeOutOnTestTag() {
        timeOutTest(false /* on test */);
    }

    @Test
    public void noTimeOut() {
      addClass(TestTimeOutSampleTest.class);
      run();

      verifyPassedTests("timeoutTest");
      verifyFailedTests();
    }

    @Test
    public void twoDifferentTests() {
      XmlSuite result = new XmlSuite();
      result.setName("Suite");

      createXmlTest(result, "WithoutTimeOut");
      createXmlTest(result, "WithTimeOut").setTimeOut(1_000);

      setSuite(result);
      run();

      verifyPassedTests("timeoutTest");
      verifyFailedTests("timeoutTest");
    }

    private XmlTest createXmlTest(XmlSuite suite, String name) {
        XmlTest result = new XmlTest(suite);
        result.setName(name);
        List<XmlClass> classes = new ArrayList<>();
        XmlClass cls = new XmlClass(TestTimeOutSampleTest.class);
        cls.setIncludedMethods(
            Collections.singletonList(new XmlInclude("timeoutTest")));
        classes.add(cls);
        result.setXmlClasses(classes);

        return result;
    }

    @Test
    public void timeOutInParallelTestsFromXml() throws IOException {
      String file = "src/test/java/test/timeout/issue575.xml";
      try (FileInputStream stream = new FileInputStream(file)) {
        SuiteXmlParser suiteParser = new SuiteXmlParser();
        XmlSuite suite = suiteParser.parse(file, stream, true);
        setSuite(suite);
        run();

        verifyPassedTests("timeoutShouldPass");
        verifyFailedTests("timeoutShouldFailByException", "timeoutShouldFailByTimeOut");
      }
    }
}
