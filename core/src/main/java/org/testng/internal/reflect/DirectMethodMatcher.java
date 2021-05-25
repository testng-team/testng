package org.testng.internal.reflect;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.testng.internal.reflect.InjectableParameter.Assistant.ALL_INJECTS;
import static org.testng.internal.reflect.InjectableParameter.Assistant.NONE;

/**
 * Checks for method argument match with or without filtering injectables.
 *
 * @author <a href="mailto:nitin.matrix@gmail.com">Nitin Verma</a>
 */
public class DirectMethodMatcher extends AbstractNodeMethodMatcher {

  public DirectMethodMatcher(final MethodMatcherContext context) {
    super(context);
  }

  /** {@inheritDoc} */
  @Override
  protected List<Set<InjectableParameter>> getConformanceInjectsOrder() {
    final List<Set<InjectableParameter>> injectsOrder = new ArrayList<>(1);
    injectsOrder.add(ALL_INJECTS);
    injectsOrder.add(NONE);
    return injectsOrder;
  }

  /** {@inheritDoc} */
  @Override
  protected boolean match(final Parameter[] parameters, final Object[] arguments) {
    return ReflectionRecipes.exactMatch(parameters, getContext().getArguments());
  }

  /** {@inheritDoc} */
  @Override
  protected Object[] matchingArguments(Parameter[] parameters, Object[] arguments) {
    return arguments;
  }
}
