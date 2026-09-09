package org.testng.internal.reflect;

import static org.testng.internal.reflect.InjectableParameter.Assistant.ALL_INJECTS;

import java.lang.reflect.Parameter;
import org.jspecify.annotations.Nullable;

/** Checks the conformance as per data-provide specifications. */
public class DataProviderMethodMatcher extends AbstractMethodMatcher {

  private final DirectMethodMatcher directMethodMatcher;
  private final ArrayEndingMethodMatcher arrayEndingMethodMatcher;
  private @Nullable MethodMatcher matchingMatcher = null;

  public DataProviderMethodMatcher(final MethodMatcherContext context) {
    super(context);
    this.directMethodMatcher = new DirectMethodMatcher(context);
    this.arrayEndingMethodMatcher = new ArrayEndingMethodMatcher(context);
  }

  /** {@inheritDoc} */
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

  /** {@inheritDoc} */
  @Override
  public Object[] getConformingArguments() {
    if (getConforms() == null) {
      conforms();
    }
    if (matchingMatcher != null) {
      return matchingMatcher.getConformingArguments();
    }
    throw new MethodMatcherException(
        mismatchMessage(getContext()), getContext().getMethod(), getContext().getArguments());
  }

  private static String mismatchMessage(final MethodMatcherContext context) {
    final Parameter[] methodParameters = context.getMethodParameter();
    final Object[] arguments = context.getArguments();
    final int supplied = arguments == null ? 0 : arguments.length;
    if (methodParameters.length == 0) {
      return String.format(
          "[%s] has no parameters defined but was found to be using a "
              + "data provider (either explicitly specified or "
              + "inherited from class level annotation).\nData provider mismatch",
          context.getMethod());
    }
    final Parameter[] fromProvider = ReflectionRecipes.filter(methodParameters, ALL_INJECTS);
    final int expected = fromProvider.length;
    final boolean arrayEnding = expected > 0 && fromProvider[expected - 1].getType().isArray();
    if (arrayEnding) {
      if (supplied < expected) {
        return countMismatch(expected, supplied);
      }
      return typeMismatch();
    }
    if (supplied != expected && supplied != methodParameters.length) {
      return countMismatch(expected, supplied);
    }
    return typeMismatch();
  }

  private static String countMismatch(final int expected, final int supplied) {
    return String.format(
        "Data provider mismatch: expected %d argument%s, got %d",
        expected, expected == 1 ? "" : "s", supplied);
  }

  private static String typeMismatch() {
    return "Data provider mismatch: argument types do not match the method parameters";
  }
}
