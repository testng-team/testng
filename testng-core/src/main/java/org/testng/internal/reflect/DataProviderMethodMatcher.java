package org.testng.internal.reflect;

import static org.testng.internal.reflect.InjectableParameter.Assistant.ALL_INJECTS;
import static org.testng.internal.reflect.InjectableParameter.Assistant.NONE;

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
    // A parameter a resolver owns is no more the provider's to supply than a native injection
    // is; leave it out or the arity named here is wrong for exactly the methods that use one.
    final ResolvedParameters resolved = context.getResolvedParameters();
    final Parameter[] fromProvider =
        ReflectionRecipes.filter(methodParameters, ALL_INJECTS, resolved);
    final int expected = fromProvider.length;
    // The other count a provider may legitimately supply: DirectMethodMatcher also tries with the
    // native injections left in (the NONE pass). Without a resolver that is every parameter.
    final int expectedWithNatives =
        ReflectionRecipes.filter(methodParameters, NONE, resolved).length;
    final boolean arrayEnding = expected > 0 && fromProvider[expected - 1].getType().isArray();
    if (arrayEnding) {
      if (supplied < expected) {
        return countMismatch(expected, supplied);
      }
      return typeMismatch();
    }
    if (supplied != expected && supplied != expectedWithNatives) {
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
