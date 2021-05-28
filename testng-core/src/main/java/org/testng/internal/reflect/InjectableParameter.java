package org.testng.internal.reflect;

import java.util.EnumSet;
import java.util.Set;

/**
 * Enumeration of injectables.
 *
 * @author <a href="mailto:nitin.matrix@gmail.com">Nitin Verma</a>
 */
public enum InjectableParameter {
  CURRENT_TEST_METHOD,
  ITEST_CONTEXT,
  ITEST_RESULT,
  XML_TEST;

  /** convenience means to add and remove injectables. */
  public static class Assistant {
    public static final Set<InjectableParameter> NONE = EnumSet.noneOf(InjectableParameter.class);
    public static final Set<InjectableParameter> ALL_INJECTS =
        EnumSet.allOf(InjectableParameter.class);
  }
}
