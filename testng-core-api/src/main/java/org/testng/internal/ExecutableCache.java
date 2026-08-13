package org.testng.internal;

import java.lang.reflect.Executable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A process-wide pool that de-duplicates resolved methods and constructors: every caller asking for
 * the same member gets one shared {@link Executable} instead of making its own copy. Members are
 * looked up by declaring class, then by {@link MemberKey}.
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

  private final ClassValue<ConcurrentMap<MemberKey, Executable>> cache =
      new ClassValue<>() {
        @Override
        protected ConcurrentMap<MemberKey, Executable> computeValue(Class<?> type) {
          return new ConcurrentHashMap<>();
        }
      };

  /**
   * Returns the one shared handle for {@code seed}'s member, keeping {@code seed} itself the first
   * time the member is seen. Concurrent callers racing on the same member converge on one instance.
   */
  public Executable intern(Executable seed) {
    return cache.get(seed.getDeclaringClass()).computeIfAbsent(new MemberKey(seed), key -> seed);
  }
}
