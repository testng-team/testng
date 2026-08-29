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
 * End-to-end guard for the original problem: a big suite wraps the same few methods once per test
 * class, which used to leave one reflective handle alive per wrapper. Every method lookup goes
 * through {@code Class.getDeclaredMethods()}, which hands back a fresh {@link Executable} every
 * time, so a base class carrying the configuration and test methods is re-wrapped by each of its
 * subclasses.
 *
 * <p>It runs that scenario twice — interning on and off — and checks both ends: with interning on
 * the whole suite shares one handle per distinct method; with it off the per-class duplicates
 * reappear and the distinct-handle count grows with the number of test classes. The off run is what
 * proves the test really reproduces the original problem, so if a future refactor silently bypasses
 * the cache the on run fails.
 *
 * <p>A {@code @Factory} used to be the shape that amplified this, one wrapper per instance, because
 * {@code TestClass.initMethods} looked its configuration methods up once per instance. It no longer
 * does (GITHUB-775), so a factory now shares its handles whether interning is on or not and the
 * duplication the cache removes is the per-class one measured here;
 * aFactoryNoLongerWrapsItsConfigurationMethodsOncePerInstance pins that.
 */
public class InterningRegressionTest {

  private static final int INSTANCES = 50;

  /** The four distinct methods a test class contributes: setUp, tearDown, one, two. */
  private static final int METHODS_PER_CLASS = 4;

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

  // Subclasses declare nothing: every method they run is inherited from Bean, so a handle the
  // suite holds more than once can only be a duplicate wrapper of the same base-class method.
  public static class Bean1 extends Bean {}

  public static class Bean2 extends Bean {}

  public static class Bean3 extends Bean {}

  public static class Bean4 extends Bean {}

  public static class Bean5 extends Bean {}

  public static class Bean6 extends Bean {}

  private static final Class<?>[] SUBCLASSES = {
    Bean1.class, Bean2.class, Bean3.class, Bean4.class, Bean5.class, Bean6.class
  };

  @Test
  public void interningCollapsesPerClassHandlesAndDisablingItBringsThemBack() {
    Result on = runSuite(true, SUBCLASSES);
    Result off = runSuite(false, SUBCLASSES);

    assertThat(on.invocations)
        .as("%s classes x 2 test methods should all run", SUBCLASSES.length)
        .isEqualTo(SUBCLASSES.length * 2);
    assertThat(off.invocations).isEqualTo(SUBCLASSES.length * 2);

    // With interning on, the whole suite shares one handle per distinct method: setUp, tearDown,
    // one, two — four in all, no matter how many classes inherited them.
    assertThat(on.distinctHandles)
        .as("interning on: one shared handle per distinct method")
        .isEqualTo(METHODS_PER_CLASS);
    // With interning off each class re-wraps the four it inherited, so the distinct count grows
    // with the number of classes. That is the original problem, reproduced.
    assertThat(off.distinctHandles)
        .as("interning off: the per-class duplicates reappear and scale with the class count")
        .isEqualTo(METHODS_PER_CLASS * SUBCLASSES.length);
  }

  /**
   * A factory's instances share their configuration methods' handles even with interning off,
   * because the lookup that produces them now happens once for the test class rather than once per
   * instance.
   */
  @Test
  public void aFactoryNoLongerWrapsItsConfigurationMethodsOncePerInstance() {
    Result off = runSuite(false, BeanFactory.class);

    assertThat(off.invocations)
        .as("%s instances x 2 test methods should all run", INSTANCES)
        .isEqualTo(INSTANCES * 2);
    assertThat(off.distinctHandles)
        .as("one handle per distinct method, whatever the number of instances")
        .isEqualTo(METHODS_PER_CLASS);
  }

  private static Result runSuite(boolean intern, Class<?>... classes) {
    String saved = System.getProperty(RuntimeBehavior.INTERN_REFLECTIVE_MEMBERS);
    System.setProperty(RuntimeBehavior.INTERN_REFLECTIVE_MEMBERS, Boolean.toString(intern));
    try {
      HandleCollector collector = new HandleCollector();
      TestNG tng = new TestNG();
      tng.setUseDefaultListeners(false);
      tng.setVerbose(0);
      tng.setTestClasses(classes);
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
   * identity. The duplication the cache removes shows up on the methods each subclass re-wraps (a
   * fresh handle per class without the cache), not on the {@code @Test} methods of a single class,
   * which TestNG already shares by cloning.
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
