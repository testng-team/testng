package org.testng.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.testng.xml.XmlTest;

/**
 * Preserves the historical {@link org.testng.ITestResult#getParameters()} representation for
 * backward compatibility.
 *
 * <p>A reporter reads a result long after the invocation is over, so a data provider that hands the
 * same mutable row to every invocation would otherwise be reported in its final state rather than
 * the state each invocation ran with. Since GITHUB-447 the answer has been to store a clone of
 * every {@link Cloneable} parameter. That rule is type-blind: it applies to values TestNG injected
 * as readily as to user data.
 *
 * <p>{@link XmlTest} is the one exception, and is kept by reference. TestNG injects it, so it is
 * not user data at all, and its {@code clone()} is a suite-building helper rather than a copy: it
 * builds the copy with {@code new XmlTest(suite)}, whose constructor registers it in that suite.
 * Cloning one therefore appended a phantom {@code <test>} to the suite that was running
 * (GITHUB-1994).
 */
final class LegacyParameterSnapshotter {

  private LegacyParameterSnapshotter() {}

  /**
   * @param parameters - The values an invocation ran with.
   * @return - A new array holding the reporting view of those values, never the caller's array.
   */
  static Object[] snapshot(Object[] parameters) {
    Object[] snapshot = new Object[parameters.length];
    for (int i = 0; i < parameters.length; i++) {
      snapshot[i] = snapshotParameter(parameters[i]);
    }
    return snapshot;
  }

  private static Object snapshotParameter(Object parameter) {
    if (!(parameter instanceof Cloneable) || parameter instanceof XmlTest) {
      return parameter;
    }
    try {
      // Cloneable declares no clone(), and Object#clone() is protected, so the only way through is
      // reflectively and only on the exact runtime class. A type that merely inherits clone() is
      // consequently not snapshotted -- historical behaviour, kept deliberately.
      Method clone = parameter.getClass().getDeclaredMethod("clone");
      return clone.invoke(parameter);
    } catch (NoSuchMethodException
        | InvocationTargetException
        | IllegalAccessException
        | SecurityException e) {
      return parameter;
    }
  }
}
