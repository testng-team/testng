package org.testng.internal.reflect;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.testng.internal.reflect.InjectableParameter.Assistant.ALL_INJECTS;

/**
 * Checks for array ending method argument match with or without filtering injectables.
 *
 * @author <a href="mailto:nitin.matrix@gmail.com">Nitin Verma</a>
 * @see ReflectionRecipes#matchArrayEnding(Class[], Object[])
 */
public class ArrayEndingMethodMatcher extends AbstractNodeMethodMatcher {

  public ArrayEndingMethodMatcher(final MethodMatcherContext context) {
    super(context);
  }

  /** {@inheritDoc} */
  @Override
  protected List<Set<InjectableParameter>> getConformanceInjectsOrder() {
    final List<Set<InjectableParameter>> injectsOrder = new ArrayList<>(1);
    injectsOrder.add(ALL_INJECTS);
    return injectsOrder;
  }

  /**
   * {@inheritDoc}
   *
   * @see ReflectionRecipes#matchArrayEnding(Class[], Object[])
   */
  @Override
  protected boolean match(final Parameter[] parameters, final Object[] arguments) {
    return ReflectionRecipes.matchArrayEnding(parameters, getContext().getArguments());
  }

  /** {@inheritDoc} */
  @Override
  protected Object[] matchingArguments(final Parameter[] parameters, final Object[] arguments) {
    final Class<?>[] classes = ReflectionRecipes.classesFromParameters(parameters);
    final Object[] objects = new Object[classes.length];
    final Class<?> componentType = classes[classes.length - 1].getComponentType();
    final Object array = Array.newInstance(componentType, arguments.length - classes.length + 1);
    System.arraycopy(arguments, 0, objects, 0, classes.length - 1);
    int j = 0;
    for (int i = classes.length - 1; i < arguments.length; i++, j++) {
      Array.set(array, j, arguments[i]);
    }
    objects[classes.length - 1] = array;
    return objects;
  }
}
