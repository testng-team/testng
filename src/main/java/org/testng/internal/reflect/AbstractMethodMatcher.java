package org.testng.internal.reflect;

import org.testng.internal.ThreeStateBoolean;

import static org.testng.internal.ThreeStateBoolean.FALSE;
import static org.testng.internal.ThreeStateBoolean.TRUE;

/**
 * Created on 1/4/16.
 *
 * @author <a href="mailto:nitin.matrix@gmail.com">Nitin Verma</a>
 */
public abstract class AbstractMethodMatcher implements MethodMatcher {
  private final MethodMatcherContext context;
  private ThreeStateBoolean conforms = ThreeStateBoolean.NONE;

  public AbstractMethodMatcher(final MethodMatcherContext context) {
    this.context = context;
  }

  protected MethodMatcherContext getContext() {
    return context;
  }

  protected ThreeStateBoolean getConforms() {
    return conforms;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean conforms() {
    boolean hasConformance = false;
    try {
      hasConformance = hasConformance();
    } finally {
      conforms = hasConformance ? TRUE : FALSE;
    }
    return hasConformance;
  }

  /**
   * Checks if the arguments conform to the method.
   *
   * @return conformance
   * @throws MethodMatcherException if any internal failure.
   */
  protected abstract boolean hasConformance();
}
