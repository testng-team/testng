package test.timeout;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.SuiteXmlParser;
import org.testng.xml.XmlSuite;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import test.BaseTest;


/**
 * This class
 *
 * @author cbeust
 */
public class TimeOutTest extends BaseTest {
  private Long m_id;

  public TimeOutTest() {
    m_id = System.currentTimeMillis();
  }

  private void privateTimeOutTest(String parallel) {
    addClass("test.timeout.TimeOutSampleTest");
    if (parallel != null) {
      setParallel(parallel);
    }
    runAndVerify();
  }

  private void runAndVerify() {
    run();
    String[] passed = {
        "timeoutShouldPass",
    };
    String[] failed = {
        "timeoutShouldFailByException", "timeoutShouldFailByTimeOut"
    };

    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }

  @DataProvider(name = "parallelModes")
  public Iterator<Object[]> createData() {
    final Iterator<String> parallelModes = XmlSuite.PARALLEL_MODES.iterator();
    return new Iterator<Object[]>() {
      @Override
      public boolean hasNext() {
        return parallelModes.hasNext();
      }

      @Override
      public Object[] next() {
        return new Object[]{ parallelModes.next() };
      }
    };
  }


  @Test(dataProvider = "parallelModes")
  public void timeOutInParallel(String parallelMode) {
    privateTimeOutTest(parallelMode);
  }

  @Test
  public void timeOutInParallelTestsFromXml() throws IOException {
    String file = "src/test/java/test/timeout/issue575.xml";
    try (FileInputStream stream = new FileInputStream(file)) {
      SuiteXmlParser suiteParser = new SuiteXmlParser();
      XmlSuite suite = suiteParser.parse(file, stream, true);
      setSuite(suite);
      runAndVerify();
    }
  }

  @Test
  public void timeOutInNonParallel() {
    privateTimeOutTest(null);
  }

  @Test
  public void verifyInvocationTimeOut() {
    addClass("test.timeout.InvocationTimeOutSampleTest");
    run();
    String[] passed = {
        "shouldPass",
      };
      String[] failed = {
          "shouldFail"
      };
      verifyTests("Passed", passed, getPassedTests());
      verifyTests("Failed", failed, getFailedTests());
  }

  @Override
  public Long getId() {
    return m_id;
  }


}
