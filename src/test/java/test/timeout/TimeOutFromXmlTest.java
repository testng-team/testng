package test.timeout;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import test.BaseTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TimeOutFromXmlTest extends BaseTest {
    
    private void timeOutTest(boolean onSuite) {
        addClass("test.timeout.TestTimeOutSampleTest");
        if (onSuite) {
            setSuiteTimeOut(1000);
        } else {
            setTestTimeOut(1000);
        }
        run();
        String[] passed = {
          };
        String[] failed = {
          "timeoutTest"
        };
      
//        dumpResults("Passed", getPassedTests());
//        dumpResults("Failed", getFailedTests());

        verifyTests("Passed", passed, getPassedTests());
        verifyTests("Failed", failed, getFailedTests());
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
      addClass("test.timeout.TestTimeOutSampleTest");
      run();
      String[] passed = {
          "timeoutTest"
        };
        String[] failed = {
        };
        
        verifyTests("Passed", passed, getPassedTests());
        verifyTests("Failed", failed, getFailedTests());
    }

    @Test
    public void twoDifferentTests() {
      XmlSuite result = new XmlSuite();
      result.setName("Suite");
      
      createXmlTest(result, "WithoutTimeOut");
      createXmlTest(result, "WithTimeOut").setTimeOut(1000);

      TestNG tng = new TestNG();
      tng.setVerbose(0);
      tng.setXmlSuites(Arrays.asList(new XmlSuite[] { result }));
      TestListenerAdapter tla = new TestListenerAdapter();
      tng.addListener(tla);
      tng.run();
      
//      System.out.println("Passed:" + tla.getPassedTests().size()
//          + " Failed:" + tla.getFailedTests().size());
      Assert.assertEquals(tla.getPassedTests().size(), 1);
      Assert.assertEquals(tla.getFailedTests().size(), 1);
    }

    private XmlTest createXmlTest(XmlSuite suite, String name) {
        XmlTest result = new XmlTest(suite);
        result.setName(name);
        List<XmlClass> classes = new ArrayList<XmlClass>();
        XmlClass cls = new XmlClass(TestTimeOutSampleTest.class);
        cls.setIncludedMethods(
            Arrays.asList(new XmlInclude[] { new XmlInclude("timeoutTest") }));
        classes.add(cls);
        result.setXmlClasses(classes);

        return result;
    }}
