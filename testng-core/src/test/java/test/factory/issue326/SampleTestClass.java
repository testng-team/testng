package test.factory.issue326;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class SampleTestClass {

  static final String THREAD_ID = "threadId";
  static final String FREDDY = "Freddy";
  static final String BARNEY = "Barney";

  private final String instance;
  private final Random random;

  @Factory(dataProvider = "dp")
  public SampleTestClass(String instance) {
    this.instance = instance;
    random = new Random();
  }

  @DataProvider
  public static Iterator<Object[]> dp() {
    List<Object[]> names = Arrays.asList(new Object[] {FREDDY}, new Object[] {BARNEY});
    return names.iterator();
  }

  @Test
  public void test1() throws InterruptedException {
    printer();
  }

  @Test
  public void test2() throws InterruptedException {
    printer();
  }

  @Override
  public String toString() {
    return this.instance;
  }

  private void printer() throws InterruptedException {
    ITestResult result = Reporter.getCurrentTestResult();
    result.setAttribute(THREAD_ID, Thread.currentThread().getId());
    TimeUnit.MILLISECONDS.sleep(10 * random.nextInt(100));
  }
}
