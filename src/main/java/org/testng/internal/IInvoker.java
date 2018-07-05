package org.testng.internal;

import org.testng.*;
import org.testng.xml.XmlSuite;

import java.util.List;
import java.util.Map;

/**
 * This class defines an invoker.
 *
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public interface IInvoker {

  /**
   * Invoke configuration methods if they belong to the same TestClass passed in parameter..
   *
   * <p>TODO: Calculate ahead of time which methods should be invoked for each class. Might speed
   * things up for users who invoke the same test class with different parameters in the same suite
   * run.
   *
   * @param testClass the class whose configuration methods must be run
   */
  void invokeConfigurations(
      IClass testClass,
      ITestNGMethod[] allMethods,
      XmlSuite suite,
      Map<String, String> parameters,
      Object[] parameterValues,
      Object instance);

  /**
   * Invoke all the test methods. Note the plural: the method passed in parameter might be invoked
   * several times if the test class it belongs to has more than one instance (i.e., if an @Factory
   * method has been declared somewhere that returns several instances of this TestClass). If
   * no @Factory method was specified, testMethod will only be invoked once.
   *
   * <p>Note that this method also takes care of invoking the beforeTestMethod and afterTestMethod,
   * if any.
   *
   * <p>Note (alex): this method can be refactored to use a SingleTestMethodWorker that directly
   * invokes {@link #invokeTestMethod(Object, ITestNGMethod, Object[], int, XmlSuite, Map,
   * ITestClass, ITestNGMethod[], ITestNGMethod[], ConfigurationGroupMethods, AbstractInvoker.FailureContext)} and
   * this would simplify the implementation (see how DataTestMethodWorker is used)
   */
  List<ITestResult> invokeTestMethods(
      ITestNGMethod testMethod,
      Map<String, String> parameters,
      ConfigurationGroupMethods groupMethods,
      Object instance,
      ITestContext testContext);
}
