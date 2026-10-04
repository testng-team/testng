package org.testng;

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import org.jspecify.annotations.Nullable;
import org.testng.log4testng.Logger;

/**
 * Finds the {@link ITestNGCliRunner} that runs TestNG from the command line.
 *
 * <p><b>Note</b>: This class is for the use of TestNG only. It is not part of the public API.
 */
final class CliRunners {

  private static final Logger LOGGER = Logger.getLogger(CliRunners.class);

  private static final String MISSING =
      "TestNG command line support is not available: no implementation of "
          + ITestNGCliRunner.class.getName()
          + " was found on the classpath.\n"
          + "The org.testng:testng jar bundles one. If TestNG was repackaged, or is consumed as "
          + "individual modules, make sure a runner and its META-INF/services entry are present "
          + "(along with its parsing library), or drive TestNG through the org.testng.TestNG Java "
          + "API instead.";

  private static volatile @Nullable ITestNGCliRunner cached;

  /**
   * The error from the last search that found no runner, or {@code null}.
   *
   * <p>A runner that fails to load gives the same empty result as a missing runner. Without this
   * error, the message would tell the user to add a runner that is already there.
   */
  private static volatile @Nullable Throwable lastFailure;

  private CliRunners() {}

  /**
   * Finds the installed runner.
   *
   * <p>This method first searches the class loader that loaded {@link ITestNGCliRunner}. If that
   * class loader has no runner, it searches the context class loader of the current thread. It
   * keeps the first runner that it finds, and returns it on later calls.
   *
   * @return the runner, or {@code null} when there is none.
   */
  // Compare the class loaders with == on purpose. The second search is useful only when the context
  // class loader is a different object from the first one.
  @SuppressWarnings("ReferenceEquality")
  static @Nullable ITestNGCliRunner find() {
    ITestNGCliRunner local = cached;
    if (local != null) {
      return local;
    }
    // Search the class loader that loaded ITestNGCliRunner first. On a plain class path, this is
    // the application class loader. In OSGi, it is the class loader of the testng.jar bundle. That
    // bundle also holds the runner and its META-INF/services entry, so it works without SPI-Fly.
    lastFailure = null;
    ClassLoader owner = ITestNGCliRunner.class.getClassLoader();
    local = load(owner);
    if (local == null) {
      // Then search the context class loader. This helps when a parent class loader loaded TestNG,
      // but only a child class loader can see the runner.
      ClassLoader context = Thread.currentThread().getContextClassLoader();
      if (context != null && context != owner) {
        local = load(context);
      }
    }
    if (local != null) {
      // Keep a runner that was found, but do not keep a null. A runner can appear on a later call.
      // Also, a null from one thread must not replace a runner that another thread found.
      cached = local;
    }
    return local;
  }

  /**
   * Returns the installed runner, or throws when there is none.
   *
   * @return the runner.
   * @throws TestNGException when there is no runner. When a runner failed to load, the exception
   *     carries that error as its cause.
   */
  static ITestNGCliRunner required() {
    ITestNGCliRunner runner = find();
    if (runner == null) {
      Throwable cause = lastFailure;
      throw cause == null ? new TestNGException(MISSING) : new TestNGException(MISSING, cause);
    }
    return runner;
  }

  private static @Nullable ITestNGCliRunner load(ClassLoader loader) {
    try {
      Iterator<ITestNGCliRunner> it = ServiceLoader.load(ITestNGCliRunner.class, loader).iterator();
      if (!it.hasNext()) {
        return null;
      }
      ITestNGCliRunner runner = it.next();
      warnIfAnotherProviderFollows(it, runner);
      return runner;
    } catch (ServiceConfigurationError | RuntimeException e) {
      lastFailure = e;
      return null;
    }
  }

  /**
   * Logs a warning when the class loader has more than one runner.
   *
   * <p>This method looks at the next runner only to warn about it. If that look fails, the method
   * ignores the failure. The failure says nothing about the runner that was already found.
   */
  private static void warnIfAnotherProviderFollows(
      Iterator<ITestNGCliRunner> it, ITestNGCliRunner chosen) {
    boolean ambiguous;
    try {
      ambiguous = it.hasNext();
    } catch (ServiceConfigurationError | RuntimeException ignored) {
      return;
    }
    if (ambiguous) {
      LOGGER.warn(
          "Several "
              + ITestNGCliRunner.class.getName()
              + " implementations are on the classpath. Using "
              + chosen.getClass().getName()
              + ". Ordering is defined by the classloader and is not stable.");
    }
  }
}
