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
   */
  boolean conforms();

  /**
   * If possible gives an array consumable by java method invoker.
   *
   * @return conforming argument array
   */
  Object[] getConformingArguments();
}
