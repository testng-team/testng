package test.reflect;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Executable;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestNGListener;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * End-to-end guard for the original problem: a big {@code @Factory} suite wraps the same few
 * methods once per instance, which used to leave one reflective handle alive per wrapper. This runs
 * that exact scenario and checks that all the per-instance wrappers collapse onto one shared handle
 * per distinct method. If a future refactor bypasses the cache, the distinct-handle count jumps
 * back up and this test fails.
 */
public class InterningRegressionTest {

  private static final int INSTANCES = 50;

  public static class Bean {
    @Test
    public void one() {}

    @Test
    public void two() {}
  }

  public static class BeanFactory {
    @Factory
    public Object[] create() {
      Bean[] beans = new Bean[INSTANCES];
      for (int i = 0; i < INSTANCES; i++) {
        beans[i] = new Bean();
      }
      return beans;
    }
  }

  @Test
  public void perInstanceWrappersCollapseToOneHandlePerMethod() {
    HandleCollector collector = new HandleCollector();

    TestNG tng = new TestNG();
    tng.setUseDefaultListeners(false);
    tng.setVerbose(0);
    tng.setTestClasses(new Class[] {BeanFactory.class});
    tng.addListener((ITestNGListener) collector);
    tng.run();

    assertThat(collector.testInvocations)
        .as("%s instances x 2 test methods should all run", INSTANCES)
        .isEqualTo(INSTANCES * 2);
    assertThat(collector.distinctHandles())
        .as("every wrapper for a given method should share one interned handle")
        .isEqualTo(2);
  }

  /** Gathers the reflective handle behind every test-method invocation, by identity. */
  private static final class HandleCollector implements IInvokedMethodListener {
    private final Set<Executable> handles = Collections.newSetFromMap(new IdentityHashMap<>());
    private int testInvocations = 0;

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
      if (!method.isTestMethod()) {
        return;
      }
      testInvocations++;
      handles.add(method.getTestMethod().getConstructorOrMethod().getMethod());
    }

    int distinctHandles() {
      return handles.size();
    }
  }
}
