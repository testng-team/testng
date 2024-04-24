package org.testng;

/** Represents the ability to retrieve the parameters associated with a factory method. */
public interface ITestClassInstance {

  /** @return - The actual instance associated with a factory method */
  Object getInstance();

  /**
   * @return - The actual index of instance associated with a factory method. This index has a 1:1
   *     correspondence with what were specified via the <code>indices</code> attribute of the
   *     <code>@Factory</code> annotation. For e.g., lets say you specified the indices to the "1"
   *     and your factory returned 4 instances, then the instance on which this method is invoked
   *     would have the value as "1".
   */
  int getIndex();

  /**
   * @return - returns an index which indicates the running position in the array of test class
   *     instances that were produced by a <code>@Factory</code> annotated constructor or static
   *     method. For e.g., lets say your <code>@Factory</code> method returned 4 instances, then
   *     each of the invocations to this method would return a value from <code>0</code> to <code>3
   *     </code>
   */
  int getInvocationIndex();

  /** @return - The parameters associated with the factory method as an array. */
  Object[] getParameters();

  static Object embeddedInstance(Object original) {
    if (original instanceof ITestClassInstance) {
      return ((ITestClassInstance) original).getInstance();
    }
    return original;
  }
}
