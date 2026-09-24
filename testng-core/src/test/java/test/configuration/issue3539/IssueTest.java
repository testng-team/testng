package test.configuration.issue3539;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.TestNG;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.testng.internal.MethodInstance;
import org.testng.internal.WrappedTestNGMethod;
import test.SimpleBaseTest;

/**
 * GITHUB-3539. A pooled method whose instance id is not an index key still runs its first and last
 * configurations only for the instance that owns the method.
 */
public class IssueTest extends SimpleBaseTest {

  @Test(description = "GITHUB-3539")
  public void pooledTimeOnlyConfigsStayOnTheOwningInstanceWhenTheIndexIdIsMissing() {
    PooledWrappedFactory.reset();
    NonDelegatingWrap wrap = new NonDelegatingWrap();
    TestNG testng = create(PooledWrappedFactory.class);
    testng.addListener(wrap);
    testng.run();

    assertThat(wrap.sawNonDelegatingWrap).isTrue();
    assertThat(testng.getStatus()).isZero();
    assertThat(PooledWrappedFactory.counts).hasSize(2);
    assertThat(PooledWrappedFactory.counts.values())
        .as(
            "counts=%s",
            PooledWrappedFactory.counts.values().stream()
                .map(
                    counts ->
                        counts.before.get() + "/" + counts.after.get() + "/" + counts.tests.get())
                .collect(Collectors.toList()))
        .allMatch(
            counts ->
                counts.before.get() == 1 && counts.after.get() == 1 && counts.tests.get() == 2);
  }

  /**
   * Replaces each test method with a {@link WrappedTestNGMethod} whose delegate is not a {@code
   * BaseTestMethod}. That wrapper invents an id, so {@code configInstanceId} is null.
   */
  static final class NonDelegatingWrap implements IMethodInterceptor {
    boolean sawNonDelegatingWrap;

    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
      List<IMethodInstance> wrapped = new ArrayList<>();
      for (IMethodInstance methodInstance : methods) {
        ITestNGMethod original = methodInstance.getMethod();
        ITestNGMethod delegate = nonBaseDelegate(original);
        WrappedTestNGMethod wrapper = new WrappedTestNGMethod(delegate);
        sawNonDelegatingWrap = !wrapper.hasDelegatedInstanceId();
        wrapped.add(new MethodInstance(wrapper));
      }
      return wrapped;
    }

    private static ITestNGMethod nonBaseDelegate(ITestNGMethod original) {
      return (ITestNGMethod)
          Proxy.newProxyInstance(
              ITestNGMethod.class.getClassLoader(),
              new Class<?>[] {ITestNGMethod.class},
              (proxy, method, args) -> {
                try {
                  return method.invoke(original, args == null ? new Object[0] : args);
                } catch (InvocationTargetException ex) {
                  Throwable cause = ex.getCause();
                  if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                  }
                  if (cause instanceof Error) {
                    throw (Error) cause;
                  }
                  throw ex;
                }
              });
    }
  }

  public static class PooledWrappedFactory {
    static final ConcurrentHashMap<Object, HookCounts> counts = new ConcurrentHashMap<>();

    static void reset() {
      counts.clear();
    }

    @Factory
    public static Object[] instances() {
      return new Object[] {new PooledWrappedFactory(), new PooledWrappedFactory()};
    }

    @BeforeMethod(firstTimeOnly = true)
    public void beforeFirst() {
      counts.computeIfAbsent(this, key -> new HookCounts()).before.incrementAndGet();
    }

    @AfterMethod(lastTimeOnly = true)
    public void afterLast() {
      counts.computeIfAbsent(this, key -> new HookCounts()).after.incrementAndGet();
    }

    @Test(invocationCount = 2, threadPoolSize = 2)
    public void test() {
      counts.computeIfAbsent(this, key -> new HookCounts()).tests.incrementAndGet();
    }
  }

  static final class HookCounts {
    final AtomicInteger before = new AtomicInteger();
    final AtomicInteger after = new AtomicInteger();
    final AtomicInteger tests = new AtomicInteger();
  }
}
