package org.testng.internal.reflect;

import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.Nullable;

public abstract class AbstractNodeMethodMatcher extends AbstractMethodMatcher {

  private Parameter @Nullable [] conformingParameters = null;

  public AbstractNodeMethodMatcher(final MethodMatcherContext context) {
    super(context);
  }

  protected Parameter @Nullable [] getConformingParameters() {
    return conformingParameters;
  }

  /** {@inheritDoc} */
  @Override
  protected boolean hasConformance() {
    boolean matching = false;
    for (final Set<InjectableParameter> injects : getConformanceInjectsOrder()) {
      final Parameter[] parameters =
          ReflectionRecipes.filter(
              getContext().getMethodParameter(), injects, getContext().getResolvedParameters());
      matching = match(parameters, getContext().getArguments());
      if (matching) {
        conformingParameters = parameters;
        break;
      }
    }
    return matching;
  }

  /** @return injects to check against. */
  protected abstract List<Set<InjectableParameter>> getConformanceInjectsOrder();

  /**
   * Checks if its possible to gives an array consumable by java method invoker.
   *
   * @param parameters array of parameter instances under question.
   * @param arguments instances to be verified.
   * @return matches or not
   */
  protected abstract boolean match(final Parameter[] parameters, final Object[] arguments);

  /** {@inheritDoc} */
  @Override
  public Object[] getConformingArguments() {
    if (getConforms() == null) {
      conforms();
    }
    // Bound once: NullAway cannot refine across two getter calls.
    final Parameter[] parameters = getConformingParameters();
    if (parameters == null) {
      throw new MethodMatcherException(
          this.getClass().getSimpleName() + " mismatch",
          getContext().getMethod(),
          getContext().getArguments());
    }

    return ReflectionRecipes.inject(
        getContext().getMethodParameter(),
        InjectableParameter.Assistant.ALL_INJECTS,
        getContext().getResolvedParameters(),
        matchingArguments(parameters, getContext().getArguments()),
        getContext().getMethod(),
        getContext().getTestContext(),
        getContext().getTestResult());
  }

  /**
   * If possible gives an array consumable by java method invoker.
   *
   * @param parameters array of parameter instances under question.
   * @param arguments instances to conform.
   * @return conforming argument array
   */
  protected abstract Object[] matchingArguments(
      final Parameter[] parameters, final Object[] arguments);
}
