package org.testng;

/**
 * One test class instance produced by a <code>&#64;Factory</code>.
 *
 * <p>Obtained from {@link ITestResult#getFactoryInstance()} or {@link
 * ITestNGMethod#getFactoryInstance()}, which return an empty {@link java.util.Optional} for a test
 * class that no factory produced.
 *
 * <p>None of the methods below needs the instance itself, so reading them never triggers the
 * creation of a lazily instantiated factory instance. That is deliberate: listeners and
 * interceptors inspect methods before their instances exist.
 *
 * @since 7.13.0
 */
public interface IFactoryInstance {

  /**
   * Returns the position of this instance in the output of its factory.
   *
   * @return - The zero based position, counted <em>before</em> any filtering by the <code>indices
   *     </code> attribute of the <code>
   *     &#64;Factory
   *     </code> annotation. A factory producing four instances under <code>
   *     &#64;Factory(indices = {1,
   *     3})</code> therefore yields two instances whose indexes are <code>1</code> and <code>3
   *     </code>, not <code>0</code> and <code>1</code>. The value is stable for a given instance
   *     and unique among the instances of one factory.
   */
  int getIndex();

  /**
   * Returns the parameters of the factory invocation that produced this instance.
   *
   * @return - A copy of those parameters. For a data provider driven factory that is the data
   *     provider row; for a factory taking no parameters it is an empty array. A factory method
   *     returning several instances from a single invocation gives all of them the same parameters
   *     -- {@link #getIndex()} is what tells those instances apart.
   */
  Object[] getParameters();

  /**
   * Returns the factory that produced this instance.
   *
   * @return - The factory. Never <code>null</code>.
   */
  IFactory getFactory();
}
