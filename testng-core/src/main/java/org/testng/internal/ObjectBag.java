package org.testng.internal;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.testng.ISuite;
import org.testng.log4testng.Logger;

/**
 * A simple bean bag that is intended to help share objects during the lifetime of TestNG without
 * needing it to be a singleton.
 */
public final class ObjectBag {

  private static final Logger logger = Logger.getLogger(ObjectBag.class);
  private final Map<Class<?>, Object> bag = new ConcurrentHashMap<>();

  private static final Map<UUID, ObjectBag> instances = new ConcurrentHashMap<>();

  public static ObjectBag getInstance(ISuite suite) {
    return instances.computeIfAbsent(suite.getXmlSuite().SUITE_ID, k -> new ObjectBag());
  }

  public static void cleanup(ISuite suite) {
    UUID uid = suite.getXmlSuite().SUITE_ID;
    Optional.ofNullable(instances.get(uid)).ifPresent(ObjectBag::cleanup);
    instances.remove(uid);
  }

  /**
   * @param type - The type of the object to be created
   * @param supplier - A {@link Supplier} that should be used to produce a new instance
   * @return - Either the newly produced instance or the existing instance.
   */
  public Object createIfRequired(Class<?> type, Supplier<Object> supplier) {
    return bag.computeIfAbsent(type, t -> supplier.get());
  }

  public void cleanup() {
    bag.values().stream()
        .filter(it -> it instanceof Closeable)
        .map(it -> (Closeable) it)
        .forEach(
            it -> {
              try {
                it.close();
              } catch (IOException e) {
                logger.debug("Could not clean-up " + it, e);
              }
            });
    bag.clear();
  }
}
