package test.timeout;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;

import java.util.Arrays;
import java.util.Iterator;

import test.BaseTest;

public class TimeOutTest extends BaseTest {
  private final long m_id;

  public TimeOutTest() {
    m_id = System.currentTimeMillis();
  }

  private void privateTimeOutTest(XmlSuite.ParallelMode parallel) {
    addClass(TimeOutSampleTest.class);
    if (parallel != null) {
      setParallel(parallel);
    }
    run();

    verifyPassedTests("timeoutShouldPass");
    verifyFailedTests("timeoutShouldFailByException", "timeoutShouldFailByTimeOut");
  }

  @DataProvider(name = "parallelModes")
  public Iterator<Object[]> createData() {
    final Iterator<XmlSuite.ParallelMode> parallelModes = Arrays.asList(XmlSuite.ParallelMode.values()).iterator();
    return new Iterator<Object[]>() {
      @Override
      public boolean hasNext() {
        return parallelModes.hasNext();
      }

      @Override
      public Object[] next() {
        return new Object[]{ parallelModes.next() };
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("remove");
      }
    };
  }


  @Test(dataProvider = "parallelModes")
  public void timeOutInParallel(XmlSuite.ParallelMode parallelMode) {
    privateTimeOutTest(parallelMode);
  }

  @Test
  public void timeOutInNonParallel() {
    privateTimeOutTest(null);
  }

  @Test
  public void verifyInvocationTimeOut() {
    addClass(InvocationTimeOutSampleTest.class);
    run();
    verifyPassedTests("shouldPass");
    verifyFailedTests("shouldFail");
  }

  @Override
  public Long getId() {
    return m_id;
  }
}
