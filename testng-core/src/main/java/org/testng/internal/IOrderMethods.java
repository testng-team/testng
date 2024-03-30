package org.testng.internal;

import java.util.Comparator;
import org.testng.ITestNGMethod;

/**
 * Helps produce a {@link Comparator} that can be used to determine order of execution for a bunch
 * of {@link ITestNGMethod} methods.
 */
@FunctionalInterface
public interface IOrderMethods {

  /** @return - The {@link Comparator} to be used. */
  Comparator<ITestNGMethod> comparator();
}
