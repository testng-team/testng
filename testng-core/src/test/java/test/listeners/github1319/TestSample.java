package test.listeners.github1319;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.google.common.collect.Maps;
import java.util.Map;
import org.testng.IConfigurationListener;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.*;

@Listeners(TestSample.Listener.class)
public class TestSample {
  public static int hashcode;

  public TestSample() {
    hashcode = hashCode();
  }

  @Test
  public void test1() {
    assertThat(true).isTrue();
  }

  @Test
  public void test2() {
    fail();
  }

  @Test
  public void test3() {
    throw new SkipException("simulating a skip");
  }

  @AfterClass
  public void afterClass() {}

  @AfterTest
  public void afterTest() {
    throw new RuntimeException("Simulating a failure");
  }

  @AfterSuite
  public void afterSuite() {
    throw new SkipException("simulating a skip");
  }

  public static class Listener implements IConfigurationListener, ITestListener {
    public static Map<String, Object> maps = Maps.newConcurrentMap();

    public void onConfigurationSuccess(ITestResult itr) {
      maps.put(itr.getMethod().getMethodName(), itr.getInstance());
    }

    public void onConfigurationFailure(ITestResult itr) {
      maps.put(itr.getMethod().getMethodName(), itr.getInstance());
    }

    public void onConfigurationSkip(ITestResult itr) {
      maps.put(itr.getMethod().getMethodName(), itr.getInstance());
    }

    @Override
    public void onTestSuccess(ITestResult itr) {
      maps.put(itr.getMethod().getMethodName(), itr.getInstance());
    }

    @Override
    public void onTestFailure(ITestResult itr) {
      maps.put(itr.getMethod().getMethodName(), itr.getInstance());
    }

    @Override
    public void onTestSkipped(ITestResult itr) {
      maps.put(itr.getMethod().getMethodName(), itr.getInstance());
    }
  }
}
