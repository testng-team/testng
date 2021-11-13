package test.testng1232;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.*;
import org.testng.collections.Maps;
import org.testng.xml.XmlSuite;

public class TestListenerFor1232 extends ListenerTemplate {
  static Map<CounterTypes, AtomicInteger> counters = Maps.newHashMap();

  static synchronized void resetCounters() {
    counters = Maps.newHashMap();
  }

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    incrementCounter(CounterTypes.METHOD);
  }

  @Override
  public void onBeforeClass(ITestClass testClass) {
    incrementCounter(CounterTypes.CLASS);
  }

  @Override
  public void onStart(ITestContext context) {
    incrementCounter(CounterTypes.TEST);
  }

  @Override
  public void onStart(ISuite suite) {
    incrementCounter(CounterTypes.SUITE);
  }

  @Override
  public void alter(List<XmlSuite> suites) {
    incrementCounter(CounterTypes.ALTER_SUITE);
  }

  @Override
  public void onExecutionStart() {
    incrementCounter(CounterTypes.EXECUTION);
  }

  @Override
  public void generateReport(
      List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
    incrementCounter(CounterTypes.REPORTER);
  }

  private void incrementCounter(CounterTypes type) {
    if (!counters.containsKey(type)) {
      counters.put(type, new AtomicInteger(0));
    }
    AtomicInteger value = counters.get(type);
    value.incrementAndGet();
    counters.put(type, value);
  }

  enum CounterTypes {
    METHOD,
    CLASS,
    TEST,
    SUITE,
    ALTER_SUITE,
    EXECUTION,
    REPORTER
  }
}
