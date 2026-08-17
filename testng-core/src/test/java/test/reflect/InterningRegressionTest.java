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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.testng.internal.RuntimeBehavior;

/**
 * End-to-end guard for the original problem: a big {@code @Factory} suite wraps the same few
 * methods once per instance, which used to leave one reflective handle alive per wrapper.
 *
 * <p>It runs that exact scenario twice — interning on and off — and checks both ends: with
 * interning on the whole suite shares one handle per distinct method; with it off the per-instance
 * duplicates reappear (on the config methods, which TestNG does not share by cloning) and the
 * distinct-handle count grows with the instance count. The off run is what proves the test really
 * reproduces the original problem, so if a future refactor silently bypasses the cache the on run
 * fails.
 */
public class InterningRegressionTest {

  private static final int INSTANCES = 50;

  public static class Bean {
    @BeforeMethod
    public void setUp() {}

    @AfterMethod
    public void tearDown() {}

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
  public void interningCollapsesPerInstanceHandlesAndDisablingItBringsThemBack() {
    Result on = runSuite(true);
    Result off = runSuite(false);

    assertThat(on.invocations)
        .as("%s instances x 2 test methods should all run", INSTANCES)
        .isEqualTo(INSTANCES * 2);
    assertThat(off.invocations).isEqualTo(INSTANCES * 2);

    // With interning on, the whole suite shares one handle per distinct method: setUp, tearDown,
    // one, two — four in all, no matter how many instances the factory made.
    assertThat(on.distinctHandles)
        .as("interning on: one shared handle per distinct method")
        .isEqualTo(4);
    // With interning off the config methods are no longer shared — a fresh handle per instance — so
    // the distinct count grows with the instance count. That is the original problem, reproduced.
    assertThat(off.distinctHandles)
        .as("interning off: the per-instance duplicates reappear and scale with instance count")
        .isGreaterThanOrEqualTo(INSTANCES);
  }

  private static Result runSuite(boolean intern) {
    String saved = System.getProperty(RuntimeBehavior.INTERN_REFLECTIVE_MEMBERS);
    System.setProperty(RuntimeBehavior.INTERN_REFLECTIVE_MEMBERS, Boolean.toString(intern));
    try {
      HandleCollector collector = new HandleCollector();
      TestNG tng = new TestNG();
      tng.setUseDefaultListeners(false);
      tng.setVerbose(0);
      tng.setTestClasses(new Class[] {BeanFactory.class});
      tng.addListener((ITestNGListener) collector);
      tng.run();
      return new Result(collector.testInvocations, collector.distinctHandles());
    } finally {
      if (saved == null) {
        System.clearProperty(RuntimeBehavior.INTERN_REFLECTIVE_MEMBERS);
      } else {
        System.setProperty(RuntimeBehavior.INTERN_REFLECTIVE_MEMBERS, saved);
      }
    }
  }

  private static final class Result {
    final int invocations;
    final int distinctHandles;

    Result(int invocations, int distinctHandles) {
      this.invocations = invocations;
      this.distinctHandles = distinctHandles;
    }
  }

  /**
   * Gathers the reflective handle behind every invoked method — test <em>and</em> config — by
   * identity. The per-instance duplication the cache removes shows up on the config methods (a
   * fresh handle per instance without the cache), not on the {@code @Test} methods, which TestNG
   * already shares by cloning.
   */
  private static final class HandleCollector implements IInvokedMethodListener {
    private final Set<Executable> handles = Collections.newSetFromMap(new IdentityHashMap<>());
    private int testInvocations = 0;

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
      handles.add(method.getTestMethod().getConstructorOrMethod().getMethod());
      if (method.isTestMethod()) {
        testInvocations++;
      }
    }

    int distinctHandles() {
      return handles.size();
    }
  }
}
