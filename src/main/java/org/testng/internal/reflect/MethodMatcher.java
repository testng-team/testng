package org.testng.internal.reflect;

/**
 * An interface to validate conformance of input arguments to its target method.
 *
 * @author <a href="mailto:nitin.matrix@gmail.com">Nitin Verma</a>
 */
public interface MethodMatcher {
  /**
   * Checks if the arguments conform to the method.
   *
   * @return conformance
   * @throws MethodMatcherException if any internal failure.
   */
  boolean conforms() throws MethodMatcherException;

  /**
   * If possible gives an array consumable by java method invoker.
   *
   * @return conforming argument array
   * @throws MethodMatcherException internal failure or non-conformance
   */
  Object[] getConformingArguments() throws MethodMatcherException;
}
