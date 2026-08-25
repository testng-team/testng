package org.testng;

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import org.jspecify.annotations.Nullable;
import org.testng.log4testng.Logger;

/**
 * Resolves the {@link ITestNGCliRunner} service.
 *
 * <p><b>Note</b>: this class is not part of the public API and is meant for internal usage only.
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
   * Why the last lookup came back empty. A provider that is present but fails to load reports the
   * very same "nothing found" outcome as a provider that is simply absent, so the cause has to be
   * carried along or the diagnostic tells people to install what they already installed.
   */
  private static volatile @Nullable Throwable lastFailure;

  private CliRunners() {}

  /** @return the installed runner, or {@code null} when none is available. */
  // Identity on purpose: the fallback exists for a deployment where the context classloader is a
  // different loader from the one that owns the SPI, and a loader is only ever the same loader as
  // itself.
  @SuppressWarnings("ReferenceEquality")
  static @Nullable ITestNGCliRunner find() {
    ITestNGCliRunner local = cached;
    if (local != null) {
      return local;
    }
    // The classloader that owns the SPI. On a plain classpath this is the application classloader.
    // Inside the merged testng.jar OSGi bundle this is the bundle classloader, which also owns the
    // provider class and its META-INF/services entry, so no SPI-Fly weaving is required.
    lastFailure = null;
    ClassLoader owner = ITestNGCliRunner.class.getClassLoader();
    local = load(owner);
    if (local == null) {
      // Fallback for parent/child deployments where only the child sees the provider.
      ClassLoader context = Thread.currentThread().getContextClassLoader();
      if (context != null && context != owner) {
        local = load(context);
      }
    }
    if (local != null) {
      // A null result is deliberately not cached: the provider may show up on a later lookup, and
      // caching it would also let a provider-less lookup clobber a concurrent successful one.
      cached = local;
    }
    return local;
  }

  /**
   * @return the installed runner.
   * @throws TestNGException when no runner is available.
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
   * Peeks at the next provider only to report an ambiguity. A failure here says nothing about the
   * runner already in hand, so it must not cost us that runner.
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
