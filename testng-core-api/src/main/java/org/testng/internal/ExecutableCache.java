package org.testng.internal;

import java.lang.reflect.Executable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * A process-wide pool that de-duplicates resolved methods and constructors: every caller asking for
 * the same member gets one shared {@link Executable} instead of making its own copy. Members are
 * looked up by declaring class, then by the {@link Executable} itself.
 *
 * <p>The {@link Executable} is its own key: {@link java.lang.reflect.Method#equals(Object)} and
 * {@link java.lang.reflect.Constructor#equals(Object)} already compare declaring class, name,
 * parameter types and (for methods) return type, so a covariant method and the compiler-generated
 * bridge that backs it stay distinct. Keying on the handle directly means no extra key object is
 * built per lookup.
 *
 * <p>Each declaring class's table lives in a {@link ClassValue}, so it is discarded together with
 * the class — the cache never keeps a class (or its class loader) alive on its own.
 *
 * <p>Handles are held strongly. That is deliberately simple: the deduplicated set is only one
 * handle per <em>distinct</em> member, which is small, so there is nothing worth reclaiming under
 * memory pressure (see the measurements in the pull request that added this).
 *
 * <p>Internal to TestNG; not part of any public API.
 */
public final class ExecutableCache {

  /** The singleton used in production. */
  public static final ExecutableCache DEFAULT = new ExecutableCache();

  private final ClassValue<ConcurrentMap<Executable, Executable>> cache =
      new ClassValue<>() {
        @Override
        protected ConcurrentMap<Executable, Executable> computeValue(Class<?> type) {
          return new ConcurrentHashMap<>();
        }
      };

  /**
   * Returns the one shared handle for {@code seed}'s member, keeping {@code seed} itself the first
   * time the member is seen. Concurrent callers racing on the same member converge on one instance.
   *
   * <p>If {@code seed} had already been made accessible (a caller called {@code
   * setAccessible(true)} on it before handing it over), that is carried onto the shared handle, so
   * a later duplicate lookup never silently loses it. Accessibility is only ever turned on here,
   * never off.
   */
  public Executable intern(Executable seed) {
    // Outer lookup (ClassValue): pick out this declaring class's own table.
    ConcurrentMap<Executable, Executable> membersOfClass = cache.get(seed.getDeclaringClass());
    // Inner lookup: de-duplicate the member within that table, keeping the first one seen. The key
    // is the seed itself, so identity() returns that same seed as the value.
    Executable canonical = membersOfClass.computeIfAbsent(seed, Function.identity());
    if (canonical != seed && isAccessible(seed)) {
      canonical.setAccessible(true);
    }
    return canonical;
  }

  @SuppressWarnings("deprecation") // isAccessible() reads exactly the flag setAccessible(true) sets
  private static boolean isAccessible(Executable member) {
    return member.isAccessible();
  }
}
