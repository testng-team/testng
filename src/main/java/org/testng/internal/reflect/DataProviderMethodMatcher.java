package org.testng.internal.reflect;

import org.testng.internal.ThreeStateBoolean;

import static org.testng.internal.ThreeStateBoolean.FALSE;
import static org.testng.internal.ThreeStateBoolean.TRUE;

/**
 * Checks the conformance as per data-provide specifications.
 *
 * @author <a href="mailto:nitin.matrix@gmail.com">Nitin Verma</a>
 */
public class DataProviderMethodMatcher extends AbstractMethodMatcher {

  private final DirectMethodMatcher directMethodMatcher;
  private final ArrayEndingMethodMatcher arrayEndingMethodMatcher;
  private MethodMatcher matchingMatcher = null;

  public DataProviderMethodMatcher(final MethodMatcherContext context) {
    super(context);
    this.directMethodMatcher = new DirectMethodMatcher(context);
    this.arrayEndingMethodMatcher = new ArrayEndingMethodMatcher(context);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean hasConformance() {
    boolean matching = false;
    if (directMethodMatcher.conforms()) {
      matching = true;
      matchingMatcher = directMethodMatcher;
    } else if (arrayEndingMethodMatcher.conforms()) {
      matching = true;
      matchingMatcher = arrayEndingMethodMatcher;
    }
    return matching;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object[] getConformingArguments() throws MethodMatcherException {
    if (ThreeStateBoolean.NONE.equals(getConforms())) {
      conforms();
    }
    if (matchingMatcher != null) {
      return matchingMatcher.getConformingArguments();
    }
    throw new MethodMatcherException("Data provider mismatch", getContext().getMethod(), getContext().getArguments());
  }
}
