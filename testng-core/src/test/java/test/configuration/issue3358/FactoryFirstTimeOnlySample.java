package test.configuration.issue3358;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class FactoryFirstTimeOnlySample {
  public static final ConcurrentHashMap<Object, AtomicInteger> befores = new ConcurrentHashMap<>();
  public static final ConcurrentHashMap<Object, AtomicInteger> tests = new ConcurrentHashMap<>();

  public static void reset() {
    befores.clear();
    tests.clear();
  }

  @Factory
  public static Object[] instances() {
    return new Object[] {new FactoryFirstTimeOnlySample(), new FactoryFirstTimeOnlySample()};
  }

  @BeforeMethod(firstTimeOnly = true)
  public void before() {
    befores.computeIfAbsent(this, key -> new AtomicInteger()).incrementAndGet();
  }

  @Test(invocationCount = 2)
  public void test() {
    tests.computeIfAbsent(this, key -> new AtomicInteger()).incrementAndGet();
  }
}
