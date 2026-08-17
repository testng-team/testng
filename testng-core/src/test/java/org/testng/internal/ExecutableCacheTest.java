package org.testng.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.testng.annotations.Test;

/** Unit tests for {@link ExecutableCache}. */
public class ExecutableCacheTest {

  @SuppressWarnings("unused")
  static class Sample {
    Sample(String ignored) {}

    void foo(String s) {}

    void foo(int i) {}
  }

  interface Parent<T> {
    T value();
  }

  // A covariant override: the compiler adds a bridge method `Object value()` next to the real
  // `String value()`, so this class has two `value()` methods that differ only in return type.
  static class Child implements Parent<String> {
    @Override
    public String value() {
      return "child";
    }
  }

  private static Method foo() throws NoSuchMethodException {
    return Sample.class.getDeclaredMethod("foo", String.class);
  }

  @Test
  public void internReturnsOneSharedHandleForEqualMembers() throws NoSuchMethodException {
    ExecutableCache cache = new ExecutableCache();
    // Reflection hands back a fresh copy per lookup...
    assertThat(foo()).isNotSameAs(foo());

    // ...but the cache collapses them to a single shared instance.
    Executable first = cache.intern(foo());
    Executable second = cache.intern(foo());

    assertThat(second).isSameAs(first);
    assertThat(first).isEqualTo(foo());
  }

  @Test
  public void overloadsAreKeptDistinct() throws NoSuchMethodException {
    ExecutableCache cache = new ExecutableCache();

    Executable withString = cache.intern(Sample.class.getDeclaredMethod("foo", String.class));
    Executable withInt = cache.intern(Sample.class.getDeclaredMethod("foo", int.class));

    assertThat(withString).isNotSameAs(withInt);
  }

  @Test
  public void aCovariantMethodAndItsBridgeAreKeptDistinct() throws NoSuchMethodException {
    // The cache keys on the Executable itself, and Method#equals compares the return type, so a
    // covariant method and its compiler-generated bridge must not collapse onto one shared handle.
    ExecutableCache cache = new ExecutableCache();

    Method real = Child.class.getDeclaredMethod("value"); // most specific: returns String
    Method bridge = null;
    for (Method m : Child.class.getDeclaredMethods()) {
      if (m.getName().equals("value") && m.isBridge()) {
        bridge = m;
      }
    }
    assertThat(bridge).as("the compiler should have generated a bridge method").isNotNull();

    assertThat(cache.intern(real)).isNotSameAs(cache.intern(bridge));
  }

  @Test
  @SuppressWarnings("deprecation")
  public void anAccessibleIncomingMethodUpgradesTheSharedHandle() throws NoSuchMethodException {
    ExecutableCache cache = new ExecutableCache();

    // The member is first interned through a plain (not-accessible) copy, so the shared handle
    // starts out not accessible.
    Executable shared = cache.intern(foo());
    assertThat(shared.isAccessible()).isFalse();

    // A later caller hands over a copy it had already made accessible...
    Method accessible = foo();
    accessible.setAccessible(true);
    Executable returned = cache.intern(accessible);

    // ...so the shared handle is upgraded, and every later lookup sees it as accessible.
    assertThat(returned).isSameAs(shared);
    assertThat(shared.isAccessible()).isTrue();
    assertThat(cache.intern(foo()).isAccessible()).isTrue();
  }

  @Test
  @SuppressWarnings("deprecation")
  public void anAccessibleIncomingConstructorUpgradesTheSharedHandle()
      throws NoSuchMethodException {
    ExecutableCache cache = new ExecutableCache();

    Executable shared = cache.intern(Sample.class.getDeclaredConstructor(String.class));
    assertThat(shared.isAccessible()).isFalse();

    Constructor<?> accessible = Sample.class.getDeclaredConstructor(String.class);
    accessible.setAccessible(true);

    assertThat(cache.intern(accessible)).isSameAs(shared);
    assertThat(shared.isAccessible()).isTrue();
  }

  @Test
  public void concurrentInternConvergesOnOneSharedInstance() throws Exception {
    // computeIfAbsent installs atomically, so threads racing to intern the same member must all end
    // up with the one shared handle, not one fresh copy each.
    ExecutableCache cache = new ExecutableCache();

    int threads = 8;
    ExecutorService pool = Executors.newFixedThreadPool(threads);
    CountDownLatch startTogether = new CountDownLatch(1);
    Set<Executable> distinct = Collections.newSetFromMap(new IdentityHashMap<>());
    try {
      List<Future<Executable>> results = new ArrayList<>();
      for (int i = 0; i < threads; i++) {
        Callable<Executable> task =
            () -> {
              startTogether.await();
              return cache.intern(foo());
            };
        results.add(pool.submit(task));
      }
      startTogether.countDown(); // release all threads at the same moment
      for (Future<Executable> result : results) {
        distinct.add(result.get());
      }
    } finally {
      pool.shutdownNow();
    }

    assertThat(distinct).as("every caller should receive the single shared handle").hasSize(1);
  }
}
