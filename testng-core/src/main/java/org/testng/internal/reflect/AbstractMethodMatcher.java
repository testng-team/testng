package org.testng.internal.reflect;

import org.jspecify.annotations.Nullable;

public abstract class AbstractMethodMatcher implements MethodMatcher {

  private final MethodMatcherContext context;
  private @Nullable Boolean conforms = null;

  public AbstractMethodMatcher(final MethodMatcherContext context) {
    this.context = context;
  }

  protected MethodMatcherContext getContext() {
    return context;
  }

  protected @Nullable Boolean getConforms() {
    return conforms;
  }

  /** {@inheritDoc} */
  @Override
  public boolean conforms() {
    boolean hasConformance = false;
    try {
      hasConformance = hasConformance();
    } finally {
      conforms = hasConformance ? Boolean.TRUE : Boolean.FALSE;
    }
    return hasConformance;
  }

  /**
   * Checks if the arguments conform to the method.
   *
   * @return conformance
   */
  protected abstract boolean hasConformance();
}
