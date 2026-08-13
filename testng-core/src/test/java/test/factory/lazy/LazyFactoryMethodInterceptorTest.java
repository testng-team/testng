package test.factory.lazy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

/**
 * Documents how a custom {@link IMethodInterceptor} interacts with lazy instantiation.
 *
 * <p>The interceptor runs after the run has started but before any test method executes. The
 * objects it receives ({@link IMethodInstance}) expose {@link IMethodInstance#getInstance()}, and
 * calling that instantiates the (otherwise lazy) instance. So:
 *
 * <ul>
 *   <li>an interceptor that calls {@code getInstance()} forces every instance to be built up-front
 *       at interception time — negating laziness for the run (though it never runs before the run
 *       starts, and results are unchanged); whereas
 *   <li>an interceptor that only reads method metadata leaves laziness fully intact.
 * </ul>
 */
public class LazyFactoryMethodInterceptorTest extends SimpleBaseTest {

  /** Inspects the instance behind every method — which forces it to be constructed. */
  static class InstanceTouchingInterceptor implements IMethodInterceptor {
    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
      methods.forEach(IMethodInstance::getInstance);
      return methods;
    }
  }

  /** Only reads method metadata — never touches the instance. */
  static class MetadataOnlyInterceptor implements IMethodInterceptor {
    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
      methods.forEach(mi -> mi.getMethod().getMethodName());
      return methods;
    }
  }

  @Test
  public void interceptorCallingGetInstanceNegatesLaziness() {
    CountingFactorySample.reset();
    TestNG tng = create(CountingFactorySample.class);
    tng.setPreserveOrder(true);
    tng.setLazyFactoryInstantiation(true);
    InstancesConstructedAtSuiteStart atSuiteStart =
        new InstancesConstructedAtSuiteStart(CountingFactorySample.CONSTRUCTED::get);
    tng.addListener(atSuiteStart);
    tng.addListener(new InstanceTouchingInterceptor());
    tng.run();

    // The interceptor runs after the run has started, so the "nothing before the run" guarantee
    // still holds.
    assertThat(atSuiteStart.countAtStart())
        .as("nothing is constructed before the run starts, even with the interceptor")
        .isZero();
    // But because the interceptor called getInstance() on every method, all four instances were
    // instantiated before the first test ran — so each test sees all four (laziness is negated).
    assertThat(CountingFactorySample.INSTANCES_ALIVE_WHEN_EACH_TEST_RAN)
        .as("a getInstance()-calling interceptor forces every instance to be built up-front")
        .containsExactly(4, 4, 4, 4);
  }

  @Test
  public void interceptorReadingOnlyMetadataKeepsLaziness() {
    CountingFactorySample.reset();
    TestNG tng = create(CountingFactorySample.class);
    tng.setPreserveOrder(true);
    tng.setLazyFactoryInstantiation(true);
    tng.addListener(new MetadataOnlyInterceptor());
    tng.run();

    // An interceptor that never touches the instance leaves lazy instantiation fully intact.
    assertThat(CountingFactorySample.INSTANCES_ALIVE_WHEN_EACH_TEST_RAN)
        .as("an interceptor that only reads method metadata preserves laziness")
        .containsExactly(1, 2, 3, 4);
  }
}
